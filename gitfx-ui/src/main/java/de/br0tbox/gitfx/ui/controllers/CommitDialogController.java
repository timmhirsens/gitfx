package de.br0tbox.gitfx.ui.controllers;

import static de.br0tbox.gitfx.core.util.Preconditions.checkNotNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/CommitDialogView.fxml")
public class CommitDialogController extends AbstractController {

	private ProjectModel projectModel;
	@FXML
	private ListView<String> unstagedFiles;
	@FXML
	private ListView<String> stagedFiles;

	@FXML
	public void stageSelected() throws NoFilepatternException, GitAPIException {
		final AddCommand add = projectModel.getFxProject().getGit().add();
		add.setUpdate(true);
		final List<String> selectedItems = unstagedFiles.getSelectionModel().getSelectedItems();
		for (final String unstagedFile : selectedItems) {
			add.addFilepattern(unstagedFile);
		}
		add.call();
	}

	@FXML
	public void stageAll() throws NoFilepatternException, GitAPIException {
		final AddCommand add = projectModel.getFxProject().getGit().add();
		add.addFilepattern(".");
		add.call();
	}

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
