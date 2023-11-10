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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

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
    
    private static final Logger LOGGER = Logger.getLogger(SVGObject.class.getName());
    
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
        
        if (parent.toSVGData() != null) {
            parent.setMinimumDimension(this.getXPosition() + this.getWidth(), this.getYPosition() + this.getHeight());
        }
    }
    
    public SVGObject getParent() {
        return new SVGObject(this.svgDataReference.getParent());
    }
    
    /**
     * Returns an SVGObject of matching id from the current SVGObject.
     * Use depth first search.
     * @param id
     * @return SVGObject
     */
    public final SVGObject getChild(final String id) {
        final SVGData child = this.svgDataReference.getChild(id);
        if (child != null) {
            return new SVGObject(child); 
        } else {
            return null;
        }
    }
    
    /**
     * Removes an SVGObject of matching id nested from the current SVGObject.
     * Uses depths first search.
     * @param id
     */
    public final void removeChild(final String id) {
        this.svgDataReference.removeChild(id);
    }
    
    private Collection<SVGObject> getAllChildren() {
        final ArrayList<SVGObject> children = new ArrayList<>();
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
        this.setAttribute(SVGAttributeConstant.ID, id);
    }
    
    /**
     * Sets the id value of the SVGObject.
     * This value must be unique, or may produce undesirable results.
     * id values should be descriptive.
     * Uniqueness is not currently enforced.
     * @param id 
     */
    public void setID(final String id) {
        this.setAttribute(SVGAttributeConstant.ID, id);
    }
    
    /**
     * Gets the id value of the SVGObject.
     * Will be useful when parent element id's prefix their child element id's to reflect their position in the SVGData structure.
     * @return idString
     */
    public final String getID() {
        return svgDataReference.getAttributeValue(SVGAttributeConstant.ID.getName());
    }
    
    /**
     * Adds a String attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstant attributeKey, final String attributeValue) {
        svgDataReference.setAttribute(attributeKey, attributeValue);
    }
    
    /**
     * Adds a float attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstant attributeKey, final float attributeValue) {
        svgDataReference.setAttribute(attributeKey, String.format("%.2f", attributeValue));
    }
    
    /**
     * Adds an int attribute to the current SVGData. 
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstant attributeKey, final int attributeValue) {
        svgDataReference.setAttribute(attributeKey, String.format("%s", attributeValue));
    }
    
    /**
     * Adds a ConstellationColor attribute to the current SVGData.
     * @param attributeKey 
     * @param attributeValue 
     */
    private void setAttribute(final SVGAttributeConstant attributeKey, final ConstellationColor attributeValue) {
        svgDataReference.setAttribute(attributeKey, String.format("%s", attributeValue.getHtmlColor()));
    }
    
    private String getAttributeString(final SVGAttributeConstant attributeKey) {
        return svgDataReference.getAttributeValue(attributeKey.getName());
    } 
    
    /**
     * Retrieves an attribute value as a float.
     * Returns null if the requested value is not a float. 
     * @param attributeKey
     * @return 
     */
    private Float getAttributeFloat(final SVGAttributeConstant attributeKey) {
        String attribute = getAttributeString(attributeKey);
        try {
            return Float.parseFloat(attribute);
        } catch (final Exception ex) {
             return null; 
        }
    } 
    
    /**
     * Sets the height of the SVGObject.
     * @param height
     */
    private void setHeight(final float height) {
        this.height = height;
        this.setAttribute(SVGAttributeConstant.HEIGHT, height);
    }
    
    /**
     * Ensures that the SVGObject has width that is greater than or equal to the provided width. 
     * 
     * @param minimumWidth
     * @param minimumHeight 
     */
    private void setMinimumHeight(final float minimumHeight) {
        if (this.getAttributeString(SVGAttributeConstant.HEIGHT) == null || !this.getAttributeString(SVGAttributeConstant.HEIGHT).contains("%")) {
            if (this.getHeight() < minimumHeight) {
                this.setHeight(minimumHeight);
                if (this.svgDataReference.getParent() != null) {
                    this.getParent().setMinimumHeight(minimumHeight);
                }
            }
        }
    }
    
    public float getHeight() {        
        return this.getPositionalData(this.height, SVGAttributeConstant.HEIGHT);
    }
    
    /**
     * Sets the width of the SVGObject.
     * @param width 
     */
    private void setWidth(final float width) {
        this.width = width;
        this.setAttribute(SVGAttributeConstant.WIDTH, width);
    }
    
    /**
     * Ensures that the SVGObject has width that is greater than or equal to the provided width. 
     * 
     * @param minimumWidth
     * @param minimumHeight 
     */
    private void setMinimumWidth(final float minimumWidth) {
        if (this.getAttributeString(SVGAttributeConstant.WIDTH) == null || !this.getAttributeString(SVGAttributeConstant.WIDTH).contains("%")) {      
            if (this.getWidth() < minimumWidth) {
                this.setWidth(minimumWidth);
                if (this.svgDataReference.getParent() != null) {
                    this.getParent().setMinimumWidth(minimumWidth);
                }
            }
        }
    }
    
    public float getWidth() {
        return this.getPositionalData(this.width, SVGAttributeConstant.WIDTH);
    }
    
    private float getPositionalData(final Float quickReference, final SVGAttributeConstant longReference) {
        
        if (quickReference != null) {
            //Return the localy stored value;
            return quickReference;
        }
            
        final Float attributeFloat = this.getAttributeFloat(longReference);          
        if (attributeFloat != null) {
            //Return the value set during parsing;
            return attributeFloat;
        } else {
            //No value has been set
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
     * Ensures that the SVGObject has dimensions that are greater than or equal two the provided dimensions. 
     * 
     * @param minimumWidth
     * @param minimumHeight 
     */
    public void setMinimumDimension(final float minimumWidth, final float minimumHeight) {
        this.setMinimumWidth(minimumWidth);
        this.setMinimumHeight(minimumHeight);
        
    }
    
    /**
     * Sets the X position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param x 
     */
    private void setXPosition(final float x) {
        this.x = x;
        if ("circle".equals(this.toSVGData().getType())) {
            this.setAttribute(SVGAttributeConstant.CX, x);
        } else {
            this.setAttribute(SVGAttributeConstant.X, x);
        }
        
    }
    
    public float getXPosition() {
        return this.getPositionalData(this.x, SVGAttributeConstant.X);
    }
    /**
     * Sets the Y position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param y
     */
    public void setYPosition(final float y) {
        this.y = y;
        if ("circle".equals(this.toSVGData().getType())) {
            this.setAttribute(SVGAttributeConstant.CY, y);
        } else {
            this.setAttribute(SVGAttributeConstant.Y, y);
        }
    }
    
    public float getYPosition() {
        return this.getPositionalData(this.y, SVGAttributeConstant.Y);
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

        this.setAttribute(SVGAttributeConstant.SOURCE_X, position.getX());
        this.setAttribute(SVGAttributeConstant.SOURCE_Y, position.getY());
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
        
        this.setAttribute(SVGAttributeConstant.DESTINATION_X, position.getX());
        this.setAttribute(SVGAttributeConstant.DESTINATION_Y, position.getY());
    }
    
    public void setFontSize(final float size) {
        this.setAttribute(SVGAttributeConstant.FONT_SIZE, size);
    }
    /**
     * Sets the transformation values of the SVGObject.
     * @param transformationData
     */
    public void setTransformation(final String transformationData) {
        this.setAttribute(SVGAttributeConstant.TRANSFORM,  transformationData);
    }
    
    /**
     * Sets the color of the SVGObject.
     * @param color 
     */
    public void setFillColor(final ConstellationColor color) {
        this.setAttribute(SVGAttributeConstant.FILL_COLOR, color);
    }

    /**
     * Sets the stoke color of the SVGObject.
     * @param color 
     */
    public void setStrokeColor(final ConstellationColor color) {
        this.setAttribute(SVGAttributeConstant.STROKE_COLOR, color);
    }
    
    /**
     * Sets the stroke style of the SVGObject.
     * @param style 
     */
    public void setStrokeStyle(final LineStyle style) {
        if (style == LineStyle.DOTTED) {
            this.setStrokeArray(35, 35);
        } else if (style == LineStyle.DASHED) {
            this.setStrokeArray(70, 35);
        }
    }
    
    public void setStrokeArray(final float strokeA, final float strokeB) {
        this.setAttribute(SVGAttributeConstant.DASH_ARRAY, String.format("%s %s", strokeA, strokeB));
    }
    
    public void setStrokeArray(final float strokeA, final float strokeB, final float strokeC) {
        this.setAttribute(SVGAttributeConstant.DASH_ARRAY, String.format("%s %s %s", strokeA, strokeB, strokeC));
    }
    
    /**
     * Removes the SVGObject wrapper class from the SVGData.
     * preserves all relevant SVG elements for generating svgText
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
    
    public static SVGObject loadFromInputStream(InputStream is) {
        try{
            return new SVGObject(SVGParser.parse(is));
        } catch (IOException e) {
            return null;
        }
    }

    public void setDimensionScale(final String width, final String height) {
        this.setViewBox(0, 0, this.width, this.height);
        this.svgDataReference.setAttribute(SVGAttributeConstant.WIDTH, width);
        this.svgDataReference.setAttribute(SVGAttributeConstant.HEIGHT, height);
    }
    
    public void setViewBox(final float x, final float y, final float w, final float h) {
        this.setAttribute(SVGAttributeConstant.VIEW_BOX, String.format("%s, %s, %s, %S", x, y, w, h));
    }
    
    public void saturateSVG(final ConstellationColor color) {
        this.setFillColor(color);
        this.setStrokeColor(color);
        this.getAllChildren().forEach(child -> child.saturateSVG(color));
    }
    
    public void setSortOrderValue(final float sortOrderValue) {
        this.setAttribute(SVGAttributeConstant.CUSTOM_SORT_ORDER, sortOrderValue);
    }

    public void applyGrayScaleFileter() {
        this.setAttribute(SVGAttributeConstant.FILTER, "grayscale(1)");
    }

    public void setBaseline(final String baseline) {
        setAttribute(SVGAttributeConstant.BASELINE, baseline);
    }
    
    public void setPoints(final String path) {
        setAttribute(SVGAttributeConstant.POINTS, path);
    }
}
