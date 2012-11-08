package de.br0tbox.gitfx.ui.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.progress.GitFxProgressMonitor;

@FXMLController(controllerId = "/LoadingDialogView.fxml")
public class LoadingDialogController extends AbstractController {

	GitFxProgressMonitor progressMonitor;

	public void setProgressMonitor(GitFxProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		progressBar.progressProperty().bind(progressMonitor.getProgressProperty());
		tasktitle.textProperty().bind(progressMonitor.getTitleProperty());
		progressMonitor.getCanceledProperty().bind(canceled);
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				canceled.set(true);
			}
		});
	}

	private BooleanProperty canceled = new SimpleBooleanProperty(false);
	@FXML
	Button cancelButton;
	@FXML
	Label tasktitle;
	@FXML
	AnchorPane pane;
	@FXML
	ProgressBar progressBar;
	private Stage stage;

	public LoadingDialogController() {
		super();
	}

	@Override
	protected void onInit() {
	}

	public void show() {
		stage = new Stage();
		final Scene scene = new Scene(pane);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.showAndWait();
	}

	public void hide() {
		stage.close();
	}

}
