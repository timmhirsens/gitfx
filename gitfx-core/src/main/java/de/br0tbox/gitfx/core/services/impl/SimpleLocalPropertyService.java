/*
 * Copyright 2013 Timm Hirsens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package de.br0tbox.gitfx.core.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.br0tbox.gitfx.core.services.IPropertyService;

/**
 * @author fr1zle
 *
 */
public class SimpleLocalPropertyService implements IPropertyService {
	private String userhome = System.getProperty("user.home");
	private File appDir = new File(userhome, ".gitfx");
	private File propertiesFile = new File(appDir, "gitfx-prop.properties");
	private Properties properties = new Properties();

	public SimpleLocalPropertyService() {
		appDir.mkdirs();
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
			} catch (final IOException e) {
			}
		}
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveProperty(String name, Object value) {
		properties.put(name, value);
		try {
			properties.store(new FileOutputStream(propertiesFile), "");
		} catch (final IOException e) {
		}
	}

	@Override
	public String getStringProperty(String name) {
		final Object value = properties.get(name);
		if (value != null) {
			return (String) value;
		} else {
			return null;
		}
	}
}
