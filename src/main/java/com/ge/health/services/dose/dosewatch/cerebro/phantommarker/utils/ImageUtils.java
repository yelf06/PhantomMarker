package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serphydose.serphycommons.link.exceptions.DicomDecompressorException;
import com.serphydose.serphycommons.link.utils.DicomDecompressor;

import ij.ImagePlus;
import ij.plugin.DICOM;

public class ImageUtils {
	
	/** LOGGER */
	private static Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

	/**
	 * Extract an image from the dcm file
	 * 
	 * @param bytes
	 *            dcm file in bytes
	 * @param name
	 *            name of the file
	 * @return the image
	 * @throws Exception
	 */
	public static ImagePlus getImage(byte[] bytes, String name) throws Exception {
		boolean imageCompressed = false;
		DICOM dicomImage = null;

		try {
			dicomImage = new DICOM(new BufferedInputStream(new ByteArrayInputStream(bytes)));
			dicomImage.run("DICOM");
			((ImagePlus) dicomImage).getProcessor().getPixel(0, 0);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			imageCompressed = true;
		}

		if (imageCompressed) {
			LOGGER.debug("The image is compressed, we will try to decompress it");
			byte[] result = null;
			try {
				File file = new File(UIUtils.class.getClassLoader().getResource("dcmtk/dcmdjpeg.exe").getFile());
				DicomDecompressor dicomDecompressor = new DicomDecompressor(file.getAbsolutePath());
				result = dicomDecompressor.decompressDicom(bytes);
			} catch (DicomDecompressorException ex) {
				throw new Exception(ex);
			}
			if (result != null && result.length > 0) {
				LOGGER.debug("Decompressed image is " + result.length + " byte length");
				dicomImage = new DICOM(new BufferedInputStream(new ByteArrayInputStream(result)));
			}
			dicomImage.run(name);
		}
		
		LOGGER.debug("Image height : " + ((ImagePlus)dicomImage).getHeight());

		return (ImagePlus) dicomImage;
	}
}
