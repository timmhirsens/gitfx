package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitPullTask extends AbstractMonitorableGitTask<PullResult> {

	protected GitPullTask(GitCommand<PullResult> gitCommand) {
		super(gitCommand);
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		((PullCommand) gitCommand).setProgressMonitor(progressMonitor);
	}
}
