package de.br0tbox.gitfx.ui.fx;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ChangedFileListCellFactory implements Callback<ListView<String>, ListCell<String>> {

	@Override
	public ListCell<String> call(ListView<String> arg0) {
		return new ChangedFileListCell<>();
	}

}
