package de.br0tbox.gitfx.ui.progress;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import org.eclipse.jgit.lib.ProgressMonitor;

/**
 * @author fr1zle
 *
 */
public abstract class AbstractMonitorableGitTask<V> extends Task<V> {

	private BooleanProperty canceledProperty = new SimpleBooleanProperty(false);
	private boolean canceled;

	public boolean isCanceled() {
		return canceled;
	}

	protected AbstractMonitorableGitTask() {
		canceledProperty.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				canceled = newValue;
			}
		});
	}

	protected ProgressMonitor createProgressMonitor() {
		final ProgressMonitor progressMonitor = new GitFxProgressMonitor(this);
		return progressMonitor;
	}

	public BooleanProperty getCanceledProperty() {
		return canceledProperty;
	}

	public final void init() {
		final ProgressMonitor progressMonitor = createProgressMonitor();
		addMonitorToGitCommand(progressMonitor);
	}

	protected abstract void addMonitorToGitCommand(ProgressMonitor progressMonitor);

	// Making the update-methods visible in this package.

	@Override
	protected void updateTitle(String title) {
		super.updateTitle(title);
	}

	@Override
	protected void updateMessage(String message) {
		super.updateMessage(message);
	}

	@Override
	protected void updateProgress(double workDone, double max) {
		super.updateProgress(workDone, max);
	}

}
