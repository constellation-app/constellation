/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.preferences;

import au.gov.asd.tac.constellation.views.AbstractTopComponent;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a map of the default floating preferences for the currently applicable views.
 *
 * @author sol695510
 */
@ServiceProvider(service = ViewOptionsProvider.class)
public class ViewOptionsPreferencesMap extends ViewOptionsProvider {

    final Map<String, Boolean> dfpInfo = new TreeMap<>();

    /**
     * Gets a map of the default floating preferences.
     *
     * @return a map of the default floating preferences.
     */
    @Override
    public Map<String, Boolean> getDefaultFloatingPreferences() {

        if (dfpInfo.isEmpty()) {
            EventQueue.invokeLater(() -> Lookup.getDefault().lookupAll(AbstractTopComponent.class).forEach(lookup -> dfpInfo.put(
                    (String) lookup.getDefaultFloatingInfo().getFirst(),
                    (Boolean) lookup.getDefaultFloatingInfo().getSecond()
            )));
        }

        return Collections.unmodifiableMap(dfpInfo);
    }
}
