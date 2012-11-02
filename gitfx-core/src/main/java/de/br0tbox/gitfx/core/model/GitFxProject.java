package de.br0tbox.gitfx.core.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;

public class GitFxProject {

	private final Git git;

	public GitFxProject(Git git) {
		this.git = git;
	}
	
	public Set<String> getAllUncommitedChanges() {
		final Set<String> uncommitedChanges = new HashSet<>();
		try {
			final Status status = getGit().status().call();
			uncommitedChanges.addAll(status.getAdded());
			uncommitedChanges.addAll(status.getChanged());
			uncommitedChanges.addAll(status.getModified());
			uncommitedChanges.addAll(status.getUntracked());
			return uncommitedChanges;
		} catch (NoWorkTreeException | GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Integer getUncommitedChangesNumber() {
		return getAllUncommitedChanges().size();
	}
	
	public Status getStatus() {
		try {
			return getGit().status().call();
		} catch (NoWorkTreeException | GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public Git getGit() {
		return git;
	}
	
}
