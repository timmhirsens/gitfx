/*
 * Copyright 2013 Timm Hirsens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.br0tbox.gitfx.ui.progress;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.lib.ProgressMonitor;

/**
 * @author fr1zle
 *
 */
public abstract class AbstractMonitorableGitTask<V> extends Task<V> {

	private static final Logger LOGGER = LogManager.getLogger(AbstractMonitorableGitTask.class);

	private BooleanProperty canceledProperty = new SimpleBooleanProperty(false);
	private boolean canceled;
	protected GitCommand<V> gitCommand;

	public boolean isCanceled() {
		return canceled;
	}

	protected AbstractMonitorableGitTask(GitCommand<V> gitCommand) {
		this.gitCommand = gitCommand;
		canceledProperty.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				canceled = newValue;
			}
		});
	}

	@Override
	protected V call() throws Exception {
		try {
			onBeforeCall();
			final V returnValue = gitCommand.call();
			onAfterCall(returnValue);
			return returnValue;
		} catch (final Throwable t) {
			LOGGER.error("Error while perfoming Task " + gitCommand.toString(), t);
			throw t;
		}
	}

	protected void onAfterCall(V returnValue) {
		// design for extension
	}

	protected void onBeforeCall() {
		// design for extension
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
