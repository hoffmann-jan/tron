package de.tron.client_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

import de.tron.client_java.gui.view.View;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("Window.fxml"));
    	Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        
        Object controller = loader.getController();
        
        if (controller instanceof View) {
        	scene.setOnKeyPressed(((View) controller)::changeDirection);
        	stage.setOnCloseRequest(e -> ((View) controller).exit());
        }
        
        stage.sizeToScene();
        stage.setTitle("Tron-Game");
        stage.setScene(scene);     
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}