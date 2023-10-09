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

/**
 * This class serves as a constant to fill the attribute key for SVG attributes.
 * 
 * @author capricornunicorn123
 */
public enum SVGAttributeConstant {
    BASELINE("dominant-baseline"),
    CLASS("class"),
    DESTINATION_X("x2"),
    DESTINATION_Y("y2"),
    DASH_ARRAY("stroke-dasharray"),
    EXTERNAL_RESOURCE_REFERENCE("xlink:href"),
    FILL_COLOR("fill"),
    FILTER("filter"),
    FONT_SIZE("font-size"),
    HEIGHT("height"),
    ID("id"),
    SOURCE_X("x1"),
    SOURCE_Y("y1"),
    STROKE_COLOR("stroke"),
    TRANSFORM("transform"),
    WIDTH("width"),
    X("x"),
    Y("y");
    
    private final String name;
    
    private SVGAttributeConstant(final String attributeName){
        this.name = attributeName;
    }
    
    /**
     * Returns the key value used by SVG attributes.
     * @return 
     */
    public String getName(){
        return this.name;
    }
}
