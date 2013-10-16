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
package de.br0tbox.gitfx.ui.controllers;

import static de.br0tbox.gitfx.ui.util.Preconditions.checkNotNull;
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

import de.br0tbox.gitfx.ui.progress.AbstractMonitorableGitTask;

@FXMLController(controllerId = "/LoadingDialogView.fxml")
public class LoadingDialogController extends AbstractController {

	private AbstractMonitorableGitTask<?> task;

	public void setTask(AbstractMonitorableGitTask<?> task) {
		this.task = task;
		progressBar.progressProperty().bind(task.progressProperty());
		tasktitle.textProperty().bind(task.messageProperty());
		task.getCanceledProperty().bind(canceled);
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
	@FXML
	ProgressBar currentTaskBar;
	private Scene scene;

	public LoadingDialogController() {
		super();
	}

	@Override
	protected void onInit() {
	}

	public void show() {
		checkNotNull(task, "task");
		stage = new Stage();
		scene = new Scene(pane);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.showAndWait();
	}

	public void hide() {
		stage.close();
	}

}
