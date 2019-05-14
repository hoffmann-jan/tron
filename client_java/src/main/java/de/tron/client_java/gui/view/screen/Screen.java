package de.tron.client_java.gui.view.screen;

import javafx.animation.Transition;

public interface Screen {
	
	/**
	 * Returns the transition of this screen
	 * 
	 * @param reverse
	 * @return
	 */
	public Transition getTransition(boolean reverse);
	
	/**
	 * Change the visibility of the screen
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible);
	
}
