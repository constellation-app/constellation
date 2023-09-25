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
 * Class to capture constant values for stings that indicate a svgContainer
 * element. these element names are tightly coupled to the Layout.svg file and
 * reduce the repetitive use of string literals.
 *
 * @author capricornunicorn123
 */
public enum SVGLayoutConstant {
    NODES("nodes"),
    CONNECTIONS("connections"),
    CONTENT("content"),
    HEADER("header"),
    FOOTER("footer"),
    BACKGROUND("background"),
    BORDER("border"),
    FOOTNOTE("footnote"),
    BOTTOM_LABELS("bottom-labels"),
    TOP_LABELS("top-labels"),
    BACKGROUND_IMAGE("background-image"),
    FOREGROUND_IMAGE("foreground-image"),
    NORTH_WEST_DECORATOR("north-west-decorator"),
    NORTH_EAST_DECORATOR("north-east-decorator"),
    SOUTH_WEST_DECORATOR("south-west-decorator"),
    SOUTH_EAST_DECORATOR("south-east-decorator"),
    TITLE("title"),
    SUBTITLE("subtitle"),
    ARROW_SHAFT("arrow-shaft");

    public final String id;

    private SVGLayoutConstant(final String id) {
        this.id = id;
    }
}
