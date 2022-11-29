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
package au.gov.asd.tac.constellation.webserver.transport;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.rest.RestDirectory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default shared directory provider.
 *
 * @author algol
 */
@ServiceProvider(service = RestDirectory.class)
public class DefaultRestDirectory implements RestDirectory {

    @Override
    public Path getRESTDirectory() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        return Paths.get(ApplicationPreferenceKeys.getUserDir(prefs), "REST");
    }
}
