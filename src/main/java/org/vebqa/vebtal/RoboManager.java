package org.vebqa.vebtal;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.splash.AppPreloader;

import com.sun.javafx.application.LauncherImpl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SocketAppender;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class RoboManager extends Application {

	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(RoboManager.class);
	
	public static final RestServer singleServer = new RestServer();

    public RoboManager() {
    	// standard constructor
	}
    
	public static void main(String[] args) {
		LauncherImpl.launchApplication(RoboManager.class, AppPreloader.class, args);
	}

    @Override
    public void init() throws Exception {
    	
//    	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        PatternLayoutEncoder ple = new PatternLayoutEncoder();
//
//        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
//        ple.setContext(lc);
//        ple.start();
//        
//        SocketAppender socketAppender = new SocketAppender();
//    	socketAppender.setPort(4445);
//    	socketAppender.setName("vebtal-manager");
//    	socketAppender.setRemoteHost("127.0.0.1");
//        
//
//    	Logger logger = (Logger) LoggerFactory.getLogger(RoboManager.class);
//        logger.addAppender(socketAppender);
//        logger.setLevel(Level.DEBUG);
        
        // BorderPane zur Aufnahme der Tabs
		// BorderPane mainPane = new BorderPane();
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
		
		double p = 10;
		while (plugins.hasNext()) {
			p = p + 10;
			TestAdaptionPlugin robo = plugins.next();
			LauncherImpl.notifyPreloader(this, new AppPreloader.ActualTaskNotification("Start plugin of type (" + robo.getType() + "): " + robo.getName()));
			// we will start adapter only at this point
			if (robo.getType() == TestAdaptionType.ADAPTER) {
				try {
					logger.info("Start plugin of type (" + robo.getType() + "): " + robo.getName());
					GuiManager.getinstance().getMainTab().getTabs().add(robo.startup());
				} catch (Exception e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			}
			Thread.sleep(1000);
		}
		
		// Start REST Server
		Thread t = new Thread(new Runnable() {

			public void run() {
				singleServer.startServer();
			}
		});

		t.start();

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
