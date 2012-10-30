package de.br0tbox.gitfx.ui.progress;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;

import org.eclipse.jgit.lib.ProgressMonitor;

public class GitFxProgressMonitor implements ProgressMonitor {

	private ProgressIndicator progressIndicator;
	private SimpleDoubleProperty progressProperty;
	private StringProperty titleProperty;
	private int currentTaskTotalWork;
	private int currentTaskDone;

	public GitFxProgressMonitor(ProgressIndicator progressIndicator, StringProperty title) {
		this.progressIndicator = progressIndicator;
		this.titleProperty = title;
		init();
	}

	private void init() {
		progressProperty.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				//FIXME: wieso geht hier nicht Double als Typ?
				progressIndicator.setProgress((Double) newValue);
			}
		});
	}

	@Override
	public void start(int totalTasks) {

	}

	@Override
	public void beginTask(String title, int totalWork) {
		titleProperty.setValue(title);
		progressProperty.set(0.0);
		currentTaskTotalWork = totalWork;
	}

	@Override
	public void update(int completed) {
		currentTaskDone += completed;
		final double progess = (double) currentTaskDone / (double) currentTaskTotalWork * 100.0;
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

}
