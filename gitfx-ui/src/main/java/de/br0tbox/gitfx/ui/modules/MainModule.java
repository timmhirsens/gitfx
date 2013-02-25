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
