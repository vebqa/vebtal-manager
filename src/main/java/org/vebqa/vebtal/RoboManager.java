package org.vebqa.vebtal;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class RoboManager extends Application {

	private static final Logger logger = LoggerFactory.getLogger(RoboManager.class);

	public static final RestServer singleServer = new RestServer();

	private static TabPane mainTabPane = new TabPane();

	/** Logs **/
	private static TextArea textArea = new TextArea();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		// application title
		// TODO: I18N
		primaryStage.setTitle("Test Adaption Manager");

		// BorderPane zur Aufnahme der Tabs
		BorderPane mainPane = new BorderPane();
		mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		// Create help tab
		Tab tabHelp = new Tab();
		// TODO: I18N
		tabHelp.setText("Help");
		mainTabPane.getTabs().add(tabHelp);

		mainPane.setCenter(mainTabPane);

		// Log Area
		/** Logs **/
		mainPane.setBottom(RoboManager.textArea);

		// in stage einfuegen
		primaryStage.setScene(new Scene(mainPane, 1024, 800));

		// Plugins laden und ausfuehren
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		if (!plugins.hasNext()) {
			writeToArea("No plugins found!");
		}
		while (plugins.hasNext()) {
			TestAdaptionPlugin robo = plugins.next();
			writeToArea("Plugin of type (" + robo.getType() + ") found: " + robo.getName());

			// we will start adapter only at this point
			if (robo.getType() == TestAdaptionType.ADAPTER) {
				try {
					mainTabPane.getTabs().add(robo.startup());
				} catch (Exception e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			}
		}

		// Close all -> shutdown all plugins
		primaryStage.setOnCloseRequest(evt -> {
			// execute own shutdown procedure
			shutdown(primaryStage);
		});

		Thread t = new Thread(new Runnable() {

			public void run() {
				singleServer.startServer();
			}
		});

		t.start();

		// anzeigen
		primaryStage.show();

	}

	/**
	 * Shutdown application - call all plugins and tear down.
	 * 
	 * @param mainWindow
	 */
	private void shutdown(Stage mainWindow) {
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		while (plugins.hasNext()) {
			TestAdaptionPlugin robo = plugins.next();
			writeToArea("Shutdown plugin " + robo.getName());
			try {
				robo.shutdown();
			} catch (Exception e) {
				logger.error("Error while starting plugin: " + robo.getName(), e);
			}
		}
		RoboManager.singleServer.shutdownServer();
		try {
			stop();
		} catch (Exception e) {
			logger.error("Error while stopping application.", e);
		}
		
		// close this...
		mainWindow.close();
	}

	public static void addTab(Tab aTab) {
		mainTabPane.getTabs().add(aTab);
	}

	public static TextArea getLogArea() {
		return textArea;
	}

	public static void setLogArea(TextArea anArea) {
		textArea = anArea;
	}

	public static void writeToArea(String someText) {
		Platform.runLater(() -> textArea.appendText(someText + "\n"));
	}
}
