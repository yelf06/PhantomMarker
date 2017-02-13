package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans;

import com.ge.health.services.dose.dosewatch.cerebro.enums.AnatomicalRegion;

public class ButtonUserData {
	
	/** The top region of the landmark created by this button */
	private AnatomicalRegion topRegion;
	/** The bottom region of the landmark created by this button */
	private AnatomicalRegion bottomRegion;
	/** Landmark position from top */
	private Integer readLandmarkPosition;
	
	/**
	 * @param topRegion
	 * @param bottomRegion
	 */
	public ButtonUserData(AnatomicalRegion topRegion, AnatomicalRegion bottomRegion, Integer readLandmarkPosition) {
		this.topRegion = topRegion;
		this.bottomRegion = bottomRegion;
		this.readLandmarkPosition = readLandmarkPosition;
	}

	/**
	 * @return the topRegion
	 */
	public AnatomicalRegion getTopRegion() {
		return topRegion;
	}

	/**
	 * @param topRegion the topRegion to set
	 */
	public void setTopRegion(AnatomicalRegion topRegion) {
		this.topRegion = topRegion;
	}

	/**
	 * @return the bottomRegion
	 */
	public AnatomicalRegion getBottomRegion() {
		return bottomRegion;
	}

	/**
	 * @param bottomRegion the bottomRegion to set
	 */
	public void setBottomRegion(AnatomicalRegion bottomRegion) {
		this.bottomRegion = bottomRegion;
	}

	/**
	 * @return the readLandmarkPosition
	 */
	public Integer getReadLandmarkPosition() {
		return readLandmarkPosition;
	}

	/**
	 * @param readLandmarkPosition the readLandmarkPosition to set
	 */
	public void setReadLandmarkPosition(Integer readLandmarkPosition) {
		this.readLandmarkPosition = readLandmarkPosition;
	}
}
