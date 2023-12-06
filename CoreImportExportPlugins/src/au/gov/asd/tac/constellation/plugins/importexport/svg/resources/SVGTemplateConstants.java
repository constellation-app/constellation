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

import au.gov.asd.tac.constellation.utilities.svg.SVGFile;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;

/**
 * This class serves to provide references to resource file names as constants. 
 * The class also serves to provide the class path to the resources 
 * as it is located within the same package. 
 * 
 * @author capricornunicorn123
 */

public enum SVGTemplateConstants implements SVGFile{
    ARROW_HEAD_LINK("Diamond.svg"),
    ARROW_HEAD_TRANSACTION("Triangle.svg"),
    BLAZE("Triangle.svg"),
    CONNECTION_LOOP("ConnectionLoop.svg"),
    CONNECTION_LINEAR("ConnectionLinear.svg"),
    IMAGE("Image.svg"),
    LABEL("Label.svg"),
    LAYOUT("Layout.svg"),
    LINK("Link.svg"),
    NODE("Node.svg");

    private final String resourceName;
        
    private SVGTemplateConstants(final String resourceName) {
        this.resourceName = resourceName;
    }
    
    /**
     * Helper function to simplify getting SVGObject from SVGTemplate file
     * @return 
     */
    public final SVGObject getSVGObject() {
        return SVGObject.loadFromTemplate(this);
    }

    @Override
    public String getFileName() {
        return this.resourceName;
    }
        
}
