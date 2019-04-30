package de.tron.client_java.gui.view.screen;

import de.tron.client_java.gui.model.GameViewModel;
import de.tron.client_java.gui.model.Rectangle;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

public class GameScreen extends Canvas implements Screen {

	private GameViewModel viewModel;
		
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<Rectangle> playerTails = new SimpleListProperty<>(FXCollections.observableArrayList());
	
	private void update(BooleanProperty updatesExistProperty) {
		if (updatesExistProperty.get()) {
			redrawField();
			updatesExistProperty.set(false);
		}
	}
	
	private void redrawField() {
		GraphicsContext context = getGraphicsContext2D();
		context.clearRect(0, 0, getWidth(), getHeight());
		this.playerRectangels.forEach(r -> drawRectangel(r, context));
		this.playerTails.forEach(r -> drawRectangel(r, context));
	}

	private void drawRectangel(Rectangle rectangel, GraphicsContext context) {
		context.setFill(rectangel.getFill());
		context.fillRect(rectangel.getX(), rectangel.getY(), rectangel.getWidth(), rectangel.getHeight());
	}
	
	@Override
	public Transition getTransition(boolean reverse) {
		return new FadeTransition(new Duration(0), this);
	}
	
	private void bindProperties() {
		this.viewModel.updatesExistProperty().addListener((p,o,n) -> update((BooleanProperty) p));
		
		this.playerRectangels.bind(this.viewModel.playerRectangelsProperty());
		this.playerTails.bind(this.viewModel.playerTailsProperty());
	}

	public void setViewModel(GameViewModel viewModel) {
		this.viewModel = viewModel;
		bindProperties();
	}

}
