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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data structure to store SVG information and associations.
 * Tree-like data structure capable of storing SVG types, attributes and children.
 * acts as a bare-bones object to be able to store SVG Data for association with minimal impact on memory.
 * 
 * @author capricornunicorn123
 */
public class SVGData {

    private static final Logger LOGGER = Logger.getLogger(SVGData.class.getName());
    private final String type;
    private final Map<String, String> attributes;
    private final Map<String, SVGData> children;
    private String content;
    private SVGData parent;

    public SVGData(final String type, final SVGData parent, final Map<String, String> attributes) {
        this.type = type;
        this.children = new LinkedHashMap<>();
        if (attributes == null) {
            this.attributes = new LinkedHashMap<>();
        } else {
            this.attributes = new LinkedHashMap<>(attributes);
        }        
        this.setParent(parent);
        this.content = null;
    }
    
    /**
     * Returns the SVG type.
     * @return 
     */
    public final String getType() {
        return this.type;
    }
    
    /**
     * Adds a child SVGObject to the current SVG element.
     * To avoid duplication, this method should only be called by the setParent() method.
     * Child SVGObjects are stored in HashMap based on their id attribute.
     * @param child 
     */
    private void setChild(final SVGData child) {
        final String childID = child.getAttributeValue(SVGAttributeConstant.ID.getName());
        this.children.put(childID, child);
    }
    
    /**
     * Returns an SVGData with a specified id attribute value.
     * @param idValue 
     * @return child
     */
    public SVGData getChild(final String idValue) {
        return this.children.get(idValue);
    }
    
    /**
     * Returns an ArrayList of SVGObjects containing all child SVGData elements.
     * @return children
     */
    public List<SVGData> getAllChildren() {
        final List<SVGData> allChildren = new ArrayList<>();
        children.keySet().forEach(key -> allChildren.add(this.children.get(key)));
        return allChildren;
    }
    
    /**
     * Associates SVGData as a parent of this SVGData.
     * By extension the parent sets this object as its child. 
     * Warning: Child object must have a unique ID.
     * @param parent
     */
    public final void setParent(final SVGData parent) {
        this.parent = parent;
        if (this.parent != null){
            this.parent.setChild(this);
        }
    }
    
    /**
     * Gets the parent of this SVGData.
     * @return parent
     */
    public final SVGData getParent() {
        return this.parent;
    }
    
    /**
     * Adds an attribute to the current SVG element. 
     * Attributes are stored as a String 
     * Only one attribute value can be provided per key.
     * Attempting to add a value to an existing key 
     * will override the last value provided.
     * @param attributeKey such as "height", "width", "fill"
     * @param attributeValue such as "100", "stroke-width:3", "rgb(0,0,255)".
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final String attributeValue) {
        this.attributes.put(attributeKey.getName(), attributeValue);
    }
        
    /**
     * Returns the value of an attribute based on a provided attribute key.
     * @param attributeKey
     * @return attributeValue
     */
    public final String getAttributeValue(final String attributeKey) {
        return this.attributes.get(attributeKey);
    }
    
    /**
     * Sets the content of this SVGData.
     * Content is considered plain text that lies within a SVG element 
     * such as is the case with text elements.
     * @param content
     */
    public void setContent(final String content) {
        this.content = content;
    }
    
    /**
     * Returns the value of the content of this SVGData.
     * @return content
     */
    public final String getContent(){
        return this.content;
    }
    
    /**
     * Generates a string representation of SVG data captured within this object.
     * will be formatted with indentations and line breaks to be written 
     * directly to an output file.
     * @return String in an SVG format.
     */
    @Override
    public final String toString() {
        return toString(null);
    }
    
    /**
     * Recursive function to generate a string equivalent of complex SVG data 
     * captured within this object.
     * @param prefix
     * @return String representation of the current element and all of it's child elements.
     */
    private String toString(final String prefix) {
        final StringBuilder svgString = new StringBuilder();
        if (this.children.isEmpty() && this.content == null){
            svgString.append(elementToSVG(prefix));
        } else {
            svgString.append(elementHeaderToSVG(prefix));
            if (this.children.isEmpty()){
                svgString.append(content);
            } else {
                svgString.append(elementChildrenToSVG(prefix));
            }
            svgString.append(elementFooterToSVG(prefix));
        }
        return svgString.toString();
    }
    /**
     * Generates an "inline" SVG element.
     * @param prefix represents indentation prefixes for the element.
     * @return String representation of the current element
     */
    private String elementToSVG(final String prefix) {
        final StringBuilder attributeBuilder = new StringBuilder();
        final String linePrefix = SeparatorConstants.NEWLINE + prefix;
        final Set<String> keys = attributes.keySet();
        keys.forEach(key -> {
            if (SVGAttributeConstant.NAME_SPACE.getName().equals(key) && this.parent != null) {
                //do nothing here
            } else {
                attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key)));
            }
        });

        return String.format("%s<%s%s />", linePrefix, this.type, attributeBuilder.toString());
    }
    
    /**
     * Generates the header or opening portion of an SVG element. 
     * The header will contain the element type and 
     * all of the attributes associated with the element.
     * @param prefix
     * @return String representation of element header.
     */
    private String elementHeaderToSVG(final String prefix) {
        String linePrefix = SeparatorConstants.NEWLINE;
        if (prefix != null){
            linePrefix += prefix;
        }
        
        final StringBuilder attributeBuilder = new StringBuilder();
        
        final Set<String> keys = attributes.keySet();
        keys.forEach(key -> attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key))));
        
        return String.format("%s<%s%s>", linePrefix, this.type, attributeBuilder.toString());
    }
    
    /**
     * Generates the footer or closing portion of an SVG element. 
     * The footer will contain the element type.
     * @param prefix
     * @return String representation of element footer.
     */
    private String elementFooterToSVG(final String prefix) {
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
    private String elementChildrenToSVG (final String prefix) {
        final StringBuilder childSVGString = new StringBuilder();
        String childPrefix = SeparatorConstants.TAB;
        if (prefix != null){
            childPrefix += prefix;
        }
        for (final SVGData child : this.getAllChildren()){
            childSVGString.append(child.toString(childPrefix));
        }
        return childSVGString.toString();
    }
    
    /**
    * Creates a SVGData object from a template SVG file
    * The object will be returned with no parent.
    * @param templateResource the filename of the template file.
    * @return svgData
    */
    public static final SVGData loadFromTemplate(final SVGFile templateResource) {
        final InputStream inputStream = templateResource.getClass().getResourceAsStream(templateResource.getFileName());
        SVGData templateSVG = null;
        try {
            templateSVG = SVGParser.parse(inputStream);
        } catch (final IOException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage());
        }
        return templateSVG;
    }
}
