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
package au.gov.asd.tac.constellation.views.attributecalculator.panes;

import org.openide.util.Lookup;

/**
 *
 * @author twilight_sparkle
 */
public abstract class AbstractCalculatorTemplateCategoryDescriptions {

    /**
     * return the instance of the class based on a Lookup
     *
     * @return instance of Auditor
     */
    public static AbstractCalculatorTemplateCategoryDescriptions getDefault() {
        AbstractCalculatorTemplateCategoryDescriptions descriptions = Lookup.getDefault().lookup(AbstractCalculatorTemplateCategoryDescriptions.class);
        if (descriptions == null) {
            descriptions = new DefaultCalculatorTemplateCategoryDescriptions();
        }

        return descriptions;
    }

    public abstract String[] getDescriptions(String key);

    public abstract String[] getUsageExamples(String key);
}
