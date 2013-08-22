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
/**
 * 
 */
package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.PullCommand;

/**
 * @author fr1zle
 *
 */
public final class GitTaskFactory {

	private GitTaskFactory() {

	}

	public static GitCloneTask cloneTask(CloneCommand cloneCommand) {
		final GitCloneTask gitCloneTask = new GitCloneTask(cloneCommand);
		gitCloneTask.init();
		return gitCloneTask;
	}

	public static GitFetchTask fetchTask(FetchCommand fetchCommand) {
		final GitFetchTask gitFetchTask = new GitFetchTask(fetchCommand);
		gitFetchTask.init();
		return gitFetchTask;
	}

	public static GitPullTask pullTask(PullCommand pullCommand) {
		final GitPullTask gitPullTask = new GitPullTask(pullCommand);
		gitPullTask.init();
		return gitPullTask;
	}
}
