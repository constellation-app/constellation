/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.qualitycontrol.widget;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewTopComponent;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlListener;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A Button that updates itself in response to quality control events.
 *
 * @author algol
 */
@ServiceProvider(service = QualityControlAutoButton.class)
@NbBundle.Messages("MSG_NoRisk=N/A")
public final class DefaultQualityControlAutoButton extends QualityControlAutoButton implements QualityControlListener {

    protected static final String DEFAULT_TEXT_STYLE = "-fx-text-fill: rgb(0,0,0); -fx-background-color: rgb(0,200,0);";
    public static final String QUERY_RISK_DEFAULT_STYLE = "-fx-text-fill: rgb(0,0,0);";
    public static final String QUALITY_CONTROL_WIDGET_TEXT = "Quality Category: %s";

    public DefaultQualityControlAutoButton() {
        getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        setStyle(QUERY_RISK_DEFAULT_STYLE + String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

        QualityControlViewPane.readSerializedRulePriorities();
        QualityControlViewPane.readSerializedRuleEnabledStatuses();

        setOnAction(value -> SwingUtilities.invokeLater(() -> {
            final TopComponent qualityControlView = WindowManager.getDefault().findTopComponent(QualityControlViewTopComponent.class.getSimpleName());
            if (qualityControlView != null) {
                if (!qualityControlView.isOpened()) {
                    qualityControlView.open();
                }
                qualityControlView.requestActive();
            }
        }));

        QualityControlAutoVetter.getInstance().init();
    }

    @Override
    public void qualityControlChanged(final QualityControlState state) {
        update(state);
    }

    @Override
    protected void update(final QualityControlState state) {
        QualityControlEvent event = null;
        if (state != null) {
            event = state.getHighestScoringEvent();
        }

        final String riskText;
        final String styleText;
        final String tooltipText;
        if (event != null && event.getCategory() != QualityCategory.OK) {
            riskText = String.format(QUALITY_CONTROL_WIDGET_TEXT, String.valueOf(event.getCategory().name()));
            styleText = QualityControlViewPane.qualityStyle(event.getCategory(), 1);
            tooltipText = event.getReasons();
        } else {
            riskText = String.format(QUALITY_CONTROL_WIDGET_TEXT, String.valueOf(QualityCategory.OK.name()));
            styleText = DEFAULT_TEXT_STYLE;
            tooltipText = null;
        }

        Platform.runLater(() -> {
            setText(riskText);
            setStyle(styleText);
            setTooltip(tooltipText != null ? new Tooltip(tooltipText) : null);
        });
    }

    @Override
    public DefaultQualityControlAutoButton copy() {
        return new DefaultQualityControlAutoButton();
    }

    /**
     * Add this button as a listener to the quality control auto vetter. This
     * allows the containing top component to effectively subscribe/unsubscribe
     * listeners as the components are opened or closed.
     */
    public void addQCListener() {
        QualityControlAutoVetter.getInstance().addListener(this);
        QualityControlAutoVetter.getInstance().invokeListener(this);
    }

    /**
     * Remove this button as a listener from the quality control auto vetter.
     * This allows the containing top component to effectively
     * subscribe/unsubscribe listeners as the components are opened or closed.
     */
    public void removeQCListener() {
        QualityControlAutoVetter.getInstance().removeListener(this);
    }
}
