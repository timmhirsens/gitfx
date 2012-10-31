package de.br0tbox.gitfx.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import com.google.inject.Module;

import de.br0tbox.gitfx.ui.controllers.AbstractController;
import de.br0tbox.gitfx.ui.modules.MainModule;
import de.br0tbox.gitfx.ui.shutdown.IShutdownHook;

public class GitFxApplication extends GuiceApplication {

	private static final Logger LOGGER = LogManager.getLogger(GitFxApplication.class);

	@Inject
	GuiceFXMLLoader fxmlLoader;
	@Inject
	private Set<IShutdownHook> shutdownHooks;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public Collection<Module> initModules() {
		final List<Module> modules = new ArrayList<>();
		final MainModule mainModule = new MainModule();
		modules.add(mainModule);
		return modules;

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		LOGGER.debug("Starting Application");
		final Result result = fxmlLoader.load(this.getClass().getResource("/ProjectView.fxml"));
		final Parent root = result.getRoot();
		result.<AbstractController> getController().init(primaryStage);
		final Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
		LOGGER.debug("Startup finished");
	}

	@Override
	public void stop() throws Exception {
		for (final IShutdownHook shutdownHook : shutdownHooks) {
			shutdownHook.onShutdown();
		}
		super.stop();
	}

}
