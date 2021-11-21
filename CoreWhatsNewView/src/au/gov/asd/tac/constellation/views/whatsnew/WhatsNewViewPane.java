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
package au.gov.asd.tac.constellation.views.whatsnew;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.views.whatsnew.WhatsNewProvider.WhatsNewEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * Whats New View Pane.
 *
 * @author aquila Refactor by:
 * @author aldebaran30701
 */
public class WhatsNewViewPane extends BorderPane {
    
    private static final Logger LOGGER = Logger.getLogger(WhatsNewViewPane.class.getName());

    private final BorderPane whatsNewViewPane;

    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = "Welcome to Constellation";

    /**
     * The number of days an entry is considered recent
     */
    public static final int RECENT_DAYS = -30; // 1 month

    /**
     * The number of days before an entry is archived and no longer shown
     */
    public static final int ARCHIVE_DAYS = -180; // 6 months

    /**
     * Required date format
     */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public WhatsNewViewPane() {
        whatsNewViewPane = new BorderPane();
        ConstellationSecurityManager.startSecurityLaterFX(() -> {
            Platform.setImplicitExit(false);

            final VBox contentVBox = new VBox();
            whatsNewViewPane.setCenter(contentVBox);

            // Create a checkbox to change users preference regarding showing the Whats New Page on startup
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final CheckBox showOnStartUpCheckBox = new CheckBox("Show on Startup");
            contentVBox.getChildren().add(showOnStartUpCheckBox);
            contentVBox.setAlignment(Pos.TOP_RIGHT);
            contentVBox.paddingProperty().set(new Insets(5, 150, 5, 150));
            showOnStartUpCheckBox.selectedProperty().addListener((ov, oldVal, newVal)
                    -> prefs.putBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, newVal));
            showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT));

            // Create a preferenceListener in order to identify when user preference is changed
            // Keeps tutorial page and options tutorial selections in-sync when both are open
            prefs.addPreferenceChangeListener(evt
                    -> showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, showOnStartUpCheckBox.isSelected())));

            final WebView whatsNewView = new WebView();
            VBox.setVgrow(whatsNewView, Priority.ALWAYS);
            whatsNewView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldVal, newVal) -> {
                if (newVal == Worker.State.SUCCEEDED) {
                    // Add a click listener for <a> tags.
                    // If there's a valid href attribute, open the default browser at that URL.
                    // If there's a valid helpId attribute, open NetBeans help using the helpId.
                    // An <a> without an href doesn't get underlined, so use href="" in addition to helpId="...".
                    final EventListener listener = event -> {
                        final String eventType = event.getType();
                        if ("click".equals(eventType)) {
                            event.preventDefault();

                            final String href = ((Element) event.getTarget()).getAttribute("href");
                            if (StringUtils.isNotBlank(href)) {
                                PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
                                        .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open Tutorial Link")
                                        .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, href)
                                        .executeLater(null);
                            } else {
                                final String helpId = ((Element) event.getTarget()).getAttribute("helpId");
                                if (StringUtils.isNotBlank(helpId)) {
                                    new HelpCtx(helpId).display();
                                }
                            }
                        }
                    };

                    final Document doc = whatsNewView.getEngine().getDocument();
                    final NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, true);
                    }
                }
            });
            whatsNewView.getEngine().setUserStyleSheetLocation(WhatsNewTopComponent.class.getResource("resources/whatsnew.css").toExternalForm());
            whatsNewView.getStyleClass().add("web-view");
            try {
                whatsNewView.getEngine().loadContent(getWhatsNew());
            } catch (final ParseException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            contentVBox.getChildren().add(whatsNewView);

            //Finally, insert the tutorialViewPane object into the BorderPane
            this.setCenter(whatsNewViewPane);
        });
    }

    private String getWhatsNew() throws ParseException {
        final Collection<? extends WhatsNewProvider> whatsNew = Lookup.getDefault().lookupAll(WhatsNewProvider.class);
        final List<WhatsNewEntry> wnList = new ArrayList<>();
        whatsNew.stream().forEach(wnp -> {
            wnList.addAll(WhatsNewProvider.getWhatsNew(wnp.getClass(), wnp.getResource(), wnp.getSection()));
        });

        Collections.sort(wnList);

        final Date now = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, RECENT_DAYS);
        final Date twoWeeksOld = cal.getTime();

        cal.setTime(now);
        cal.add(Calendar.DATE, ARCHIVE_DAYS);
        final Date archiveLimit = cal.getTime();

        boolean headerDone = false;
        final StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html><html><body>\n");
        buf.append(String.format("<style>body{font-size:%spx;}</style>", FontUtilities.getApplicationFontSize() * 1.5));
        buf.append(String.format("<style>body{font-family:%s;}</style>", FontUtilities.getApplicationFontFamily()));

        for (final WhatsNewEntry wne : wnList) {
            // Use a far-future date to indicate an undated fixed position at the top.
            final String dt;
            if (wne.date.compareTo("3000") == -1) {
                if (!headerDone) {
                    buf.append("<hr>\n<h2>What's New</h2>\n");
                    headerDone = true;
                }

                final Date whatsNewDate = dateFormatter.parse(wne.date);
                if (whatsNewDate.after(twoWeeksOld) && whatsNewDate.before(now)) {
                    // display date, header, recent badge and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"badge badge-recent\">Recent</span> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                } else if (whatsNewDate.before(archiveLimit)) {
                    dt = null;
                } else {
                    // The "normal" case: display date, header and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                }

            } else {
                // Display a pegged entry without the date.
                dt = String.format("<strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.header, wne.section);
            }

            if (dt != null) {
                buf.append(String.format("<dl><dt>%s</dt>%n<dd>", dt));
                buf.append(wne.text);
                buf.append("</dd></dl>\n");
            }
        }

        buf.append("</body></html>");
        return buf.toString();
    }
}
