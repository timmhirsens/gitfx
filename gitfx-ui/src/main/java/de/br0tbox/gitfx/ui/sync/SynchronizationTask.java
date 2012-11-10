package de.br0tbox.gitfx.ui.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.concurrent.Task;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevWalk;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

public class SynchronizationTask extends Task<Void> {

	private ProjectModel projectModel;

	public SynchronizationTask(ProjectModel model) {
		this.projectModel = model;
	}

	@Override
	protected Void call() throws Exception {
		refreshCurrentBranch();
		refreshUncommitedChangesNumber();
		refreshBranches();
		refreshCommits();
		return null;
	}

	private void refreshUncommitedChangesNumber() {
		final Integer uncommitedChangesNumber = projectModel.getFxProject().getUncommitedChangesNumber();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				projectModel.getUncommitedChanges().set(uncommitedChangesNumber);
			}
		});
	}

	private void refreshCurrentBranch() throws IOException {
		final String branch = projectModel.getFxProject().getGit().getRepository().getBranch();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				projectModel.getCurrentBranchProperty().set(branch);
			}
		});
	}

	private void refreshCommits() throws MissingObjectException, IncorrectObjectTypeException, IOException {
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
		revWalk.release();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				projectModel.getCommitsProperty().clear();
				projectModel.getCommitsProperty().addAll(commits);
			}
		});
	}

	private void refreshBranches() throws IOException {
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
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				projectModel.getLocalBranchesProperty().clear();
				projectModel.getLocalBranchesProperty().addAll(localList);
				projectModel.getRemoteBranchesProperty().clear();
				projectModel.getRemoteBranchesProperty().addAll(remoteList);
				projectModel.getTagsProperty().clear();
				projectModel.getTagsProperty().addAll(tagsList);
			}
		});
	}

}
