package de.br0tbox.gitfx.ui.uimodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.br0tbox.gitfx.core.model.GitFxProject;

public class ProjectModel {

	private SimpleIntegerProperty changesProperty = new SimpleIntegerProperty();
	private SimpleStringProperty projectNameProperty = new SimpleStringProperty("");
	private SimpleStringProperty currentBranchProperty = new SimpleStringProperty("");
	private SimpleStringProperty pathProperty = new SimpleStringProperty("");
	private IntegerProperty uncommitedChanges = new SimpleIntegerProperty();
	private ObservableList<GitFxCommit> commitsProperty = FXCollections.observableArrayList();
	private ObservableList<String> localBranchesProperty = FXCollections.observableArrayList();
	private ObservableList<String> tagsProperty = FXCollections.observableArrayList();
	private ObservableList<String> remoteBranchesProperty = FXCollections.observableArrayList();
	private GitFxProject fxProject;

	public ObservableList<GitFxCommit> getCommits() {
		return commitsProperty;
	}

	public GitFxProject getFxProject() {
		return fxProject;
	}

	public ProjectModel(GitFxProject fxProject) {
		this.fxProject = fxProject;
	}

	public int getChanges() {
		return getChangesProperty().get();
	}

	public void setChanges(int changes) {
		this.getChangesProperty().set(changes);
	}

	public String getCurrentBranch() {
		return currentBranchProperty.get();
	}

	public void setCurrentBranch(String currentBranch) {
		this.currentBranchProperty.set(currentBranch);
	}

	public String getProjectName() {
		return projectNameProperty.get();
	}

	public void setProjectName(String projectName) {
		this.projectNameProperty.set(projectName);
	}

	public String getPath() {
		return pathProperty.get();
	}

	public void setPath(String path) {
		this.pathProperty.set(path);
	}

	@Override
	public String toString() {
		return projectNameProperty.get() + " (" + currentBranchProperty.get() + ")" + " (" + getChangesProperty().get() + ")";
	}

	public SimpleIntegerProperty getChangesProperty() {
		return changesProperty;
	}

	public SimpleStringProperty getProjectNameProperty() {
		return projectNameProperty;
	}

	public SimpleStringProperty getCurrentBranchProperty() {
		return currentBranchProperty;
	}

	public SimpleStringProperty getPathProperty() {
		return pathProperty;
	}

	public ObservableList<GitFxCommit> getCommitsProperty() {
		return commitsProperty;
	}

	public ObservableList<String> getLocalBranchesProperty() {
		return localBranchesProperty;
	}

	public ObservableList<String> getTagsProperty() {
		return tagsProperty;
	}

	public ObservableList<String> getRemoteBranchesProperty() {
		return remoteBranchesProperty;
	}

	public IntegerProperty getUncommitedChanges() {
		return uncommitedChanges;
	}
}
