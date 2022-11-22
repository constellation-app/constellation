/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This Class is for the StringCriteriaValues which are created from a
 * StringCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class StringCriteriaValues extends FindCriteriaValues {

    private String text;
    private List<String> textList = new ArrayList<>();
    private boolean ignoreCase;
    private boolean useList;

    // first contsructor takes one text value
    public StringCriteriaValues(final String attributeType, final String attribute, final String filter, final String text, final boolean ignoreCase, final boolean useList) {
        super(attributeType, attribute, filter);
        this.text = text;
        this.ignoreCase = ignoreCase;
        this.useList = useList;
    }

    // secondary constructor takes a list of text values
    public StringCriteriaValues(final String attributeType, final String attribute, final String filter, final List<String> textList, final boolean ignoreCase, final boolean useList) {
        super(attributeType, attribute, filter);
        Collections.copy(this.textList, textList);
        this.ignoreCase = ignoreCase;
        this.useList = useList;
    }

    /**
     * Gets the singular text value
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the list of all the text values
     *
     * @return
     */
    public List<String> getTextList() {
        List<String> textListCopy = new ArrayList<>();
        Collections.copy(textListCopy, textList);
        return textListCopy;
    }

    /**
     * Gets the ignoreCase Value
     *
     * @return
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Gets the useList Boolean
     *
     * @return
     */
    public boolean isUseList() {
        return useList;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.text);
        hash = 97 * hash + Objects.hashCode(this.textList);
        hash = 97 * hash + (this.ignoreCase ? 1 : 0);
        hash = 97 * hash + (this.useList ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StringCriteriaValues other = (StringCriteriaValues) obj;
        if (this.ignoreCase != other.ignoreCase) {
            return false;
        }
        if (this.useList != other.useList) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (!Objects.equals(this.textList, other.textList)) {
            return false;
        }
        return true;
    }

}
