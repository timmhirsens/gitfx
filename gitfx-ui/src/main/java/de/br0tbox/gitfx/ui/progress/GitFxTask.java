package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.GitCommand;

public class GitFxTask {
	
	private GitCommand<?> gitCommand;
	private GitFxProgressMonitor monitor;

	public GitFxTask(GitCommand<?> gitCommand, GitFxProgressMonitor monitor) {
		this.setGitCommand(gitCommand);
		this.setMonitor(monitor);
	}

	public GitCommand<?> getGitCommand() {
		return gitCommand;
	}

	public void setGitCommand(GitCommand<?> gitCommand) {
		this.gitCommand = gitCommand;
	}

	/**
	 * @return the monitor
	 */
	public GitFxProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor the monitor to set
	 */
	public void setMonitor(GitFxProgressMonitor monitor) {
		this.monitor = monitor;
	}

}
