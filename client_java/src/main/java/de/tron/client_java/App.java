package de.tron.client_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tron.client_java.gui.view.View;

public class App extends Application {

	private static final Logger LOGGER = Logger.getLogger("root");
	
	public static final String JAR_PATH_PREFIX = "";
	
    @Override
    public void start(Stage stage) throws IOException {	
    	App.LOGGER.log(Level.INFO, "Starting application");
    	
    	loadFonts();
    	Scene scene = loadScene(stage);
        
        stage.setTitle("Tron-Game");
        stage.setScene(scene);     
        stage.sizeToScene();
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth()); 
    }

	private Scene loadScene(Stage stage) throws IOException {
		App.LOGGER.log(Level.INFO, "Loading scene");
		FXMLLoader loader = new FXMLLoader(getClass().getResource(App.JAR_PATH_PREFIX + "Window.fxml"));
    	Parent root = loader.load();
        Scene scene = new Scene(root, 500, 500);
        scene.setFill(Color.rgb(64, 64, 64));
        configureController(stage, loader, scene);
		return scene;
	}

	private void configureController(Stage stage, FXMLLoader loader, Scene scene) {
		Object controller = loader.getController();
        
        if (controller instanceof View) {
        	View view = (View) controller;
        	scene.setOnKeyPressed(view::changeDirection);
        	view.widthProperty().bind(scene.widthProperty());
        	view.heightProperty().bind(scene.heightProperty());
        	stage.setOnCloseRequest(e -> view.exit());
        }
	}

	private void loadFonts() {
		App.LOGGER.log(Level.INFO, "Loading fonts");
		Font.loadFont(getClass().getResourceAsStream(App.JAR_PATH_PREFIX + "TR2N.ttf"), 16);
    	Font.loadFont(getClass().getResourceAsStream(App.JAR_PATH_PREFIX + "Orbitron-Regular.ttf"), 16);
	}

    public static void main(String[] args) {
        launch();
    }

}