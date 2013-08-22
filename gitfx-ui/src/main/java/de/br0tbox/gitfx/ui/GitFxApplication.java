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
package de.br0tbox.gitfx.ui;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import com.google.inject.Module;
import com.sun.javafx.runtime.VersionInfo;
import de.br0tbox.gitfx.ui.controllers.AbstractController;
import de.br0tbox.gitfx.ui.modules.MainModule;
import de.br0tbox.gitfx.ui.shutdown.IShutdownHook;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialogs;

import javax.inject.Inject;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Set;

public class GitFxApplication extends GuiceApplication {

	private static final Logger LOGGER = LogManager.getLogger(GitFxApplication.class);

	@Inject
	GuiceFXMLLoader fxmlLoader;
	@Inject
	private Set<IShutdownHook> shutdownHooks;

	private static UncaughtExceptionHandler uncaughtExceptionHandler;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		uncaughtExceptionHandler = new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error("Unhandled Exception: " + e.getMessage() + "", e);
				Dialogs.create().masthead("An unexpected error occurred. Application will exit.").title("Unexpected Error").showExceptionInNewWindow(e);
				System.exit(100);
			}
		};
		launch(args);
	}

	@Override
	public void init(List<Module> modules) throws Exception {
		final MainModule mainModule = new MainModule();
		modules.add(mainModule);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		LOGGER.debug("Starting Application");
		LOGGER.debug("Running JavaFX version {}", VersionInfo.getRuntimeVersion());
		final Result result = fxmlLoader.load(this.getClass().getResource("/ProjectView.fxml"));
		final Parent root = result.getRoot();
		result.<AbstractController>getController().init(primaryStage);
		// XXX: This doesn't work yet, since Platform.runLater() swallows every Exception :/
		// 		@see: http://javafx-jira.kenai.com/browse/RT-15332
		Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
		final Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				for (final IShutdownHook shutdownHook : shutdownHooks) {
					if (!event.isConsumed()) {
						shutdownHook.onShutdown(event);
					}
				}
				if (!event.isConsumed()) {
					Platform.exit();
				}
			}
		});
		LOGGER.debug("Startup finished");
	}

}
