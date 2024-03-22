/*
 * Copyright 2010-2023 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.Date;
import java.util.logging.Logger;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.modules.Places;

/**
 * This defines the dialog that will be displayed when an Unexpected Error
 * occurs
 *
 * @author OrionsGuardian
 */
public class ErrorReportDialog {

    protected final JFXPanel fxPanel;
    protected JDialog dialog;
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Insets BORDERPANE_PADDING = new Insets(8);
    private static final Insets BUTTONPANE_PADDING = new Insets(4, 4, 4, 4);

    private final CheckBox blockRepeatsCheckbox = new CheckBox("Block all future popups for this exception");
    private final Button showHideButton = new Button("Show Details");
    private final TextArea errorMsgArea;
    private final Label summaryLabel = new Label("");
    private final BorderPane root;
    private final VBox detailsBox;

    protected double mouseOrigX = 0;
    protected double mouseOrigY = 0;
    private ErrorReportEntry currentError = null;
    private boolean showingDetails = false;

    private static final Logger LOGGER = Logger.getLogger(ErrorReportDialog.class.getName());

    /**
     * Construct the Error Report Dialog for a supplied Error Report Entry
     *
     * @param errorEntry
     */
    public ErrorReportDialog(final ErrorReportEntry errorEntry) {
        currentError = errorEntry;
        currentError.setDialog(this);
        fxPanel = new JFXPanel();
        final BoxLayout layout = new BoxLayout(fxPanel, BoxLayout.Y_AXIS);
        fxPanel.setLayout(layout);
        fxPanel.setOpaque(false);
        fxPanel.setBackground(TRANSPARENT);
        final boolean showOccs = errorEntry.getOccurrences() > 1;

        final File userDir = Places.getUserDirectory();
        String logFileLocation = "var/log/";
        if (userDir != null) {
            final File f = new File(userDir, "/var/log"); //NOSONAR
            logFileLocation = f.getAbsolutePath();
        }

        root = new BorderPane();

        root.setPadding(BORDERPANE_PADDING);

        final Label imageLabel = new Label(" \n ");
        final Color errorIconColor;
        switch (errorEntry.getErrorLevel().getName()) {
            case "SEVERE":
                errorIconColor = new Color(215, 95, 95);
                break;
            case "WARNING":
                errorIconColor = new Color(210, 130, 65);
                break;
            case "INFO":
                errorIconColor = new Color(170, 170, 65);
                break;
            case "FINE":
            case "FINER":
            case "FINEST":
                errorIconColor = new Color(50, 160, 160);
                break;
            default:
                errorIconColor = new Color(200, 120, 150);
        }
        final ImageView errorImage = new ImageView(UserInterfaceIconProvider.ERROR.buildImage(36, errorIconColor));
        imageLabel.setGraphic(errorImage);
        imageLabel.setPadding(new Insets(2 + (showOccs ? 4 : 0), 0, 0, 0));
        detailsBox = new VBox();
        root.setTop(detailsBox);

        final BorderPane errorHeadingPane = new BorderPane();
        final TextFlow errorHeadingText = new TextFlow();
        final FlowPane headerSeverityPane = new FlowPane();
        final Label severityDesc = new Label("Error Level: " + errorEntry.getErrorLevel().getName() + " ");
        severityDesc.setPadding(new Insets(0, 0, 0, 0));
        final Label occurrenceDesc = new Label(" " + errorEntry.getOccurrences() + " Occurrences ");
        headerSeverityPane.getChildren().add(severityDesc);

        // Text just needs some better formatting, spacing is good, just wrapping or something
        final Label messageDesc = new Label(errorEntry.getHeading());
        //final Font outputFont = FontUtilities.getOutputFont();
        //messageDesc.setStyle("-fx-font-family: " + outputFont.getFamily());
        messageDesc.setStyle("-fx-font-weight: bold; ");
        
        errorHeadingText.getChildren().add(messageDesc);
        errorHeadingText.setPadding(new Insets(3, 0, 10, 0));

        final BorderPane headingSection = new BorderPane();

        if (showOccs) {
            headerSeverityPane.setPadding(new Insets(4, 0, 0, 0));
            occurrenceDesc.setTooltip(new Tooltip("Repeated Occurrences of this Exception"));
            occurrenceDesc.setStyle("-fx-border-color:#black; -fx-background-color: #333333");
            occurrenceDesc.setTextAlignment(TextAlignment.CENTER);
            occurrenceDesc.setPadding(new Insets(0));
            final BorderPane occBox = new BorderPane();
            occBox.setCenter(occurrenceDesc);
            occBox.setStyle("-fx-background-color: #444444");
            detailsBox.getChildren().add(occBox);
        }

        headingSection.setCenter(headerSeverityPane);
        headingSection.setBottom(errorHeadingText);
        errorHeadingPane.setLeft(imageLabel);
        errorHeadingPane.setCenter(headingSection);

        detailsBox.getChildren().add(errorHeadingPane);
        errorMsgArea = new TextArea(errorEntry.getSummaryHeading() + SeparatorConstants.NEWLINE + errorEntry.getErrorData());
        errorMsgArea.setPrefRowCount(24);
        errorMsgArea.setEditable(false);

        summaryLabel.setText(" Click Show Details or see the messages.log file in your\n " + logFileLocation + " folder");
        final ImageView blankImage = new ImageView(DefaultIconProvider.TRANSPARENT.buildImage(36, Color.RED));
        summaryLabel.setGraphic(blankImage);
        detailsBox.getChildren().add(summaryLabel);
        final BorderPane buttonPane = new BorderPane();
        buttonPane.setPadding(BUTTONPANE_PADDING);
        root.setBottom(buttonPane);

        showHideButton.setOnAction((final ActionEvent event) -> toggleExceptionDisplay());
        final Button closeButton = new Button("Close");
        closeButton.setOnAction((final ActionEvent event) -> hideDialog());
        buttonPane.setLeft(showHideButton);
        blockRepeatsCheckbox.setSelected(errorEntry.isBlockRepeatedPopups());
        buttonPane.setCenter(blockRepeatsCheckbox);
        buttonPane.setRight(closeButton);
        final Scene scene = new Scene(root);
        scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        fxPanel.setScene(scene);
    }

