package de.br0tbox.gitfx.ui.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.core.util.Preconditions;
import de.br0tbox.gitfx.ui.fx.ChangedFileListCell;
import de.br0tbox.gitfx.ui.history.CommitTableCell;
import de.br0tbox.gitfx.ui.history.JavaFxPlotRenderer;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/SingleProjectView.fxml")
public class SingleProjectController extends AbstractController {

	static final Image IMAGE_COMMIT_CLEAN = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/accept.png"));
	static final Image IMAGE_COMMIT_DIRTY = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/asterisk_yellow.png"));
	private static final Logger LOGGER = LogManager.getLogger(SingleProjectController.class);

	private ProjectModel projectModel;
	@FXML
	private TableView<GitFxCommit> tableView;
	@FXML
	private TreeView treeView;
	@FXML
	private TreeItem branchesItem;
	@FXML
	private TreeItem tagsItem;
	@FXML
	private TreeItem remotesItem;
	@FXML
	private Button commitButton;
	private ToggleGroup toggleGroup = new ToggleGroup();
	@FXML
	private ToggleButton recentButton;
	@FXML
	private ToggleButton listButton;
	@FXML
	ListView commitList;
	@FXML
	private TextArea changesView;

	private List<ListenerHandle> listenerHandles = new ArrayList<>();
	private ChangeListener<Number> uncommitedChangesListener;
	@FXML
	ImageView commitButtonImage;

