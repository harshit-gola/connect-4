package com.connectfour;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FirstController implements Initializable {
	public Controller controller;
	@FXML
	private ChoiceBox choice;

	private static final String hvh = "Human vs Human";
	private static final String hvc = "Human vs Computer";
	private static final String displayText = "Choose the gaming mode";

	private boolean isHvH = true;
	private int i = -1;

	public void startGame(){

		choice.getItems().add(hvh);
		choice.getItems().add(hvc);
		choice.getItems().add(displayText);
		System.out.println("options added");

		choice.setValue(displayText);

		System.out.println("display value");

		choice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue.equals(hvh)){
				isHvH = true;
				System.out.println("ishvh true");
				i=0;
			}
			else if(newValue.equals(hvc))
			{
				isHvH = false;
				System.out.println("ishvh false");
				i=1;
			}
			else{
				i=-1;
			}

		});

	}

	@FXML
	private void handleButton(ActionEvent event) throws IOException {
		System.out.println("button pressed");
		if(i==0)
		{
			System.out.println("i=0");
			game(i);
		}
		else if(i == 1){
			System.out.println("AI vs Human");
			game(i);
			//i=-1;
		}

	}
	private void game(int i) throws IOException
	{

		FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
		GridPane gridPane = loader.load();
		controller = loader.getController();
		controller.labelInit();
		controller.createPlayGround(i);

		Pane menuPane = (Pane) gridPane.getChildren().get(0);
		MenuBar menuBar = createMenu();
		Stage stage = new Stage();
		menuBar.prefWidthProperty().bind(stage.widthProperty());
		menuPane.getChildren().addAll(menuBar);
		//root.add(menuBar,0,0,2,1);

		Scene scene = new Scene(gridPane);
		stage.setTitle("Connect4 Game");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
		//i=-1;

	}
	private MenuBar createMenu() {
		Menu fileMenu = new Menu("File");
		MenuItem newGame = new MenuItem("New Game");

		newGame.setOnAction(Event -> controller.resetGame(0));

		MenuItem reset = new MenuItem("Reset Game");

		reset.setOnAction(Event -> controller.resetGame(1));

		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
		MenuItem exitGame = new MenuItem("Exit Game");
		exitGame.setOnAction(Event -> exitG());

		fileMenu.getItems().addAll(newGame,reset,separatorMenuItem,exitGame);

		Menu helpMenu = new Menu("Help");
		MenuItem aboutConnect4 = new MenuItem("About Connect4");
		aboutConnect4.setOnAction(event -> aboutC4());

		SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();
		MenuItem aboutMe = new MenuItem("About Me");
		aboutMe.setOnAction(event -> aboutM());

		helpMenu.getItems().addAll(aboutConnect4,separatorMenuItem1,aboutMe);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu,helpMenu);

		return menuBar;

	}

	private void aboutM() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About Developer ");
		alert.setContentText(" Description ");
		alert.show();
	}

	private void aboutC4() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About Connect4");
		alert.setHeaderText("How to play?");
		alert.setContentText("Connect Four is a two-player connection game in which the players first choose a color and then take turns dropping colored discs from the top into a seven-column, six-row vertically suspended grid. The pieces fall straight down, occupying the next available space within the column. The objective of the game is to be the first to form a horizontal, vertical, or diagonal line of four of one's own discs. Connect Four is a solved game. The first player can always win by playing the right moves.");
		alert.show();
	}

	private void exitG() {
		Platform.exit();
		System.exit(0);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
