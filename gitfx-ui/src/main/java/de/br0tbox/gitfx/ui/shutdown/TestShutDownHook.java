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
		final DialogResponse quit = Dialogs.showConfirmDialog((Stage) source, "Are you sure you want to quit GitFx?");
		if (!DialogResponse.YES.equals(quit)) {
			event.consume();
		}
	}

}
