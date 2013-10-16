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
package de.br0tbox.gitfx.ui.fx;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChangedFileListCell<T> extends ListCell<T> {

	static final Image IMAGE_ADDED = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/page_white_add.png"));
	static final Image IMAGE_MODIFIED = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/page_white_edit.png"));
	static final Image IMAGE_DELETED = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/page_white_delete.png"));
	static final Image IMAGE_RENAME = new Image(ChangedFileListCell.class.getResourceAsStream("/icons/arrow_switch.png"));

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {
			super.setText(null);
			super.setGraphic(null);
		} else {
			final String substring = item.toString().substring(0, 1);
			final String text = item.toString().substring(2, item.toString().length());
			super.setText(text);
			switch (substring) {
			case "M":
				super.setGraphic(new ImageView(IMAGE_MODIFIED));
				break;
			case "A":
				super.setGraphic(new ImageView(IMAGE_ADDED));
				break;
			case "D":
				super.setGraphic(new ImageView(IMAGE_DELETED));
				break;
			case "R":
				super.setGraphic(new ImageView(IMAGE_RENAME));
				break;
			default:
				System.out.println(item.toString());
			}
		}
	}
}
