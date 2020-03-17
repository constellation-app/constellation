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
package au.gov.asd.tac.constellation.views.attributecalculator.script;

import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author twilight_sparkle
 */
public abstract class AbstractScriptLoader {

    /**
     * return the instance of the class based on a Lookup
     *
     * @return instance of Auditor
     */
    public static AbstractScriptLoader getDefault() {
        AbstractScriptLoader descriptions = Lookup.getDefault().lookup(AbstractScriptLoader.class);
        if (descriptions == null) {
            descriptions = new DefaultScriptLoader();
        }

        return descriptions;
    }

    public abstract Map<String, String[]> getScripts();

}
