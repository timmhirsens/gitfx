/**
 * 
 */
package de.br0tbox.gitfx.ui.progress;

import org.eclipse.jgit.api.CloneCommand;

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
}
