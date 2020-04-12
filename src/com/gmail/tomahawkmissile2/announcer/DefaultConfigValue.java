package com.gmail.tomahawkmissile2.announcer;

public enum DefaultConfigValue {
	
	ANNOUNCE_INTERVAL("interval","90000");
	
	private final String path;
	private final String value;
	DefaultConfigValue(String path,String value) {
		this.path=path;
		this.value=value;
	}
	public String getPath() {
		return path;
	}
	public String getValue() {
		return value;
	}
}
