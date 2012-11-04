package de.br0tbox.gitfx.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.core.model.GitFxProject;
import de.br0tbox.gitfx.core.model.PersistentProject;
import de.br0tbox.gitfx.core.services.IProjectPersistentService;
import de.br0tbox.gitfx.core.services.IPropertyService;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

/**
 * The controller for the projects view.
 * @author fr1zle
 *
 */
@FXMLController(controllerId = "/ProjectView.fxml")
public class ProjectsController extends AbstractController {

	private static final String LASTOPEN_PROPERTY = "projectcontroller.lastopened";

	private static final Logger LOGGER = LogManager.getLogger(ProjectsController.class);

	@Inject
	private IPropertyService propertyService;
	@Inject
	private IProjectPersistentService projectPersistentService;

	private File lastOpened = null;

	@FXML
	private ListView<ProjectModel> projectList;

	@FXML
	private Button cloneButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button addButton;

	@Override
	public void onInit() {
		final String lastOpenProperty = propertyService.getStringProperty(LASTOPEN_PROPERTY);
		if (lastOpenProperty != null) {
			lastOpened = new File(lastOpenProperty);
		}
		loadProjects();
		projectList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {
				if (MouseButton.PRIMARY.equals(mouseEvent.getButton())) {
					if (mouseEvent.getClickCount() == 2) {
						openSelectedProject();
					}
				}
			}
		});
		cloneButton.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event arg0) {
				// TODO: Clone Project
				// final File gitDir = new
				// File(System.getProperty("user.home") +
				// File.separatorChar + "gittest");
				// gitDir.delete();
				// gitDir.mkdirs();
				// final CloneCommand cloneRepository =
				// Git.cloneRepository();
				// cloneRepository.setDirectory(gitDir).setURI("https://github.com/VanillaDev/Vanilla.git");
				// final GitFxProgressMonitor fxProgressMonitor = new
				// GitFxProgressMonitor();
				// cloneRepository.setProgressMonitor(fxProgressMonitor);
				// final GitFxTask fxTask = new
				// GitFxTask(cloneRepository, fxProgressMonitor);
				// runGitTaskWithProgressDialog(fxTask);
			}
		});
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				final File choosedProject = openExistingProjectFileChooser();
				if (choosedProject != null) {
					addProject(choosedProject, null, false);
				}
			}
		});

		deleteButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				final ProjectModel selectedItem = projectList.getSelectionModel().getSelectedItem();
				projectList.getItems().remove(selectedItem);
				projectPersistentService.delete(new PersistentProject(selectedItem.getPath(), selectedItem.getProjectName()));
			}
		});
	}

	private void loadProjects() {
		final List<PersistentProject> loadedProjects = projectPersistentService.loadAll();
		if(loadedProjects != null) {
			for (final PersistentProject project : loadedProjects) {
				final String path = project.getPath();
				final String name = project.getName();
				addProject(new File(path), name, true);
			}
		}
	}

	private void startTimer(final ProjectModel projectModel) {
		final TimerTask task = new TimerTask() {
			Repository repository = projectModel.getFxProject().getGit().getRepository();

			@Override
			public void run() {
				try {
					// final IndexDiff diff = new IndexDiff(repository,
					// repository.getRef("HEAD").getObjectId(), new
					// FileTreeIterator(repository));
					// diff.diff();
					// final Set<String> untracked = diff.getUntracked();
					// if (untracked.size() > 0) {
					// System.out.println(untracked);
					// }
					final Integer uncommitedChangesNumber = projectModel.getFxProject().getUncommitedChangesNumber();
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							projectModel.getChangesProperty().set(uncommitedChangesNumber);
						}
					});
					repository.scanForRepoChanges();
				} catch (final IOException e) {
					LOGGER.error(e);
				}
			}
		};
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, 4000);
	}

	private void openSelectedProject() {
		try {
			final ProjectModel projectModel = projectList.getSelectionModel().getSelectedItem();
			LOGGER.debug("Opening Project : {}", projectModel.getProjectName());
			final Result projectView = fxmlLoader.load(ProjectsController.class.getResource("/SingleProjectView.fxml"));
			final Parent projectViewRoot = projectView.getRoot();
			final SingleProjectController controller = projectView.getController();
			controller.setProject(projectModel);
			final Stage stage = new Stage();
			final Scene scene = new Scene(projectViewRoot);
			stage.setScene(scene);
			controller.init(stage);
			stage.show();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void addProject(File file, String name, boolean fromFile) {
		final FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository;
		try {
			repository = builder.setGitDir(file).readEnvironment().findGitDir().build();
			final Git git = new Git(repository);
			final GitFxProject gitFxProject = new GitFxProject(git);
			final ListenerHandle addIndexChangedListener = repository.getListenerList().addIndexChangedListener(new IndexChangedListener() {

				@Override
				public void onIndexChanged(IndexChangedEvent event) {
					System.out.println("Bla");
					System.out.println(event);
				}
			});
			final ProjectModel projectModel = new ProjectModel(gitFxProject);
			if (name == null) {
				projectModel.setProjectName(new File(file.getParent()).getName());
			} else {
				projectModel.setProjectName(name);
			}
			projectModel.setCurrentBranch(gitFxProject.getGit().getRepository().getBranch());
			projectModel.setChanges(gitFxProject.getUncommitedChangesNumber());
			projectModel.setPath(file.getAbsolutePath());
			startTimer(projectModel);
			projectList.getItems().add(projectModel);
			if (!fromFile) {
				projectPersistentService.save(new PersistentProject(projectModel.getPath(), projectModel.getProjectName()));
			}
		} catch (final IOException e) {
			LOGGER.error(e);
		}

	}

	private File openExistingProjectFileChooser() {
		File file = openDirectoryChooserAtLastOpened();
		if (file != null) {
			if (!file.getAbsolutePath().endsWith(".git")) {
				file = new File(file, ".git");
			}
			for (final ProjectModel model : projectList.getItems()) {
				if (model.getPath().equals(file.getAbsolutePath())) {
					return null;
				}
			}
			return file;
		}
		return null;
	}

	private File openDirectoryChooserAtLastOpened() {
		final DirectoryChooser chooser = new DirectoryChooser();
		if (lastOpened != null) {
			chooser.setInitialDirectory(lastOpened);
		}
		final File file = chooser.showDialog(getStage());
		if (file != null) {
			lastOpened = file;
			propertyService.saveProperty(LASTOPEN_PROPERTY, file.getAbsolutePath());
		}
		return file;
	}
}
