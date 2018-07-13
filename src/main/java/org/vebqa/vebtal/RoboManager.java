package org.vebqa.vebtal;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.splash.AppPreloader;

import com.sun.javafx.application.LauncherImpl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class RoboManager extends Application {

	public static final Logger logger = LoggerFactory.getLogger(RoboManager.class);
	
	public static final RestServer singleServer = new RestServer();

    public RoboManager() {
    	// standard constructor
	}
    
	public static void main(String[] args) {
		LauncherImpl.launchApplication(RoboManager.class, AppPreloader.class, args);
	}

    @Override
    public void init() throws Exception {
        
		ConfigurationBuilder<BuiltConfiguration> configBuilder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		configBuilder.setStatusLevel(Level.INFO);
		configBuilder.setConfigurationName("VEBTALRT");

		// create the appender
		LayoutComponentBuilder layoutBuilder = configBuilder.newLayout("JsonLayout");
		
		AppenderComponentBuilder appenderBuilder = configBuilder.newAppender("remoteAppender", "Socket").addAttribute("host", "localhost").addAttribute("port", 4445).add(layoutBuilder);
		configBuilder.add(appenderBuilder);
		
		// create a new logger
		configBuilder.add(configBuilder.newLogger("rtlogger", Level.DEBUG).add(configBuilder.newAppenderRef("remoteAppender")).addAttribute("additivity", false));
		
		configBuilder.add(configBuilder.newRootLogger(Level.DEBUG).add(configBuilder.newAppenderRef("remoteAppender")));
		
		LoggerContext ctx = Configurator.initialize(configBuilder.build());    	
    	
        // BorderPane zur Aufnahme der Tabs
		GuiManager.getinstance().getMainTab().setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		// Create help tab
		Tab tabHelp = new Tab();
		// TODO: I18N
		tabHelp.setText("Help");
		GuiManager.getinstance().getMainTab().getTabs().add(tabHelp);

		GuiManager.getinstance().getMain().setCenter(GuiManager.getinstance().getMainTab());

		// Log Area
		/** Logs **/
		GuiManager.getinstance().getMain().setBottom(GuiManager.getinstance().getLogArea());
		
		// Plugins laden und ausfuehren
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		if (!plugins.hasNext()) {
			GuiManager.getinstance().writeLog("No plugins found!");
		}
		
		while (plugins.hasNext()) {
			TestAdaptionPlugin robo = plugins.next();
			LauncherImpl.notifyPreloader(this, new AppPreloader.ActualTaskNotification("Start plugin of type (" + robo.getType() + "): " + robo.getName()));
			// we will start adapter only at this point
			if (robo.getType() == TestAdaptionType.ADAPTER) {
				try {
					// logger.info("Start plugin of type (" + robo.getType() + "): " + robo.getName());
					GuiManager.getinstance().getMainTab().getTabs().add(robo.startup());
				} catch (Exception e) {
					// logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			}
			Thread.sleep(250);
		}
		
		// Start REST Server
		Thread t = new Thread(new Runnable() {

			public void run() {
				singleServer.startServer();
			}
		});

		t.start();
		
		while (!t.isAlive()) {
			LauncherImpl.notifyPreloader(this, new AppPreloader.ActualTaskNotification("Wait for service startup completion."));
			Thread.sleep(50);
		}

    }
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// application title
		// TODO: I18N
		primaryStage.setTitle("Test Adaption Manager");

		// in stage einfuegen
		primaryStage.setScene(new Scene(GuiManager.getinstance().getMain(), 1024, 800));

		// Close all -> shutdown all plugins
		primaryStage.setOnCloseRequest(evt -> {
			// execute own shutdown procedure
			shutdown(primaryStage);
		});

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
				// logger.error("Error while starting plugin: " + robo.getName(), e);
			}
		}
		RoboManager.singleServer.shutdownServer();
		try {
			stop();
		} catch (Exception e) {
			// logger.error("Error while stopping application.", e);
		}
		
		// close this...
		mainWindow.close();
	}

	public static void addTab(Tab aTab) {
		GuiManager.getinstance().getMainTab().getTabs().add(aTab);
	}
}
