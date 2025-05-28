/*
* Copyright 2010-2025 Australian Signals Directorate
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final SVGTypeConstants type;
    private final Map<String, String> attributes;
    private final Map<String, SVGData> children;
    private String content;
    private SVGData parent;

    public SVGData(final SVGTypeConstants type, final SVGData parent, final Map<String, String> attributes) {
        if (type == null){
            throw new IllegalArgumentException("SVGData elements cannot have a type Null");
        }
        this.type = type;
        this.children = new LinkedHashMap<>();
        this.attributes = SVGAttributeConstants.initialiseBasicAttributes();
        if (attributes != null) {
            this.attributes.putAll(attributes);
        } 
        this.setParent(parent);
        this.content = null;
    }
    
    /**
     * Returns the SVG type.
     * @return 
     */
    public final String getType() {
        return this.type.getTypeString();
    }
    
    /**
     * Adds a child SVGObject to the current SVG element.
     * To avoid duplication, this method should only be called by the setParent() method.
     * Child SVGObjects are stored in HashMap based on their id attribute.
     * @param child 
     */
    private void setChild(final SVGData child) {
        final String childID = child.getAttributeValue(SVGAttributeConstants.ID.getName());
        this.children.put(childID, child);
    }
    
    /**
     * Returns an SVGData with a specified id attribute value.
     * Use depth first search.
     * @param idValue 
     * @return child
     */
    public SVGData getChild(final String idValue) {
        SVGData child = this.children.get(idValue);
        if (child == null) {
            for (final SVGData childIndex : this.getAllChildren()) {
                child = childIndex.getChild(idValue);
                if (child != null) {
                    break;
                }
            }
        }
        return child;
    }
    
    /**
     * Adds a list of child SVGObjects to the current SVG element.
     * Use depth first search.
     * @param nodes
     */
    public void setChildren(final List<SVGObject> nodes) {
        for (final SVGObject node : nodes){
            this.setChild(node.toSVGData());
        }
    }
    
    /**
     * Removes an SVGData with a specified id attribute value.
     * Use depth first search.
     * @param idValue 
     * @return child
     */
    public SVGData removeChild(final String idValue) {   
        //Try and remove the child from this SVGObjects set fo children.
        SVGData child = this.children.remove(idValue);
        
        //If the child wasnt removed, recursively itterate through levels of children untill the id is found.
        if (child == null) {
            for (final SVGData childIndex : this.getAllChildren()) {
                child = childIndex.removeChild(idValue);

                //Break out of te search once the ID is found.
                if (child != null) {
                    break;
                }
            }
        }
        
        // If a child was found sever the childs reference to the parent.
        if (child != null){
            child.parent = null;
        }
        return child;
    }
    
    /**
     * Returns a List of SVGObjects containing all child SVGData elements.
     * @return children
     */
    public List<SVGData> getAllChildren() {
        final List<SVGData> allChildren = new ArrayList<>();
        children.keySet().forEach(key -> allChildren.add(this.children.get(key)));
        if (!allChildren.isEmpty() && allChildren.get(0).attributes.keySet().contains(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName())) {
            Collections.sort(allChildren, (SVGData first, SVGData second) -> {
                final String firstValue = first.getAttributeValue(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName());
                final String secondValue = second.getAttributeValue(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName());
                return Float.compare(Float.parseFloat(secondValue), Float.parseFloat(firstValue));
            });
        }
        return allChildren;
    }
    
    /**
     * Associates SVGData as a parent of this SVGData.
     * By extension the parent sets this object as its child. 
     * Warning: Child object must have a unique ID.
     * @param parent
     */
    public final void setParent(final SVGData parent) {
        final String idAttributeName = SVGAttributeConstants.ID.getName(); 
        
        if (parent != null) {            
            if (parent.getChild(this.getAttributeValue(idAttributeName)) != null){
                throw new IllegalStateException(String.format("Parent SVGData %s already has a child of id %s", 
                        parent.getAttributeValue(idAttributeName), 
                        this.getAttributeValue(idAttributeName)
                ));
            }
            parent.setChild(this);
        }
        
        this.parent = parent;
    }
    
    /**
     * Associates SVGObject as a parent of this SVGData.
     * By extension the parent sets this object as its child. 
     * Warning: Child object must have a unique ID.
     * @param parent
     */
    public final void setParent(final SVGObject parent) {
        this.setParent(parent.toSVGData());
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
    public final void setAttribute(final SVGAttributeConstants attributeKey, final String attributeValue) {
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
    public final String getContent() {
        return this.content;
    }
    
    /**
     * Generates a string representation of SVG data captured within this object.
     * will be formatted with indentations and line breaks to be written 
     * directly to an output file.
     * @return an ArrayList<String> of lines in an SVG format.
     */
    public final List<String> toLines() {
        cleanAttributes();
        return toString(null);
    }
    
    /**
     * Prepares SVGData for export. 
     * Attributes containing null values are removed.
     */
    public void cleanAttributes() {
        this.attributes.values().removeIf(Objects::isNull);
        this.getAllChildren().forEach(SVGData::cleanAttributes);
    }
    
    /**
     * Recursive function to generate a string equivalent of complex SVG data 
     * captured within this object.
     * @param prefix
     * @return List<String> representation of the current element and all of it's child elements.
     */
    private List<String> toString(final String prefix) {
        final List<String> svgString = new ArrayList<>();
        if (this.children.isEmpty() && this.content == null) {
            svgString.add(elementToSVG(prefix));
        } else {
            svgString.add(elementHeaderToSVG(prefix));
            if (this.children.isEmpty()) {
                svgString.add(content);
            } else {
                svgString.addAll(elementChildrenToSVG(prefix));
            }
            svgString.add(elementFooterToSVG(prefix));
        }
        return svgString;
    }
    /**
     * Generates an "inline" SVG element.
     * @param prefix represents indentation prefixes for the element.
     * @return String representation of the current element
     */
    private String elementToSVG(final String prefix) {
        final StringBuilder attributeBuilder = new StringBuilder();
        final String linePrefix = SeparatorConstants.NEWLINE + (prefix == null ? "" : prefix);
        final Set<String> keys = attributes.keySet();
        keys.forEach(key -> {
            if (!SVGAttributeConstants.NAME_SPACE.getName().equals(key) || this.parent == null) {
                attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key)));
            }
        });

        return String.format("%s<%s%s />", linePrefix, this.getType(), attributeBuilder.toString());
    }
    
    /**
     * Generates the header or opening portion of an SVG element. 
     * The header will contain the element type and 
     * all of the attributes associated with the element.
     * @param prefix
     * @return String representation of element header.
     */
    private String elementHeaderToSVG(final String prefix) {
        final StringBuilder attributeBuilder = new StringBuilder();
        final String linePrefix = SeparatorConstants.NEWLINE + (prefix == null ? "" : prefix);
        final Set<String> keys = attributes.keySet();
        keys.forEach(key -> {
            if (!SVGAttributeConstants.NAME_SPACE.getName().equals(key) || this.parent == null) {
                attributeBuilder.append(String.format(" %s=\"%s\"", key, attributes.get(key)));
            }
        });
        return String.format("%s<%s%s>", linePrefix, this.getType(), attributeBuilder.toString());
    }
    
    /**
     * Generates the footer or closing portion of an SVG element. 
     * The footer will contain the element type.
     * @param prefix
     * @return String representation of element footer.
     */
    private String elementFooterToSVG(final String prefix) {
        final String linePrefix = SeparatorConstants.NEWLINE + (prefix == null ? "" : prefix);
        return String.format("%s</%s>", linePrefix, this.getType());
    }
    
    /**
     * Generates SVG equivalents for child elements of the current SVG element. 
     * The prefix of the current element should be provided as this method
     * manages the indented of child elements
     * 
     * @param prefix
     * @return List<String>
     */
    private List<String> elementChildrenToSVG (final String prefix) {
        final List<String> childSVGString = new ArrayList<>();
        final StringBuilder childPrefix = new StringBuilder(SeparatorConstants.TAB); 
        if (prefix != null) {
            childPrefix.append(prefix);
        }
        for (final SVGData child : this.getAllChildren()) {
            childSVGString.addAll(child.toString(childPrefix.toString()));
        }
        return childSVGString;
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
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return templateSVG;
    }
}
