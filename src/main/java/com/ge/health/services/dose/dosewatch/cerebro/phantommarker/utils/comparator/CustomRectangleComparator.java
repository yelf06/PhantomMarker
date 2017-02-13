package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.comparator;

import java.util.Comparator;

import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.CustomRectangle;

import javafx.scene.Node;

/**
 * Used to compare rectangles positions on the dcm image
 * @SuppressWarnings("restriction") is used because the javafx use is restricted in
 * the 1.8.0_25 jdk version. It's present by default beginning from the 1.8.0_40 jdk version
 * @author Yacine
 */
@SuppressWarnings("restriction")
public class CustomRectangleComparator implements Comparator<Node> {

	/**
	 * Compare two nodes
	 * 
	 * @param node1 the first node
	 * @param node2 the second node
	 * 
	 * return -1 if node1 is higher than node2
	 * 		  0 if they have the same position
	 * 		  1 if node1 is lower than node2
	 */
	@Override
	public int compare(Node node1, Node node2) {
		CustomRectangle rectangle1 = (CustomRectangle) node1;
		CustomRectangle rectangle2 = (CustomRectangle) node2;
		return Double.compare(rectangle1.getY(), rectangle2.getY());
	}

}
