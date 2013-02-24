package de.br0tbox.gitfx.ui.controllers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.stage.Stage;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.ui.message.Message;
import de.br0tbox.gitfx.ui.message.MessageBundle;
import de.br0tbox.gitfx.ui.progress.AbstractMonitorableGitTask;

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
			}
		};
	}

	protected DialogResponse showErrorMessage(String code, Throwable throwable, String... parameters) {
		final Message message = messageBundle.getMessage(code, parameters);
		return Dialogs.showErrorDialog(getStage(), message.getText(), message.getMasthead(), message.getTitel(), throwable);
	}

	protected DialogResponse showMessage(String code, String... parameters) {
		final Message message = messageBundle.getMessage(code, parameters);
		switch (message.getType()) {
		case ERROR:
			return Dialogs.showErrorDialog(getStage(), message.getText(), message.getMasthead(), message.getTitel());
		case CONFIRMATION:
			return Dialogs.showConfirmDialog(getStage(), message.getText(), message.getMasthead(), message.getTitel(), DialogOptions.YES_NO_CANCEL);
		case INFO:
			Dialogs.showInformationDialog(getStage(), message.getText(), message.getMasthead(), message.getTitel());
			return null;
		case WARNING:
			return Dialogs.showWarningDialog(getStage(), message.getText(), message.getMasthead(), message.getTitel());
		default:
			throw new UnsupportedOperationException();
		}
	}
}
