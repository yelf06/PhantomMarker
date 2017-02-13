package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.comparator;

import java.util.Comparator;

import com.ge.health.services.dose.dosewatch.cerebro.beans.Landmark;

public class LandmarkComparator implements Comparator<Landmark> {

	/**
	 * Compare two landmarks
	 *
	 * @param o1 the first landmark
	 * @param o2 the second landmark
	 *
	 * return -1 if o1 is higher than o2
	 * 		  0 if they have the same position
	 * 		  1 if o1 is lower than o2
	 */
	@Override
	public int compare(Landmark o1, Landmark o2) {
		return Integer.compare(o1.getDistanceFromImageTopInPixels().intValue(),
				o2.getDistanceFromImageTopInPixels().intValue());
	}

}
