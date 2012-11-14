/**
 * 
 */
package de.br0tbox.gitfx.ui.shutdown;

import javafx.event.Event;

/**
 * @author fr1zle
 *
 */
public interface IShutdownHook {

	void onShutdown(Event event);

}
