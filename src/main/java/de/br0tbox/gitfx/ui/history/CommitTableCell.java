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
package de.br0tbox.gitfx.ui.history;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;
import de.br0tbox.gitfx.ui.uimodel.GitFxCommit;
import javafx.scene.Group;
import javafx.scene.control.TableCell;
import org.eclipse.jgit.revplot.PlotCommit;

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
