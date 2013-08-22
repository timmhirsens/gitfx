package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.PushResult;

public class GitPushTask extends AbstractMonitorableGitTask<Iterable<PushResult>> {

	protected GitPushTask(GitCommand<Iterable<PushResult>> gitCommand) {
		super(gitCommand);
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		((PushCommand) gitCommand).setProgressMonitor(progressMonitor);
	}
}
