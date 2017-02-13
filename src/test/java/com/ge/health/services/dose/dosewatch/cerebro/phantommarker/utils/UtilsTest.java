package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ge.health.services.dose.dosewatch.cerebro.enums.AnatomicalRegion;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.CustomRectangle;

import javafx.scene.layout.Pane;

public class UtilsTest {
	
	@Test
	public void testGetSurrounding2Regions() {
		Pane pane = new Pane();
		pane.getChildren().add(new CustomRectangle(0, 0, 100, 100, AnatomicalRegion.HEAD));
		pane.getChildren().add(new CustomRectangle(0, 300, 100, 100, AnatomicalRegion.ABDOMEN));
		
		List<AnatomicalRegion> list = UIUtils.getSurroundingRegions(pane, 200);
		
		Assert.assertEquals(list.size(), 2);
	}
	
	@Test
	public void testGetSurroundingInTopRegion() {
		Pane pane = new Pane();
		pane.getChildren().add(new CustomRectangle(0, 0, 100, 100, AnatomicalRegion.HEAD));
		pane.getChildren().add(new CustomRectangle(0, 300, 100, 100, AnatomicalRegion.ABDOMEN));
		
		List<AnatomicalRegion> list = UIUtils.getSurroundingRegions(pane, 50);
		
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), AnatomicalRegion.HEAD);
	}
	
	@Test
	public void testGetSurroundingInBottomRegion() {
		Pane pane = new Pane();
		pane.getChildren().add(new CustomRectangle(0, 0, 100, 100, AnatomicalRegion.HEAD));
		pane.getChildren().add(new CustomRectangle(0, 300, 100, 100, AnatomicalRegion.ABDOMEN));
		
		List<AnatomicalRegion> list = UIUtils.getSurroundingRegions(pane, 350);
		
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), AnatomicalRegion.ABDOMEN);
	}
	
}
