package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.health.services.dose.dosewatch.cerebro.beans.Landmark;
import com.ge.health.services.dose.dosewatch.cerebro.beans.ReferenceAcquisition;
import com.ge.health.services.dose.dosewatch.cerebro.enums.AnatomicalRegion;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.Main;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.ButtonUserData;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.CustomRectangle;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.Marker;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.RawFileInfo;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.controller.EventsManager;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.comparator.CustomRectangleComparator;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.comparator.LandmarkComparator;
import com.ge.health.services.dose.dosewatch.xcat.beans.XCat;
import com.ge.health.services.dose.dosewatch.xcat.factory.XCatFactory;
import com.google.gson.JsonArray;

import ij.ImagePlus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utils class to manage all the static methods used in the application
 * @SuppressWarnings("restriction") is used because the javafx use is restricted in
 * the 1.8.0_25 jdk version. It's present by default beginning from the 1.8.0_40 jdk version
 * @author Yacine
 *
 */
@SuppressWarnings("restriction")
public class UIUtils {

	/** LOGGER */
	private static Logger LOGGER = LoggerFactory.getLogger(UIUtils.class);

	/**
	 * Signature generator menu bar construction
	 *
	 * @param stage
	 *            the signator generator stage
	 * @return the newly constructed menu bar
	 */
	public static void initMenuBar(Stage stage) {
		MenuBar bar = new MenuBar();
		bar.setStyle("-fx-background-color: #A9A9A9; -fx-border-color: black;  -fx-border-width: 0.5;");

		Menu fileMenu = new Menu(Constants.MENU_ITEM_FILE);
		MenuItem loadMenu = new MenuItem(Constants.MENU_ITEM_LOAD);
		loadMenu.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
		loadMenu.setOnAction(new EventsManager(stage));
		fileMenu.getItems().add(loadMenu);

		MenuItem unloadMenu = new MenuItem(Constants.MENU_ITEM_UNLOAD);
		unloadMenu.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
		unloadMenu.setOnAction(new EventsManager(stage));
		unloadMenu.setDisable(true);
		fileMenu.getItems().add(unloadMenu);

		MenuItem exportMenu = new MenuItem(Constants.MENU_ITEM_EXPORT);
		exportMenu.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
		exportMenu.setOnAction(new EventsManager(stage));
		exportMenu.setDisable(true);
		fileMenu.getItems().add(exportMenu);

		MenuItem exitItem = new MenuItem(Constants.MENU_ITEM_EXIT);
		exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		exitItem.setOnAction(new EventsManager(stage));
		fileMenu.getItems().add(exitItem);

		bar.getMenus().add(fileMenu);

		((VBox) stage.getScene().getRoot()).getChildren().add(bar);
	}

	/**
	 * Initializing tabs using the files in the selected directory
	 *
	 * @param studiesFileList
	 *            the file list to load
	 * @throws Exception
	 * @throws IOException
	 */
	public static ScrollPane loadUIFromDataFiles(List<File> studiesFileList, Stage stage) {
		ScrollPane scrollPane = initScrollPane(stage);
		TabPane tabPane = (TabPane) scrollPane.getContent();

		List<Tab> tabList = new ArrayList<Tab>();
		for (File f : studiesFileList) {
			if (f.isDirectory()) {
				LOGGER.debug("Directory : " + f.getAbsolutePath());
				File refAcqJson = null;
				File markersJson = null;
				File latDcm = null;
				RawFileInfo rawFileInfo = null;
				for (File file : Arrays.asList(f.listFiles())) {
					if (Constants.REFERENCE_ACQUISITION_FILE_NAME.equals(file.getName())) {
						refAcqJson = file;
					} else if (Constants.MARKERS_FILE_NAME.equals(file.getName())) {
						markersJson = file;
					} else if (Constants.LAT_FILE_NAME.equals(file.getName())) {
						latDcm = file;
					} else if (Constants.RAW_FILE_EXTENSION.equals(FilenameUtils.getExtension(file.getName()))) {
						initModalForRawImage(file, tabPane, stage);
						break;
					}
				}

				Tab newTab = null;

				if (rawFileInfo == null && refAcqJson != null) {
					newTab = initTab(refAcqJson, latDcm, markersJson, tabPane, stage);
				}

				if (newTab != null) {
					tabList.add(newTab);
				}
			}
		}

		tabPane.getTabs().addAll(tabList);

		manageAllTabsButtonsState(tabPane);

		return scrollPane;
	}

