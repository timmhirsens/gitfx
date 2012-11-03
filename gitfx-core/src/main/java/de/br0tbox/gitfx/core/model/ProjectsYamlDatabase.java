package de.br0tbox.gitfx.core.model;

import java.util.List;

public class ProjectsYamlDatabase {
	
	public ProjectsYamlDatabase() {
	}

	List<PersistentProject> projects;

	public List<PersistentProject> getProjects() {
		return projects;
	}

	public void setProjects(List<PersistentProject> projects) {
		this.projects = projects;
	}

}
