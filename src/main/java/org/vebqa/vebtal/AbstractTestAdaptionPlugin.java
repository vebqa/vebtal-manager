package org.vebqa.vebtal;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public abstract class AbstractTestAdaptionPlugin implements TestAdaptionPlugin {

	public Tab startup() {
		throw new UnsupportedOperationException("startup not yet implemented.");
	}

	public boolean shutdown() {
		throw new UnsupportedOperationException("shutdown not yet implemented.");
	}
	
	public Class getImplementation() {
		throw new UnsupportedOperationException("not yet implemented.");
	}

}
