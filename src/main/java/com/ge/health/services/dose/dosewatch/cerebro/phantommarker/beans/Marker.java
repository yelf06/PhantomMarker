package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans;

import java.util.ArrayList;
import java.util.List;

import com.ge.health.services.dose.dosewatch.cerebro.enums.AnatomicalRegion;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Represents a selected marker
 * 
 * @author Yacine
 *
 */
public class Marker {

	/** Anatomical Region */
	private AnatomicalRegion topRegion;
	/** Anatomical Region */
	private AnatomicalRegion bottomRegion;
	/** The selected position */
	private Double selectedPosition;

	/**
	 * @param topRegion
	 * @param bottomRegion
	 * @param selectedPosition
	 */
	public Marker(AnatomicalRegion topRegion, AnatomicalRegion bottomRegion, Double selectedPosition) {
		this.topRegion = topRegion;
		this.bottomRegion = bottomRegion;
		this.selectedPosition = selectedPosition;
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
	 * @return the selectedPosition
	 */
	public Double getSelectedPosition() {
		return selectedPosition;
	}

	/**
	 * @param selectedPosition the selectedPosition to set
	 */
	public void setSelectedPosition(Double selectedPosition) {
		this.selectedPosition = selectedPosition;
	}

	/**
	 * Retrieves the JSON representation of a marker
	 * 
	 * @param marker
	 * @return a jsonElement
	 */
	public static JsonElement toJson(Marker marker) {
		return new Gson().toJsonTree(marker);
	}

	/**
	 * Builds a marker from a given json
	 * 
	 * @param json
	 * @return the built object
	 * @throws Exception
	 */
	public static Marker fromJson(String json) throws Exception {
		return new Gson().fromJson(json, Marker.class);
	}
	
	/**
	 * Builds a marker from a given json
	 * 
	 * @param json
	 * @return the built object
	 * @throws Exception
	 */
	public static Marker fromJsonElement(JsonElement jsonElement) throws Exception {
		return new Gson().fromJson(jsonElement, Marker.class);
	}
	
	/**
	 * Builds a list of markers from json
	 * @param json
	 * @return
	 */
	public static List<Marker> listFromJson(String json) {
		List<Marker> markerList = new ArrayList<>();
		
		JsonArray array = new Gson().fromJson(json, JsonArray.class);
		array.forEach(jsonElement -> {
			try {
				markerList.add(fromJsonElement(jsonElement));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		return markerList;
	}
}
