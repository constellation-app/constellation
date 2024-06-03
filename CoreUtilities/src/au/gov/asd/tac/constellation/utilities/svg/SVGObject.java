/*
* Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for SVGData to enable simplified usage.
 * Provides a set of helper methods to construct SVGData.
 * The user is prevented from setting unsupported attributes through this SVDObject wrapper.
 * This class should provide a barrier to prevent unsupported attributes and values from being set.
 * Custom attributes can bypass this barrier though SVGData 
 * by converting this class back to a SVGData class with toSVGData();
 * 
 * @author capricornunicorn123
 */
public class SVGObject {
    
    private final SVGData svgDataReference;
    private Float x = null;
    private Float y = null;
    private Float width = null;
    private Float height = null;
    
    /**
     * Wrapper class for SVGObjects. 
     * Provides helper methods to manipulate SVGData efficiently.
     * @param svg 
     */
    public SVGObject(final SVGData svg) {
        this.svgDataReference = svg;
    }
    
    /**
     * Sets the relationship between child and parent SVGObject through association of respective SVGData references.
     * Associations can only be made by child element setting their parent element. 
     * Doing so prompts the parent element to register the child element as its child and update its size
     * @param parent
     */
    public void setParent(final SVGObject parent) {
        svgDataReference.setParent(parent.toSVGData());
    }
    
    public SVGObject getParent() {
        final SVGData parent = this.svgDataReference.getParent();
        return parent != null ? new SVGObject(this.svgDataReference.getParent()) : null;
    }
    
    /**
     * Returns an SVGObject of matching id from the current SVGObject.
     * Use depth first search.
     * @param id
     * @return SVGObject
     */
    public final SVGObject getChild(final String id) {
        final SVGData child = this.svgDataReference.getChild(id);
        return child != null ? new SVGObject(child) : null;
    }
    
    /**
     * Removes an SVGObject of matching id nested from the current SVGObject.
     * Uses depths first search.
     * @param id
     */
    public final void removeChild(final String id) {
        this.svgDataReference.removeChild(id);
    }
    
    /**
     * Gets a reference to all children of this SVGObject. 
     * @return 
     */
    private List<SVGObject> getAllChildren() {
        final List<SVGObject> children = new ArrayList<>();
        this.svgDataReference.getAllChildren().forEach(child -> children.add(new SVGObject(child)));
        return children;
    }   
    
    /**
     * Sets the content value of an SVGObjects.
     * Content is considered plain text that lies within a SVG element 
     * such as is the case with text elements.
     * @param content
     */
    public void setContent(final String content) {
        this.svgDataReference.setContent(SVGParser.sanitisePlanText(content));
    }
    
    /**
     * Sets the id value of the SVGObject.
     * This value must be unique, or may produce undesirable results.
     * id values should be descriptive.
     * Uniqueness is not currently enforced.
     * @param id 
     */
    public void setID(final int id) {
        this.setAttribute(SVGAttributeConstants.ID, id);
    }
    
    /**
     * Sets the id value of the SVGObject.
     * This value must be unique, or may produce undesirable results.
     * id values should be descriptive.
     * Uniqueness is not currently enforced.
     * @param id 
     */
    public void setID(final String id) {
        this.setAttribute(SVGAttributeConstants.ID, id);
    }
    
    /**
     * Gets the id value of the SVGObject.
     * Will be useful when parent element id's prefix their child element id's to reflect their position in the SVGData structure.
     * @return idString
     */
    public final String getID() {
        return svgDataReference.getAttributeValue(SVGAttributeConstants.ID.getName());
    }
    
    /**
     * Adds a String attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstants attributeKey, final String attributeValue) {
        svgDataReference.setAttribute(attributeKey, attributeValue);
    }
    
    /**
     * Adds a float attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstants attributeKey, final float attributeValue) {
        svgDataReference.setAttribute(attributeKey, String.format("%.2f", attributeValue));
    }
    
    /**
     * Adds an int attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstants attributeKey, final int attributeValue) {
        svgDataReference.setAttribute(attributeKey, String.valueOf(attributeValue));
    }
    
    /**
     * Adds a ConstellationColor attribute to the current SVGData.
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstants attributeKey, final ConstellationColor attributeValue) {
        svgDataReference.setAttribute(attributeKey, attributeValue.getHtmlColor());
    }
    
    private String getAttributeString(final SVGAttributeConstants attributeKey) {
        return svgDataReference.getAttributeValue(attributeKey.getName());
    } 
    
    /**
     * Retrieves an attribute value as a float.
     * Returns null if the requested value is not a float. 
     * @param attributeKey
     * @return 
     */
    private Float getAttributeFloat(final SVGAttributeConstants attributeKey) {
        final String attribute = getAttributeString(attributeKey);
        if (attribute == null){
            return null;
        }
        try {
            return Float.parseFloat(attribute);
        } catch (final NumberFormatException e) {
            return null; 
        }
    } 
    
