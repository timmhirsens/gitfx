package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitCloneTask extends AbstractMonitorableGitTask<Git> {

	private CloneCommand cloneCommand;

	protected GitCloneTask(CloneCommand cloneCommand) {
		this.cloneCommand = cloneCommand;
	}

	@Override
	protected Git call() throws Exception {
		try {
			final Git git = cloneCommand.call();
			return git;
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		cloneCommand.setProgressMonitor(progressMonitor);
	}

}
