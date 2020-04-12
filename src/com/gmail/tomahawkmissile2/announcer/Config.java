package com.gmail.tomahawkmissile2.announcer;

import java.io.File;
import java.util.List;

public class Config {

static YamlManager manager = new YamlManager(new File(Main.plugin.getDataFolder()+"/config.yml"));
	
	public synchronized static void set(String path, Object value) {
		manager.writeYaml(path, value);
	}
	public synchronized static Object get(String path) {
		return manager.readYaml(path);
	}
	public synchronized static void setDefaults() {
		for(DefaultConfigValue val : DefaultConfigValue.values()) {
			manager.writeYaml(val.getPath(), val.getValue());
		}
	}
	public synchronized static void setDefault(DefaultConfigValue val) {
		manager.writeYaml(val.getPath(), val.getValue());
	}
	public synchronized static List<String> getStringList(String path) {
		return manager.readStringList(path);
	}
	public synchronized static List<String> getSectionHeaders(String path) {
		return manager.readSectionHeaders(path);
	}
	public synchronized static void addStringToList(String path, String element) {
		manager.addStringToList(path, element);
	}
	public synchronized static void removeStringFromList(String path, int id) {
		manager.removeStringFromList(path, id);
	}
}
