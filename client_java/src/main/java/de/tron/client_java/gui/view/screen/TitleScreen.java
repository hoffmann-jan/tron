package de.tron.client_java.gui.view.screen;

import java.io.IOException;

import de.tron.client_java.App;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class TitleScreen extends ImageView implements Screen {

	public TitleScreen() {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(App.JAR_PATH_PREFIX + "Title.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	@Override
	public Transition getTransition(boolean reverse) {
		FadeTransition transition = new FadeTransition(Duration.seconds(1), this);
		transition.setDelay(Duration.seconds(2));
		transition.setFromValue(reverse ? 1 : 0);
		transition.setToValue(reverse ? 0 : 1);
		return transition;
	}

}
