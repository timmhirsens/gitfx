package de.br0tbox.gitfx.ui.sync;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

public interface IRepositorySyncService {

	void startWatchingRepository(ProjectModel projectModel);

	void stopWatchingRepository(ProjectModel projectModel);

}