	/**
	 * Building a tab using data in the reference acquisition and the lat files
	 *
	 * @param refAcqJson
	 *            the reference acquision file
	 * @param latDcm
	 *            the lat file
	 * @param markersJson
	 *            the markers file
	 * @return new tab to add to the tabpane in the scene
	 * @throws Exception
	 */
	private static Tab initTab(File refAcqJson, File latDcm, File markersJson, TabPane tabPane, Stage stage) {
		try {
			ReferenceAcquisition refAcq = ReferenceAcquisition.fromJson(FileUtils.readFile(refAcqJson.getAbsolutePath()));
			List<Marker> markerList = null;
			if (markersJson != null) {
				markerList = Marker.listFromJson(FileUtils.readFile(markersJson.getAbsolutePath()));
			}

			BorderPane tabContentPane = new BorderPane();
			tabContentPane.setTop(initTabHeader(refAcq, stage));
			tabContentPane.setCenter(initImagePane(refAcq, markerList, latDcm, tabPane, stage));
			tabContentPane.setRight(initMarkingManagementPane(refAcq, markerList, stage));

			Text text = new Text(new String(refAcqJson.getParentFile().getAbsolutePath()));
			text.setWrappingWidth(500);
			text.setVisible(false);
			tabContentPane.setBottom(text);

			Tab tab = new Tab();
			tab.setContent(tabContentPane);
			Label l = new Label(refAcq.getStudyId() + " - " + refAcq.getSerieNumber());
			l.setRotate(90);
			l.setMinSize(200, 40);
			tab.setGraphic(l);
			tab.setTooltip(new Tooltip(l.getText()));

			return tab;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Building a tab using a raw file
	 *
	 * @param xCatRawFile
	 *            the image file
	 * @return new tab to add to the tabpane in the scene
	 * @throws IOException
	 */
	private static Tab initTab(File xCatRawFile, RawFileInfo rawFileInfo, Stage stage) {
		try {
			String xCatCode = xCatRawFile.getName().split("_")[0];
			XCatFactory xCatFactory = new XCatFactory("");
			XCat xCat = xCatFactory.getXCatByCode(xCatCode);

			if(xCat == null) {
				return null;
			}

			Integer xdim = xCat.getxCatInfo().getXdim();
			Integer ydim = xCat.getxCatInfo().getYdim();
			Integer zdim = xCat.getxCatInfo().getZdim();

			BufferedImage im = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_ARGB);

			int[][][] voxelTab = xCat.getVoxelArray();
			for (int irow = 0; irow < ydim; irow++) {
				for (int icol = 0; icol < xdim; icol++) {
					int currentOrg = voxelTab[icol][irow][zdim / 2];
					int redLevel;
					int greenLevel;
					int blueLevel;
					if (voxelTab[icol][irow][zdim / 2] == 6) {
						redLevel = 255;
						greenLevel = 255;
						blueLevel = 102;
					} else {
						redLevel = currentOrg * 3;
						greenLevel = currentOrg * 3;
						blueLevel = currentOrg * 3;
					}

					im.setRGB(icol, irow, new java.awt.Color(redLevel, greenLevel, blueLevel).getRGB());
				}
			}

			ImageView imgView = new ImageView((SwingFXUtils.toFXImage(im, null)));
			imgView.setOnMouseClicked(new EventsManager(stage));

			BackgroundImage backgroundImage = new BackgroundImage(imgView.getImage(), BackgroundRepeat.SPACE,
					BackgroundRepeat.SPACE, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			Background back = new Background(new BackgroundImage[] { backgroundImage });

			Pane pane = new Pane();
			pane.setMaxSize(imgView.getImage().getWidth(), imgView.getImage().getHeight());
			pane.setId(Constants.IMAGE_PANE_ID);
			pane.setOnMouseClicked(new EventsManager(stage));
			pane.setBackground(back);
			pane.setOnScroll(new EventsManager(stage));

			BorderPane.setAlignment(pane, Pos.TOP_CENTER);
			BorderPane tabContentPane = new BorderPane();
			tabContentPane.setCenter(pane);

			tabContentPane.setRight(initMarkingManagementPane(null, null, stage));
			tabContentPane.setTop(initTabHeader(null, stage));

			Text text = new Text(new String(xCatRawFile.getParentFile().getAbsolutePath()));
			text.setWrappingWidth(500);
			text.setVisible(false);
			tabContentPane.setBottom(text);

			Tab tab = new Tab();
			tab.setContent(tabContentPane);
			Label l = new Label(xCatRawFile.getName());
			l.setRotate(90);
			l.setMinSize(200, 40);
			tab.setGraphic(l);
			tab.setTooltip(new Tooltip(l.getText()));

			return tab;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Initialize a tab for a xcat raw image file
	 * @param rawImageFile the xcat file
	 * @param tabPane the tabpane that contains all the app tabs
	 * @param stage
	 */
	private static void initModalForRawImage(File rawImageFile, TabPane tabPane, Stage stage) {
		Stage newStage = new Stage();
		BorderPane modalWindowContent = new BorderPane();

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField imageHeight = new TextField();
		imageHeight.setPromptText(Constants.XCAT_HEIGHT);
		TextField imageWidth = new TextField();
		imageWidth.setPromptText(Constants.XCAT_WIDTH);
		TextField nbImages = new TextField();
		nbImages.setPromptText(Constants.XCAT_NB_IMAGES);
		Button okButton = new Button(Constants.OK_BUTTON_LABEL);
		okButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		imageHeight.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue.booleanValue() && !newValue.booleanValue()) {
					if (imageHeight.getText().isEmpty()) {
						imageHeight.setStyle("-fx-text-box-border:red;-fx-focus-color:red;");
					} else {
						imageHeight.setStyle("");
					}
				}
			}
		});

		imageHeight.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.isEmpty()) {
					try {
						new BigInteger(newValue);
					} catch (NumberFormatException e) {
						LOGGER.error("Format error", e);
						imageHeight.setText(oldValue);
					}
				}

