package org.vebqa.vebtal;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerTestAdaptionPlugin extends AbstractTestAdaptionPlugin {

	private static final Logger logger = LoggerFactory.getLogger(ManagerTestAdaptionPlugin.class);
	
	public static final String ID = "manager";
	
	public ManagerTestAdaptionPlugin() {
		super(TestAdaptionType.EXTENSION);
	}
	
	@Override
	public Class<?> getImplementation() {
		return null;
	}
	
	@Override
	public String getAdaptionID() {
		return ID;
	}	
	
	@Override
	public String getName() {
		return "VEBTAL Manager Plugin";
	}

	/**
	 * load core config and merge with user config.
	 */
	@Override
	public CombinedConfiguration loadConfig() {
		return loadConfig(ID);
	}
}
