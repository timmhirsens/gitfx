package de.br0tbox.gitfx.ui.history;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;

public class JavaFxPlotRenderer extends AbstractPlotRenderer<JavaFxLane, Color> {

	private Group currentShape;

	public Group draw(PlotCommit<JavaFxLane> commit, double height) {
		currentShape = new Group();
		paintCommit(commit, (int) height);
		return currentShape;
	}

	@Override
	protected int drawLabel(int x, int y, Ref ref) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Color laneColor(JavaFxLane myLane) {
		if (myLane != null) {
			return myLane.color;
		}
		return Color.GRAY;
	}

	@Override
	protected void drawLine(Color color, int x1, int y1, int x2, int y2, int width) {
		final Line path = new Line();
		path.setStartX(x1);
		path.setStartY(y1);
		path.setEndX(x2);
		path.setEndY(y2);
		path.setStrokeWidth(width * 1.3);
		path.setStroke(color);
		currentShape.getChildren().add(path);
	}

	@Override
	protected void drawCommitDot(int x, int y, int w, int h) {
		System.out.println(w);
		final Circle circle = new Circle();
		circle.setCenterX(Math.floor(x + w / 2) + 1);
		circle.setCenterY(Math.floor(y + h / 2));
		circle.setRadius(w / 2);
		circle.setFill(Color.GREY);
		currentShape.getChildren().add(circle);
	}

	@Override
	protected void drawBoundaryDot(int x, int y, int w, int h) {
		final Circle circle = new Circle();
		circle.setCenterX(x + w / 2);
		circle.setCenterY(y + h / 2);
		circle.setRadius(h / 2);
		circle.setFill(Color.GRAY);
		currentShape.getChildren().add(circle);
	}

	@Override
	protected void drawText(String msg, int x, int y) {
		// Lets do nothing here for now.

	}

	public Group getCurrentShape() {
		return currentShape;
	}

}
