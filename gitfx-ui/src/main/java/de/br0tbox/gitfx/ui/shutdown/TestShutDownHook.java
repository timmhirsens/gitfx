/**
 * 
 */
package de.br0tbox.gitfx.ui.shutdown;

import javafx.event.Event;

/**
 * @author fr1zle
 *
 */
public class TestShutDownHook implements IShutdownHook {

	@Override
	public void onShutdown(Event event) {
		System.out.println("Shutdown!");
	}

}
