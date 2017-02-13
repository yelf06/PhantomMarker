package com.ge.health.services.dose.dosewatch.cerebro.phantommarker.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.Main;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.beans.ButtonUserData;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.ExportUtils;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.FileUtils;
import com.ge.health.services.dose.dosewatch.cerebro.phantommarker.utils.UIUtils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@SuppressWarnings({ "restriction", "deprecation" })
public class EventsManager implements EventHandler {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(EventsManager.class.getName());

	/** The ui stage */
	private Stage stage;

	/**
	 * @param stage
	 *            the stage to set
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * @param rootBox
	 * @param stage
	 */
	public EventsManager(Stage stage) {
		super();
		this.stage = stage;
	}

	/**
	 * Default method to handle all the events
	 *
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void handle(Event event) {
		if (event.getSource() instanceof MenuItem) {
			LOGGER.debug("Handling menu action");
			handleMenuItem(event);
		} else if (event.getSource() instanceof Pane) {
			LOGGER.debug("Handling image action");
			handleImagePane(event);
		} else if (event.getSource() instanceof Button) {
			LOGGER.debug("Handling button action");
			handleButton(event);
		}
	}

	/**
	 * Handles delete and reset buttons actions
	 *
	 * @param event
	 */
	private void handleButton(Event event) {
		Button button = (Button) event.getSource();
		ButtonUserData buttonUserData = (ButtonUserData) button.getUserData();

		Set<Node> nodeSet = button.getScene().getRoot().lookupAll("TabPane");

		if (nodeSet != null && nodeSet.size() > 0) {
			Optional<Node> firstNodeOptional = nodeSet.stream().findFirst();

			if (firstNodeOptional.isPresent()) {
				TabPane tabPane = (TabPane) firstNodeOptional.get();

				Node imageNode = tabPane.getSelectionModel().getSelectedItem().getContent().lookup("Pane");

				if (imageNode != null) {
					Pane imagePane = (Pane) imageNode;

					if (button.getText().equals("Delete")) {
						UIUtils.deleteLandmark(imagePane, buttonUserData.getTopRegion(), buttonUserData.getBottomRegion());

						UIUtils.manageAllTabsButtonsState(tabPane);

						Label selectedLabel = (Label) ((GridPane) button.getParent()).getChildren().get(1);
						selectedLabel.setText("Selected pos. :");
					} else if (button.getText().equals("Reset")) {
						Label readLabel = (Label) ((GridPane) button.getParent()).getChildren().get(0);

						String[] readLabelParts = readLabel.getText().split(" : ");
						if (readLabelParts.length > 1 && readLabelParts[1] != "") {
							if (UIUtils.checkLandmarkValidity(imagePane, buttonUserData,
									Double.valueOf(readLabelParts[1]).doubleValue())) {
								// Delete landmark before redraw
								UIUtils.deleteLandmark(imagePane, buttonUserData.getTopRegion(), buttonUserData.getBottomRegion());
								// Redraw deleted landmark
								UIUtils.drawRegionsOnImage(imagePane, buttonUserData.getTopRegion(),
										buttonUserData.getBottomRegion(), Double.valueOf(readLabelParts[1]).doubleValue());

								Label selectedLabel = (Label) ((GridPane) button.getParent()).getChildren().get(1);
								selectedLabel.setText("Selected pos. :");
							}
						} else {
							Dialogs.create().owner(stage).title("Error").message("The landmark cannot be reset")
									.showError();
						}
					}

					UIUtils.manageButtonsState(imagePane);
				}
			}
		}
	}

