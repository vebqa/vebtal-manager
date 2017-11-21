package org.veba.roborest.model;

import javafx.beans.property.SimpleStringProperty;

@SuppressWarnings("restriction")
public class CommandResult {
	private final SimpleStringProperty command;
	private final SimpleStringProperty target;
	private final SimpleStringProperty value;
	private final SimpleStringProperty result;
	private final SimpleStringProperty loginfo;

	public CommandResult(String aCommand, String aTarget, String aValue) {
		this.command = new SimpleStringProperty(aCommand);
		this.target = new SimpleStringProperty(aTarget);
		this.value = new SimpleStringProperty(aValue);
		this.result = new SimpleStringProperty("testing..");
		this.loginfo = new SimpleStringProperty("");
	}

	public String getCommand() {
		return command.get();
	}

	public String getTarget() {
		return target.get();
	}

	public String getValue() {
		return value.get();
	}

	public String getResult() {
		return result.get();
	}
	
	public void setResult(boolean bResult) {
		if (!bResult) {
			this.result.set("FAILED");
		} else {
			this.result.set("PASSED");
		}
	}

	public String getLoginfo() {
		return this.loginfo.get();
	}
	
	public void setLogInfo(String anInfo) {
		this.loginfo.set(anInfo);
	}
}
