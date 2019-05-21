package de.tron.client_java.gui.view;

import java.util.logging.Level;
import java.util.logging.Logger;

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
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.control.Label;
import de.tron.client_java.gui.view.screen.GameScreen;
import de.tron.client_java.gui.view.screen.ResultScreen;
import javafx.scene.layout.VBox;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;

/**
 * Main class of the client gui. Defines the general procedure of the game.
 * 
 * @author emaeu
 *
 */
public class View {

	private static final Logger LOGGER = Logger.getLogger("root");
	
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
		View.LOGGER.log(Level.INFO, "Initializing the main view");
		
		this.viewModel.statusProperty().addListener((p,o,n) -> showStatusInformation((StringProperty) p, n));
		
		// define the executed action if the view model wants to change the screen
		this.viewModel.setOnShowLobby(() -> changeToScreen(this.lobbyScreen));
		this.viewModel.setOnStart(() -> changeToScreen(this.gameScreen));
		this.viewModel.setOnResult(() -> changeToScreen(this.resultScreen));
		this.viewModel.setOnReturnToStart(() -> changeToScreen(this.connectionScreen));
		
		this.connectionScreen.setViewModel(this.viewModel.getConnectionViewModel());
		this.lobbyScreen.setViewModel(this.viewModel.getLobbyViewModel());
		this.resultScreen.setViewModel(this.viewModel.getResultViewModel());
		this.gameScreen.setViewModel(this.viewModel.getGameViewModel());
		this.gameScreen.applyBlurEffect(this.backgroundBlur);
		
		// handle window resizing
		this.height.addListener((p,o,n) -> resize()); 
		this.width.addListener((p,o,n) -> resize()); 
		this.background.fitHeightProperty().bind(heightProperty());
		this.background.fitWidthProperty().bind(widthProperty());
		
		// start the gui procedure with the title screen
		this.currentScreen = this.titleScreen;
		changeToScreen(this.connectionScreen);
	}

	/**
	 * Resize the window without changing the aspect ratio of the contained components
	 */
	private void resize() {
		double min = Math.min(this.width.get(), this.height.get());
		this.gameScreen.setWidth(min);
		this.gameScreen.setHeight(min);
	}

	private void showStatusInformation(StringProperty property, String status) {
		if (status != null) {
			View.LOGGER.log(Level.INFO, "Showing the status information \"{0}\"", status);
			
			Label statusInformation = new Label(status);
			statusInformation.setId("status-text");
			this.statusInformationBox.getChildren().add(statusInformation);
			createAndPlayStatusTransition(statusInformation);
			property.set(null);
		}
	}

	private void createAndPlayStatusTransition(Label statusInformation) {
		FadeTransition transition = new FadeTransition(new Duration(1000), statusInformation);
		transition.setDelay(Duration.seconds(1.5));
		transition.setFromValue(1);
		transition.setToValue(0);
		transition.setOnFinished(e -> this.statusInformationBox.getChildren().remove(statusInformation));
		transition.play();
	}
	
	private void changeToScreen(Screen newScreen) {
		if (newScreen != currentScreen) {
			View.LOGGER.log(Level.INFO, "Changing to {0}", newScreen.getClass().getSimpleName());
			
			newScreen.setVisible(true);
			adjustBackgroundBlur(newScreen);
			createAndPlayChangeTransition(newScreen);
		}
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
