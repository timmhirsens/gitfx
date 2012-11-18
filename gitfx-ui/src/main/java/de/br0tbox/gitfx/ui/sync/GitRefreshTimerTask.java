package de.br0tbox.gitfx.ui.sync;

import java.io.IOException;
import java.util.TimerTask;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.lib.Repository;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

public class GitRefreshTimerTask extends TimerTask {

	private static final Logger LOGGER = LogManager.getLogger(GitRefreshTimerTask.class);

	private ProjectModel projectModel;
	private Repository repository;

	public GitRefreshTimerTask(ProjectModel projectModel) {
		this.projectModel = projectModel;
		repository = projectModel.getFxProject().getGit().getRepository();
	}

	@Override
	public void run() {
		try {
			final Integer uncommitedChangesNumber = projectModel.getFxProject().getUncommitedChangesNumber();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					final int changesInRepo = projectModel.getChangesProperty().get();
					if (changesInRepo != uncommitedChangesNumber) {
						projectModel.getFxProject().getGit().getRepository().getListenerList().dispatch(new IndexChangedEvent());
					}
					projectModel.getChangesProperty().set(uncommitedChangesNumber);
				}
			});
			repository.scanForRepoChanges();
		} catch (final IOException e) {
			LOGGER.error(e);
		}
	}

}
