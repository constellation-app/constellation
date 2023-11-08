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
package au.gov.asd.tac.constellation.utilities.svg;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class serves as a constant to fill the attribute key for SVG attributes.
 * 
 * @author capricornunicorn123
 */
public enum SVGAttributeConstant {
    BASELINE("dominant-baseline"),
    CLASS("class"),
    CUSTOM_SORT_ORDER("data-sort-order"),
    CX("cx"),
    CY("cy"),
    DESTINATION_X("x2"),
    DESTINATION_Y("y2"),
    DASH_ARRAY("stroke-dasharray"),
    EXTERNAL_RESOURCE_REFERENCE("xlink:href"),
    FILL_COLOR("fill"),
    FILTER("filter"),
    FONT_SIZE("font-size"),
    HEIGHT("height"),
    ID("id"),
    NAME_SPACE("xmlns"),
    POINTS("points"),
    RADIUS("r"),
    SOURCE_X("x1"),
    SOURCE_Y("y1"),
    STROKE_COLOR("stroke"),
    TRANSFORM("transform"),
    VIEW_BOX("viewBox"),
    WIDTH("width"),
    X("x"),
    Y("y");
    
    private final String attributeName;
    
    private SVGAttributeConstant(final String attributeName){
        this.attributeName = attributeName;
    }
    
    /**
     * Returns the key value used by SVG attributes.
     * @return 
     */
    public String getName(){
        return this.attributeName;
    }
    
    /**
     * Generates a LinkedHashMap of important attribute values.
     * Created for the SVGData constructor to ensure that significant attributes such as id, x and y are forced to the front of the svg tag. 
     * @return 
     */
    public static Map<String, String> initialiseBasicAttributes(){
        Map<String, String> map = new LinkedHashMap<>();
        
            //Identity Data
            map.put(SVGAttributeConstant.CLASS.getName(), null);
            map.put(SVGAttributeConstant.ID.getName(), null); 
            
            //Positional Data
            map.put(SVGAttributeConstant.X.getName(), null);
            map.put(SVGAttributeConstant.Y.getName(), null);
            map.put(SVGAttributeConstant.SOURCE_X.getName(), null);
            map.put(SVGAttributeConstant.SOURCE_Y.getName(), null);
            map.put(SVGAttributeConstant.DESTINATION_X.getName(), null);
            map.put(SVGAttributeConstant.DESTINATION_Y.getName(), null);
            map.put(SVGAttributeConstant.CX.getName(), null);
            map.put(SVGAttributeConstant.CY.getName(), null);
            
            //Size Data
            map.put(SVGAttributeConstant.WIDTH.getName(), null);
            map.put(SVGAttributeConstant.HEIGHT.getName(), null);
            map.put(SVGAttributeConstant.RADIUS.getName(), null);
            
            //StyleData
            map.put(SVGAttributeConstant.FONT_SIZE.getName(), null);
            map.put(SVGAttributeConstant.FILL_COLOR.getName(), null);
            map.put(SVGAttributeConstant.STROKE_COLOR.getName(), null);
            return map;
    }
}
