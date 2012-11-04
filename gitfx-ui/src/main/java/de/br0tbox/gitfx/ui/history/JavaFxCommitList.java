package de.br0tbox.gitfx.ui.history;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.paint.Color;

import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;

public class JavaFxCommitList extends PlotCommitList<JavaFxLane> {

	// private static final Color[] COLORS = new Color[] { Color.ALICEBLUE,
	// Color.BEIGE, Color.BLANCHEDALMOND, Color.SPRINGGREEN, Color.BLUEVIOLET,
	// Color.CHARTREUSE, Color.RED, Color.YELLOW };
	private static final Color[] COLORS = new Color[] { Color.GREEN, Color.RED, Color.BLUE, Color.BROWN };
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
