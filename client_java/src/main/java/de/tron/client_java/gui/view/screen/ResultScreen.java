package de.tron.client_java.gui.view.screen;

import java.io.IOException;

import de.tron.client_java.App;
import de.tron.client_java.gui.model.ResultViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ResultScreen extends VBox implements Screen {

	private ResultViewModel viewModel;
	
	@FXML private LobbyPlayerEntry winner;
	
	public ResultScreen() {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(App.JAR_PATH_PREFIX + "Result.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	private void bindProperties() {
		this.winner.playerColorProperty().bind(this.viewModel.playerColorProperty());
		this.winner.playerNameProperty().bind(this.viewModel.playerNameProperty());
	}
	
	@Override
	public Transition getTransition(boolean reverse) {
		FadeTransition transition = new FadeTransition(Duration.seconds(1), this);
		transition.setFromValue(reverse ? 1 : 0);
		transition.setToValue(reverse ? 0 : 1);
		return transition;
	}
	
	public void setViewModel(ResultViewModel viewModel) {
		this.viewModel = viewModel;
		bindProperties();
	}

}
