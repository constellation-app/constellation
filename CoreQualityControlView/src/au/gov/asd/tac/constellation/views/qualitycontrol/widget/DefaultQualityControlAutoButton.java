/*
 * Copyright 2010-2019 Australian Signals Directorate
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

    private static final String DEFAULT_TEXT_STYLE = "-fx-text-fill: rgb(0,0,0); -fx-background-color: rgb(255,255,255);";
    private static final String BUTTON_STYLE = "-fx-padding: 2 5 2 5;";
    public static final String QUERY_RISK_DEFAULT_STYLE = "-fx-text-fill: rgb(0,0,0); -fx-padding: 2 5 2 5;";
    public static final String QUALITY_CONTROL_WIDGET_TEXT = "Quality Score: %s";

    public DefaultQualityControlAutoButton() {
        getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
        setStyle(QUERY_RISK_DEFAULT_STYLE + BUTTON_STYLE + String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));

        setOnAction(value -> {
            SwingUtilities.invokeLater(() -> {
                final TopComponent qualityControlView = WindowManager.getDefault().findTopComponent(QualityControlViewTopComponent.class.getSimpleName());
                if (qualityControlView != null) {
                    if (!qualityControlView.isOpened()) {
                        qualityControlView.open();
                    }
                    qualityControlView.requestActive();
                }
            });
        });

        QualityControlAutoVetter.getInstance().addListener(this);
        QualityControlAutoVetter.getInstance().invokeListener(this);
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
        if (event != null && event.getQuality() > 0) {
            riskText = String.format(QUALITY_CONTROL_WIDGET_TEXT, String.valueOf(event.getQuality()));
            styleText = QualityControlViewPane.qualityStyle(event.getQuality(), 1);
            tooltipText = event.getReasons();
        } else {
            riskText = String.format(QUALITY_CONTROL_WIDGET_TEXT, Bundle.MSG_NoRisk());
            styleText = DEFAULT_TEXT_STYLE;
            tooltipText = null;
        }

        Platform.runLater(() -> {
            setText(riskText);
            setStyle(styleText + BUTTON_STYLE);
            setTooltip(tooltipText != null ? new Tooltip(tooltipText) : null);
        });
    }
}