				if (!imageHeight.getText().isEmpty() && !imageWidth.getText().isEmpty()
						&& !nbImages.getText().isEmpty()) {
					okButton.setDisable(false);
				} else {
					okButton.setDisable(true);
				}
			}
		});

		imageWidth.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue.booleanValue() && !newValue.booleanValue()) {
					if (imageWidth.getText().isEmpty()) {
						imageWidth.setStyle("-fx-text-box-border:red;-fx-focus-color:red;");
					} else {
						imageWidth.setStyle("");
					}
				}
			}
		});

		imageWidth.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.isEmpty()) {
					try {
						new BigInteger(newValue);
					} catch (NumberFormatException e) {
						LOGGER.error("Format error", e);
						imageWidth.setText(oldValue);
					}
				}

				if (!imageHeight.getText().isEmpty() && !imageWidth.getText().isEmpty()
						&& !nbImages.getText().isEmpty()) {
					okButton.setDisable(false);
				} else {
					okButton.setDisable(true);
				}
			}
		});

		nbImages.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue.booleanValue() && !newValue.booleanValue()) {
					if (nbImages.getText().isEmpty()) {
						nbImages.setStyle("-fx-text-box-border:red;-fx-focus-color:red;");
					} else {
						nbImages.setStyle("");
					}
				}
			}
		});

		nbImages.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.isEmpty()) {
					try {
						new BigInteger(newValue);
					} catch (NumberFormatException e) {
						LOGGER.error("Format error", e);
						nbImages.setText(oldValue);
					}
				}

				if (!imageHeight.getText().isEmpty() && !imageWidth.getText().isEmpty()
						&& !nbImages.getText().isEmpty()) {
					okButton.setDisable(false);
				} else {
					okButton.setDisable(true);
				}
			}
		});

		okButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				RawFileInfo rawFileInfo = new RawFileInfo();
				rawFileInfo.setImageHeight(new Integer(imageHeight.getText()).intValue());
				rawFileInfo.setImageWidth(new Integer(imageWidth.getText()).intValue());
				rawFileInfo.setNumberOfImages(new Integer(nbImages.getText()).intValue());
				Tab tab = initTab(rawImageFile, rawFileInfo, stage);
				if (tab != null) {
					tabPane.getTabs().add(tab);
				}
				newStage.close();
			}
		});

		Button cancelButton = new Button(Constants.CANCEL_BUTTON_LABEL);
		cancelButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				newStage.close();
			}
		});

		grid.add(new Label(Constants.XCAT_HEIGHT), 0, 0);
		grid.add(imageHeight, 1, 0);
		grid.add(new Label(Constants.XCAT_WIDTH), 0, 1);
		grid.add(imageWidth, 1, 1);
		grid.add(new Label(Constants.XCAT_NB_IMAGES), 0, 2);
		grid.add(nbImages, 1, 2);
		grid.add(okButton, 0, 3);
		grid.add(cancelButton, 1, 3);

		newStage.setScene(new Scene(modalWindowContent));
		newStage.initModality(Modality.WINDOW_MODAL);
		newStage.initOwner(stage.getScene().getWindow());

		modalWindowContent.setCenter(grid);

		newStage.showAndWait();
	}

	/**
	 * Initializing the UI to draw under the menu
	 *
	 * @return
	 */
	private static ScrollPane initScrollPane(Stage stage) {
		TabPane tabPane = new TabPane();
		tabPane.setSide(Side.LEFT);
		tabPane.setTabMinHeight(250);
		tabPane.setTabMaxWidth(15);

		tabPane.setStyle(
				"-fx-border-color: black;  -fx-border-width: 1; -fx-open-tab-animation: grow; -fx-background-color: #A9A9A9;");
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			if (oldTab != null && newTab != null) {
				try {
					saveCurrentSerieMarkers(oldTab);
				} catch (Exception e) {
					LOGGER.error("An error occured while saving the markers", e);
					Dialogs.create().owner(stage).title("Exception Dialog")
							.message("An error occured while generating the atlas").showException(e);
				}
			}
		});

		// bind to take available space
		ScrollPane scrollPane = new ScrollPane(tabPane);
		scrollPane.setStyle("-fx-background-color: #A9A9A9");
		scrollPane.setPrefHeight(stage.getHeight());

		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		return scrollPane;
	}

	/**
	 * Creating a titled pane that contains buttons to mark up body parts
	 *
	 * @param landmarkPosition
	 *            the landmark's position
	 * @return the titled pane
	 */
	private static VBox initMarkingManagementPane(ReferenceAcquisition refAcq, List<Marker> markerlist, Stage stage) {
		VBox rightBox = new VBox();

		ToggleGroup toggleGroup = new ToggleGroup();
		for (int i = 1; i < AnatomicalRegion.values().length; i++) {
			AnatomicalRegion currAnatomicalRegion = AnatomicalRegion.values()[i];
			AnatomicalRegion prevAnatomicalRegion = AnatomicalRegion.values()[i - 1];

			Landmark currentLandmark = null;
			if (refAcq != null) {
				for (Landmark landmarkPosition : refAcq.getLandmarkList()) {
					if (landmarkPosition.getAnatomicalRegion().equals(currAnatomicalRegion)) {
						currentLandmark = landmarkPosition;
						break;
					}
				}
			}

			GridPane buttonsGrid = new GridPane();
			buttonsGrid.setPadding(new Insets(5, 5, 5, 5));
			buttonsGrid.setHgap(30);
			buttonsGrid.setVgap(5);

			Integer landmarkDistanceFromTop = (currentLandmark != null ? currentLandmark.getDistanceFromImageTopInPixels()
					: null);

			Label read = new Label(Constants.READ_POSITION + (landmarkDistanceFromTop != null ? landmarkDistanceFromTop : ""));
			read.setMinWidth(100);
			GridPane.setConstraints(read, 0, 0);

			String markerPosition = "";
			if (markerlist != null) {
				Optional<Marker> markerOpt = markerlist.stream()
						.filter(m -> m.getTopRegion().equals(prevAnatomicalRegion)
								&& m.getBottomRegion().equals(currAnatomicalRegion))
						.findAny();
				markerPosition = markerOpt.isPresent() ? markerOpt.get().getSelectedPosition().toString() : "";
			}

			Label selected = new Label(Constants.SELECTED_POSITION + markerPosition);
			selected.setMinWidth(100);
			GridPane.setConstraints(selected, 2, 0);

			ToggleButton mark = new ToggleButton(Constants.MARK_BUTTON_LABEL);
			mark.setMinWidth(100);
			mark.setOnAction(new EventsManager(stage));
			mark.setUserData(new ButtonUserData(prevAnatomicalRegion, currAnatomicalRegion, landmarkDistanceFromTop));
			mark.setToggleGroup(toggleGroup);
			mark.setDisable((landmarkDistanceFromTop == null ? false : true));
			GridPane.setConstraints(mark, 0, 1);

			Button delete = new Button(Constants.DELETE_BUTTON_LABEL);
			delete.setMinWidth(100);
			delete.setOnAction(new EventsManager(stage));
			delete.setUserData(new ButtonUserData(prevAnatomicalRegion, currAnatomicalRegion, landmarkDistanceFromTop));
			GridPane.setConstraints(delete, 1, 1);

			Button reset = new Button(Constants.RESET_BUTTON_LABEL);
			reset.setMinWidth(100);
			reset.setOnAction(new EventsManager(stage));
			reset.setUserData(new ButtonUserData(prevAnatomicalRegion, currAnatomicalRegion, landmarkDistanceFromTop));
			GridPane.setConstraints(reset, 2, 1);

			buttonsGrid.getChildren().addAll(read, selected, mark, delete, reset);
			buttonsGrid.setStyle("-fx-background-color: #FFFFFF;");

			TitledPane titledPane = new TitledPane(prevAnatomicalRegion.name() + " / " + currAnatomicalRegion.name(),
					buttonsGrid);
			titledPane.setCollapsible(false);
			titledPane.setMinWidth(400);
			titledPane.setStyle("-fx-content-display: top;  " + "-fx-border-insets: 20 15 15 15;"
					+ "-fx-border-color: black;" + "-fx-border-width: 1;" + "-fx-background-color: #A9A9A9;");

			rightBox.getChildren().add(titledPane);
			BorderPane.setAlignment(rightBox, Pos.CENTER);
		}

		VBox warningBox = new VBox();
		warningBox.setPadding(new Insets(10, 10, 10, 10));
		CheckBox refAcqToExport = new CheckBox("Export this reference acquisition");
		warningBox.getChildren().add(refAcqToExport);

		if (refAcq != null && ! Main.CUSTOM_OUTPUT_FORMAT && (refAcq.getAttenuationList() == null || refAcq.getAttenuationList().isEmpty()
				|| refAcq.getDwFirst() == null || refAcq.getDwLast() == null || refAcq.getDwMiddle() == null)) {
			Rectangle rectangle = new Rectangle(100, 30);
			rectangle.setFill(Color.TRANSPARENT);
			warningBox.getChildren().add(rectangle);

			refAcqToExport.setDisable(true);

			HBox hbox = new HBox();
			ImageView im = new ImageView(
					UIUtils.class.getClassLoader().getResource("images/warning.png").toExternalForm());
			hbox.getChildren().add(im);
			Text warningText = new Text(
					"Data loaded in this tab cannot be transformed to an atlas element. It won't be exported");
			warningText.setWrappingWidth(330);
			hbox.getChildren().add(warningText);
			warningBox.getChildren().add(hbox);
		} else {
			refAcqToExport.setSelected(true);
		}

		warningBox.setAlignment(Pos.CENTER);
		rightBox.getChildren().add(warningBox);

		return rightBox;
	}

	/**
	 * Intializes the tab header
	 *
	 * @param refAcq
	 * @param stage
	 * @return
	 */
	private static GridPane initTabHeader(ReferenceAcquisition refAcq, Stage stage) {
		// Initializing top panel with patient infos
		GridPane headerPane = new GridPane();
		headerPane.setPadding(new Insets(5));
		headerPane.setHgap(5);
		headerPane.setVgap(5);
		ColumnConstraints column = new ColumnConstraints(stage.getWidth() / 4);
		column.setHalignment(HPos.CENTER);
		headerPane.getColumnConstraints().addAll(column, column, column);

		Label ageLabel = new Label("Age : " + (refAcq != null ? refAcq.getPatientAge() : ""));
		Label heightLabel = new Label("Height : " + (refAcq != null ? refAcq.getPatientHeight() : ""));
		Label widthLabel = new Label("Weight : " + (refAcq != null ? refAcq.getPatientWeight() : ""));
		headerPane.add(ageLabel, 0, 0);
		headerPane.add(heightLabel, 1, 0);
		headerPane.add(widthLabel, 2, 0);
		headerPane.setStyle("-fx-background-color: #A9A9A9;");

		return headerPane;
	}

	/**
	 * Creates the image with the different regions
	 *
	 * @param refAcq
	 *            the reference acquisition
	 * @param latDcm
	 *            the lat dcm file
	 * @param tabPane
	 *            the tabpane
	 * @param stage
	 *            the stage
	 * @return an initialized pane
	 * @throws Exception
	 */
	private static Pane initImagePane(ReferenceAcquisition refAcq, List<Marker> markerList, File latDcm,
			TabPane tabPane, Stage stage) throws Exception {
		ImagePlus imagePlus = ImageUtils.getImage(IOUtils.toByteArray(new FileInputStream(latDcm)), "lat.dcm");
		ImageView view = new ImageView(SwingFXUtils.toFXImage(imagePlus.getBufferedImage(), null));
		view.setPreserveRatio(true);
		view.setFitHeight(tabPane.getHeight());

		Pane pane = new Pane();
		pane.setMaxSize(view.getImage().getWidth(), view.getImage().getHeight());
		pane.setId("ImagePane");

		BackgroundImage backgroundImage = new BackgroundImage(view.getImage(), BackgroundRepeat.SPACE,
				BackgroundRepeat.SPACE, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		Background back = new Background(new BackgroundImage[] { backgroundImage });
		pane.setBackground(back);

		Collections.sort(refAcq.getLandmarkList(), new LandmarkComparator());

		if (markerList != null && !markerList.isEmpty()) {
			pane.getChildren().addAll(
					getCustomRectangleListFromData(new ArrayList<Object>(markerList),
					true,
					view.getImage().getHeight(),
					view.getImage().getWidth()));
		} else {
			pane.getChildren().addAll(
					getCustomRectangleListFromData(new ArrayList<Object>(refAcq.getLandmarkList()),
					false,
					view.getImage().getHeight(),
					view.getImage().getWidth()));
		}


		pane.setOnMouseClicked(new EventsManager(stage));

		BorderPane.setAlignment(pane, Pos.TOP_CENTER);

		return pane;
	}

	/**
	 * Used to mark the anatomical regions on the image
	 *
	 * @param pane
	 *            the pane containing the image
	 * @param buttonUserData
	 *            the button that has been toggled
	 * @param landmarkY
	 *            the click position on the image
	 */
	public static void drawRegionsOnImage(Pane pane, AnatomicalRegion topRegion, AnatomicalRegion bottomRegion,
			double landmarkY) {
		// First click on the image : no regions yet
		if (pane.getChildren().size() == 0) {
			CustomRectangle topRec = new CustomRectangle(0, 0, pane.getWidth(), landmarkY, topRegion);
			topRec.setFill(Color.rgb(topRegion.getColor().getRed(), topRegion.getColor().getGreen(),
					topRegion.getColor().getBlue(), 0.3));

			Tooltip t = new Tooltip(topRec.getAnatomicalRegion().name());
			Tooltip.install(topRec, t);

			CustomRectangle bottomRec = new CustomRectangle(0, topRec.getHeight(), pane.getWidth(),
					pane.getHeight() - landmarkY, bottomRegion);
			bottomRec.setFill(Color.rgb(bottomRegion.getColor().getRed(), bottomRegion.getColor().getGreen(),
					bottomRegion.getColor().getBlue(), 0.3));

			t = new Tooltip(bottomRec.getAnatomicalRegion().name());
			Tooltip.install(bottomRec, t);

			pane.getChildren().addAll(topRec, bottomRec);

			return;
		}

		// Regions can be inserted only from top or down
		CustomRectangle firstRegionInImage = getFirstRegionInImage(pane);
		CustomRectangle lastRegionInImage = getLastRegionInImage(pane);
		CustomRectangle customRectangle = null;

		// The new rectangle must be inserted from top
		if (firstRegionInImage != null && firstRegionInImage.getAnatomicalRegion().equals(bottomRegion)) {
			customRectangle = new CustomRectangle(0, 0, pane.getWidth(), landmarkY, topRegion);
			customRectangle.setFill(Color.rgb(topRegion.getColor().getRed(), topRegion.getColor().getGreen(),
					topRegion.getColor().getBlue(), 0.3));
			Tooltip t = new Tooltip(customRectangle.getAnatomicalRegion().name());
			Tooltip.install(customRectangle, t);

			firstRegionInImage.setHeight(firstRegionInImage.getHeight() + (firstRegionInImage.getY() - landmarkY));
			firstRegionInImage.setY(landmarkY);

			pane.getChildren().add(customRectangle);
		} else if (lastRegionInImage != null && lastRegionInImage.getAnatomicalRegion().equals(topRegion)) {
			// The new rectangle must be inserted from bottom
			customRectangle = new CustomRectangle(0, landmarkY, pane.getWidth(), pane.getHeight() - landmarkY,
					bottomRegion);
			customRectangle.setFill(Color.rgb(bottomRegion.getColor().getRed(), bottomRegion.getColor().getGreen(),
					bottomRegion.getColor().getBlue(), 0.3));
			lastRegionInImage.setHeight(landmarkY - lastRegionInImage.getY());

			Tooltip t = new Tooltip(customRectangle.getAnatomicalRegion().name());
			Tooltip.install(customRectangle, t);
			pane.getChildren().add(customRectangle);
		} else {
			// In this case, regions have been deleted in the middle and there's
			// a blank space
			// where the the two new regions have to be inserted
			double totalRectanglesHeightFromTopToClick = 0;
			double totalRectanglesHeightFromClickToBottom = 0;
			for (Node node : pane.getChildren()) {
				CustomRectangle rectangle = (CustomRectangle) node;

				if (landmarkY > rectangle.getY() + rectangle.getHeight()) {
					totalRectanglesHeightFromTopToClick += rectangle.getHeight();
				} else {
					totalRectanglesHeightFromClickToBottom += rectangle.getHeight();
				}
			}

			CustomRectangle topRegionToInsert = null;
			CustomRectangle bottomRegionToInsert = null;
			for (Node node : pane.getChildren()) {
				CustomRectangle rectangle = (CustomRectangle) node;

				if (rectangle.getAnatomicalRegion().equals(topRegion.prev())
						|| rectangle.getAnatomicalRegion().equals(bottomRegion.next())) {
					topRegionToInsert = new CustomRectangle(0, totalRectanglesHeightFromTopToClick, pane.getWidth(),
							landmarkY - totalRectanglesHeightFromTopToClick, topRegion);
					topRegionToInsert.setFill(Color.rgb(topRegion.getColor().getRed(), topRegion.getColor().getGreen(),
							topRegion.getColor().getBlue(), 0.3));
					Tooltip t = new Tooltip(topRegionToInsert.getAnatomicalRegion().name());
					Tooltip.install(topRegionToInsert, t);

					bottomRegionToInsert = new CustomRectangle(0, landmarkY, pane.getWidth(),
							pane.getHeight() - (landmarkY + totalRectanglesHeightFromClickToBottom), bottomRegion);
					bottomRegionToInsert.setFill(Color.rgb(bottomRegion.getColor().getRed(),
							bottomRegion.getColor().getGreen(), bottomRegion.getColor().getBlue(), 0.3));
					t = new Tooltip(bottomRegionToInsert.getAnatomicalRegion().name());
					Tooltip.install(bottomRegionToInsert, t);

					break;
				}
			}

			if (topRegionToInsert != null && topRegionToInsert.getHeight() > 0) {
				pane.getChildren().addAll(topRegionToInsert);
			}
			if (bottomRegionToInsert != null && bottomRegionToInsert.getHeight() > 0) {
				pane.getChildren().add(bottomRegionToInsert);
			}

			ObservableList<Node> collectionToSort = FXCollections.observableArrayList(pane.getChildren());
			Collections.sort(collectionToSort, new CustomRectangleComparator());
		}
	}

	/**
	 * Used to manage mark buttons state
	 *
	 * @param pane
	 *            the image container
	 */
	public static void manageButtonsState(Pane pane) {
		Set<Node> titlePaneList = ((BorderPane) pane.getParent()).getRight().lookupAll("TitledPane");

		for (Node node : titlePaneList) {
			TitledPane titledPane = (TitledPane) node;

			Set<Node> toggleButtonSet = titledPane.getContent().lookupAll("ToggleButton");

			for (Node buttonNode : toggleButtonSet) {
				ToggleButton toggleButton = (ToggleButton) buttonNode;
				ButtonUserData buttonUserData = (ButtonUserData) toggleButton.getUserData();

				// DELETE BUTTON
				Button deleteButton = ((Button) ((GridPane) toggleButton.getParent()).getChildren().get(3));

				// MARK BUTTON
				if (pane.getChildren().isEmpty()) {
					toggleButton.setDisable(false);
					deleteButton.setDisable(true);
				} else if (doesImageContainRegion(pane, buttonUserData.getTopRegion())
						&& doesImageContainRegion(pane, buttonUserData.getBottomRegion())) {
					toggleButton.setDisable(true);
				} else if (!doesImageContainRegion(pane, buttonUserData.getTopRegion())
						&& !doesImageContainRegion(pane, buttonUserData.getBottomRegion())) {
					if (doesImageContainRegion(pane, buttonUserData.getTopRegion().prev())
							&& doesImageContainRegion(pane, buttonUserData.getBottomRegion().next())) {
						toggleButton.setDisable(false);
					} else {
						toggleButton.setDisable(true);
					}
				} else if (doesImageContainRegion(pane, buttonUserData.getTopRegion())
						&& !doesImageContainRegion(pane, buttonUserData.getBottomRegion())) {
					CustomRectangle lastRectangleInImage = getLastRegionInImage(pane);
					if (buttonUserData.getTopRegion().equals(lastRectangleInImage.getAnatomicalRegion())) {
						toggleButton.setDisable(false);
					} else {
						toggleButton.setDisable(true);
					}
				} else if (!doesImageContainRegion(pane, buttonUserData.getTopRegion())
						&& doesImageContainRegion(pane, buttonUserData.getBottomRegion())) {
					CustomRectangle firstRectangleInImage = getFirstRegionInImage(pane);
					if (buttonUserData.getBottomRegion().equals(firstRectangleInImage.getAnatomicalRegion())) {
						toggleButton.setDisable(false);
					} else {
						toggleButton.setDisable(true);
					}
				}

				if (doesImageContainRegion(pane, buttonUserData.getTopRegion())
						|| doesImageContainRegion(pane, buttonUserData.getBottomRegion())) {
					deleteButton.setDisable(false);
				} else {
					deleteButton.setDisable(true);
				}
			}
		}
	}

	/**
	 * Used to manage mark buttons state in all the tabs
	 *
	 * @param tabPane
	 */
	public static void manageAllTabsButtonsState(TabPane tabPane) {
		if (tabPane != null) {
			for (Tab tab : tabPane.getTabs()) {
				manageButtonsState((Pane) ((BorderPane) tab.getContent()).getCenter());
			}
		}
	}

	/**
	 * Checks whether the click position is valid or not
	 *
	 * @param pane
	 * @param buttonUserData
	 * @param landmarkY
	 * @return
	 */
	public static boolean checkLandmarkValidity(Pane pane, ButtonUserData buttonUserData, double landmarkY) {
		if (pane.getChildren().size() == 0) {
			return true;
		} else {
			List<AnatomicalRegion> surrondingRegionList = getSurroundingRegions(pane, landmarkY);
			if (surrondingRegionList.size() == 1) {
				if (surrondingRegionList.get(0) == buttonUserData.getTopRegion()
						|| surrondingRegionList.get(0) == buttonUserData.getBottomRegion()) {
					return true;
				}
			} else if (surrondingRegionList.size() == 2) {
				AnatomicalRegion topRegion = surrondingRegionList.get(0);
				AnatomicalRegion bottomRegion = surrondingRegionList.get(1);
				if (buttonUserData.getTopRegion().prev() == topRegion
						&& buttonUserData.getBottomRegion().next() == bottomRegion) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether the image contains a region or not
	 *
	 * @param pane
	 * @param region
	 * @return
	 */
	private static boolean doesImageContainRegion(Pane pane, AnatomicalRegion region) {
		for (Node node : pane.getChildren()) {
			CustomRectangle rectangle = (CustomRectangle) node;

			if (rectangle.getAnatomicalRegion().equals(region)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the region clicked in the pane
	 *
	 * @param clickPosition
	 * @return the clicked region or null if none
	 */
	public static List<AnatomicalRegion> getSurroundingRegions(Pane pane, double clickPosition) {
		List<AnatomicalRegion> regionList = new ArrayList<>();

		ObservableList<Node> collectionToSort = FXCollections.observableArrayList(pane.getChildren());
		Collections.sort(collectionToSort, new CustomRectangleComparator());

		if (collectionToSort != null && collectionToSort.size() > 0) {
			for (Node node : collectionToSort) {
				CustomRectangle currentRectangle = (CustomRectangle) node;
				AnatomicalRegion currentRegion = currentRectangle.getAnatomicalRegion();

				// If the click is in an existing rectangle
				if (clickPosition > currentRectangle.getY()
						&& clickPosition < currentRectangle.getY() + currentRectangle.getHeight()) {
					regionList.add(currentRegion);
					return regionList;
				}
			}

			Stream<Node> nodeStream = collectionToSort.stream()
					.filter(n -> ((CustomRectangle) n).getY() + ((CustomRectangle) n).getHeight() < clickPosition);

			CustomRectangle lastRectangleFromTop = (CustomRectangle) nodeStream.reduce((a, b) -> b).orElse(null);

			if (lastRectangleFromTop != null) {
				regionList.add(lastRectangleFromTop.getAnatomicalRegion());
			}

			Stream<Node> nodeStream5 = collectionToSort.stream()
					.filter(n -> ((CustomRectangle) n).getY() > clickPosition);

			Optional<Node> firstRectangleToBottomOpt = nodeStream5.findFirst();

			if (firstRectangleToBottomOpt.isPresent()) {
				CustomRectangle customRectangle = (CustomRectangle) firstRectangleToBottomOpt.get();
				regionList.add(customRectangle.getAnatomicalRegion());
			}
		}
		return regionList;
	}

	/**
	 * Deletes the landmark with the two topRegion and bottomRegion
	 *
	 * @param topRegion
	 * @param bottomRegion
	 */
	public static void deleteLandmark(Pane imagePane, AnatomicalRegion topRegion, AnatomicalRegion bottomRegion) {
		// Delete regions
		List<Node> nodesToDeleteList = new ArrayList<Node>();
		imagePane.getChildren().forEach(rectangle -> {
			CustomRectangle customRectangle = (CustomRectangle) rectangle;
			if (customRectangle.getAnatomicalRegion().equals(topRegion)
					|| customRectangle.getAnatomicalRegion().equals(bottomRegion)) {
				// Adding them first to a list to avoid concurrency issues
				nodesToDeleteList.add(rectangle);
			}
		});
		// Regions deletion
		if (nodesToDeleteList.size() > 0) {
			imagePane.getChildren().removeAll(nodesToDeleteList);

			UIUtils.manageButtonsState(imagePane);
		}
	}

	/**
	 * Retrieves the first region in the image or null if none
	 *
	 * @param pane
	 * @return
	 */
	private static CustomRectangle getFirstRegionInImage(Pane pane) {
		ObservableList<Node> collectionToSort = FXCollections.observableArrayList(pane.getChildren());
		Collections.sort(collectionToSort, new CustomRectangleComparator());

		Optional<Node> firstNode = collectionToSort.stream().findFirst();
		if (firstNode != null && firstNode.isPresent()) {
			return (CustomRectangle) firstNode.get();
		}
		return null;
	}

	/**
	 * Retrieves the last region in the image or null if none
	 *
	 * @param pane
	 * @return
	 */
	private static CustomRectangle getLastRegionInImage(Pane pane) {
		ObservableList<Node> collectionToSort = FXCollections.observableArrayList(pane.getChildren());
		Collections.sort(collectionToSort, new CustomRectangleComparator());

		Node lastNode = collectionToSort.stream().reduce((a, b) -> b).orElse(null);

		if (lastNode != null) {
			return (CustomRectangle) lastNode;
		}

		return null;
	}

	/**
	 * Saves the markers selected by the user when he changes the onscreen study
	 *
	 * @param tab
	 *            the old selected tab
	 * @throws IOException
	 */
	private static void saveCurrentSerieMarkers(Tab tab) throws IOException {
		BorderPane tabContent = (BorderPane) tab.getContent();

		Set<Node> nodeSet = tabContent.getRight().lookupAll("GridPane");

		List<Marker> markerList = new ArrayList<>();
		nodeSet.stream().forEach((node) -> {
			GridPane gridPane = (GridPane) node;

			Label selectedLabel = (Label) gridPane.getChildren().get(1);
			String[] values = selectedLabel.getText().split(" : ");

			ToggleButton button = (ToggleButton) gridPane.getChildren().get(2);
			ButtonUserData buttonUserData = (ButtonUserData) button.getUserData();

			if (values.length > 1) {
				markerList.add(new Marker(buttonUserData.getTopRegion(), buttonUserData.getBottomRegion(),
						new Double(values[1])));
			}
		});

		if (!markerList.isEmpty()) {
			JsonArray markersArray = new JsonArray();
			markerList.forEach(marker -> {
				markersArray.add(Marker.toJson(marker));
			});

			Text filePath = (Text) tabContent.getBottom();
			File f = new File(filePath.getText() + "/markers.json");
			if (f.exists()) {
				f.delete();
			} else {
				f.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(f);
			fileWriter.write(markersArray.toString());
			fileWriter.flush();
			fileWriter.close();
		}
	}

	/**
	 * Builds the list of regions to draw on the image
	 *
	 * @param positionList
	 *            the position of the landmarks (landmarks or previously
	 *            selected positions)
	 * @param isMarker
	 *            whether it is a landmark or a previously exported markage
	 * @param imageHeight
	 *            the image height
	 * @param imageWidth
	 *            the image width
	 * @return the list of regions to draw on the image
	 */
	private static List<CustomRectangle> getCustomRectangleListFromData(List<Object> positionList, boolean isMarker,
			double imageHeight, double imageWidth) {
		List<CustomRectangle> regionList = new ArrayList<>();
		Double totalRegionsHeight = Double.valueOf(0);

		if (isMarker) {
			for (int i = 0; i < positionList.size(); i++) {
				Marker marker = (Marker) positionList.get(i);

				double firstRegionToDrawHeight = imageHeight - marker.getSelectedPosition().doubleValue();

				CustomRectangle topRegion = null;
				CustomRectangle bottomRegion = null;

				if (i == positionList.size() - 1) {
					bottomRegion = new CustomRectangle(0, marker.getSelectedPosition().doubleValue(), imageWidth,
							firstRegionToDrawHeight, marker.getBottomRegion());

					bottomRegion.setFill(Color.rgb(marker.getBottomRegion().getColor().getRed(),
							marker.getBottomRegion().getColor().getGreen(),
							marker.getBottomRegion().getColor().getBlue(), 0.3));
				} else {
					Marker nextMarker = (Marker) positionList.get(i + 1);
					if (i == 0) {
						topRegion = new CustomRectangle(0, totalRegionsHeight.doubleValue(), imageWidth,
								marker.getSelectedPosition().doubleValue() - totalRegionsHeight.doubleValue(),
								marker.getTopRegion());

						topRegion.setFill(Color.rgb(marker.getTopRegion().getColor().getRed(),
								marker.getTopRegion().getColor().getGreen(), marker.getTopRegion().getColor().getBlue(),
								0.3));
					}

					bottomRegion = new CustomRectangle(0, marker.getSelectedPosition().doubleValue(), imageWidth,
							nextMarker.getSelectedPosition().doubleValue() - marker.getSelectedPosition().doubleValue(),
							marker.getBottomRegion());

					bottomRegion.setFill(Color.rgb(marker.getBottomRegion().getColor().getRed(),
							marker.getBottomRegion().getColor().getGreen(),
							marker.getBottomRegion().getColor().getBlue(), 0.3));
				}

				if (topRegion != null) {
					regionList.add(topRegion);

					totalRegionsHeight += topRegion.getHeight();
				}

				if (bottomRegion != null) {
					regionList.add(bottomRegion);

					totalRegionsHeight += bottomRegion.getHeight();
				}
			}
		} else {
			for (int i = 0; i < positionList.size(); i++) {
				Landmark landmark = (Landmark) positionList.get(i);

				double rectangleHeight = imageHeight - landmark.getDistanceFromImageTopInPixels().doubleValue();
				if (i + 1 < positionList.size()) {
					rectangleHeight = ((Landmark) positionList.get(i + 1)).getDistanceFromImageTopInPixels().doubleValue()
							- landmark.getDistanceFromImageTopInPixels().doubleValue();
				}

				CustomRectangle rectangle = new CustomRectangle(0, landmark.getDistanceFromImageTopInPixels().doubleValue(),
						imageWidth, rectangleHeight, landmark.getAnatomicalRegion());

				rectangle.setFill(Color.rgb(landmark.getAnatomicalRegion().getColor().getRed(),
						landmark.getAnatomicalRegion().getColor().getGreen(),
						landmark.getAnatomicalRegion().getColor().getBlue(), 0.3));

				regionList.add(rectangle);
			}
		}
		return regionList;
	}
}
