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
package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitCloneTask extends AbstractMonitorableGitTask<Git> {

	protected GitCloneTask(GitCommand<Git> gitCommand) {
		super(gitCommand);
	}

	@Override
	protected void addMonitorToGitCommand(ProgressMonitor progressMonitor) {
		((CloneCommand) gitCommand).setProgressMonitor(progressMonitor);
	}

	@Override
	protected void onAfterCall(Git returnValue) {
		if (returnValue != null && returnValue.getRepository() != null) {
			returnValue.getRepository().close();
		}
	}

}
