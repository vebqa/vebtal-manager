package org.vebqa.vebtal;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class RoboManager extends Application {

	private static final Logger logger = LoggerFactory.getLogger(RoboManager.class);

	public static final RestServer singleServer = new RestServer();

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
		GuiManager.getinstance().getMain().setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		// Create help tab
		Tab tabHelp = new Tab();
		// TODO: I18N
		tabHelp.setText("Help");
		GuiManager.getinstance().getMain().getTabs().add(tabHelp);

		mainPane.setCenter(GuiManager.getinstance().getMain());

		// Log Area
		/** Logs **/
		mainPane.setBottom(GuiManager.getinstance().getLogArea());

		// in stage einfuegen
		primaryStage.setScene(new Scene(mainPane, 1024, 800));

		// Plugins laden und ausfuehren
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		if (!plugins.hasNext()) {
			GuiManager.getinstance().writeLog("No plugins found!");
		}
		while (plugins.hasNext()) {
			TestAdaptionPlugin robo = plugins.next();
			GuiManager.getinstance().writeLog("Plugin of type (" + robo.getType() + ") found: " + robo.getName());

			// we will start adapter only at this point
			if (robo.getType() == TestAdaptionType.ADAPTER) {
				try {
					GuiManager.getinstance().getMain().getTabs().add(robo.startup());
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
			GuiManager.getinstance().writeLog("Shutdown plugin " + robo.getName());
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
		GuiManager.getinstance().getMain().getTabs().add(aTab);
	}
}
