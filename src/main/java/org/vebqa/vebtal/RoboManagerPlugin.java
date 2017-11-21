package org.veba.roborest;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public interface RoboManagerPlugin {

	String getName();
	
	Class getImplementation();
	
	Tab startup();
	
	boolean shutdown();
}
