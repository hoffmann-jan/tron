package de.tron.client_java.gui.view.screen;

import java.util.List;

import de.tron.client_java.gui.model.GameViewModel;
import de.tron.client_java.gui.model.Rectangle;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameScreen extends Canvas implements Screen {

	private GameViewModel viewModel;
		
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final MapProperty<Integer, List<Rectangle>> playerTails = new SimpleMapProperty<>(FXCollections.observableHashMap());
	
	private void update(BooleanProperty updatesExistProperty) {
		if (updatesExistProperty.get()) {
			redrawField();
			updatesExistProperty.set(false);
		}
	}
	
	private void redrawField() {
		GraphicsContext context = getGraphicsContext2D();
		context.clearRect(0, 0, getWidth(), getHeight());
		this.playerRectangels.forEach(r -> drawPlayer(r, context));
		drawPlayerTails(context);
		
	}

	private void drawPlayerTails(GraphicsContext context) {
		this.playerTails.values().forEach(l -> drawPlayerTail(l, context));		
	}
	
	private void drawPlayerTail(List<Rectangle> tail, GraphicsContext context) {
		if (tail.isEmpty()) {
			return;
		}
		
		Rectangle start = tail.get(0);
		Rectangle previous = new Rectangle();
		Color color = start.getFill().deriveColor(0, 1, 1, 0.5);
		
		context.beginPath();
		context.moveTo(start.getX(), start.getY());
		
		tail.forEach(r -> {
			drawNextPoint(r, previous, context);
			previous.setX(r.getX());
			previous.setY(r.getY());
		});
		
		context.setStroke(color);
		context.setLineWidth(start.getWidth());
		context.stroke();
	}

	private void drawNextPoint(Rectangle rectangle, Rectangle previous, GraphicsContext context) {
		if (previous != null) {
			double diffX = Math.abs(rectangle.getX() - previous.getX());
			double diffY = Math.abs(rectangle.getY() - previous.getY());
			if (diffX > 100 || diffY > 100) {
				context.moveTo(rectangle.getX(), rectangle.getY());
				return;
			}
		}
		context.lineTo(rectangle.getX(), rectangle.getY());
	}

	private void drawPlayer(Rectangle rectangel, GraphicsContext context) {
		context.setFill(rectangel.getFill());
		context.fillRect(rectangel.getX(), rectangel.getY(), rectangel.getWidth(), rectangel.getHeight());
	}
	
	@Override
	public Transition getTransition(boolean reverse) {
		return new FadeTransition(Duration.seconds(reverse ? 1 : 0), this);
	}
	
	private void bindProperties() {
		this.viewModel.updatesExistProperty().addListener((p,o,n) -> update((BooleanProperty) p));
		
		this.playerRectangels.bind(this.viewModel.playerRectangelsProperty());
		this.playerTails.bind(this.viewModel.playerTailsProperty());
		
		this.viewModel.widthProperty().bind(widthProperty());
		this.viewModel.heightProperty().bind(heightProperty());
	}

	public void setViewModel(GameViewModel viewModel) {
		this.viewModel = viewModel;
		bindProperties();
	}

	public void applyBlurEffect(BoxBlur backgroundBlur) {
		setEffect(backgroundBlur);		
	}

}