    /**
     * Sets the height of the SVGObject.
     * @param height
     */
    private void setHeight(final float height) {
        // Get the current height wihtout the local reference. 
        final float currentWidth = getPositionalData(null, SVGAttributeConstants.HEIGHT);
        
        //This evluation will be true if the current height is a percentage so set the width as the viewbox attribute.
        if (this.height != null && currentWidth != this.height){
            this.height = height;
            setViewBox(this.x, this.y, this.width, this.height);
        } else {
            this.height = height;
            this.setAttribute(SVGAttributeConstants.HEIGHT, height);
        }
    }
    
    public float getHeight() {        
        return this.getPositionalData(this.height, SVGAttributeConstants.HEIGHT);
    }
    
    /**
     * Sets the width of the SVGObject.
     * @param width 
     */
    private void setWidth(final float width) {       
        // Get the current width wihtout the local reference. 
        final float currentWidth = getPositionalData(null, SVGAttributeConstants.WIDTH);
        
        // This evluation will be true if the current width is a percentage so set the width as the viewbox attribute.
        if (this.width != null && currentWidth != this.width){
            this.width = width;
            setViewBox(this.x, this.y, this.width, this.height);
        } else {
            this.width = width;
            this.setAttribute(SVGAttributeConstants.WIDTH, width);
        }
    }
      
    public float getWidth() {
        return this.getPositionalData(this.width, SVGAttributeConstants.WIDTH);
    }
    
    private float getPositionalData(final Float quickReference, final SVGAttributeConstants longReference) {  
        if (quickReference != null) {
            // The localy stored value
            return quickReference;
        }
        final Float attributeFloat = this.getAttributeFloat(longReference);          
        if (attributeFloat != null) {
            // The value set during parsing
            return attributeFloat;
        } else {
            // No value has been set
            return 0;
        }
    }
    
    /**
     * Sets the height and width attributes of the SVGObject.
     * Current implementations of this method are undesirable. 
     * Dimensions are defined and set by the SVGGraphBuilder. 
     * Eventually this method should be able to set its own dimensions 
     * based off of the dimensions of the elements nested within it.
     * @param width
     * @param height
     */
    public void setDimension(final float width, final float height) {
        this.setWidth(width);
        this.setHeight(height);
    }
    
    /**
     * Sets the X position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param x 
     */
    private void setXPosition(final float x) {
        this.x = x;
        if ("circle".equals(this.toSVGData().getType())) {
            this.setAttribute(SVGAttributeConstants.CX, x);
        } else {
            this.setAttribute(SVGAttributeConstants.X, x);
        }
        
    }
    
    public float getXPosition() {
        return this.getPositionalData(this.x, SVGAttributeConstants.X);
    }
    /**
     * Sets the Y position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param y
     */
    public void setYPosition(final float y) {
        this.y = y;
        if ("circle".equals(this.toSVGData().getType())) {
            this.setAttribute(SVGAttributeConstants.CY, y);
        } else {
            this.setAttribute(SVGAttributeConstants.Y, y);
        }
    }
    
    public float getYPosition() {
        return this.getPositionalData(this.y, SVGAttributeConstants.Y);
    }
     
    /**
     * Sets the position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param x
     * @param y 
     */
    public void setPosition(final float x, final float y) {
        this.setXPosition(x);
        this.setYPosition(y);
    }
    
    /**
     * Sets the position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param x
     * @param y 
     */
    public void setPosition(final double x, final double y) {
        this.setXPosition((float) x);
        this.setYPosition((float) y);
    }
    
    /**
     * Sets the source position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param position
     */
    public void setSourcePosition(final Vector4f position) {
        if (this.x != null) {
            this.width = Math.abs(this.x - position.getX());
        }
        
        if (this.x == null || this.x > position.getX()) {
            this.x = position.getX();
        }
        
        if (this.y != null) {
            this.height = Math.abs(this.y - position.getY());
        }
        
        if (this.y == null || this.y > position.getY()) {
            this.y = position.getY();
        }

        this.setAttribute(SVGAttributeConstants.SOURCE_X, position.getX());
        this.setAttribute(SVGAttributeConstants.SOURCE_Y, position.getY());
    }
    
    /**
     * Sets the destination position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param position
     */
    public void setDestinationPosition(final Vector4f position) {
        if (this.x != null) {
            this.width = Math.abs(this.x - position.getX());
        }
        
        if (this.x == null || this.x > position.getX()) {
            this.x = position.getX();
        }
        
        if (this.y != null) {
            this.height = Math.abs(this.y - position.getY());
        }
        
        if (this.y == null || this.y > position.getY()) {
            this.y = position.getY();
        }
        
        this.setAttribute(SVGAttributeConstants.DESTINATION_X, position.getX());
        this.setAttribute(SVGAttributeConstants.DESTINATION_Y, position.getY());
    }
    
