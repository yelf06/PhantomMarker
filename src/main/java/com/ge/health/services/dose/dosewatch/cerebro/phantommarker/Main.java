package com.ge.health.services.dose.dosewatch.cerebro.phantommarker;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.UIUtils;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Main class of the Phantom Marker javafx desktop application
 * @SuppressWarnings("restriction") is used because the javafx use is restricted in
 * the 1.8.0_25 jdk version. It's present by default beginning from the 1.8.0_40 jdk version
 * @author Yacine
 *
 */
@SuppressWarnings("restriction")
public class Main extends Application {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

	public static boolean CUSTOM_OUTPUT_FORMAT = false;

	/**
	 * The application main method
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("Phantom marker startup");

		if (args != null && args.length > 0 && ArrayUtils.contains(args, "--custom-output-format")) {
			CUSTOM_OUTPUT_FORMAT = true;
		}

		Application.launch(args);
	}

	/**
	 * Startup method. It is called when the application is launched
	 */
	@Override
	public void start(Stage stage) throws FileNotFoundException {
		stage.setTitle("Phantom Marker");
		if (CUSTOM_OUTPUT_FORMAT){
			stage.setTitle("Phantom Marker - Custom Output Format");
		}
		stage.setMaximized(true);

		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

		VBox rootBox = new VBox();
		Scene scene = new Scene(rootBox, primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight(), Color.TRANSPARENT);
		scene.getStylesheets().add("style.css");
		stage.setScene(scene);

		rootBox.setStyle("-fx-background-color: #A9A9A9;");

		// Adding the menu bar to the stage
		UIUtils.initMenuBar(stage);
		stage.show();
	}
}