package de.br0tbox.gitfx.ui.uimodel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProjectModel {

	private SimpleIntegerProperty changes = new SimpleIntegerProperty();
	private SimpleStringProperty projectName = new SimpleStringProperty("");
	private SimpleStringProperty currentBranch = new SimpleStringProperty("");

	public int getChanges() {
		return changes.get();
	}

	public void setChanges(int changes) {
		this.changes.set(changes);
	}

	public String getCurrentBranch() {
		return currentBranch.get();
	}

	public void setCurrentBranch(String currentBranch) {
		this.currentBranch.set(currentBranch);
	}

	public String getProjectName() {
		return projectName.get();
	}

	public void setProjectName(String projectName) {
		this.projectName.set(projectName);
	}

	@Override
	public String toString() {
		return projectName.get() + " (" + currentBranch.get() + ")" + " (" + changes.get() + ")";
	}

}
