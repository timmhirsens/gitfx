package de.br0tbox.gitfx.ui.controllers;

import static de.br0tbox.gitfx.core.util.Preconditions.checkNotNull;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/CommitDialogView.fxml")
public class CommitDialogController extends AbstractController {

	private ProjectModel projectModel;
	@FXML
	ListView<String> unstagedFiles;
	@FXML
	ListView<String> stagedFiles;

	@Override
	protected void onInit() {
		checkNotNull(projectModel, "projectModel");
		unstagedFiles.itemsProperty().bind(projectModel.getUnstagedChangesProperty());
		stagedFiles.itemsProperty().bind(projectModel.getStagedChangesProperty());
	}

	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public void setProjectModel(ProjectModel projectModel) {
		this.projectModel = projectModel;
	}

}
