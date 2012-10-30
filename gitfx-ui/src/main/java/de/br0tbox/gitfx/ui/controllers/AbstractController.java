package de.br0tbox.gitfx.ui.controllers;

import javafx.stage.Stage;


public abstract class AbstractController {

	private Stage stage;

	public final void init(Stage parent) {
		this.stage = parent;
		onInit();
	}
	
	protected abstract void onInit();

	public Stage getStage() {
		return stage;
	}
	
}
