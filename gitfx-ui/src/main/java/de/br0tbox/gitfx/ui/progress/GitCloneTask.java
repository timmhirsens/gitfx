package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitCloneTask extends AbstractMonitorableGitTask<Git> {

	protected GitCloneTask(GitCommand<Git> gitCommand) {
		super(gitCommand);
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		((CloneCommand) gitCommand).setProgressMonitor(progressMonitor);
	}

	@Override
	protected void onAfterCall(Git returnValue) {
		if (returnValue != null && returnValue.getRepository() != null) {
			returnValue.getRepository().close();
		}
	}

}
