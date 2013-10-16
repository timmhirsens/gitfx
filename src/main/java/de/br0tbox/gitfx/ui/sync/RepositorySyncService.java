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
package de.br0tbox.gitfx.ui.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

public class RepositorySyncService implements IRepositorySyncService {

	private Set<ProjectModel> projectModels = new HashSet<>();
	private Map<ProjectModel, TimerTask> refreshTimers = new HashMap<>();
	private Map<ProjectModel, List<ListenerHandle>> listenerHandles = new HashMap<>();
	private ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {

		private AtomicInteger threadCount = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			final Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("sync-worker #" + threadCount.getAndIncrement());
			return t;
		}
	});

	@Override
	public void startWatchingRepository(final ProjectModel projectModel) {
		startTimer(projectModel);
		List<ListenerHandle> handleList = listenerHandles.get(projectModel);
		if (handleList == null) {
			handleList = new ArrayList<>();
			listenerHandles.put(projectModel, handleList);
		}
		final ListenerHandle indexChangedHandle = projectModel.getFxProject().getGit().getRepository().getListenerList().addIndexChangedListener(new IndexChangedListener() {

			@Override
			public void onIndexChanged(IndexChangedEvent event) {
				startTask(projectModel);
			}
		});
		final ListenerHandle refsChangedHandle = projectModel.getFxProject().getGit().getRepository().getListenerList().addRefsChangedListener(new RefsChangedListener() {

			@Override
			public void onRefsChanged(RefsChangedEvent event) {
				startTask(projectModel);
			}
		});
		handleList.add(refsChangedHandle);
		handleList.add(indexChangedHandle);
		projectModels.add(projectModel);
	}

	@Override
	public void stopWatchingRepository(ProjectModel projectModel) {
		final List<ListenerHandle> list = listenerHandles.get(projectModel);
		if (list != null) {
			for (final ListenerHandle handle : list) {
				handle.remove();
			}
		}
		stopTimer(projectModel);
		projectModels.remove(projectModel);
	}

	private void startTask(ProjectModel model) {
		final SynchronizationTask synchronizationTask = new SynchronizationTask(model);
		executorService.submit(synchronizationTask);
	}

	private void startTimer(final ProjectModel projectModel) {
		final TimerTask task = new GitRefreshTimerTask(projectModel);
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, 4000);
		refreshTimers.put(projectModel, task);
	}

	private void stopTimer(ProjectModel projectModel) {
		final TimerTask timerTask = refreshTimers.get(projectModel);
		if (timerTask != null) {
			timerTask.cancel();
			refreshTimers.remove(projectModel);
		}
	}

}
