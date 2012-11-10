package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitFxProgressMonitor implements ProgressMonitor {

	private AbstractMonitorableGitTask<?> gitTask;

	public GitFxProgressMonitor(AbstractMonitorableGitTask<?> task) {
		this.gitTask = task;
	}

	private int currentTaskTotalWork;
	private String currentTaskTitle;
	private int currentTaskDone;

	@Override
	public void start(int totalTasks) {
	}

	@Override
	public void beginTask(String title, final int totalWork) {
		this.currentTaskTitle = title;
		currentTaskTotalWork = totalWork;
		currentTaskDone = 0;
		if (totalWork == UNKNOWN) {
			gitTask.updateProgress(0, 0);
			gitTask.updateMessage(currentTaskTitle + "...");
		}
		gitTask.updateMessage(currentTaskTitle + " (0 of " + totalWork + ")");
	}

	@Override
	public void update(int completed) {
		if (currentTaskTotalWork == UNKNOWN) {
			gitTask.updateProgress(0, 0);
			gitTask.updateMessage(currentTaskTitle + "...");
		} else {
			if (completed / 10 == 0) {
				currentTaskDone += completed;
				gitTask.updateMessage(currentTaskTitle + " (" + currentTaskDone + " of " + currentTaskTotalWork + ")");
				gitTask.updateProgress(currentTaskDone, currentTaskTotalWork);
			}
		}
	}

	@Override
	public void endTask() {
		currentTaskDone = 0;
		currentTaskTotalWork = 0;
	}

	@Override
	public boolean isCancelled() {
		final boolean canceled = gitTask.isCanceled();
		return canceled;
	}

}
