package com.gmail.tomahawkmissile2.announcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class YamlManager {

	private File f;
	private YamlConfiguration y;
	
	public YamlManager(File f) {
		y=YamlConfiguration.loadConfiguration(f);
		this.f=f;
	}
	public void writeYaml(String path,Object value) {
		y.set(path, value);
		try {
			y.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Object readYaml(String path) {
		return y.get(path);
	}
	public Object[] readKeys() {
		return y.getKeys(true).toArray();
	}
	public void createSection(String path) {
		y.createSection(path);
		try {
			y.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void addStringToList(String path, String element) {
		if(y.getStringList(path)==null || y.getStringList(path).isEmpty()) {
			y.set(path, new ArrayList<String>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 7724628470677099281L;
			{
				add(element);
			}});
		} else {
			List<String> newList = y.getStringList(path);
			newList.add(element);
			y.set(path, newList);
		}
		try {
			y.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void removeStringFromList(String path, int id) {
		if(y.getStringList(path)!=null && !y.getStringList(path).isEmpty()) {
			if(y.getStringList(path).size()>1) {
				List<String> newList = y.getStringList(path);
				newList.remove(id);
				y.set(path, newList);
			} else {
				y.set(path, null);
			}
			try {
				y.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public List<String> readStringList(String path) {
		return y.getStringList(path);
	}
	public List<String> readSectionHeaders(String path) {
		List<String> ret = new ArrayList<String>();
		ret.addAll(y.getConfigurationSection(path).getKeys(false));
		return ret;
	}
}
