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

import au.gov.asd.tac.constellation.plugins.importexport.svg.parser.SVGParser;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGFileNameConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
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
    private final SVGData svgRataReference;
    
    /**
     * Wrapper class for SVGObjects. 
     * Provides helper methods to manipulate SVGData efficiently.
     * @param svg 
     */
    private SVGObject(final SVGData svg) {
        this.svgRataReference = svg;
    }
    
    /**
     * Sets the relationship between child and parent SVGObject through association of respective SVGData references.
     * Associations can only be made by child element setting their parent element. 
     * Doing so prompts the parent element to register the child element as its child.
     * @param parent
     */
    public void setParent(final SVGObject parent) {
        svgRataReference.setParent(parent.toSVGData());
    }
    
    /**
     * Returns an SVGObject of matching id nested one level down from the current SVGObject.
     * @param idValue
     * @return SVGObject
     */
    public final SVGObject getChild(final SVGLayoutConstant id) {
        final SVGData child = this.svgRataReference.getChild(id.getValue());
        if (child != null){
            return new SVGObject(child); 
        } else {
            return null;
        }
    }
    
    /**
     * Sets the content value of an SVGObjects.
     * Content is considered plain text that lies within a SVG element 
     * such as is the case with text elements.
     * @param content
     */
    public void setContent(final String content) {
        this.svgRataReference.setContent(SVGParser.sanitisePlanText(content));
    }
    
    /**
     * Sets the id value of the SVGObject.
     * This value must be unique, or may produce undesirable results.
     * id values should be descriptive.
     * Uniqueness is not currently enforced.
     * @param id 
     */
    public void setID(final int id){
        this.setAttribute(SVGAttributeConstant.ID, id);
    }
    
    /**
     * Sets the id value of the SVGObject.
     * This value must be unique, or may produce undesirable results.
     * id values should be descriptive.
     * Uniqueness is not currently enforced.
     * @param id 
     */
    public void setID(final String id){
        this.setAttribute(SVGAttributeConstant.ID, id);
    }
    
    /**
     * Gets the id value of the SVGObject.
     * Will be useful when parent element id's prefix their child element id's to reflect their position in the SVGData structure.
     * @param id 
     * @return idString
     */
    public final String getID(){
        return svgRataReference.getAttributeValue(SVGAttributeConstant.ID.getName());
    }
    
    /**
     * Adds a String attribute to the current SVGData. 
     * NOTE:    This class has been made public to increase the pace of development. 
     *          Should be made private prior to release to ensure SVGData barrier is enforced in future development.
     * @param attributeKey 
     * @param attributeValue 
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final String attributeValue) {
        svgRataReference.setAttribute(attributeKey, attributeValue);
    }
    
    /**
     * Adds a float attribute to the current SVGData. 
     * NOTE:    This class has been made public to increase the pace of development. 
     *          Should be made private prior to release to ensure SVGData barrier is enforced in future development.
     * @param attributeKey 
     * @param attributeValue 
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final float attributeValue) {
        svgRataReference.setAttribute(attributeKey, String.format("%.2f", attributeValue));
    }
    
    /**
     * Adds an int attribute to the current SVGData. 
     * NOTE:    This class has been made public to increase the pace of development. 
     *          Should be made private prior to release to ensure SVGData barrier is enforced in future development.
     * @param attributeKey 
     * @param attributeValue 
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final int attributeValue) {
        svgRataReference.setAttribute(attributeKey, String.format("%s", attributeValue));
    }
    
    /**
     * Adds a double attribute to the current SVGData.
     * NOTE:    This class has been made public to increase the pace of development. 
     *          Should be made private prior to release to ensure SVGData barrier is enforced in future development.
     * @param attributeKey 
     * @param attributeValue 
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final double attributeValue) {
        svgRataReference.setAttribute(attributeKey, String.format("%.2f", attributeValue));
    }
    
    /**
     * Adds a ConstellationColor attribute to the current SVGData.
     * NOTE:    This class has been made public to increase the pace of development. 
     *          Should be made private prior to release to ensure SVGData barrier is enforced in future development.
     * @param attributeKey 
     * @param attributeValue 
     */
    public final void setAttribute(final SVGAttributeConstant attributeKey, final ConstellationColor attributeValue) {
        svgRataReference.setAttribute(attributeKey, String.format("%s", attributeValue.getHtmlColor()));
    }
    
    /**
     * Sets the height of the SVGObject.
     * @param height
     */
    private void setHeight(final float height) {
        this.setAttribute(SVGAttributeConstant.HEIGHT, height);
    }
    
    /**
     * Sets the width of the SVGObject.
     * @param width 
     */
    private void setWidth(final float width) {
        this.setAttribute(SVGAttributeConstant.WIDTH, width);
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
        this.setAttribute(SVGAttributeConstant.X, x);
    }
    /**
     * Sets the X position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param x 
     */
    private void setXPosition(final double x) {
        this.setAttribute(SVGAttributeConstant.X, x);
    }
    /**
     * Sets the Y position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param y
     */
    private void setYPosition(final float y) {
        this.setAttribute(SVGAttributeConstant.Y, y);
    }
    
    /**
     * Sets the Y position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param y
     */
    private void setYPosition(final double y) {
        this.setAttribute(SVGAttributeConstant.Y, y);
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
        this.setXPosition(x);
        this.setYPosition(y);
    }
    
    /**
     * Sets the position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param position 
     */
    public void setPosition(final Tuple<Double, Double> position) {
        this.setXPosition(position.getFirst());
        this.setYPosition(position.getSecond());
    }
    
    /**
     * Sets the source position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param posiition
     */
    void setSourcePosition(Tuple<Double, Double> position) {
        this.setAttribute(SVGAttributeConstant.SOURCE_X, position.getFirst());
        this.setAttribute(SVGAttributeConstant.SOURCE_Y, position.getSecond());
    }
    
    /**
     * Sets the destination position of the SVGObject.
     * With respect to the SVGObject it is contained within.
     * @param position
     */
    void setDestinationPosition(Tuple<Double, Double> position) {
        this.setAttribute(SVGAttributeConstant.DESTINATION_X, position.getFirst());
        this.setAttribute(SVGAttributeConstant.DESTINATION_Y, position.getSecond());
    }
    
    /**
     * Sets the transformation values of the SVGObject.
     * @param position
     */
    void setTransformation(String transformationData) {
        this.setAttribute(SVGAttributeConstant.TRANSFORM,  transformationData);
    }
    
    /**
     * Sets the color of the SVGObject.
     * @param color 
     */
    public void setFillColor(ConstellationColor color){
        this.setAttribute(SVGAttributeConstant.FILL_COLOR, color);
    }

    /**
     * Sets the stoke color of the SVGObject.
     * @param color 
     */
    void setStrokeColor(ConstellationColor color) {
        this.setAttribute(SVGAttributeConstant.STROKE_COLOR, color);
    }
    
    /**
     * Sets the stroke style of the SVGObject.
     * @param style 
     */
    void setStrokeStyle(LineStyle style) {
        if (style == LineStyle.DOTTED) {
            this.setAttribute(SVGAttributeConstant.DASH_ARRAY, "35 35");
        } else if (style == LineStyle.DASHED) {
            this.setAttribute(SVGAttributeConstant.DASH_ARRAY, "70 35");
        }
    }
    
    /**
     * Removes the SVGObject wrapper class from the SVGData.
     * preserves all relevant SVG elements for generating svgText
     * @return svgData
     */
    public final SVGData toSVGData() {
        return svgRataReference;
    }       
    
    /**
    * Creates SVGObject from a template SVG file.
    * The object will be returned with no parent.
    * @param templateResource the filename of the template file.
    * @return svgObject
    */
    public static final SVGObject loadFromTemplate(final SVGFileNameConstant templateResource) {
        return new SVGObject(SVGData.loadFromTemplate(templateResource));
    } 
}
