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
 * This class serves to provide references to resource file names as constants. 
 * the class also serves to provide the class path to the resources 
 * as it is located within the same package. 
 * 
 * @author capricornunicorn123
 */
public enum SVGFileNameConstant {
    NODE("Node.svg"),
    CONNECTION("Connection.svg"),
    LINK_ARROW_HEAD("LinkArrowHead.svg"),
    TRANSACTION_ARROW_HEAD("TransactionArrowHead.svg"),
    LAYOUT("Layout.svg"),
    BOTTOM_LABEL("BottomLabel.svg"),
    TOP_LABEL("TopLabel.svg"),
    IMAGE("Image.svg");
    
    public final String resourceName;
        
    private SVGFileNameConstant(final String resourceName){
        this.resourceName = resourceName;
    }
}