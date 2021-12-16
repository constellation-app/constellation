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
package au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Atlas139mkm
 */
public class StringCriteriaValues extends FindCriteriaValues {

    private String text;
    private List<String> textList = new ArrayList<>();
    private boolean ignoreCase;
    private boolean useList;

    public StringCriteriaValues(final String attributeType, final String attribute, final String filter, final String text, final boolean ignoreCase, final boolean useList) {
        super(attributeType, attribute, filter);
        this.text = text;
        this.ignoreCase = ignoreCase;
        this.useList = useList;
    }

    public StringCriteriaValues(final String attributeType, final String attribute, final String filter, final List<String> textList, final boolean ignoreCase, final boolean useList) {
        super(attributeType, attribute, filter);
        this.textList = textList;
        this.ignoreCase = ignoreCase;
        this.useList = useList;
    }

    public String getText() {
        return text;
    }

    public List<String> getTextList() {
        return textList;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isUseList() {
        return useList;
    }

}
