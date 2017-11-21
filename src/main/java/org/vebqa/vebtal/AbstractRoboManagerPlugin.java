package org.veba.roborest;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public abstract class AbstractRoboManagerPlugin implements RoboManagerPlugin {

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
