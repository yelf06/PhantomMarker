package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans;

import com.ge.health.services.dose.dosewatch.cerebro.enums.AnatomicalRegion;

import javafx.scene.shape.Rectangle;

/**
 * Custom rectangle extending the javafx Rectangle
 * Used to identify a rectangle with the anatomical region it represents
 * @SuppressWarnings("restriction") is used because the javafx use is restricted in
 * the 1.8.0_25 jdk version. It's present by default beginning from the 1.8.0_40 jdk version
 * @author Yacine
 */
@SuppressWarnings("restriction")
public class CustomRectangle extends Rectangle implements Comparable<Rectangle> {

	/** Anatomical region */
	private AnatomicalRegion anatomicalRegion;

	/**
	 * Constructor calling super class constructor
	 * @param anatomicalRegion
	 */
	public CustomRectangle(double x, double y, double width, double height, AnatomicalRegion anatomicalRegion) {
		super(x, y, width, height);
		this.anatomicalRegion = anatomicalRegion;
	}

	/**
	 * @return the anatomicalRegion
	 */
	public AnatomicalRegion getAnatomicalRegion() {
		return anatomicalRegion;
	}

	/**
	 * @param anatomicalRegion the anatomicalRegion to set
	 */
	public void setAnatomicalRegion(AnatomicalRegion anatomicalRegion) {
		this.anatomicalRegion = anatomicalRegion;
	}

	@Override
	public int compareTo(Rectangle o) {
		return Double.compare(getY(), o.getY());
	}
}
