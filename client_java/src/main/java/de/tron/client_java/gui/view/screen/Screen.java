package de.tron.client_java.gui.view.screen;

import javafx.animation.Transition;

public interface Screen {
	
	public Transition getTransition(boolean reverse);
	public void setVisible(boolean visible);
	
}
