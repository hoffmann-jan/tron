package de.tron.client_java.gui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;

public class View {

	@FXML 
	private Canvas field;
	
	@FXML 
	private void changeDirection(KeyEvent event) {
		switch (event.getCode()) {
		case W:
			System.out.println("UP");
			break;
		case D:
			System.out.println("RIGHT");
			break;
		case S:
			System.out.println("DOWN");
			break;
		case A:
			System.out.println("LEFT");
			break;

		default:
			break;
		}
	}


}
