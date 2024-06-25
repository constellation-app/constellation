/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.recentvalue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * A Utility for managing recent values for inputs.
 * This class was initially created for Plugin Parameters exclusively.
 * it has been refactored to serve as a utility for {@link ConstellationInputFields}
 * 
 * Some further review of the functionality may be required.
 * the intention of this calss is toenable inputs to be able to display rcent values. 
 * Inputs are identifyable by an ID and recent values will be suplied based on that id. 
 * for this reason, should a pluign be interacted with from two different Interfaces, 
 * i.e. the data access voew or a interactive dialog. the recent values can be maintaned between interfaces if the 
 * input fields hae the same id. 
 * 
 * These recent values persist between aplication shutdowns and startups.
 *
 * @author sirius
 */
public class RecentValueUtility {
    
    private static final Logger LOGGER = Logger.getLogger(RecentValueUtility.class.getName());

    private static final Map<String, List<String>> RECENT_VALUES = new HashMap<>();
    private static final List<RecentValuesListener> LISTENERS = new ArrayList<>();
    private static final Preferences PREFERENCES = NbPreferences.forModule(RecentValuesKey.class);
    private static final int SAVE_LIMIT = 5;
    private static final JsonFactory FACTORY = new MappingJsonFactory();

    /**
     * Stores a string value representing a new recent value associated with an identifiable input
     * Although recent values are expressed through interaction want a {@link ConstelationInputField}
     * it is imortant to note that the id that these fields use is comonly a parameter id.
     * 
     * @param id the id value of the input.
     * @param value 
     */
    public static void storeRecentValue(String id, String value) {
        synchronized (RECENT_VALUES) {
            List<String> values = RECENT_VALUES.get(id);
            if (values == null) {
                values = new ArrayList<>();
                RECENT_VALUES.put(id, values);
                values.add(value);
            } else {
                values.remove(value);
                values.add(0, value);
            }
            fireChangeEvent(new RecentValuesChangeEvent(id, RECENT_VALUES.get(id)));
        }
    }

    public static List<String> getRecentValues(String id) {
        synchronized(RECENT_VALUES) {
            if (RECENT_VALUES.isEmpty()) {
                loadFromPreference();
            }
            return RECENT_VALUES.get(id);
        }
    }

    /**
     * Add a listener to be informed when recent values are updated.
     *
     * @param listener the listener to be added.
     */
    public static void addListener(RecentValuesListener listener) {
        if (listener != null) {
            synchronized (LISTENERS) {
                LISTENERS.add(listener);
            }
        }
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to be removed.
     */
    public static void removeListener(RecentValuesListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    /**
     * Inform all listeners that recent values have been updated.
     *
     * @param e the change event to be sent to all listeners.
     */
    public static void fireChangeEvent(RecentValuesChangeEvent e) {
        synchronized (LISTENERS) {
            for (RecentValuesListener listener : LISTENERS) {
                listener.recentValuesChanged(e);
            }               
        }
    }

    public static void saveToPreferences() {
        synchronized (RECENT_VALUES) {
            final ByteArrayOutputStream json = new ByteArrayOutputStream();
            try (final JsonGenerator jg = FACTORY.createGenerator(json)) {
                jg.writeStartObject();
                for (Entry<String, List<String>> entry : RECENT_VALUES.entrySet()) {
                    List<String> recentVals = entry.getValue();
                    if (entry.getKey() != null) {
                        jg.writeFieldName(entry.getKey());
                        jg.writeStartArray();
                        int limit = Math.min(SAVE_LIMIT, recentVals.size());
                        for (int i = 0; i < limit; i++) {
                            jg.writeString(recentVals.get(i));
                        }
                        jg.writeEndArray();
                    }
                }
                jg.writeEndObject();
                jg.flush();
                PREFERENCES.put(RecentValuesKey.RECENT_VALUES, json.toString(StandardCharsets.UTF_8.name()));
                try {
                    PREFERENCES.flush();
                } catch (final BackingStoreException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    public static void loadFromPreference() {
        synchronized (RECENT_VALUES) {
            final String recentValuesJSON = PREFERENCES.get(RecentValuesKey.RECENT_VALUES, "");
            if (!recentValuesJSON.isEmpty()) {
                try (final JsonParser jp = FACTORY.createParser(recentValuesJSON)) {
                    if (jp.nextToken() == JsonToken.START_OBJECT) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
                                List<String> recentVals = new ArrayList<>();
                                String fieldName = jp.getCurrentName();
                                while (jp.nextToken() != JsonToken.END_ARRAY) {
                                    recentVals.add(jp.getValueAsString());
                                }
                                RECENT_VALUES.put(fieldName, recentVals);
                            }
                        }
                    }
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }
    }
}
