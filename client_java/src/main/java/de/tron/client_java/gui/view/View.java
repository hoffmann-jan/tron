package de.tron.client_java.gui.view;

import de.tron.client_java.gui.model.Rectangle;
import de.tron.client_java.gui.model.ViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;

public class View {

	private ViewModel viewModel = new ViewModel();
	
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<Rectangle> playerTails = new SimpleListProperty<>(FXCollections.observableArrayList());
	
	@FXML private Canvas field;
	@FXML private ImageView titleScreen;
	@FXML private Label statusInformation;

	@FXML private ConnectionView connectionScreen;
	@FXML private LobbyView lobbyView;
	
	@FXML
	private void initialize() {
		this.playerRectangels.bind(this.viewModel.playerRectangelsProperty());
		this.playerTails.bind(this.viewModel.playerTailsProperty());
		
		this.viewModel.setOnShowLobby(this::showLobby);
		this.viewModel.setOnStatusChange(this::showStatusInformation);
		this.viewModel.setOnUpdate(this::redrawField);
		this.viewModel.setOnStart(this::startGame);
		
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

	private void startTitleScreenTransition() {
		FadeTransition transition = new FadeTransition(Duration.seconds(1), this.titleScreen);
		transition.setDelay(Duration.seconds(2));
		transition.setFromValue(1);
		transition.setToValue(0);
		transition.setOnFinished(e -> showConnectionScreen());
		transition.play();
	}

	private void showConnectionScreen() {
		this.titleScreen.setVisible(false);
		this.connectionScreen.setViewModel(this.viewModel.getConnectionViewModel());
		this.connectionScreen.getTransition(false).play();
	}
	
	private void showLobby() {
		this.lobbyView.setVisible(true);
		Transition connectionFade = this.connectionScreen.getTransition(true);
		Transition lobbyFade = this.lobbyView.getTransition(false);
		SequentialTransition completeFade = new SequentialTransition(connectionFade, lobbyFade);
		completeFade.play();
		this.lobbyView.setViewModel(this.viewModel.getLobbyViewModel());
	}
	
	private void startGame() {
		this.lobbyView.getTransition(true).play();
	}

	private void redrawField() {
		GraphicsContext context = this.field.getGraphicsContext2D();
		context.clearRect(0, 0, this.field.getWidth(), this.field.getHeight());
		this.playerRectangels.forEach(r -> drawRectangel(r, context));
		this.playerTails.forEach(r -> drawRectangel(r, context));
	}

	private void drawRectangel(Rectangle rectangel, GraphicsContext context) {
		context.setFill(rectangel.getFill());
		context.fillRect(rectangel.getX(), rectangel.getY(), rectangel.getWidth(), rectangel.getHeight());
	}

	public void changeDirection(KeyEvent event) {
		this.viewModel.changeDirection(event.getCode());
	}
	
	public void exit() {
		this.viewModel.disconnect();
	}


}
