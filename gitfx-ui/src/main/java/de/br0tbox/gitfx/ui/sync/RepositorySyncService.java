package de.br0tbox.gitfx.ui.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
				System.out.println("Index Change");
				startTask(projectModel);
			}
		});
		final ListenerHandle refsChangedHandle = projectModel.getFxProject().getGit().getRepository().getListenerList().addRefsChangedListener(new RefsChangedListener() {

			@Override
			public void onRefsChanged(RefsChangedEvent event) {
				System.out.println("Refs Change");
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
		final Thread thread = new Thread(synchronizationTask, "Sync-" + model.getProjectName());
		thread.setDaemon(true);
		thread.start();
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
