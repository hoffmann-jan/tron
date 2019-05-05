package de.tron.client_java.gui.view;

import de.tron.client_java.gui.model.ViewModel;
import de.tron.client_java.gui.view.screen.ConnectionScreen;
import de.tron.client_java.gui.view.screen.LobbyScreen;
import de.tron.client_java.gui.view.screen.Screen;
import de.tron.client_java.gui.view.screen.TitleScreen;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.control.Label;
import de.tron.client_java.gui.view.screen.GameScreen;
import de.tron.client_java.gui.view.screen.ResultScreen;
import javafx.scene.layout.VBox;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;

public class View {

	public static final int BLUR_STRENGTH = 7;
	
	private final DoubleProperty width = new SimpleDoubleProperty();
	private final DoubleProperty height = new SimpleDoubleProperty();
	
	private ViewModel viewModel = new ViewModel();
	
	@FXML private VBox statusInformationBox;

	@FXML private TitleScreen titleScreen;
	@FXML private ConnectionScreen connectionScreen;
	@FXML private LobbyScreen lobbyScreen;
	@FXML private GameScreen gameScreen;
	@FXML private ResultScreen resultScreen;
	@FXML private BoxBlur backgroundBlur;
	@FXML private ImageView background;
	
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
		this.resultScreen.setViewModel(this.viewModel.getResultViewModel());
		
		this.height.addListener((p,o,n) -> resize()); 
		this.width.addListener((p,o,n) -> resize()); 
		
		this.background.fitHeightProperty().bind(heightProperty());
		this.background.fitWidthProperty().bind(widthProperty());
		
		startTitleScreenTransition();
	}

	private void resize() {
		double min = Math.min(this.width.get(), this.height.get());
		this.gameScreen.setWidth(min);
		this.gameScreen.setHeight(min);
	}

	private void showStatusInformation(String status) {
		Label statusInformation = new Label(status);
		statusInformation.setId("status-text");
		this.statusInformationBox.getChildren().add(statusInformation);
		FadeTransition transition = new FadeTransition(new Duration(1000), statusInformation);
		transition.setDelay(Duration.seconds(1.5));
		transition.setFromValue(1);
		transition.setToValue(0);
		transition.setOnFinished(e -> this.statusInformationBox.getChildren().remove(statusInformation));
		transition.play();
	}
	
	private void changeToScreen(Screen newScreen) {
		newScreen.setVisible(true);
		adjustBackgroundBlur(newScreen);
		createAndPlayChangeTransition(newScreen);
	}

	private void adjustBackgroundBlur(Screen newScreen) {
		if (newScreen == this.gameScreen) {
			this.backgroundBlur.setHeight(1);
			this.backgroundBlur.setWidth(1);
		} else {
			this.backgroundBlur.setHeight(View.BLUR_STRENGTH);
			this.backgroundBlur.setWidth(View.BLUR_STRENGTH);
		}
	}
	
	private void createAndPlayChangeTransition(Screen newScreen) {
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

	public DoubleProperty widthProperty() {
		return this.width;
	}
	
	public DoubleProperty heightProperty() {
		return this.height;
	}

}
