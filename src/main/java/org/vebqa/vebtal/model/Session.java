package org.veba.roborest.model;

public class Session {

	private String host;
	private String port;
	private String codepage;
	private String ssltype;
	
	public Session() {
	}
	
	public Session(String aHost, String aPort, String aCodePage, String aSslType) {
		this.host = aHost;
		this.port = aPort;
		this.codepage = aCodePage;
		this.ssltype = aSslType;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getCodepage() {
		return codepage;
	}

	public void setCodepage(String codepage) {
		this.codepage = codepage;
	}

	public String getSsltype() {
		return ssltype;
	}

	public void setSsltype(String ssltype) {
		this.ssltype = ssltype;
	}

	@Override
	public String toString() {
		return "TNCreateSession [host=" + this.host + ", port=" + this.port + ", codepage=" + this.codepage + ", ssltype=" + this.ssltype + "]";
	}
}
