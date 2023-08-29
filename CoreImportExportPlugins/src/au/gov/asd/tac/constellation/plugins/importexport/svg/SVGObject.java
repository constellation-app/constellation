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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Object to store SVG data for ease of translation and modification between
 * code and SVG files
 * 
 * @author capricornunicorn123
 */
public class SVGObject {
    private final String type;
    private final Map<String, String> attributes;
    private final Collection<SVGObject> children;
    private final SVGObject parent;
    
    public SVGObject(String type, SVGObject parent){
        this.type = type;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.parent = parent;
        if (this.parent != null){
            this.parent.setChild(this);
        }
    }
    
    /**
     * Adds an attribute to the current SVG element. 
     * Only one attribute value can be provided per key.
     * Attempting to add a value to an existing key 
     * will override the last value provided.
     * 
     * @param attributeKey such as "height", "width", "style"
     * @param attributeValue such as "100", "stroke-width:3", "fill:rgb(0,0,255)".
     */
    public void setAttribute(String attributeKey, String attributeValue){
        this.attributes.put(attributeKey, attributeValue);
    }
    
        /**
     * Adds an attribute to the current SVG element. 
     * Only one attribute value can be provided per key.
     * Attempting to add a value to an existing key 
     * will override the last value provided.
     * 
     * @param attributeKey such as "height", "width", "style"
     * @param attributeValue such as "100", "stroke-width:3", "fill:rgb(0,0,255)".
     */
    public void setAttributes(Map<String , String> attributeMap){
        this.attributes.putAll(attributeMap);
    }
    
    /**
     * Associates an SVGObject as the child element of this SVGObject parent element.
     * 
     * @param child 
     */
    public void setChild(SVGObject child){
        this.children.add(child);
    }
    
    /**
     * Generate a string representation of SVG data captured within this object.
     * will be formatted with indentations and line breaks to be written 
     * directly to an output file.
     * 
     * @return String in an SVG format.
     */
    @Override
    public String toString(){
        return toString(null);
    }
    
    /**
     * Recursive function to generate a string equivalent of 
     * complex SVG data captured within this object.
     * 
     * @param prefix
     * @return String representation of the current element and all of it's child elements.
     */
    private String toString(final String prefix){
        final StringBuilder svgString = new StringBuilder();
        if (this.children.isEmpty()){
            svgString.append(elementToSVG(prefix));
        } else {
            svgString.append(elementHeaderToSVG(prefix));
            svgString.append(elementChildrenToSVG(prefix));
            svgString.append(elementFooterToSVG(prefix));
        }
        return svgString.toString();
    }
    /**
     * Generates an "inline" SVG element.
     * 
     * @param prefix represents indentation prefixes for the element.
     * @return String representation of the current element
     */
    private String elementToSVG(final String prefix){
        StringBuilder attributeBuilder = new StringBuilder();
        String linePrefix = SeparatorConstants.NEWLINE + prefix;
        Set<String> keys = attributes.keySet();
        keys.forEach(key -> attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key))));
        
        return String.format("%s<%s%s />", linePrefix, this.type, attributeBuilder.toString());
    }
    
    /**
     * Generates the header or opening portion of an SVG element. 
     * The header will contain the element type and 
     * all of the attributes associated with the element.
     * 
     * @param prefix
     * @return String representation of element header.
     */
    private String elementHeaderToSVG(final String prefix){
        String linePrefix = SeparatorConstants.NEWLINE;
        if (prefix != null){
            linePrefix += prefix;
        }
        
        StringBuilder attributeBuilder = new StringBuilder();
        
        Set<String> keys = attributes.keySet();
        keys.forEach(key -> attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key))));
        
        return String.format("%s<%s%s>", linePrefix, this.type, attributeBuilder.toString());
    }
    
    /**
     * Generates the footer or closing portion of an SVG element. 
     * The footer will contain the element type.
     * 
     * @param prefix
     * @return String representation of element footer.
     */
    private String elementFooterToSVG(final String prefix){
        String linePrefix = SeparatorConstants.NEWLINE;
         if (prefix != null){
            linePrefix += prefix;
        }
        return String.format("%s</%s>", linePrefix, this.type);
    }
    
    /**
     * Generates SVG equivalents for child elements of the current SVG element. 
     * The prefix of the current element should be provided as this method
     * manages the indented of child elements
     * 
     * @param prefix
     * @return 
     */
    private String elementChildrenToSVG (String prefix){
        String childPrefix = SeparatorConstants.TAB;
        if (prefix != null){
            childPrefix += prefix;
        }
        
        final StringBuilder childSVGString = new StringBuilder();
        for (SVGObject child : this.children){
            childSVGString.append(child.toString(childPrefix));
        }
        return childSVGString.toString();
    }
    
    public final SVGObject getParent(){
        return this.parent;
    }
}
