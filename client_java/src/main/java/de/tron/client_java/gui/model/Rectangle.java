package de.tron.client_java.gui.model;

import javafx.scene.paint.Color;

public class Rectangle {

	private double x;
	private double y;
	private double width;
	private double height;
	
	private Color fill;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public Color getFill() {
		return this.fill;
	}
	
	public void setFill(Color fill) {
		this.fill = fill;
	}
}
