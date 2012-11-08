package de.br0tbox.gitfx.ui.uimodel;

import java.io.IOException;
import java.util.TimerTask;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.Repository;

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
					projectModel.getChangesProperty().set(uncommitedChangesNumber);
				}
			});
			repository.scanForRepoChanges();
		} catch (final IOException e) {
			LOGGER.error(e);
		}
	}

}
