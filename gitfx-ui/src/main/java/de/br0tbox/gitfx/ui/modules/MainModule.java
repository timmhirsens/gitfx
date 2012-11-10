package de.br0tbox.gitfx.ui.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import de.br0tbox.gitfx.core.services.IProjectPersistentService;
import de.br0tbox.gitfx.core.services.IPropertyService;
import de.br0tbox.gitfx.core.services.impl.SimpleLocalPropertyService;
import de.br0tbox.gitfx.core.services.impl.YamlProjectPersistentService;
import de.br0tbox.gitfx.ui.shutdown.IShutdownHook;
import de.br0tbox.gitfx.ui.shutdown.TestShutDownHook;
import de.br0tbox.gitfx.ui.sync.IRepositorySyncService;
import de.br0tbox.gitfx.ui.sync.RepositorySyncService;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		final Multibinder<IShutdownHook> multibinder = Multibinder.newSetBinder(binder(), IShutdownHook.class);
		multibinder.addBinding().toInstance(new TestShutDownHook());
		bind(IPropertyService.class).to(SimpleLocalPropertyService.class).in(Singleton.class);
		bind(IProjectPersistentService.class).to(YamlProjectPersistentService.class).in(Singleton.class);
		bind(IRepositorySyncService.class).to(RepositorySyncService.class).in(Singleton.class);
	}

}
