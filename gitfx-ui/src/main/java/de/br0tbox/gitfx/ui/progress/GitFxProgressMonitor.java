package de.br0tbox.gitfx.ui.progress;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitFxProgressMonitor implements ProgressMonitor {

	private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty titleProperty = new SimpleStringProperty();
	private int currentTaskTotalWork;

	private int currentTaskDone;

	public SimpleDoubleProperty getProgressProperty() {
		return progressProperty;
	}

	public StringProperty getTitleProperty() {
		return titleProperty;
	}

	private String currentTaskTitle;
	private int tasksRemaining;

	public GitFxProgressMonitor() {
		init();
	}

	private void init() {
	}

	@Override
	public void start(int totalTasks) {
		this.tasksRemaining = totalTasks;

	}

	@Override
	public void beginTask(String title, final int totalWork) {
		this.currentTaskTitle = title;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				titleProperty.setValue(currentTaskTitle + " (0 of " + totalWork + ")");
				progressProperty.set(0.0);
			}
		});
		currentTaskTotalWork = totalWork;
		currentTaskDone = 0;
	}

	@Override
	public void update(int completed) {
		currentTaskDone += completed;
		final double progess = (double) currentTaskDone / (double) currentTaskTotalWork;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				titleProperty.setValue(currentTaskTitle + " (" + currentTaskDone + " of " + currentTaskTotalWork + ")");
				progressProperty.set(progess);
			}
		});
	}

	@Override
	public void endTask() {
		currentTaskDone = 0;
		currentTaskTotalWork = 0;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				progressProperty.set(1.0);
			}
		});
		tasksRemaining--;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

}
