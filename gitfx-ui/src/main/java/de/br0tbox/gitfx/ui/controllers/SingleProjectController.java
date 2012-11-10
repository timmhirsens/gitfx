package de.br0tbox.gitfx.ui.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
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
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.core.util.Preconditions;
import de.br0tbox.gitfx.ui.fx.ChangedFileListCell;
import de.br0tbox.gitfx.ui.history.CommitTableCell;
import de.br0tbox.gitfx.ui.history.JavaFxCommitList;
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
	private List<ListenerHandle> listenerHandles = new ArrayList<>();
	private ChangeListener<Number> uncommitedChangesListener;
	@FXML
	ImageView commitButtonImage;

	@Override
	protected void onInit() {
		recentButton.setToggleGroup(toggleGroup);
		listButton.setToggleGroup(toggleGroup);
		modelToView();
		addCommitClickedListener();
		addProjectListeners();
		setCommitButtonText(projectModel.getChanges());
		getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
			// Clean up listeners when project is closed.
			@Override
			public void handle(WindowEvent arg0) {
				for (final ListenerHandle handle : listenerHandles) {
					handle.remove();
					projectModel.getChangesProperty().removeListener(uncommitedChangesListener);
				}
			}
		});
		addUncommitedChangesListener();
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

	private void addProjectListeners() {
		final ListenerHandle indexChangedListener = projectModel.getFxProject().getGit().getRepository().getListenerList().addIndexChangedListener(new IndexChangedListener() {

			@Override
			public void onIndexChanged(IndexChangedEvent event) {
				modelToView();
			}
		});
		final ListenerHandle refsChangedListener = projectModel.getFxProject().getGit().getRepository().getListenerList().addRefsChangedListener(new RefsChangedListener() {

			@Override
			public void onRefsChanged(RefsChangedEvent event) {
				modelToView();
			}
		});
		listenerHandles.add(indexChangedListener);
		listenerHandles.add(refsChangedListener);
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
			addCommitLogToView();
			addBranchesToView();
		} catch (final IOException e) {
			LOGGER.error("Error while creating log view.", e);
		}
	}

	private void addCommitLogToView() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		final Repository repository = projectModel.getFxProject().getGit().getRepository();
		final RevWalk revWalk = new PlotWalk(repository);
		revWalk.markStart(revWalk.parseCommit(repository.getRef("master").getObjectId()));
		final JavaFxCommitList commitList = new JavaFxCommitList();
		commitList.source(revWalk);
		commitList.fillTo(512);
		PlotCommit<?>[] array = new PlotCommit[commitList.size()];
		array = commitList.toArray(array);
		final List<GitFxCommit> commits = new ArrayList<>(array.length);
		for (final PlotCommit<?> commit : array) {
			final GitFxCommit gitFxCommit = new GitFxCommit(commit.abbreviate(7).name(), commit.getAuthorIdent().getName(), commit.getShortMessage(), commit);
			commits.add(gitFxCommit);
		}
		final JavaFxPlotRenderer renderer = new JavaFxPlotRenderer();
		final TableColumn<GitFxCommit, GitFxCommit> tableColumn = (TableColumn<GitFxCommit, GitFxCommit>) tableView.getColumns().get(0);
		tableColumn.setCellFactory(createCommitCellFactory(renderer));
		tableColumn.setCellValueFactory(createCellValueFactory());
		tableView.getItems().addAll(commits);
		revWalk.release();
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

	private void addBranchesToView() throws IOException {
		treeView.showRootProperty().set(false);
		final List<String> remoteList = new ArrayList<>();
		final List<String> tagsList = new ArrayList<>();
		final List<String> localList = new ArrayList<>();
		final Repository repository = projectModel.getFxProject().getGit().getRepository();
		final Map<String, Ref> remotes = repository.getRefDatabase().getRefs(Constants.R_REMOTES);
		final Iterator<String> remotesIterator = remotes.keySet().iterator();
		while (remotesIterator.hasNext()) {
			final String remotekey = remotesIterator.next();
			final Ref remote = remotes.get(remotekey);
			remoteList.add(remote.getName());
		}
		final Map<String, Ref> tags = repository.getRefDatabase().getRefs(Constants.R_TAGS);
		final Iterator<String> tagsIterator = tags.keySet().iterator();
		while (tagsIterator.hasNext()) {
			final String tagkey = tagsIterator.next();
			final Ref tag = tags.get(tagkey);
			tagsList.add(tag.getName());
		}
		final Map<String, Ref> all = repository.getRefDatabase().getRefs(Constants.R_REFS);
		final Iterator<String> allIterator = all.keySet().iterator();
		while (allIterator.hasNext()) {
			final String allKey = allIterator.next();
			final Ref allRef = all.get(allKey);
			if (allRef != null) {
				final String name = allRef.getName();
				if (!remoteList.contains(name) && !tagsList.contains(name)) {
					localList.add(name);
				}
			}
		}
		branchesItem.getChildren().clear();
		remotesItem.getChildren().clear();
		tagsItem.getChildren().clear();
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

			final DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
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
