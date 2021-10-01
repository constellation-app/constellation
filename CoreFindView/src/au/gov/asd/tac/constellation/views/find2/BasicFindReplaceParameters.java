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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Atlas139mkm
 */
public class BasicFindReplaceParameters {

    private final String findString;
    private final String replaceString;
    private final GraphElementType graphElement;
    private List<String> attributeList = new ArrayList<String>();
    private final boolean standardText;
    private final boolean regEx;
    private final boolean ignoreCase;
    private final boolean exactMatch;
    private final boolean addToCurrent;
    private final boolean removeFromCurrent;

    public BasicFindReplaceParameters(String findString, String replaceString, GraphElementType graphElement,
            List<String> attributeList, boolean standardText, boolean regEx, boolean ignoreCase,
            boolean exactMatch, boolean addToCurrent, boolean removeFromCurrent) {

        this.findString = findString;
        if (replaceString.isEmpty()) {
            this.replaceString = null;
        } else {
            this.replaceString = replaceString;
        }
        this.graphElement = graphElement;
        this.attributeList = attributeList;
        this.standardText = standardText;
        this.regEx = regEx;
        this.ignoreCase = ignoreCase;
        this.exactMatch = exactMatch;
        this.addToCurrent = addToCurrent;
        this.removeFromCurrent = removeFromCurrent;

    }

}
