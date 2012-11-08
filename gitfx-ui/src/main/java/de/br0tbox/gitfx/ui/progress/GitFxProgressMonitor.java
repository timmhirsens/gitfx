package de.br0tbox.gitfx.ui.progress;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitFxProgressMonitor implements ProgressMonitor {

	private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty titleProperty = new SimpleStringProperty();
	private BooleanProperty canceledProperty = new SimpleBooleanProperty(false);
	private int currentTaskTotalWork;
	private String currentTaskTitle;
	private int tasksRemaining;
	private int currentTaskDone;
	private boolean canceled;

	public SimpleDoubleProperty getProgressProperty() {
		return progressProperty;
	}

	public StringProperty getTitleProperty() {
		return titleProperty;
	}

	public GitFxProgressMonitor() {
		init();
	}

	private void init() {
		canceledProperty.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				canceled = true;
			}
		});
	}

	@Override
	public void start(int totalTasks) {
		this.tasksRemaining = totalTasks;

	}

	@Override
	public void beginTask(String title, final int totalWork) {
		this.currentTaskTitle = title;
		setTitle(currentTaskTitle + " (0 of " + totalWork + ")");
		setProgess(0.0);
		currentTaskTotalWork = totalWork;
		currentTaskDone = 0;
	}

	@Override
	public void update(int completed) {
		currentTaskDone += completed;
		final double progess = (double) currentTaskDone / (double) currentTaskTotalWork;
		setTitle(currentTaskTitle + " (" + currentTaskDone + " of " + currentTaskTotalWork + ")");
		setProgess(progess);
	}

	@Override
	public void endTask() {
		currentTaskDone = 0;
		currentTaskTotalWork = 0;
		setProgess(1.0);
		tasksRemaining--;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	public BooleanProperty getCanceledProperty() {
		return canceledProperty;
	}

	private void setTitle(final String title) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				titleProperty.setValue(title);
			}
		});
	}

	private void setProgess(final double progess) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressProperty.set(progess);
			}
		});
	}

}