	/**
	 * Handle clicks on the image
	 *
	 * @param event
	 *            the event
	 */
	private void handleImagePane(Event event) {
		Pane pane = (Pane) event.getSource();
		Set<Node> nodeSet = ((BorderPane) pane.getParent()).getRight().lookupAll("ToggleButton");

		for (Node node : nodeSet) {
			ToggleButton toggleButton = (ToggleButton) node;

			if (toggleButton.isSelected()) {
				LOGGER.debug(toggleButton.getText() + " is selected");

				if (UIUtils.checkLandmarkValidity(pane, (ButtonUserData) toggleButton.getUserData(),
						((MouseEvent) event).getY())) {
					UIUtils.drawRegionsOnImage(pane, ((ButtonUserData) toggleButton.getUserData()).getTopRegion(),
							((ButtonUserData) toggleButton.getUserData()).getBottomRegion(),
							((MouseEvent) event).getY());
					UIUtils.manageButtonsState(pane);

					Label selectedLabel = (Label) ((GridPane) toggleButton.getParent()).getChildren().get(1);
					selectedLabel.setText("Selected pos. : ".concat(String.valueOf(((MouseEvent) event).getY())));

					toggleButton.setSelected(false);
					break;
				} else {
					LOGGER.debug("Erreur");
					toggleButton.setSelected(false);
				}
			}
		}
	}

	/**
	 * Handle action on menu item
	 * @param menuItem the object to handle
	 */
	private void handleMenuItem(Event event) {
		MenuItem menuItem = (MenuItem) event.getSource();
		if (menuItem.getText().equals("Load")) {
			File selectedDirectory = FileUtils.getSelectedFolderFromChooser(stage);

			if (selectedDirectory == null) {
				LOGGER.debug("A folder should be selected");
			} else {
				ScrollPane scrollPane = UIUtils.loadUIFromDataFiles(Arrays.asList(selectedDirectory.listFiles()), stage);

				menuItem.setDisable(true);
				menuItem.getParentMenu().getItems().get(1).setDisable(false);
				menuItem.getParentMenu().getItems().get(2).setDisable(false);

				((VBox)stage.getScene().getRoot()).getChildren().add(scrollPane);
			}
		} else if (menuItem.getText().equals("Unload")) {
			((VBox)stage.getScene().getRoot()).getChildren().clear();
			UIUtils.initMenuBar(stage);
		} else if (menuItem.getText().equals("Export")) {
			Action response = Dialogs.create()
					.owner(stage)
					.title("Data export")
					.message("Do you want to export the data ?")
					.showConfirm();

			if (response == Dialog.ACTION_YES) {
				Optional<String> versionResponse;
				if (Main.CUSTOM_OUTPUT_FORMAT) {
					versionResponse = Dialogs.create()
							.owner(stage)
							.title("Data category")
							.message("Please enter the data category")
							.showTextInput("D750");
				} else {

					versionResponse = Dialogs.create()
							.owner(stage)
							.title("Atlas version")
							.message("Please enter the atlas version")
							.showTextInput("0.0.0");
				}
				versionResponse.ifPresent(version -> {
					File selectedDirectory = FileUtils.getSelectedFolderFromChooser(stage);

					if (selectedDirectory == null) {
						Dialogs.create()
							.owner(stage)
							.title("Error")
							.message("No destination folder have been selected")
							.showError();
					} else {
						Set<Node> nodeSet = stage.getScene().getRoot().lookupAll("TabPane");
						if (nodeSet != null && nodeSet.size() > 0) {
							nodeSet.forEach(node -> {
								try {
									if (ExportUtils.generateAtlas((TabPane)node, selectedDirectory.getAbsolutePath(), version)) {
										if (Main.CUSTOM_OUTPUT_FORMAT) {
											Dialogs.create()
													.owner(stage)
													.title("Data exported")
													.message("Data have been exported to the folder : "
															+ selectedDirectory.getAbsolutePath())
													.showInformation();
										} else {
											Dialogs.create()
													.owner(stage)
													.title("Data exported")
													.message("Data have been exported to chosen "
															+ selectedDirectory.getAbsolutePath() + "\\" + version + ".json")
													.showInformation();
										}
									} else {
										Dialogs.create()
									        .owner(stage)
									        .title("No data exported")
									        .message("No reference acquisition has been selected to be exported")
									        .showWarning();
									}

								} catch (Exception e) {
									LOGGER.error("An error occured while generating the atlas", e);
									Dialogs.create()
								        .owner(stage)
								        .title("Exception Dialog")
								        .masthead("An exception occured")
								        .message("An error occured while generating the atlas")
								        .showException(e);
								}
							});
						}
					}
				});
			}
		} else {
			System.exit(0);
		}
	}
}
