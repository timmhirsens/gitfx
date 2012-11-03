package de.br0tbox.gitfx.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.ActionEvent;
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
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.core.model.GitFxProject;
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

		projectList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {
				if (MouseButton.PRIMARY.equals(mouseEvent.getButton())) {
					if (mouseEvent.getClickCount() == 2) {
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
				}
			}
		});
		addButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				final DirectoryChooser chooser = new DirectoryChooser();
				if (lastOpened != null) {
					chooser.setInitialDirectory(lastOpened);
				}
				File file = chooser.showDialog(getStage());
				if (file != null) {
					lastOpened = file;
					propertyService.saveProperty(LASTOPEN_PROPERTY, file.getAbsolutePath());
					if (!file.getAbsolutePath().endsWith(".git")) {
						file = new File(file, ".git");
					}
					for (final ProjectModel model : projectList.getItems()) {
						if (model.getPath().equals(file.getAbsolutePath())) {
							return;
						}
					}
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
						projectModel.setProjectName(new File(file.getParent()).getName());
						projectModel.setCurrentBranch(gitFxProject.getGit().getRepository().getBranch());
						projectModel.setChanges(gitFxProject.getUncommitedChangesNumber());
						projectModel.setPath(file.getAbsolutePath());
						startTimer(repository);
						projectList.getItems().add(projectModel);
					} catch (final IOException e) {
						LOGGER.error(e);
					}

				}
			}

		});
	}

	private void startTimer(final Repository repository) {
		final TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// try {
				// final DirCache cache = DirCache.read(repository);
				// final DirCacheBuilder builder = cache.builder();
				// for (int i = 0; i < cache.getEntryCount(); i++) {
				// final DirCacheEntry entry = cache.getEntry(i);
				// System.out.println(entry.getPathString() + " " +
				// entry.getStage());
				// }
				// cache.unlock();
				// } catch (final IOException e) {
				// e.printStackTrace();
				// }
				try {
					final IndexDiff diff = new IndexDiff(repository, repository.getRef("HEAD").getObjectId(), new FileTreeIterator(repository));
					diff.diff();
					final Set<String> untracked = diff.getUntracked();
					if (untracked.size() > 0) {
						System.out.println(untracked);
					}
					repository.scanForRepoChanges();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, 4000);
	}
}
