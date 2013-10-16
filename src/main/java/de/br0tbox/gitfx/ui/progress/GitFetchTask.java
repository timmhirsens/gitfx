package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.FetchResult;

public class GitFetchTask extends AbstractMonitorableGitTask<FetchResult> {

	protected GitFetchTask(FetchCommand fetchCommand) {
		super(fetchCommand);
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		((FetchCommand) gitCommand).setProgressMonitor(progressMonitor);
	}
}
