/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.welcome;

import javafx.scene.control.Button;

/**
 * A plugin designed to be supported by the Welcome Page.
 *
 * @author canis_majoris
 */
public abstract class WelcomePageProvider {
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    public String getName() {
        return null;
    }
    
    /**
     * Get a description for the link that will appear on the Welcome Page 
     *
     * @return a unique reference
     */
    public String getLinkDescription() {
        return null;
    }
    
    /**
     * Get a link to appear on the Welcome Page
     *
     * @return a link address
     */
    public String getLink() {
        StringBuilder href = new StringBuilder();
        href.append("<a href=\"");
        href.append(getName());
        href.append("\">");
        href.append(getLinkDescription());
        href.append("</a>");
        href.append(getDescription());
        return href.toString();
    }
    
    /**
     * Get an optional textual description that appears on the Welcome Page.
     *
     * @return a unique reference
     */
    public String getDescription() {
        return "";
    }
    
    /**
     * Returns a link to a resource that can be used instead of text.
     *
     * @return a unique reference
     */
    public String getImage() {
        return null;
    }

    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    public void run() {
    }

    /**
     * Determines whether this analytic appear on the Welcome Page 
     *
     * @return true is this analytic should be visible, false otherwise.
     */
    public boolean isVisible() {
        return true;
    }
    
    /**
     * Creates the button object to represent this plugin
     * 
     * @return the button object
     */
    public Button getButton() {
        return null;
    }
}
