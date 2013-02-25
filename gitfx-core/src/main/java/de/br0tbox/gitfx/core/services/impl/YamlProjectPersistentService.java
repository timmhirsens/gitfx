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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import de.br0tbox.gitfx.core.model.PersistentProject;
import de.br0tbox.gitfx.core.model.ProjectsYamlDatabase;
import de.br0tbox.gitfx.core.services.IProjectPersistentService;

/**
 * @author fr1zle
 *
 */
public class YamlProjectPersistentService implements IProjectPersistentService {

	private String userhome = System.getProperty("user.home");
	private File appDir = new File(userhome, ".gitfx");
	private File projects = new File(appDir, "gitfx-projects.yaml");

	@Override
	public List<PersistentProject> loadAll() {
		final Yaml yaml = new Yaml();
		if (projects.exists()) {
			ProjectsYamlDatabase projectDatabase;
			try {
				projectDatabase = yaml.loadAs(new FileReader(projects), ProjectsYamlDatabase.class);
				return projectDatabase.getProjects();
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void save(PersistentProject project) {
		final Yaml yaml = new Yaml();
		final List<PersistentProject> list = new ArrayList<>();
		final List<PersistentProject> loadedProjects = loadAll();
		if (loadedProjects != null && loadedProjects.size() > 0) {
			list.addAll(loadedProjects);
		}
		list.add(project);
		final ProjectsYamlDatabase database = new ProjectsYamlDatabase();
		database.setProjects(list);
		saveAll(yaml, database);
	}

	private void saveAll(final Yaml yaml, final ProjectsYamlDatabase database) {
		final String dump = yaml.dumpAs(database, Tag.MAP, FlowStyle.AUTO);
		final FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(projects);
			fileWriter.write(dump);
			fileWriter.flush();
			fileWriter.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void delete(PersistentProject project) {
		final List<PersistentProject> loadedProjects = loadAll();
		PersistentProject toDelete = null;
		if (loadedProjects != null) {
			for (final PersistentProject persistentProject : loadedProjects) {
				if (persistentProject.getPath().equals(project.getPath()) && persistentProject.getName().equals(project.getName())) {
					toDelete = persistentProject;
				}
			}
			if (toDelete != null) {
				loadedProjects.remove(toDelete);
				final ProjectsYamlDatabase projectsYamlDatabase = new ProjectsYamlDatabase();
				projectsYamlDatabase.setProjects(loadedProjects);
				saveAll(new Yaml(), projectsYamlDatabase);
			}
		}
	}

}
