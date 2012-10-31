package de.br0tbox.gitfx.ui.progress;

public interface ProgressCallback {
	
	void onFinished();
	
	void onNewTask(String newTask);

}
