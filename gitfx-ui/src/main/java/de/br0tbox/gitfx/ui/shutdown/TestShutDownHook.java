/**
 * 
 */
package de.br0tbox.gitfx.ui.shutdown;

/**
 * @author fr1zle
 *
 */
public class TestShutDownHook implements IShutdownHook {

	@Override
	public void onShutdown() {
		System.out.println("Shutdown!");
	}

}