    /**
     * Switch the dialog between summary mode and detailed mode
     */
    public void toggleExceptionDisplay() {
        showingDetails = !showingDetails;
        showHideButton.setText(showingDetails ? "Hide Details" : "Show Details");
        final int headerLen = currentError.getHeading().length();
        if (showingDetails) {
            detailsBox.getChildren().remove(summaryLabel);
            detailsBox.getChildren().add(errorMsgArea);
            final int prefHeight = 575 + Math.min(75, 25 * (headerLen / 125));
            dialog.setSize(new Dimension(575, prefHeight));
        } else {
            detailsBox.getChildren().remove(errorMsgArea);
            detailsBox.getChildren().add(summaryLabel);
            final int prefHeight = 225 + Math.min(75, 25 * (headerLen / 75));
            dialog.setSize(new Dimension(430, prefHeight));
        }
    }

    /**
     * Shows this dialog with no title.
     */
    public void showDialog() {
        showDialog(null);
    }

    /**
     * Shows this dialog.
     *
     * @param title The title of the dialog.
     */
    public void showDialog(final String title) {
        SwingUtilities.invokeLater(() -> {
            final DialogDescriptor dd = new DialogDescriptor(fxPanel, title);
            dd.setOptions(new Object[0]);
            ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(currentError.getEntryId(), null, null, null, this);
            dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
            updateDialogSettings(true, false);
            // upon closing dialog, session settings need updating ...
            finaliseSessionSettings();
        });
    }

    public void updateDialogSettings(final boolean isModal, final boolean autoClose) {
        final int headerLen = currentError.getHeading().length();
        final int prefHeight = 225 + Math.min(75, 25 * (headerLen / 75));
        dialog.setSize(new Dimension(430, prefHeight));
        dialog.setEnabled(true);
        dialog.setModal(isModal);
        dialog.setVisible(true);
        if (autoClose) {
            hideDialog();
        }
    }

    public void finaliseSessionSettings() {
        ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(currentError.getEntryId(), new Date(), blockRepeatsCheckbox.isSelected(), null, this);
        ErrorReportDialogManager.getInstance().removeActivePopupId(currentError.getEntryId());
        ErrorReportDialogManager.getInstance().setLatestPopupDismissDate(new Date());
        ErrorReportSessionData.requestScreenUpdate(true);
    }

    public JDialog getMainDialog() {
        return dialog;
    }

    /**
     * Hides this dialog.
     */
    public void hideDialog() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null && dialog.isVisible()) {
                dialog.setVisible(false);
                dialog.dispose();
                ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(currentError.getEntryId(), null, null, null, null);
            }
        });
    }

}
