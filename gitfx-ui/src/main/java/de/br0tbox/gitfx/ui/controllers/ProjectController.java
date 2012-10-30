package de.br0tbox.gitfx.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.cathive.fx.guice.FXMLController;

/**
 * The controller for the projects view.
 * @author fr1zle
 *
 */
@FXMLController(controllerId = "/ProjectView.fxml")
public class ProjectController extends AbstractController {

	private static final Logger LOGGER = LogManager.getLogger(ProjectController.class);

	@FXML
	private Button openButton;

	private File lastOpened = null;

	public ProjectController() {
		System.out.println("Constructed");
	}

	@Override
	public void onInit() {
		System.out.println("init");
		openButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				final DirectoryChooser chooser = new DirectoryChooser();
				if (lastOpened != null) {
					chooser.setInitialDirectory(lastOpened);
				}
				File file = chooser.showDialog(getStage());
				if (file != null) {
					lastOpened = file;
					System.out.println(file.getAbsolutePath());
					if (!file.getAbsolutePath().endsWith(".git")) {
						file = new File(file, ".git");
					}
					final FileRepositoryBuilder builder = new FileRepositoryBuilder();
					try {
						final Repository repository = builder.setGitDir(file).readEnvironment().findGitDir().build();
						final Git git = new Git(repository);
						final Status status = git.status().call();
						final Set<String> untracked = status.getUntracked();
						System.out.println(untracked);
						final ObjectId resolve = repository.resolve("HEAD");
						System.out.println(resolve.getName());
					} catch (final IOException | NoWorkTreeException | GitAPIException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		});
	}

}
