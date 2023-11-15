/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg.resources;

import au.gov.asd.tac.constellation.utilities.svg.SVGObject;

/**
 * Class to capture constant values for strings that indicate key SVGdata element in the output SVGGraph.
 * These element names are tightly coupled to the template SCGfiles and
 * reduce the repetitive use of string literals.
 *
 * @author capricornunicorn123
 */
public enum SVGObjectConstant {
    ARROW_HEAD("arrow-head"),
    ARROW_SHAFT("arrow-shaft"),
    BACKGROUND("background"),
    BACKGROUND_IMAGE("background-image"),
    BORDER("border"),
    BOTTOM_LABELS("bottom-labels"),
    CONNECTIONS("connections"),
    CONTENT("content"),
    DOMINANT_BASELINE("after-edge"),
    FOOTER("footer"),
    FOOTNOTE("footnote"),
    FOREGROUND_IMAGE("foreground-image"),
    HEADER("header"),
    LABEL_TEXT("label-text"),
    LABELS("labels"),
    LINKS("links"),
    LOOPED_CONNECTIONS("looped-connections"),
    NODES("nodes"),
    NODE_IMAGES("node-images"),
    NORTH_EAST_DECORATOR("north-east-decorator"),
    NORTH_WEST_DECORATOR("north-west-decorator"),
    SOUTH_WEST_DECORATOR("south-west-decorator"),
    SOUTH_EAST_DECORATOR("south-east-decorator"),
    SUBTITLE("subtitle"),
    TITLE("title"),
    TOP_LABELS("top-labels");

    protected final String idValue;

    private SVGObjectConstant(final String idValue) {
        this.idValue = idValue;
    }
    
    /**
     * Finds a SVGobject of a given ID within the parent object.
     * Uses depth first search.
     * @param parent
     * @return 
     */
    public SVGObject findIn(SVGObject parent) {
        return parent.getChild(idValue);
    }
    
    /**
     * Removed a SVGobject of a given ID from the parent object.
     * Uses depth first search.
     * @param parent 
     */
    public void removeFrom(SVGObject parent) {
        parent.removeChild(idValue);
    }
}
