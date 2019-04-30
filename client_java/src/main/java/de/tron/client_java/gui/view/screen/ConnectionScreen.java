package de.tron.client_java.gui.view.screen;

import java.io.IOException;

import de.tron.client_java.App;
import de.tron.client_java.gui.model.ConnectionViewModel;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ConnectionScreen extends AnchorPane implements Screen {

	@FXML private Label networkHead;
	@FXML private Label gameHead;

	@FXML private Button connectButton;
	
	@FXML private ProgressBar connectIndicator;
	
	@FXML private HBox ipLine;
	@FXML private HBox portLine;
	@FXML private HBox roomLine;
	@FXML private HBox nameLine;
	@FXML private HBox colorLine;
	
	@FXML private TextField ipInput;
	@FXML private TextField portInput;
	@FXML private TextField roomInput;
	@FXML private TextField nameInput;
	
	@FXML private ColorPicker colorInput;
	
	private ConnectionViewModel viewModel;
	
	public ConnectionScreen() {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Connection.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        initalize();
	}
	
	private void initalize() {
		BooleanBinding buttonVisibility = this.ipInput.textProperty().isEmpty().not();
		buttonVisibility = buttonVisibility.and(this.portInput.textProperty().isEmpty().not());
		buttonVisibility = buttonVisibility.and(this.connectIndicator.visibleProperty().not());
		this.connectButton.visibleProperty().bind(buttonVisibility);
		
		this.ipInput.disableProperty().bind(this.connectIndicator.visibleProperty());
		this.portInput.disableProperty().bind(this.connectIndicator.visibleProperty());
		this.roomInput.disableProperty().bind(this.connectIndicator.visibleProperty());
		this.nameInput.disableProperty().bind(this.connectIndicator.visibleProperty());
		this.colorInput.disableProperty().bind(this.connectIndicator.visibleProperty());
	}
	
	private void bindProperties() {
		this.ipInput.textProperty().bindBidirectional(this.viewModel.ipProperty());	
		this.portInput.textProperty().bindBidirectional(this.viewModel.portProperty());	
		this.roomInput.textProperty().bindBidirectional(this.viewModel.roomProperty());	
		this.nameInput.textProperty().bindBidirectional(this.viewModel.nameProperty());	
		this.colorInput.valueProperty().bindBidirectional(this.viewModel.colorProperty());
		this.connectIndicator.visibleProperty().bindBidirectional(this.viewModel.isConnectingProperty());
	}
	
	@Override
	public Transition getTransition(boolean reverse) {
		this.connectIndicator.setOpacity(0);
		Transition transition = new Transition() {
			{
				setCycleDuration(Duration.seconds(2));
			}
			@Override
			protected void interpolate(double frac) {
				if (reverse) {
					frac = Math.abs(frac - 1);
				}
				
				double opacityHead = Math.min(1, frac * 2);
				double opacityInput = Math.max(0, frac * 2 - 1);
				double leftAnchorHead = frac * 100;
				double leftAnchorLine = frac * 200 - 60;
				
				networkHead.setOpacity(opacityHead);
				AnchorPane.setLeftAnchor(networkHead, leftAnchorHead);
				gameHead.setOpacity(opacityHead);
				AnchorPane.setLeftAnchor(gameHead, leftAnchorHead);
				
				ipLine.setOpacity(opacityInput);
				AnchorPane.setLeftAnchor(ipLine, leftAnchorLine);
				portLine.setOpacity(opacityInput);
				AnchorPane.setLeftAnchor(portLine, leftAnchorLine);
				roomLine.setOpacity(opacityInput);
				AnchorPane.setLeftAnchor(roomLine, leftAnchorLine);
				nameLine.setOpacity(opacityInput);
				AnchorPane.setLeftAnchor(nameLine, leftAnchorLine);
				colorLine.setOpacity(opacityInput);
				AnchorPane.setLeftAnchor(colorLine, leftAnchorLine);
				
			}
		};
		transition.setInterpolator(Interpolator.LINEAR);
		transition.setOnFinished(e -> setVisible(!reverse));
		return transition;
	}
	
	@FXML
	private void connect() {
		this.viewModel.connect();
	}

	public void setViewModel(ConnectionViewModel viewModel) {
		this.viewModel = viewModel;
		bindProperties();
	}
	
}
