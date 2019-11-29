package org.vebqa.vebtal.splash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AppPreloader extends Preloader {
	
	private static final Logger logger = LoggerFactory.getLogger(AppPreloader.class);
	
    private static final double WIDTH = 512;
    private static final double HEIGHT = 384;

    private Stage preloaderStage;
    private Scene scene;

    private Label progress;

    public AppPreloader() {
    }

    @Override
    public void init() throws Exception {

    	Image image = new Image("/splash/splash001.png");
    	final ImageView imageView = new ImageView(image);
    	
        Platform.runLater(() -> {
            Label title = new Label("Starting openTAL Manager!\nLoading, please wait...");
            title.setTextAlignment(TextAlignment.CENTER);
            progress = new Label("0%");

            HBox hbox = new HBox(imageView);
            
            VBox root = new VBox(title, progress);
            root.setAlignment(Pos.CENTER);
            
            

            scene = new Scene(hbox, WIDTH, HEIGHT);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.preloaderStage = primaryStage;

        // Set preloader scene and show stage.
        preloaderStage.setScene(scene);
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        // Handle application notification in this point (see MyApplication#init).
        if (info instanceof ProgressNotification) {
            progress.setText(((ProgressNotification) info).getProgress() + "%");
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        // Handle state change notifications.
        StateChangeNotification.Type type = info.getType();
        switch (type) {
            case BEFORE_LOAD:
                // Called after MyPreloader#start is called.
                logger.debug("BEFORE_LOAD");
                break;
            case BEFORE_INIT:
                // Called before MyApplication#init is called.
                logger.debug("BEFORE_INIT");
                break;
            case BEFORE_START:
                // Called after MyApplication#init and before MyApplication#start is called.
                logger.debug("BEFORE_START");

                preloaderStage.hide();
                break;
        }
    }
    
    public static class ActualTaskNotification implements PreloaderNotification {

    	private String details = "";

        /**
         * Constructs an error notification.
         *
         * @param details a string describing the error; must be non-null
         */
        public ActualTaskNotification(String details) {
            if (details == null) throw new NullPointerException();

            this.details = details;
        }

        /**
         * Retrieves the description of the error.
         * It may be the empty string, but is always non-null.
         *
         * @return the description of the error
         */
        public String getDetails() {
            return details;
        }

        /**
         * Returns a string representation of this {@code ErrorNotification} object.
         * @return a string representation of this {@code ErrorNotification} object.
         */
        @Override public String toString() {
            StringBuilder str = new StringBuilder("Preloader.ActualTaskNotification: ");
            str.append(details);
            return str.toString();
        }
    }    
}
