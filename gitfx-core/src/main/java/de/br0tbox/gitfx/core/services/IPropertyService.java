package de.br0tbox.gitfx.core.services;

public interface IPropertyService {
	
	void saveProperty(String name, Object value);
	
	String getStringProperty(String name);
	
}
