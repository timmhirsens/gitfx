package de.br0tbox.gitfx.ui.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import de.br0tbox.gitfx.ui.shutdown.IShutdownHook;
import de.br0tbox.gitfx.ui.shutdown.TestShutDownHook;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		final Multibinder<IShutdownHook> multibinder = Multibinder.newSetBinder(binder(), IShutdownHook.class);
		multibinder.addBinding().toInstance(new TestShutDownHook());
	}

}
