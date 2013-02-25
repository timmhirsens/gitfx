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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.paint.Color;

import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;

public class JavaFxCommitList extends PlotCommitList<JavaFxLane> {

	private static final Color[] COLORS = new Color[] { Color.GREEN, Color.RED, Color.BLUE, Color.BROWN, Color.VIOLET, Color.CYAN, Color.MAGENTA, Color.DARKCYAN, Color.CORNFLOWERBLUE };
	private List<Color> allColors;
	private LinkedList<Color> availableColors;

	public JavaFxCommitList() {
		allColors = new ArrayList<>(COLORS.length);
		for (final Color color : COLORS) {
			allColors.add(color);
		}
		availableColors = new LinkedList<>();
		availableColors.addAll(allColors);
	}

	@Override
	protected JavaFxLane createLane() {
		if (availableColors.isEmpty()) {
			availableColors.addAll(allColors);
		}
		final Color first = availableColors.removeFirst();
		return new JavaFxLane(first);
	}

	static class JavaFxLane extends PlotLane {

		private static final long serialVersionUID = -2444657576507237665L;

		Color color;

		public JavaFxLane(Color color) {
			this.color = color;
		}

	}

}
