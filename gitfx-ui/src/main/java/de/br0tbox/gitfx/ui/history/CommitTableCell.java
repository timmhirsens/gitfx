package de.br0tbox.gitfx.ui.history;

import javafx.scene.Group;
import javafx.scene.control.TableCell;

import org.eclipse.jgit.revplot.PlotCommit;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;

public class CommitTableCell<S> extends TableCell<S, GitFxCommit> {

	private JavaFxPlotRenderer fxPlotRenderer;

	public CommitTableCell(JavaFxPlotRenderer fxPlotRenderer) {
		this.fxPlotRenderer = fxPlotRenderer;
	}

	@Override
	protected void updateItem(GitFxCommit item, boolean visible) {
		super.updateItem(item, visible);
		if (item == null) {
			return;
		}
		final PlotCommit<?> commit = item.getCommit();
		@SuppressWarnings("unchecked")
		final Group currentShape = fxPlotRenderer.draw((PlotCommit<JavaFxLane>) commit, 24);
		setGraphic(currentShape);
	}
}
