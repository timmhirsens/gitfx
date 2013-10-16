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
package de.br0tbox.gitfx.ui.model;

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
			uncommitedChanges.addAll(status.getRemoved());
			uncommitedChanges.addAll(status.getMissing());
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
