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

import java.util.ArrayList;

/**
 * A wrapper class for SVGObjects.
 * Represents a non renderable element that serves to provide
 * a container for elements to base their relative positions from.
 * Enables the sizing and positioning of an element and all of the 
 * elements nested within it.
 * 
 * @author capricornunicorn123
 */
public class SVGContainer {
    
    final private SVGObject svgObjectReference;
    
    public SVGContainer(final SVGObject svg) {
        this.svgObjectReference = svg;
    }
    
    /**
     * Sets the height of the container element.
     * @param height
     */
    private void setHeight(final Float height){
        svgObjectReference.setAttribute("height", height.toString());
    }
    
    /**
     * Sets the width of the container element.
     * @param width 
     */
    private void setWidh(final Float width){
        svgObjectReference.setAttribute("width", width.toString());
    }
    
    /**
     * Sets the X position of the container element.
     * With respect to the element it is contained within.
     * @param x 
     */
    private void setXPosition(final Float x){
        svgObjectReference.setAttribute("x", x.toString());
    }
    
    /**
     * Sets the y position of the container element.
     * With respect to the element it is contained within.
     * @param y
     */
    private void setYPosition(final Float y){
        svgObjectReference.setAttribute("y", y.toString());
    }
    
    /**
     * Sets the height and width attributes of the container element.
     * Current implementations of this method are undesirable. 
     * dimensions are defined and set by the SVGGraphBuilder. 
     * Eventually this method should be able to set its own dimensions 
     * based off of the dimensions of the elements nested within it.
     * @param width
     * @param height
     */
    public void setDimension(final Float width, final Float height){
        this.setWidh(width);
        this.setHeight(height);
    }
    
    /**
     * Sets the position of the container element.
     * With respect to the element it is contained within.
     * @param x
     * @param y 
     */
    public void setposition(final Float x, final Float y){
        this.setXPosition(x);
        this.setYPosition(y);
    }

    /**
     * Returns an SVGContainer nested one level down from the current SVGContainer.
     * To be considered a container the element must be nested, be of type svg 
     * and must contain a unique class name.
     * @param classValue
     * @return 
     */
    public final SVGContainer getContainer(final String classValue) {
        final ArrayList<SVGObject> children = this.svgObjectReference.getChildren(classValue);
        if (children != null && children.size() == 1){
           return new SVGContainer(children.get(0)); 
        }
        else {
            return null;
        }
    }
    
    /**
     * Removes the SVGContainer wrapper class from the SVGObject.
     * @return 
     */
    public final SVGObject toSVGObject() {
        return svgObjectReference;
    }
    
}
