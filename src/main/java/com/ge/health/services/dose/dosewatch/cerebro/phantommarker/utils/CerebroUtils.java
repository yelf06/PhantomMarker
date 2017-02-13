package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.util.ArrayList;

import com.ge.health.services.dose.dosewatch.cerebro.beans.ReferenceAcquisition;
import com.ge.health.services.dose.dosewatch.cerebro.beans.SignatureContext;

public class CerebroUtils {
	
	/**
	 * Converts a reference acquisition to a signature context
	 * 
	 * @param referenceAcquisition
	 *            the ref acq to convert
	 * @return a signature context instance
	 */
	public static SignatureContext getSignatureContextFromReferenceAcquisition(
			ReferenceAcquisition referenceAcquisition) {
		SignatureContext signatureContext = new SignatureContext();

		signatureContext.setAttenuationDistribution(new ArrayList<>());
		referenceAcquisition.getAttenuationList().stream().forEach(attenuation -> {
			signatureContext.getAttenuationDistribution().add(new Integer(attenuation.intValue()));
		});
		signatureContext.setDwFirst(referenceAcquisition.getDwFirst());
		signatureContext.setDwLast(referenceAcquisition.getDwLast());
		signatureContext.setDwMiddle(referenceAcquisition.getDwMiddle());
		signatureContext.setPatientSizeInCm(referenceAcquisition.getPatientHeight());
		signatureContext.setPatientWeightInKg(referenceAcquisition.getPatientWeight());
		signatureContext.setPixelSpacingY(referenceAcquisition.getPixelSpacingY());
		signatureContext.setScannedAreaEndRow(referenceAcquisition.getScannedAreaEndRow());
		signatureContext.setScannedAreaStartRow(referenceAcquisition.getScannedAreaStartRow());
		signatureContext.setSex(referenceAcquisition.getPatientSex());

		return signatureContext;
	}

}
