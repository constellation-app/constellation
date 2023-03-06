/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import java.util.Date;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author OrionsGuardian
 */
public class ErrorReportDialog {
    
    protected final JFXPanel fxPanel;
    protected JDialog dialog;
    private static final java.awt.Color TRANSPARENT = new java.awt.Color(0, 0, 0, 0);
    private static final int GRIDPANE_GAP = 5;
    private static final Insets BORDERPANE_PADDING = new Insets(10);
    private static final Insets BUTTONPANE_PADDING = new Insets(5);


    protected double mouseOrigX = 0;
    protected double mouseOrigY = 0;
    private ErrorReportEntry currentError = null;

    public ErrorReportDialog(ErrorReportEntry errorEntry){
        currentError = errorEntry;
        fxPanel = new JFXPanel();
        final BoxLayout layout = new BoxLayout(fxPanel, BoxLayout.Y_AXIS);
        fxPanel.setLayout(layout);
        fxPanel.setOpaque(false);
        fxPanel.setBackground(TRANSPARENT);
        
        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;");
        root.setPrefSize(400, 600);
        root.setPadding(BORDERPANE_PADDING);
        
        final Label errorLabel = new Label("Error Details:");   
        VBox detailsBox = new VBox();
        root.setTop(detailsBox);
        detailsBox.getChildren().add(errorLabel);
        Text data = new Text(400,600, errorEntry.getErrorData());
        detailsBox.getChildren().add(data);
        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(BUTTONPANE_PADDING);
        buttonPane.setHgap(GRIDPANE_GAP);
        root.setBottom(buttonPane);

        final Button closeButton = new Button("Close");
        closeButton.setOnAction((ActionEvent event) -> hideDialog());
        buttonPane.getChildren().add(closeButton);
        final Scene scene = new Scene(root);
        fxPanel.setScene(scene);
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
            currentError.setLastPopupDate(new Date());
            dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
            dialog.setEnabled(true);
            dialog.setModal(true);
            dialog.setVisible(true);
            
            currentError.setLastPopupDate(new Date());
            ErrorReportDialogManager.getInstance().removeActivePopupId(currentError.getEntryId());
            ErrorReportDialogManager.getInstance().setLatestDismissDate(new Date());
        });
    }
    
    /**
     * Hides this dialog.
     */
    public void hideDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.setVisible(false);            
            dialog.dispose();
        });
    }

}
