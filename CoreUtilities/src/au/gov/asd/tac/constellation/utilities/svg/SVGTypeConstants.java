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

/**
 * A constant reference for SVG element types.
 * 
 * @author capricornunicorn123
 */
public enum SVGTypeConstants {
    A("a"),
    ANIMATE("animate"),
    ANIMATEMOTION("animateMotion"),
    ANIMATETRANSFORM("animateTransform"),
    CIRCLE("circle"),
    CLIPPATH("clipPath"),
    DEFS("defs"),
    DESC("desc"),
    ELLIPSE("ellipse"),
    FEBLEND("feBlend"),
    FECOLORMATRIX("feColorMatrix"),
    FECOMPONENTTRANSFER("feComponentTransfer"),
    FECOMPOSITE("feComposite"),
    FECONVOLVEMATRIX("feConvolveMatrix"),
    FEDIFFUSELIGHTING("feDiffuseLighting"),
    FEDISPLACEMENTMAP("feDisplacementMap"),
    FEDISTANTLIGHT("feDistantLight"),
    FEDROPSHADOW("feDropShadow"),
    FEFLOOD("feFlood"),
    FEFUNCA("feFuncA"),
    FEFUNCB("feFuncB"),
    FEFUNCG("feFuncG"),
    FEFUNCR("feFuncR"),
    FEGAUSSIANBLUR("feGaussianBlur"),
    FEIMAGE("feImage"),
    FEMERGE("feMerge"),
    FEMERGENODE("feMergeNode"),
    FEMORPHOLOGY("feMorphology"),
    FEOFFSET("feOffset"),
    FEPOINTLIGHT("fePointLight"),
    FESPECULARLIGHTING("feSpecularLighting"),
    FESPOTLIGHT("feSpotLight"),
    FETILE("feTile"),
    FETURBULENCE("feTurbulence"),
    FILTER("filter"),
    FOREIGNOBJECT("foreignObject"),
    G("g"),
    HATCH("hatch"),
    HATCHPATH("hatchpath"),
    IMAGE("image"),
    LINE("line"),
    LINEARGRADIENT("linearGradient"),
    MARKER("marker"),
    MASK("mask"),
    METADATA("metadata"),
    MPATH("mpath"),
    PATH("path"),
    PATTERN("pattern"),
    POLYGON("polygon"),
    POLYLINE("polyline"),
    RADIALGRADIENT("radialGradient"),
    RECT("rect"),
    SCRIPT("script"),
    SET("set"),
    STOP("stop"),
    STYLE("style"),
    SVG("svg"),
    SWITCH("switch"),
    SYMBOL("symbol"),
    TEXT("text"),
    TEXTPATH("textPath"),
    TITLE("title"),
    TSPAN("tspan"),
    USE("use"),
    VIEW("view");

    private final String name; 

    private SVGTypeConstants(final String type){
        this.name = type;
    }

    public String getTypeString(){
        return this.name;
    }
    
    public static SVGTypeConstants getType(final String name){
        if (name != null) {
            for (SVGTypeConstants constant : SVGTypeConstants.values()){
                if (name.equals(constant.getTypeString())){
                    return constant;
                }
            }
        }
        return null;
    }
}
