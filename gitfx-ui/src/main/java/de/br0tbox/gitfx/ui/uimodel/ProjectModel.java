package de.br0tbox.gitfx.ui.uimodel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.br0tbox.gitfx.core.model.GitFxProject;

public class ProjectModel {

	private SimpleIntegerProperty changes = new SimpleIntegerProperty();
	private SimpleStringProperty projectName = new SimpleStringProperty("");
	private SimpleStringProperty currentBranch = new SimpleStringProperty("");
	private ObservableList<GitFxCommit> commits = FXCollections.observableArrayList();
	public ObservableList<GitFxCommit> getCommits() {
		return commits;
	}

	private GitFxProject fxProject;

	public GitFxProject getFxProject() {
		return fxProject;
	}

	public ProjectModel(GitFxProject fxProject) {
		this.fxProject = fxProject;

	}

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
