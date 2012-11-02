package de.br0tbox.gitfx.ui.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/SingleProjectView.fxml")
public class SingleProjectController extends AbstractController {

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

	@Override
	protected void onInit() {
		modelToView();
	}

	private void modelToView() {
		getStage().setTitle(projectModel.getProjectName());
		final LogCommand log = projectModel.getFxProject().getGit().log();
		// Commits
		tableView.setItems(projectModel.getCommits());
		try {
			final Iterable<RevCommit> call = log.call();
			final Iterator<RevCommit> iterator = call.iterator();
			while (iterator.hasNext()) {
				final RevCommit revCommit = iterator.next();
				final String shortMessage = revCommit.getShortMessage();
				final String name = revCommit.getAuthorIdent().getName();
				final String hash = revCommit.getId().abbreviate(7).name();
				final GitFxCommit gitFxCommit = new GitFxCommit(hash, name, shortMessage);
				projectModel.getCommits().add(gitFxCommit);
			}
			// Branches
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
		} catch (final GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setProject(ProjectModel projectModel) {
		this.projectModel = projectModel;
	}

}
