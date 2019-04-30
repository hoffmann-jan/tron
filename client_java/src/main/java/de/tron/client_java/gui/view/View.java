package de.tron.client_java.gui.view;

import de.tron.client_java.gui.model.ViewModel;
import de.tron.client_java.gui.view.screen.ConnectionScreen;
import de.tron.client_java.gui.view.screen.LobbyScreen;
import de.tron.client_java.gui.view.screen.Screen;
import de.tron.client_java.gui.view.screen.TitleScreen;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.control.Label;
import de.tron.client_java.gui.view.screen.GameScreen;
import de.tron.client_java.gui.view.screen.ResultScreen;

public class View {

	private ViewModel viewModel = new ViewModel();
	
	@FXML private TitleScreen titleScreen;
	@FXML private Label statusInformation;

	@FXML private ConnectionScreen connectionScreen;
	@FXML private LobbyScreen lobbyScreen;
	@FXML private GameScreen gameScreen;
	@FXML private ResultScreen resultScreen;
	
	private Screen currentScreen;
	
	@FXML
	private void initialize() {
		this.viewModel.statusProperty().addListener((p,o,n) -> showStatusInformation(n));
		
		this.viewModel.setOnShowLobby(() -> changeToScreen(this.lobbyScreen));
		this.viewModel.setOnStart(() -> changeToScreen(this.gameScreen));
		this.viewModel.setOnResult(() -> changeToScreen(this.resultScreen));
		
		this.connectionScreen.setViewModel(this.viewModel.getConnectionViewModel());
		this.lobbyScreen.setViewModel(this.viewModel.getLobbyViewModel());
		this.gameScreen.setViewModel(this.viewModel.getGameViewModel());
		
		startTitleScreenTransition();
	}

	private void showStatusInformation(String status) {
		this.statusInformation.setOpacity(1);
		this.statusInformation.setText(status);
		FadeTransition transition = new FadeTransition(new Duration(1000), this.statusInformation);
		transition.setDelay(Duration.seconds(1));
		transition.setFromValue(1);
		transition.setToValue(0);
		transition.play();
	}
	
	private void changeToScreen(Screen newScreen) {
		newScreen.setVisible(true);
		
		Transition lastScreenFade = this.currentScreen.getTransition(true);
		Transition newScreenFade = newScreen.getTransition(false);
		SequentialTransition completeFade = new SequentialTransition(lastScreenFade, newScreenFade);
		completeFade.setOnFinished(e -> {
			this.currentScreen.setVisible(false);
			this.currentScreen = newScreen;
		});
		completeFade.play();
	}

	private void startTitleScreenTransition() {
		this.currentScreen = titleScreen;
		Transition transition = this.titleScreen.getTransition(false);
		transition.setOnFinished(e -> changeToScreen(this.connectionScreen));
		transition.play();
	}

	public void changeDirection(KeyEvent event) {
		this.viewModel.changeDirection(event.getCode());
	}
	
	public void exit() {
		this.viewModel.disconnect();
	}


}
