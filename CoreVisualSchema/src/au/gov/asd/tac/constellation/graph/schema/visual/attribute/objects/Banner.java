/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;

/**
 * A text banner to be displayed at the top and bottom of the main window.
 *
 * @author algol
 */
public class Banner {

    public static final String ATTRIBUTE_NAME = "banner";
    private boolean active;
    private final int level;
    private final String text;
    private final ConstellationColor fgColor;
    private final ConstellationColor bgColor;
    private final String template;

    public Banner() {
        this.active = true;
        this.level = 0;
        this.text = "";
        this.fgColor = ConstellationColor.WHITE;
        this.bgColor = ConstellationColor.BLACK;
        this.template = "";
    }

    /**
     * Create a new Banner.
     *
     * @param active whether banner is active or not
     * @param level The level of the Banner (higher is more important).
     * @param text The text of the banner.
     * @param fgColor The foreground color.
     * @param bgColor The background color.
     * @param template name of the banner template (empty string if user
     * defined)
     */
    public Banner(final boolean active, final int level, final String text, final ConstellationColor fgColor, final ConstellationColor bgColor, final String template) {
        this.level = level;
        this.text = text;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.active = active;
        this.template = template;
    }

    /**
     * returns whether the banner is active or not
     *
     * @return boolean.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * set the banner as active or not
     *
     * @param value boolean.
     */
    public void setActive(final boolean value) {
        active = value;
    }

    /**
     * Return the level.
     * <p>
     * Higher levels are more important.
     *
     * @return The level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Return the text of the banner.
     *
     * @return The text of the banner.
     */
    public String getText() {
        return text;
    }

    /**
     * Return the banner template.
     *
     * @return The banner template.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Return the foreground color of the banner.
     *
     * @return The foreground color of the banner.
     */
    public ConstellationColor getFgColor() {
        return fgColor;
    }

    /**
     * Return the background color of the banner.
     *
     * @return The background color of the banner.
     */
    public ConstellationColor getBgColor() {
        return bgColor;
    }

    @Override
    public String toString() {
        return String.format(text);
    }
}
