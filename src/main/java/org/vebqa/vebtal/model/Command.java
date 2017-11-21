package org.veba.roborest.model;

public class Command {

	private String cmd;
	private String target;
	private String value;
	
	public Command() {
	}
	
	public Command(String aCommand, String aTarget, String aValue) {
		this.cmd = aCommand;
		this.target = aTarget;
		this.value = aValue;
	}
	
	// Some getter and setter...
	public String getCommand() {
		return cmd;
	}
	public void setCommand(String command) {
		this.cmd = command;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "Command [command=" + this.cmd + ", target=" + this.target + ", value=" + this.value + "]";
	}
}
