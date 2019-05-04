package de.tron.client_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

import de.tron.client_java.gui.view.View;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {    	
    	Font.loadFont(getClass().getResourceAsStream("TR2N.ttf"), 16);
    	Font.loadFont(getClass().getResourceAsStream("Orbitron-Regular.ttf"), 16);
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("Window.fxml"));
    	Parent root = loader.load();
        Scene scene = new Scene(root, 500, 500);
        
        stage.sizeToScene();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
        stage.setTitle("Tron-Game");
        stage.setScene(scene);     
        stage.show();
        
        Object controller = loader.getController();
        
        if (controller instanceof View) {
        	View view = (View) controller;
        	scene.setOnKeyPressed(view::changeDirection);
        	stage.setOnCloseRequest(e -> view.exit());
        }
        
    }

    public static void main(String[] args) {
        launch();
    }

}