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
/**
 * 
 */
package de.br0tbox.gitfx.ui.shutdown;

import javafx.event.Event;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.stage.Stage;

/**
 * @author fr1zle
 *
 */
public class TestShutDownHook implements IShutdownHook {

	@Override
	public void onShutdown(Event event) {
		final Object source = event.getSource();
		((Stage) source).toFront();
		final DialogResponse quit = Dialogs.showConfirmDialog((Stage) source, "Are you sure you want to quit GitFx?");
		if (!DialogResponse.YES.equals(quit)) {
			event.consume();
		}
	}

}
