package org.vebqa.vebtal;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public interface TestAdaptionPlugin {

	TestAdaptionType getType();
	
	String getName();
	
	Class getImplementation();
	
	Tab startup();
	
	boolean shutdown();
}
