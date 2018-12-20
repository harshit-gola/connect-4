package com.connectfour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("startt.fxml"));

        FXMLLoader startLoader = new FXMLLoader(getClass().getResource("startt.fxml"));
        VBox root = startLoader.load();
        com.connectfour.FirstController firstController = startLoader.getController();
        firstController.startGame();


        Scene scene = new Scene(root);
        primaryStage.setTitle("Connect4 Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
