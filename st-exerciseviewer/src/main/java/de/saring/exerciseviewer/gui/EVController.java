package de.saring.exerciseviewer.gui;

import java.io.IOException;

import de.saring.util.SystemUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import de.saring.exerciseviewer.gui.panels.DiagramPanelController;
import de.saring.exerciseviewer.gui.panels.LapPanelController;
import de.saring.exerciseviewer.gui.panels.MainPanelController;
import de.saring.exerciseviewer.gui.panels.OptionalPanelController;
import de.saring.exerciseviewer.gui.panels.SamplePanelController;
import de.saring.exerciseviewer.gui.panels.TrackPanelController;
import de.saring.util.gui.javafx.FxmlLoader;

/**
 * Main Controller (MVC) class of the ExerciseViewer dialog window.
 *
 * @author Stefan Saring
 */
public class EVController {

    private static final String FXML_FILE = "/fxml/ExerciseViewer.fxml";
    private final EVContext context;

    private final MainPanelController mainPanelController;
    private final OptionalPanelController optionalPanelController;
    private final LapPanelController lapPanelController;
    private final SamplePanelController samplePanelController;
    private final DiagramPanelController diagramPanelController;
    private final TrackPanelController trackPanelController;

    private Stage stage;

    @FXML
    private Tab tabMain;
    @FXML
    private Tab tabOptional;
    @FXML
    private Tab tabLaps;
    @FXML
    private Tab tabSamples;
    @FXML
    private Tab tabDiagram;
    @FXML
    private Tab tabTrack;

    /**
     * Standard c'tor for dependency injection.
     *
     * @param context the ExerciseViewer UI context
     * @param document the ExerciseViewer document / model
     */
    public EVController(final EVContext context, final EVDocument document) {
        this.context = context;

        // manual dependency injection, Guice can't be used here (see comments in EVMain)
        mainPanelController = new MainPanelController(context, document);
        optionalPanelController = new OptionalPanelController(context, document);
        lapPanelController = new LapPanelController(context, document);
        samplePanelController = new SamplePanelController(context, document);
        diagramPanelController = new DiagramPanelController(context, document);
        trackPanelController = new TrackPanelController(context, document);

        mainPanelController.setDiagramPanelController(diagramPanelController);
    }

    /**
     * Initializes and displays the ExerciseViewer dialog.
     *
     * @param stage the Stage to show the dialog in
     */
    public void show(final Stage stage) {
        this.stage = stage;

        // load dialog UI from FXML
        Parent root;
        try {
            root = FxmlLoader.load(EVController.class.getResource(FXML_FILE), context.getResources()
                    .getResourceBundle(), this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the FXML resource '" + FXML_FILE + "'!", e);
        }

        setupPanels();

        // create scene and show dialog
        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();

        // trigger a garbage collection when EV has been closed to avoid allocation of additional heap space
        SystemUtils.triggerGC();
    }

    private void setupPanels() {
        // load and setup main panel immediately, this tab must be visible on startup
        tabMain.setContent(mainPanelController.loadAndSetupPanelContent());

        // load all other panels asynchronously, this reduces the startup time massively
        Platform.runLater(() -> {
            tabOptional.setContent(optionalPanelController.loadAndSetupPanelContent());
            tabLaps.setContent(lapPanelController.loadAndSetupPanelContent());
            tabSamples.setContent(samplePanelController.loadAndSetupPanelContent());
            tabDiagram.setContent(diagramPanelController.loadAndSetupPanelContent());
            tabTrack.setContent(trackPanelController.loadAndSetupPanelContent());

            // display map and exercise track not before the user wants to see it (reduces startup time)
            tabTrack.setOnSelectionChanged(event -> {
                if (tabTrack.isSelected()) {
                    trackPanelController.showMapAndTrack();
                }
            });
        });
    }

    /**
     * Action handler for closing the dialog.
     */
    @FXML
    private void onClose(final ActionEvent event) {
        stage.close();
    }
}
