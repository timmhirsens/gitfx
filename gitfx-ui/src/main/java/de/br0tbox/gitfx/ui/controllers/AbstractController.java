package de.br0tbox.gitfx.ui.controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.stage.Stage;

import javax.inject.Inject;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.ui.progress.GitFxTask;

public abstract class AbstractController {

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

	protected void runGitTaskWithProgressDialog(final GitFxTask task) {
		Result load;
		try {
			load = fxmlLoader.load(AbstractController.class.getResource("/LoadingDialogView.fxml"));
			final LoadingDialogController controller = load.getController();
			final Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						final Git call = (Git) task.getGitCommand().call();
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.hide();
							}
						});
						call.getRepository().close();
					} catch (final GitAPIException e) {
						e.printStackTrace();
					}
				}
			});
			thread.setName("Command Runner");
			controller.setProgressMonitor(task.getMonitor());
			thread.start();
			controller.show();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

}
