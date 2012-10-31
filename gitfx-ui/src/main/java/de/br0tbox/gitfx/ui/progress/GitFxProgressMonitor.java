package de.br0tbox.gitfx.ui.progress;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitFxProgressMonitor implements ProgressMonitor {

	private ProgressIndicator progressIndicator;
	private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty titleProperty;
	private int currentTaskTotalWork;

	public void setTitleProperty(StringProperty titleProperty) {
		this.titleProperty = titleProperty;
	}

	private int currentTaskDone;
	private String currentTaskTitle;

	public GitFxProgressMonitor() {
		init();
	}

	private void init() {
		progressProperty.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// FIXME: wieso geht hier nicht Double als Typ?
				progressIndicator.setProgress((Double) newValue);
			}
		});
	}

	@Override
	public void start(int totalTasks) {

	}

	@Override
	public void beginTask(String title, int totalWork) {
		this.currentTaskTitle = title;
		titleProperty.setValue(currentTaskTitle + " (0 of " + totalWork+")");
		progressProperty.set(0.0);
		currentTaskTotalWork = totalWork;
		currentTaskDone = 0;
	}

	@Override
	public void update(int completed) {
		currentTaskDone += completed;
		final double progess = (double) currentTaskDone / (double) currentTaskTotalWork;
		titleProperty.setValue(currentTaskTitle + " (" + currentTaskDone + " of " + currentTaskTotalWork+")");
		progressProperty.set(progess);
	}

	@Override
	public void endTask() {
		currentTaskDone = 0;
		currentTaskTotalWork = 0;
		progressProperty.set(1.0);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	public void setProgressIndicator(ProgressIndicator progressIndicator) {
		this.progressIndicator = progressIndicator;
	}

}
