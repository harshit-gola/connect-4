package com.connectfour;

import com.sun.javafx.font.directwrite.RECT;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";

	private String PLAYER_ONE = "Player One";
	private String PLAYER_TWO = "Player Two";

	private int option,count = -1;  //option is for hvsai mode and count for delay in the disc falling

	private int random_turn = 0;  //for first turn of ai

	private boolean isPlayerOneTurn = true;

	private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS];

	private boolean isAllowedToInsert = true;   //used to avoid same color disc being doubly added when game is double clicked.
	private boolean isAllowedToInsertai = true; //used to avoid same color disc being doubly added when game is double clicked for ai mode

	@FXML
	public GridPane gridPane;

	@FXML
	public Pane discPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField person1;

	@FXML
	public TextField person2;

	@FXML
	public Button setButton;


	public void labelInit(){

		setButton.setDisable(false);
		setButton.setOnAction(event -> {
			PLAYER_ONE = person1.getText();
			PLAYER_TWO = person2.getText();

			if(PLAYER_ONE.equals(""))
				PLAYER_ONE = "Player One";
			if(PLAYER_TWO.equals(""))
				PLAYER_TWO = "Player Two";

			playerNameLabel.setText(PLAYER_ONE);

			setButton.setDisable(true);
		});

	}

	/*private void warn() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Field Empty");
		alert.setContentText("Please enter the Player names");
		alert.show();
	}*/

	public void createPlayGround(int k){

		option = k;
		Shape rectangleWithHoles = createGame();
		gridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = createClickableColumn();
		for (Rectangle rectangle: rectangleList) {
			gridPane.add(rectangle ,0,1);
		}

	}
	private Shape createGame(){
		Shape rectangleWithHoles = new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
		for(int row=0; row<ROWS; row++)
		{
			for(int col=0; col<COLUMNS; col++)
			{
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true); //circle becomes smooth

				circle.setTranslateX(col*(CIRCLE_DIAMETER+5)+ (CIRCLE_DIAMETER/4));
				circle.setTranslateY(row*(CIRCLE_DIAMETER+5)+ (CIRCLE_DIAMETER/4));

				rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumn(){

		List<Rectangle> rectangleList = new ArrayList<>();

		for(int col=0; col<COLUMNS; col++){
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;

			rectangle.setOnMouseClicked(event -> {

				setButton.setDisable(true);
				if(isAllowedToInsert){
					System.out.println("123");
					count = 0;
					isAllowedToInsert = false;
					insertDisc(new Disc(isPlayerOneTurn),column);
					System.out.println("1 " + isPlayerOneTurn);
				}

				isAllowedToInsert = true; // for hvh if the column is full then it is needed

				if(option == 1)
				{
					if(isAllowedToInsertai){
						int colum = -1;
						isAllowedToInsertai = false;
						System.out.println(456);
						count = 1;       //to avoid executing a snippet below every time
						System.out.println("aii" + isAllowedToInsert );
						if(random_turn == 0)       //for first turn to be random only
						{
							colum = (int)(Math.random() * 7);
							random_turn++;
							System.out.println("random turn");
						}
						else
							colum = aiMove();
						insertDisc(new Disc(isPlayerOneTurn),colum);  //aiMove()
						System.out.println("2 " + isPlayerOneTurn);
						System.out.println("ai turn");
					}

				}
			});

			rectangleList.add(rectangle);
		}

		return rectangleList;
	}

	private int aiMove()
	{
		float point[][] = new float[COLUMNS][COLUMNS];  //for storing points for every possible move for 1 round
		int i,j;
		int c = 0;

		for(i = 0 ; i<COLUMNS ; i++)
		{
			for(j = 0 ; j<COLUMNS ; j++)
			{
				point[i][j] = score(i,j); //storing points for specific column for ai and player move
				if(score(i,j) == -1000)
					return j;
			}
		}

		float max = point[0][0];

		for(i = 1 ; i < COLUMNS ; i++)
		{
			for ( j = 1; j < COLUMNS ; j++)
			{
				if(point[i][j] != 0.01f)
				{
					if(max < point[i][j])
					{
						max = point[i][j];
						c = i;
					}
				}
			}
		}
		System.out.println("column"+c);
		return c;
	}

	private float score(int i, int j){  //returns point for a possible move

		int ai_row = calculate_row(i);  //getting the row for a specific column in which ai will make its move
		int player_row = calculate_row(j);  //getting the row for a specific column in which player will make its move

		if(ai_row == -1)        //column is full for ai move
			return 0.01f;
		else
		{
			insertedDiscsArray[ai_row][i] = new Disc(isPlayerOneTurn); //pretending that certain row and column is full for ai therefore will be deleted later

			if(player_row == -1)        //column is full for player move
				return 0.01f;
			else
				insertedDiscsArray[player_row][j] = new Disc(!isPlayerOneTurn); //pretending that certain row and column is full for player therefore will be deleted later

		}

		int point = calculate_score(ai_row,i,isPlayerOneTurn)+calculate_score(player_row,j,!isPlayerOneTurn);

		insertedDiscsArray[ai_row][i] = null;  //deleting the assumed disc
		insertedDiscsArray[player_row][j] = null;  //deleting the assumed disc

		if(calculate_score(player_row,j,!isPlayerOneTurn) == -1000)
			return calculate_score(player_row,j,!isPlayerOneTurn);

		return point;
	}

	private int calculate_row(int col){

		int row = ROWS - 1;
		while (row >= 0)
		{
			if(discIfPresent(row,col) == null)    //to check if there exist a disc at that specific "row" and "column"
				break;
			row--;
		}
		if(row<0)
			return -1; //***/

		return row;
	}

	private int calculate_score(int row, int column, boolean turn){

		int spaceCounter = 0;
		int discCounter = 0;
		int score;
		int point = 0;
		int rowIndex,columnIndex;

		//vertical
		for(rowIndex = row-3 ; rowIndex <= row+3 ; rowIndex++)
		{
			if(rowIndex >=0 && rowIndex < ROWS)   //to remove the outer coordinates of grid
			{
				Disc disc = discIfPresent(rowIndex,column);
				if(rowIndex <= row)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
					{
						spaceCounter = 0;
						discCounter = 0;
					}
				}
				else
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
						break;
				}
			}
		}

		score = individual_score(spaceCounter,discCounter,turn);

		if(score == -1000)  //this shows that player is going to move in next turn so ai is cancelling this.
			return score;

		System.out.println("vertical score"+score);

		point = point + score;

		spaceCounter =0;
		discCounter = 0;

		//horizontal
		for(columnIndex = column-3 ; columnIndex <= column+3 ; columnIndex++)
		{
			if(columnIndex >=0 && columnIndex < COLUMNS)   //to remove the outer coordinates of grid
			{
				Disc disc = discIfPresent(row,columnIndex);
				if(columnIndex <= column)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
					{
						spaceCounter = 0;
						discCounter = 0;
					}
				}
				else
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
						break;
				}
			}
		}

		score = individual_score(spaceCounter,discCounter,turn);

		if(score == -1000)  //this shows that player is going to move in next turn so ai is cancelling this.
			return score;

		System.out.println("horizontal score"+ score);

		point = point + score;

		spaceCounter =0;
		discCounter = 0;

		//diagonal1 /
		for(int i = 0 ; i <= 6 ; i++)
		{
			rowIndex = row-3 + i;
			columnIndex = column+3 -i ;

			if(rowIndex >=0 && columnIndex >=0 && rowIndex < ROWS && columnIndex < COLUMNS)   //to remove the outer coordinates of grid
			{
				Disc disc = discIfPresent(rowIndex,columnIndex);
				if(rowIndex <= row && columnIndex >= column)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
					{
						spaceCounter = 0;
						discCounter = 0;
					}
				}
				else if(rowIndex > row && columnIndex < column)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
						break;
				}
			}
		}

		score = individual_score(spaceCounter,discCounter,turn);

		if(score == -1000)  //this shows that player is going to move in next turn so ai is cancelling this.
			return score;

		System.out.println("diagonal1 score"+ score);

		point = point + score;

		spaceCounter =0;
		discCounter = 0;

		//diagonal2 \
		for(int i = 0 ; i <= 6 ; i++)
		{
			rowIndex = row-3 + i;
			columnIndex = column-3 +i ;

			if(rowIndex >=0 && columnIndex >=0 && rowIndex < ROWS && columnIndex < COLUMNS)   //to remove the outer coordinates of grid
			{
				Disc disc = discIfPresent(rowIndex,columnIndex);
				if(rowIndex <= row && columnIndex <= column)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
					{
						spaceCounter = 0;
						discCounter = 0;
					}
				}
				else if(rowIndex > row && columnIndex > column)
				{
					if(disc == null)
						spaceCounter++;
					if (disc != null && disc.turn == turn)
						discCounter++;
					if(disc != null && disc.turn !=turn)
						break;
				}
			}
		}

		score = individual_score(spaceCounter,discCounter,turn);

		if(score == -1000)  //this shows that player is going to move in next turn so ai is cancelling this.
			return score;

		System.out.println("diagonal2 score"+ score);

		point = point + score;
		System.out.println("total score"+point);

		return point;
	}

	private int individual_score(int spaceCounter, int discCounter, boolean turn){

		int score = 0;
		int counter = spaceCounter + discCounter;
		if(counter >= 4)
		{
			if(discCounter == 1)
			{
				if(turn == false)
					score = 1;
				else
					score = -1;
			}
			else if(discCounter == 2)
			{
				if(turn == false)
					score = 10;
				else
					score = -10;
			}
			else if(discCounter == 3)
			{
				if(turn == false)
					score = 100;
				else
					score = -100;
			}
			else if(discCounter == 4)
			{
				if(turn == false)
					score = 1000;
				else
					score = -1000;
			}
		}
		else
			score = 0;

	return score;
	}

	private static class Disc extends Circle{
		private final boolean turn;
		public Disc(boolean turn){
			this.turn = turn;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(turn? Color.valueOf(discColor1): Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	private void insertDisc(Disc disc,int column){
		System.out.println("insertdisc");
		int row = ROWS - 1;
		while (row >= 0)
		{
			if(discIfPresent(row,column) == null)    //to check if there exist a disc at that specific "row" and "column"
				break;
			row--;
		}
		if (row<0)          //if the column is full
			return;

		insertedDiscsArray[row][column] = disc; //For structural change. adding object to the game.
		discPane.getChildren().add(disc);   //For user change. adding disc to the game.

		disc.setTranslateX(column*(CIRCLE_DIAMETER+5)+ (CIRCLE_DIAMETER/4));

		int currentRow = row;

		float wait = 0.5f;
		if(option == 1 && count == 1)
			wait = 0.8f;

		/*PauseTransition pauseTransition = new PauseTransition();
		pauseTransition.setDuration(Duration.millis(wait));*/

		TranslateTransition transition = new TranslateTransition(Duration.seconds(wait),disc);
		transition.setToY(row*(CIRCLE_DIAMETER+5)+ (CIRCLE_DIAMETER/4));

		//SequentialTransition sequentialTransition = new SequentialTransition(disc,pauseTransition,transition);

		transition.setOnFinished(event -> {     // its execution will always be at the last for h & ai after both their corresponding insertdisc methods are executed
			System.out.println("finished");
			//if(option == 0)
			isAllowedToInsert = true;  //to allow the next player to insert the disc & to avoid double click blunder for hvh
			isAllowedToInsertai = true; //to allow the next player to insert the disc & to avoid double click blunder for hvai
			if(gameEnded(currentRow, column)){
				gameOver();
				return;
			}
			isPlayerOneTurn = !isPlayerOneTurn;         //used to check 4 at the last of transition for ai or to allow the next player to insert the disc
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE: PLAYER_TWO );
		});
		if(option == 1)
		{
			//isAllowedToInsertai = true;     //to allow the next player to insert the disc
			isPlayerOneTurn = !isPlayerOneTurn;       // for the next player turn (disc)
		}

		System.out.println("check " + isPlayerOneTurn);

		transition.play();

	}


	private boolean gameEnded(int row, int column){

		//for vertical combinations
		List<Point2D> verticalPoints = IntStream.rangeClosed(row -3, row +3).mapToObj(r -> new Point2D(r,column)).collect(Collectors.toList());

		//for horizontal combinations
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column -3, column +3).mapToObj(col -> new Point2D(row,col)).collect(Collectors.toList());

		//for diagonal1 / top to bottom
		Point2D startPoint1 = new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint1.add(i,-i)).collect(Collectors.toList());

		//for diagonal2 \ top to bottom
		Point2D startPoint2 = new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint2.add(i,i)).collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

		return isEnded;
	}

	/*private int aiCheckCombinations(List<Point2D> points, boolean isPlayerOneTurn)
	{
		int spaceCounter =0;
		int discCounter = 0;



		for(Point2D point2D : points)
		{
			int rowIndexForArray = (int) point2D.getX();
			int columnIndexForArray = (int) point2D.getY();
			Disc disc = discIfPresent(rowIndexForArray,columnIndexForArray);
			if(disc == null){
				spaceCounter++;
			}

			if(disc != null && disc.turn == isPlayerOneTurn)
			{
				discCounter++;
			}
			else if(disc.turn == !isPlayerOneTurn)
				discCounter = 0;
		}
		return 0;
	}*/

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point: points) {
			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
			Disc disc = discIfPresent(rowIndexForArray,columnIndexForArray);
			if(disc != null && disc.turn == isPlayerOneTurn){
				chain++;
				System.out.println(chain);
				if(chain ==4)
					return true;
			}
			else
				chain = 0;

		}
		return false;
	}
	private Disc discIfPresent(int row, int column){ //to prevent array out of bound exception
		if(row>=ROWS || row<0 || column >=COLUMNS || column <0)
			return null;

		return insertedDiscsArray[row][column];
	}

	private void gameOver(){
		String winner = isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner "+winner);

		Alert  alert =  new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect4");
		alert.setHeaderText("The winner is "+winner);
		alert.setContentText("Want to play again?");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

		Platform.runLater( () -> {  //without this, there will be error that during animation showAndWait does not work. so it will run after animation has ended.
			Optional<ButtonType> btnClicked = alert.showAndWait();
			if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
				resetGame(0);//reset game
			}
			else{
				Platform.exit();
				System.exit(0);//exit game
			}
		});

	}

	public void resetGame(int i) {

		discPane.getChildren().clear(); //clears all the discs from the pane.
		for (int row = 0; row < insertedDiscsArray.length; row++) {   //structurally removes all discs.
			for (int column = 0; column < insertedDiscsArray[row].length; column++) {
				insertedDiscsArray[row][column] = null;
			}
		}
		isPlayerOneTurn = true;
		if(i == 0){
			person1.setText("");
			person2.setText("");
			PLAYER_ONE = "Player One";
			PLAYER_TWO = "Player Two";
			playerNameLabel.setText(PLAYER_ONE);
			labelInit();
		}
		else
			playerNameLabel.setText(PLAYER_ONE);

		createPlayGround(option); //create a new playground
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
/*
PLAYER_ONE = person1.getText();

		PLAYER_TWO = person2.getText();

		System.out.println(PLAYER_ONE + PLAYER_TWO);

		setButton.setOnAction(event1 -> playerNameLabel.setText(PLAYER_ONE));

		*/