	@Override
	protected void onInit() {
		recentButton.setToggleGroup(toggleGroup);
		listButton.setToggleGroup(toggleGroup);
		changesView.setEditable(false);
		modelToView();
		addCommitClickedListener();
		setCommitButtonText(projectModel.getChanges());
		getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
			// Clean up listeners when project is closed.
			@Override
			public void handle(WindowEvent windowEvent) {
				for (final ListenerHandle handle : listenerHandles) {
					handle.remove();
					projectModel.getChangesProperty().removeListener(uncommitedChangesListener);
				}
			}
		});
		addUncommitedChangesListener();
		addCommitsListener();
		addBranchesListener();
	}

	private void addBranchesListener() {
		final ListChangeListener<String> branchesChangesListener = new ListChangeListener<String>() {

			@Override
			public void onChanged(Change<? extends String> change) {
				addBranchesToView();
			}
		};
		projectModel.getCurrentBranchProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
				getStage().setTitle(projectModel.getProjectName() + " (" + projectModel.getCurrentBranch() + ") - GitFx");
			}
		});
		projectModel.getLocalBranchesProperty().addListener(branchesChangesListener);
		projectModel.getRemoteBranchesProperty().addListener(branchesChangesListener);
		projectModel.getTagsProperty().addListener(branchesChangesListener);
	}

	private void addCommitsListener() {
		projectModel.getCommitsProperty().addListener(new ListChangeListener<GitFxCommit>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends GitFxCommit> change) {
				try {
					addCommitsLogToView();
				} catch (final IOException e) {
					LOGGER.error("Error while refreshing Commits", e);
				}
			}
		});
	}

	public void commitButtonClicked() {
		try {
			final Result result = fxmlLoader.load(getClass().getResource("/CommitDialogView.fxml"));
			final CommitDialogController commitController = result.getController();
			final Stage stage = new Stage();
			stage.setScene(new Scene((Parent) result.getRoot()));
			stage.setResizable(false);
			stage.initModality(Modality.APPLICATION_MODAL);
			commitController.setProjectModel(projectModel);
			commitController.init(stage);
			stage.showAndWait();
		} catch (final IOException e) {
			LOGGER.error("Error while openening commit dialog.", e);
		}
	}

	private void addUncommitedChangesListener() {
		uncommitedChangesListener = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) {
				setCommitButtonText(newValue);
			}
		};
		projectModel.getChangesProperty().addListener(uncommitedChangesListener);
	}

	private void addCommitClickedListener() {
		tableView.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				final GitFxCommit selectedItem = tableView.getSelectionModel().getSelectedItem();
				if (selectedItem == null) {
					return;
				}
				showHistoryForCommit(selectedItem);
			}
		});
	}

	private void modelToView() {
		getStage().setTitle(projectModel.getProjectName() + " (" + projectModel.getCurrentBranch() + ") - GitFx");
		getStage().getIcons().add(new Image(SingleProjectController.class.getResourceAsStream("/icons/package.png")));
		tableView.getItems().clear();
		try {
			addCommitsLogToView();
			addBranchesToView();
		} catch (final IOException e) {
			LOGGER.error("Error while creating log view.", e);
		}
	}

	private void addCommitsLogToView() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		tableView.getItems().clear();
		final ObservableList<GitFxCommit> commits = projectModel.getCommitsProperty();
		final JavaFxPlotRenderer renderer = new JavaFxPlotRenderer();
		final TableColumn<GitFxCommit, GitFxCommit> tableColumn = (TableColumn<GitFxCommit, GitFxCommit>) tableView.getColumns().get(0);
		tableColumn.setCellFactory(createCommitCellFactory(renderer));
		tableColumn.setCellValueFactory(createCellValueFactory());
		tableView.getItems().addAll(commits);
	}

	private Callback<CellDataFeatures<GitFxCommit, GitFxCommit>, ObservableValue<GitFxCommit>> createCellValueFactory() {
		return new Callback<TableColumn.CellDataFeatures<GitFxCommit, GitFxCommit>, ObservableValue<GitFxCommit>>() {

			@Override
			public ObservableValue<GitFxCommit> call(final CellDataFeatures<GitFxCommit, GitFxCommit> dataFeatures) {
				return new ObservableValueBase<GitFxCommit>() {

					@Override
					public GitFxCommit getValue() {
						return dataFeatures.getValue();
					}
				};
			}
		};
	}

	private Callback<TableColumn<GitFxCommit, GitFxCommit>, TableCell<GitFxCommit, GitFxCommit>> createCommitCellFactory(final JavaFxPlotRenderer renderer) {
		return new Callback<TableColumn<GitFxCommit, GitFxCommit>, TableCell<GitFxCommit, GitFxCommit>>() {

			@Override
			public TableCell<GitFxCommit, GitFxCommit> call(TableColumn<GitFxCommit, GitFxCommit> arg0) {
				final CommitTableCell<GitFxCommit> commitTableCell = new CommitTableCell<>(renderer);
				return commitTableCell;
			}
		};
	}

	private void addBranchesToView() {
		treeView.showRootProperty().set(false);
		branchesItem.getChildren().clear();
		remotesItem.getChildren().clear();
		tagsItem.getChildren().clear();
		final List<String> localList = projectModel.getLocalBranchesProperty();
		final List<String> remoteList = projectModel.getRemoteBranchesProperty();
		final List<String> tagsList = projectModel.getTagsProperty();
		for (String local : localList) {
			if (local.startsWith(Constants.R_HEADS)) {
				local = local.substring(Constants.R_HEADS.length(), local.length());
			}
			branchesItem.getChildren().add(new TreeItem(local));
		}
		for (String remote : remoteList) {
			if (remote.startsWith(Constants.R_REMOTES)) {
				remote = remote.substring(Constants.R_REMOTES.length(), remote.length());
			}
			remotesItem.getChildren().add(new TreeItem(remote));
		}
		for (String tag : tagsList) {
			if (tag.startsWith(Constants.R_TAGS)) {
				tag = tag.substring(Constants.R_TAGS.length(), tag.length());
			}
			tagsItem.getChildren().add(new TreeItem(tag));
		}
		branchesItem.expandedProperty().set(true);
		remotesItem.expandedProperty().set(true);
		tagsItem.expandedProperty().set(true);
	}

	public void setProject(ProjectModel projectModel) {
		this.projectModel = projectModel;
	}

	private void showHistoryForCommit(final GitFxCommit selectedCommit) {
		Preconditions.checkNotNull(selectedCommit, "selectedCommit");
		final Git git = projectModel.getFxProject().getGit();
		final Repository repository = git.getRepository();
		ObjectId resolve;
		try {
			resolve = repository.resolve(selectedCommit.getHash());
			final RevWalk revWalk = new RevWalk(repository);
			final RevCommit commit = revWalk.parseCommit(resolve);
			RevCommit parent = null;
			if (commit.getParents().length > 0 && commit.getParent(0) != null) {
				parent = revWalk.parseCommit(commit.getParent(0).getId());
			}

			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final DiffFormatter df = new DiffFormatter(out);
			df.setRepository(repository);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(true);
			List<DiffEntry> diffs = null;
			if (parent != null) {
				diffs = df.scan(parent.getTree(), commit.getTree());
			} else {
				diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, revWalk.getObjectReader(), commit.getTree()));
			}
			final ObservableList items = commitList.getItems();
			items.clear();
			for (final DiffEntry diff : diffs) {
				df.format(diff);
				final String changes = out.toString("UTF-8");
				changesView.setText(changes);
				if (ChangeType.DELETE.equals(diff.getChangeType())) {
					items.add(diff.getChangeType().toString().subSequence(0, 1) + " " + diff.getOldPath());
				} else {
					items.add(diff.getChangeType().toString().subSequence(0, 1) + " " + diff.getNewPath());
				}
			}
			revWalk.release();
			df.release();
		} catch (final IOException e) {
			LOGGER.error("Error while showing changes for commit " + selectedCommit.getHash(), e);
		}
	}

	private void setCommitButtonText(Number changedFileCount) {
		if (changedFileCount.intValue() > 0) {
			commitButton.setText("Commmit (" + changedFileCount + ")");
			commitButtonImage.setImage(IMAGE_COMMIT_DIRTY);
			commitButton.setDisable(false);
		} else {
			commitButton.setText("Commmit");
			commitButtonImage.setImage(IMAGE_COMMIT_CLEAN);
			commitButton.setDisable(true);
		}
	}

}
