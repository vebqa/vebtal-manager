package org.vebqa.vebtal;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public interface TestAdaptionPlugin {

	String getName();
	
	Class getImplementation();
	
	Tab startup();
	
	boolean shutdown();
}
