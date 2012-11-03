package de.br0tbox.gitfx.core.services;

import java.util.List;

import de.br0tbox.gitfx.core.model.PersistentProject;

public interface IProjectPersistentService {
	
	List<PersistentProject> loadAll();
	
	void save(PersistentProject project);

	void delete(PersistentProject project);
}
