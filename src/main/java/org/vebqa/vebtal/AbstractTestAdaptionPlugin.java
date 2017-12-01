package org.vebqa.vebtal;

import javafx.scene.control.Tab;

@SuppressWarnings("restriction")
public abstract class AbstractTestAdaptionPlugin implements TestAdaptionPlugin {
	
	protected TestAdaptionType adaptionType;

	public AbstractTestAdaptionPlugin() {
		throw new UnsupportedOperationException("Use constructor without setting the adaption type is forbidden.");
	}
	
	public AbstractTestAdaptionPlugin(TestAdaptionType aType) {
		this.adaptionType = aType;
	}
		
	public TestAdaptionType getType() {
		if (adaptionType == null) {
			throw new UnsupportedOperationException("Adaption type has to be defined before!");	
		} else {
			return adaptionType;
		}
	}
	
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
