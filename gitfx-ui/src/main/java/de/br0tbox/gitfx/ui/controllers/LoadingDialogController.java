package de.br0tbox.gitfx.ui.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.progress.GitFxProgressMonitor;

@FXMLController(controllerId = "/LoadingDialogView.fxml")
public class LoadingDialogController extends AbstractController {
	
	GitFxProgressMonitor progressMonitor;
	
	public void setProgressMonitor(GitFxProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		titleProperty = new SimpleStringProperty();
		progressMonitor.setTitleProperty(titleProperty);
		progressMonitor.setProgressIndicator(progressBar);
		titleProperty.addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				final String taskName = arg2;
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						tasktitle.setText(taskName);
					}
				});
			}

		});
	}

	@FXML Button cancelButton;
	@FXML Label tasktitle;
	@FXML AnchorPane pane;
	@FXML ProgressBar progressBar;
	private SimpleStringProperty titleProperty;
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
		stage.setScene(scene);
		stage.show();
	}
	
	public void hide() {
		stage.close();
	}

}
