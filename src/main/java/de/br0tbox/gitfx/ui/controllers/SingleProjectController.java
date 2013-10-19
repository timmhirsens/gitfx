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

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import de.br0tbox.gitfx.ui.fx.ChangedFileListCell;
import de.br0tbox.gitfx.ui.history.CommitTableCell;
import de.br0tbox.gitfx.ui.history.JavaFxPlotRenderer;
import de.br0tbox.gitfx.ui.progress.GitTaskFactory;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;
import de.br0tbox.gitfx.ui.util.Preconditions;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@FXMLController(controllerId = "/SingleProjectView.fxml")
public class SingleProjectController extends AbstractController {

	static final Image IMAGE_COMMIT_CLEAN = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/accept.png"));
	static final Image IMAGE_COMMIT_DIRTY = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/asterisk_yellow.png"));
	private static final Logger LOGGER = LogManager.getLogger(SingleProjectController.class);

	@FXML
	private SplitPane splitPane;
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
	@FXML
	private MenuBar menuBar;
	@FXML
	ImageView commitButtonImage;

	private List<ListenerHandle> listenerHandles = new ArrayList<>();
	private ChangeListener<Number> uncommitedChangesListener;

	@Override
	protected void onInit() {
		menuBar.setUseSystemMenuBar(true);
		recentButton.setToggleGroup(toggleGroup);
		listButton.setToggleGroup(toggleGroup);
		tableView.prefHeightProperty().bind(splitPane.heightProperty());
		modelToView();
		addCommitClickedListener();
		setCommitButtonText(projectModel.getChanges());
		getStage().setOnCloseRequest(event -> {
			for (final ListenerHandle handle : listenerHandles) {
				handle.remove();
				projectModel.getChangesProperty().removeListener(uncommitedChangesListener);
			}
		});
		addUncommitedChangesListener();
		addCommitsListener();
		addBranchesListener();
	}

	private void addBranchesListener() {
		final ListChangeListener<String> branchesChangesListener = change -> {
			addBranchesToView();
		};
		projectModel.getCurrentBranchProperty().addListener((event, oldValue, newValue) -> {
			getStage().setTitle(projectModel.getProjectName() + " (" + projectModel.getCurrentBranch() + ") - GitFx");
		});
		projectModel.getLocalBranchesProperty().addListener(branchesChangesListener);
		projectModel.getRemoteBranchesProperty().addListener(branchesChangesListener);
		projectModel.getTagsProperty().addListener(branchesChangesListener);
	}

	private void addCommitsListener() {
		projectModel.getCommitsProperty().addListener((ListChangeListener<? super GitFxCommit>) (change) -> {
			try {
				addCommitsLogToView();
			} catch (final IOException e) {
				LOGGER.error("Error while refreshing Commits", e);
			}
		});
	}

	@FXML
	public void commitButtonClicked() {
		try {
			final Result result = fxmlLoader.load(getClass().getResource("/CommitDialogView.fxml"));
			final CommitDialogController commitController = result.getController();
			final Stage stage = new Stage();
			stage.setScene(new Scene(result.getRoot()));
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
		uncommitedChangesListener = (event, oldValue, newValue) -> {
			setCommitButtonText(newValue);
		};
		projectModel.getChangesProperty().addListener(uncommitedChangesListener);
	}

	private void addCommitClickedListener() {
		tableView.setOnMouseClicked(event -> {
			final GitFxCommit selectedItem = tableView.getSelectionModel().getSelectedItem();
			if (selectedItem == null) {
				return;
			}
			showHistoryForCommit(selectedItem);
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

	private void addCommitsLogToView() throws IOException {
		tableView.getItems().clear();
		final ObservableList<GitFxCommit> commits = projectModel.getCommitsProperty();
		final JavaFxPlotRenderer renderer = new JavaFxPlotRenderer();
		final TableColumn<GitFxCommit, GitFxCommit> tableColumn = (TableColumn<GitFxCommit, GitFxCommit>) tableView.getColumns().get(0);
		tableColumn.setCellFactory(createCommitCellFactory(renderer));
		tableColumn.setCellValueFactory(createCellValueFactory());
		tableView.getItems().addAll(commits);
	}

	private Callback<CellDataFeatures<GitFxCommit, GitFxCommit>, ObservableValue<GitFxCommit>> createCellValueFactory() {
		return dataFeatures -> new ObservableValueBase<GitFxCommit>() {

			@Override
			public GitFxCommit getValue() {
				return dataFeatures.getValue();
			}
		};
	}

	private Callback<TableColumn<GitFxCommit, GitFxCommit>, TableCell<GitFxCommit, GitFxCommit>> createCommitCellFactory(final JavaFxPlotRenderer renderer) {
		return arg0 -> {
			final CommitTableCell<GitFxCommit> commitTableCell = new CommitTableCell<>(renderer);
			return commitTableCell;
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

	public void fetchClicked(ActionEvent actionEvent) {
		runGitTaskWithProgressDialog(GitTaskFactory.fetchTask(projectModel.getFxProject().getGit().fetch()));
	}

	public void pullAction(ActionEvent actionEvent) {
		runGitTaskWithProgressDialog(GitTaskFactory.pullTask(projectModel.getFxProject().getGit().pull()));
	}

	public void pushAction(ActionEvent actionEvent) {
		runGitTaskWithProgressDialog(GitTaskFactory.pushTask(projectModel.getFxProject().getGit().push()));
	}
}
