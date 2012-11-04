package de.br0tbox.gitfx.ui.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;

import de.br0tbox.gitfx.ui.fx.ChangedFileListCell;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/SingleProjectView.fxml")
public class SingleProjectController extends AbstractController {

	static final Image IMAGE_COMMIT_CLEAN = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/accept.png"));
	static final Image IMAGE_COMMIT_DIRTY = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/asterisk_yellow.png"));

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
		getStage().setResizable(false);
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
		addCommitButtonListener();
		addUncommitedChangesListener();
	}

	private void addCommitButtonListener() {
		commitButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				try {
					final Result result = fxmlLoader.load(getClass().getResource("/CommitDialogView.fxml"));
					final CommitDialogController commitController = result.getController();
					final Stage stage = new Stage();
					stage.setScene(new Scene((Parent) result.getRoot()));
					stage.setResizable(false);
					commitController.setProjectModel(projectModel);
					commitController.init(stage);
					stage.showAndWait();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
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
		final LogCommand log = projectModel.getFxProject().getGit().log();
		// Commits
		tableView.getItems().clear();
		tableView.setItems(projectModel.getCommits());
		try {
			addCommitsToView(log);
			// Branches
			addBranchesToView();
		} catch (final GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		for (final String local : localList) {
			branchesItem.getChildren().add(new TreeItem(local));
		}
		for (final String remote : remoteList) {
			remotesItem.getChildren().add(new TreeItem(remote));
		}
		for (final String tag : tagsList) {
			tagsItem.getChildren().add(new TreeItem(tag));
		}
		branchesItem.expandedProperty().set(true);
		remotesItem.expandedProperty().set(true);
		tagsItem.expandedProperty().set(true);
	}

	private void addCommitsToView(final LogCommand log) throws GitAPIException, NoHeadException {
		final Iterable<RevCommit> call = log.call();
		final Iterator<RevCommit> iterator = call.iterator();
		projectModel.getCommits().clear();
		while (iterator.hasNext()) {
			final RevCommit revCommit = iterator.next();
			final String shortMessage = revCommit.getShortMessage();
			final String name = revCommit.getAuthorIdent().getName();
			final String hash = revCommit.getId().abbreviate(7).name();
			final GitFxCommit gitFxCommit = new GitFxCommit(hash, name, shortMessage);
			projectModel.getCommits().add(gitFxCommit);
		}
	}

	public void setProject(ProjectModel projectModel) {
		this.projectModel = projectModel;
	}

	private void showHistoryForCommit(final GitFxCommit selectedCommit) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
