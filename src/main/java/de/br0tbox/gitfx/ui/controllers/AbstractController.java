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
package de.br0tbox.gitfx.ui.controllers;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import de.br0tbox.gitfx.ui.message.Message;
import de.br0tbox.gitfx.ui.message.MessageBundle;
import de.br0tbox.gitfx.ui.progress.AbstractMonitorableGitTask;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

import javax.inject.Inject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractController {

	private static ThreadPoolExecutor executorService;
	private MessageBundle messageBundle = new MessageBundle();
	private static final Logger LOGGER = LogManager.getLogger(AbstractController.class);

	public static ThreadPoolExecutor getExecutorService() {
		if (executorService == null) {
			executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 20, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
			executorService.setThreadFactory(new ThreadFactory() {

				private static final String PREFIX = "Executor-";
				private AtomicInteger counter = new AtomicInteger();

				@Override
				public Thread newThread(Runnable r) {
					final Thread t = new Thread(r);
					t.setName(PREFIX + counter.incrementAndGet());
					t.setDaemon(true);
					return t;
				}
			});
		}
		return executorService;
	}

	private Stage stage;
	@Inject
	protected GuiceFXMLLoader fxmlLoader;

	public final void init(Stage parent) {
		this.stage = parent;
		onInit();
	}

	protected abstract void onInit();

	public Stage getStage() {
		return stage;
	}

	protected void runGitTaskWithProgressDialog(final AbstractMonitorableGitTask<?> task) {
		Result load;
		try {
			load = fxmlLoader.load(AbstractController.class.getResource("/LoadingDialogView.fxml"));
			final LoadingDialogController controller = load.getController();
			final EventHandler<WorkerStateEvent> closeDialogEventHandler = createEventHandler(controller);
			task.setOnSucceeded(closeDialogEventHandler);
			task.setOnCancelled(closeDialogEventHandler);
			task.setOnFailed(closeDialogEventHandler);
			controller.setTask(task);
			getExecutorService().submit(task);
			controller.show();
		} catch (final Exception e) {
			LOGGER.error(e);
			showErrorMessage("00001", e);
		}

	}

	private EventHandler<WorkerStateEvent> createEventHandler(final LoadingDialogController controller) {
		return new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				System.out.println(event.getSource().getState());
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						controller.hide();
					}
				});
				Throwable exception = event.getSource().getException();
				if (exception != null) {
					Dialogs.create().nativeTitleBar().owner(stage).masthead("An error occured").title("Error").showException(exception);
				}
			}
		};
	}

	protected Action showErrorMessage(String code, Throwable throwable, String... parameters) {
		final Message message = messageBundle.getMessage(code, parameters);
		return Dialogs.create().owner(getStage()).nativeTitleBar().message(message.getText()).masthead(message.getMasthead()).title(message.getTitel()).showError();
	}

	protected Action showMessage(String code, String... parameters) {
		final Message message = messageBundle.getMessage(code, parameters);
		switch (message.getType()) {
			case ERROR:
				return Dialogs.create().owner(getStage()).nativeTitleBar().message(message.getText()).masthead(message.getMasthead()).title(message.getTitel()).showError();
			case CONFIRMATION:
				return Dialogs.create().owner(getStage()).nativeTitleBar().message(message.getText()).masthead(message.getMasthead()).title(message.getTitel()).showConfirm();
			case INFO:
				Dialogs.create().owner(getStage()).nativeTitleBar().message(message.getText()).masthead(message.getMasthead()).title(message.getTitel()).showInformation();
				return null;
			case WARNING:
				return Dialogs.create().owner(getStage()).nativeTitleBar().message(message.getText()).masthead(message.getMasthead()).title(message.getTitel()).showWarning();
			default:
				throw new UnsupportedOperationException();
		}
	}
}
