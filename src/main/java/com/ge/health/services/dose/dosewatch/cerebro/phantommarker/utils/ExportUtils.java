package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.DicomObject;

import com.ge.health.services.dose.dosewatch.cerebro.beans.Atlas;
import com.ge.health.services.dose.dosewatch.cerebro.beans.CerebroAtlasElement;
import com.ge.health.services.dose.dosewatch.cerebro.beans.CerebroPatient;
import com.ge.health.services.dose.dosewatch.cerebro.beans.Landmark;
import com.ge.health.services.dose.dosewatch.cerebro.beans.ReferenceAcquisition;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.Main;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.Marker;
import com.ge.health.services.dose.dosewatch.cerebro.signatureengine.SignatureEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.serphydose.computations.ssde.beans.PixelSpacing;
import com.serphydose.link.services.Dcm4che2ConvertUtils;
import com.serphydose.ssde.utils.SsdeUtils;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class ExportUtils {

	/**
	 * Generates the atlas to export
	 *
	 * @param tabPane
	 * @throws Exception
	 */
	public static boolean generateAtlas(TabPane tabPane, String filePath, String version) throws Exception {
		if (Main.CUSTOM_OUTPUT_FORMAT) {
			return generateCustomFormatAtlas(tabPane, filePath, version);
		} else {
			return generateCerebroAtlas(tabPane, filePath, version);
		}
	}

	/**
	 * Generates an atlas
	 * @param tabPane contains the tabs representing the studies
	 * @param filePath where to write the atlas file
	 * @param version of the atlas
	 * @return true is data exported correctly or false
	 * @throws Exception
	 */
	private static boolean generateCerebroAtlas(TabPane tabPane, String filePath, String version) throws Exception {
		Atlas atlas = new Atlas();
		atlas.setVersion(version);
		boolean isExportOK = false;

		for (Tab tab : tabPane.getTabs()) {
			Node exportCheckBox = tab.getContent().lookup("CheckBox");
			if (exportCheckBox != null && ((CheckBox) exportCheckBox).isSelected()) {
				BorderPane tabContentPane = (BorderPane) tab.getContent();
				String refAcqFolderPath = ((Text) tabContentPane.getBottom()).getText();

				String refAcqJson = FileUtils.readFile(refAcqFolderPath + "/referenceAcquisition.json");
				ReferenceAcquisition referenceAcquisition = ReferenceAcquisition.fromJson(refAcqJson);

				List<com.ge.health.services.dose.dosewatch.cerebro.beans.Landmark> cerebroLandmarkList = getCerebroLandmarksList(
						refAcqFolderPath, referenceAcquisition);

				CerebroPatient cpe = SignatureEngine.createCerebroPatientElement(
						CerebroUtils.getSignatureContextFromReferenceAcquisition(referenceAcquisition), cerebroLandmarkList);

				CerebroAtlasElement cae = new CerebroAtlasElement();
				cae.setCerebroSignature(cpe.getCerebroSignature());
				cae.setMarkers(cerebroLandmarkList);
				cae.setReferenceAcquisitionUid(referenceAcquisition.getId());
				cae.setStudyId(referenceAcquisition.getStudyId());

				atlas.getCerebroAtlasElements().add(cae);

				isExportOK = true;
			}
		}

		if (isExportOK) {
			File atlasFile = new File(filePath + "/" + version + ".json");
			if (!atlasFile.exists()) {
				atlasFile.createNewFile();
			}

			FileWriter fileWriter = new FileWriter(atlasFile);
			fileWriter.write(Atlas.generate(atlas));
			fileWriter.flush();
			fileWriter.close();
		}

		return isExportOK;
	}

	/**
	 * Extracts the list of landmarks from the ref acq files
	 * @param refAcqFolderPath folder containing all the files
	 * @param referenceAcquisition the reference acquisition
	 * @return the landmarks list
	 * @throws Exception
	 */
	private static List<Landmark> getCerebroLandmarksList(
			String refAcqFolderPath, ReferenceAcquisition referenceAcquisition)
			throws Exception {
		List<Marker> markerList = new ArrayList<>();
		File markersFile = new File(refAcqFolderPath + "/markers.json");
		if (markersFile.exists()) {
			markerList = Marker.listFromJson(FileUtils.readFile(markersFile.getAbsolutePath()));
		}

		List<Landmark> cerebroLandmarkList = new ArrayList<>();

		if (!markerList.isEmpty()) {
			for (int i = 0; i < markerList.size(); i++) {
				Marker marker = markerList.get(i);
				Landmark cerebroLandmark;
				if (i == 0) {
					cerebroLandmark = new Landmark(marker.getTopRegion(), new Integer(0), referenceAcquisition.getPixelSpacingY());
					cerebroLandmarkList.add(cerebroLandmark);
				}

				cerebroLandmark = new Landmark(marker.getBottomRegion(),Integer.valueOf(marker.getSelectedPosition().intValue()), referenceAcquisition.getPixelSpacingY());
				cerebroLandmarkList.add(cerebroLandmark);
			}
		} else {
			for (Landmark landmark : referenceAcquisition.getLandmarkList()) {
				Landmark cerebroLandmark = new Landmark(landmark.getAnatomicalRegion(), landmark.getDistanceFromImageTopInPixels(), referenceAcquisition.getPixelSpacingY());
				cerebroLandmarkList.add(cerebroLandmark);
			}
		}
		return cerebroLandmarkList;
	}

	/**
	 * Generates a custom format atlas
	 * @param tabPane contains the tabs representing the studies
	 * @param filePath where to write the atlas file
	 * @param version of the atlas
	 * @return true is data exported correctly or false
	 * @throws Exception
	 */
	private static boolean generateCustomFormatAtlas(TabPane tabPane, String filePath, String version) throws Exception {
		for (Tab tab : tabPane.getTabs()) {
			Node exportCheckBox = tab.getContent().lookup("CheckBox");
			if (exportCheckBox != null && ((CheckBox) exportCheckBox).isSelected()) {
				BorderPane tabContentPane = (BorderPane) tab.getContent();
				String refAcqFolderPath = ((Text) tabContentPane.getBottom()).getText();

				String refAcqJson = FileUtils.readFile(refAcqFolderPath + "/referenceAcquisition.json");
				ReferenceAcquisition referenceAcquisition = ReferenceAcquisition.fromJson(refAcqJson);
				byte[] latData = org.apache.commons.io.FileUtils.readFileToByteArray(new File(refAcqFolderPath, "lat.dcm"));
				referenceAcquisition.setLatLocalizer(latData);

				DicomObject dicom = Dcm4che2ConvertUtils.loadDICOM(referenceAcquisition.getLatLocalizer());
				PixelSpacing pixelSpacing = PixelSpacing.fromDicomObject(dicom);
				referenceAcquisition.setPixelSpacingY(pixelSpacing.getY());

				List<com.ge.health.services.dose.dosewatch.cerebro.beans.Landmark> cerebroLandmarkList = getCerebroLandmarksList(
						refAcqFolderPath, referenceAcquisition);

				if (cerebroLandmarkList.isEmpty()){
					continue;
				}

				JsonObject object = new JsonObject();
				JsonObject header = new JsonObject();
				JsonArray data = new JsonArray();
				JsonArray markers = new JsonArray();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();


				ImagePlus image = SsdeUtils.getImage(referenceAcquisition.getLatLocalizer(), "Localizer Image");

				// construct header
				header.addProperty("studyID", referenceAcquisition.getStudyId());
				header.addProperty("height", image.getHeight());
				header.addProperty("width", image.getWidth());
				header.add("pixelSpacing", gson.toJsonTree(pixelSpacing));
				header.addProperty("category", version);

				for (Landmark landmark : cerebroLandmarkList) {
					markers.add(gson.toJsonTree(landmark));
				}
				header.add("markers", markers);

				// construct data
				ImageProcessor imp = image.getProcessor();
				for (int i = 0; i < image.getHeight() - 1; i++) {
					for (int j = 0; j < image.getWidth() - 1; j++) {
						float p = imp.getPixelValue(j, i);
						JsonPrimitive primitive = new JsonPrimitive(p);
						data.add(primitive);
					}
				}

				object.add("header", header);
				object.add("data", data);

				File exportFile = new File(filePath, referenceAcquisition.getStudyId() + ".json");
				org.apache.commons.io.FileUtils.writeStringToFile(exportFile, gson.toJson(object));
			}
		}

		return true;
	}

}
