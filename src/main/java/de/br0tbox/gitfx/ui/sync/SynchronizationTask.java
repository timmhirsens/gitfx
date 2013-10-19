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
package de.br0tbox.gitfx.ui.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
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

	private static final Logger LOGGER = LogManager.getLogger(SynchronizationTask.class);

	private ProjectModel projectModel;

	public SynchronizationTask(ProjectModel model) {
		this.projectModel = model;
	}

	@Override
	protected Void call() throws Exception {
		LOGGER.debug("Refreshing git repository: " + projectModel.getProjectName());
		refreshCurrentBranch();
		refreshUncommitedChangesNumber();
		refreshIndex();
		refreshBranches();
		refreshCommits();
		return null;
	}

	private void refreshIndex() throws NoWorkTreeException, GitAPIException {
		final Status status = projectModel.getFxProject().getGit().status().call();
		// not in index Changes
		final List<String> unstagedChanges = new ArrayList<>();
		unstagedChanges.addAll(status.getModified());
		unstagedChanges.addAll(status.getMissing());
		unstagedChanges.addAll(status.getUntracked());
		final List<String> stagedChanges = new ArrayList<>();
		stagedChanges.addAll(status.getAdded());
		stagedChanges.addAll(status.getChanged());
		stagedChanges.addAll(status.getRemoved());
		Platform.runLater(() -> {
			projectModel.getUnstagedChangesProperty().getValue().clear();
			projectModel.getStagedChangesProperty().getValue().clear();
			projectModel.getUnstagedChangesProperty().getValue().addAll(unstagedChanges);
			projectModel.getStagedChangesProperty().getValue().addAll(stagedChanges);
		});
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
		revWalk.markStart(revWalk.parseCommit(repository.getRef(repository.getBranch()).getObjectId()));
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
		Platform.runLater(() -> {
			projectModel.getLocalBranchesProperty().clear();
			projectModel.getLocalBranchesProperty().addAll(localList);
			projectModel.getRemoteBranchesProperty().clear();
			projectModel.getRemoteBranchesProperty().addAll(remoteList);
			projectModel.getTagsProperty().clear();
			projectModel.getTagsProperty().addAll(tagsList);
		});
	}

}
