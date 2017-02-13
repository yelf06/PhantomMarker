package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class FileUtils {
	
	/**
	 * Reads a file to a string
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String fileName) throws IOException {
		int len;
		char[] chr = new char[4096];
		final StringBuffer buffer = new StringBuffer();
		final FileReader reader = new FileReader(fileName);
		try {
			while ((len = reader.read(chr)) > 0) {
				buffer.append(chr, 0, len);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}
	
	/**
	 * Method to show the directory chooser
	 * 
	 * @param stage
	 *            the app stage
	 * @return
	 */
	public static File getSelectedFolderFromChooser(Stage stage) {
		File rootDirectory = new File("C:/");

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(rootDirectory);
		return directoryChooser.showDialog(stage);
	}

}