    public void setFontSize(final float size) {
        this.setAttribute(SVGAttributeConstants.FONT_SIZE, size);
    }
    /**
     * Sets the transformation values of the SVGObject.
     * @param transformationData
     */
    public void setTransformation(final String transformationData) {
        this.setAttribute(SVGAttributeConstants.TRANSFORM,  transformationData);
    }
    
    /**
     * Sets the color of the SVGObject.
     * @param color 
     */
    public void setFillColor(final ConstellationColor color) {
        this.setAttribute(SVGAttributeConstants.FILL_COLOR, color);
    }

    /**
     * Sets the stoke color of the SVGObject.
     * @param color 
     */
    public void setStrokeColor(final ConstellationColor color) {
        this.setAttribute(SVGAttributeConstants.STROKE_COLOR, color);
    }
    
    /**
     * Sets the stroke style of the SVGObject.
     * @param style 
     */
    public void setStrokeStyle(final LineStyle style) {
        switch (style) {
            case DOTTED -> this.setStrokeArray(35, 35);
            case DASHED -> this.setStrokeArray(70, 35);
            default -> this.svgDataReference.setAttribute(SVGAttributeConstants.DASH_ARRAY, null);
        }
    }
    
    public void setStrokeArray(final float strokeA, final float strokeB) {
        this.setAttribute(SVGAttributeConstants.DASH_ARRAY, String.format("%s %s", strokeA, strokeB));
    }
    
    public void setStrokeArray(final float strokeA, final float strokeB, final float strokeC) {
        this.setAttribute(SVGAttributeConstants.DASH_ARRAY, String.format("%s %s %s", strokeA, strokeB, strokeC));
    }
    
    public void setDimensionScale(final String width, final String height) {
        this.setViewBox(null, null, this.width, this.height);
        this.svgDataReference.setAttribute(SVGAttributeConstants.WIDTH, width);
        this.svgDataReference.setAttribute(SVGAttributeConstants.HEIGHT, height);
    }
    
    public void setViewBox(final Float x, final Float y, final Float w, final Float h) {
        final float vbx = x == null ? 0F : x;
        final float vby = y == null ? 0F : y;
        final float vbw = w == null ? 0F : w;
        final float vbh = h == null ? 0F : h;
        
        this.setAttribute(SVGAttributeConstants.VIEW_BOX, String.format("%s, %s, %s, %S", vbx, vby, vbw, vbh));
    }
    
    public void saturateSVG(final ConstellationColor color) {
        this.setFillColor(color);
        this.setStrokeColor(color);
        this.getAllChildren().forEach(child -> child.saturateSVG(color));
    }
    
    /**
     * Sets the order at which svg elements will be rendered to the screen.
     * Elements with lower sortOrderValues will render on top of elements with larger sortOrderValues. 
     * @param sortOrderValue 
     */
    public void setSortOrderValue(final float sortOrderValue) {
        this.setAttribute(SVGAttributeConstants.CUSTOM_SORT_ORDER, sortOrderValue);
    }

    public void applyGrayScaleFilter() {
        this.setAttribute(SVGAttributeConstants.FILTER, "grayscale(1)");
    }

    public void setBaseline(final String baseline) {
        setAttribute(SVGAttributeConstants.BASELINE, baseline);
    }
    
    /**
     * Takes a set of Vector4f objects representing points in 2d space and stores them as a points attribute in the SVGObject
     * @param points 
     */
    public void setPoints(final Vector4f... points) {
        final StringBuilder sb = new StringBuilder();
        for (final Vector4f point : points){
            sb.append(String.format("%s %s, ", point.getX(), point.getY()));
        }
        setAttribute(SVGAttributeConstants.POINTS, sb.toString().substring(0, sb.length()-2));
    } 
    
    /**
     * Sets the opacity of a SVGObject.
     * @param opacity value between 0.0 and 1.0
     */
    public void setOpacity(final float opacity) {
        setAttribute(SVGAttributeConstants.OPACITY, opacity);
    }
    
    /**
     * Removes the SVGObject wrapper class from the SVGData.
     * Preserves all relevant SVG elements for generating svgText
     * @return svgData
     */
    public final SVGData toSVGData() {
        return svgDataReference;
    }       
    
    /**
    * Creates SVGObject from a template SVG file.
    * The object will be returned with no parent.
    * <pre>
    * Example Usage: {@code SVGObject.loadFromTemplate(SVGFileNameContant);}
    * </pre>
    * @param templateResource the filename of the template file.
    * @return svgObject
    */
    public static final SVGObject loadFromTemplate(final SVGFile templateResource) {
        return new SVGObject(SVGData.loadFromTemplate(templateResource));
    } 
    
    public static SVGObject loadFromInputStream(final InputStream is) {
        try {
            return new SVGObject(SVGParser.parse(is));
        } catch (final IOException e) {
            return null;
        }
    }
}
