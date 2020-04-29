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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A ConstellationIcon manages data related to a single icon in CONSTELLATION,
 * including metadata describing the icon as well as the icon data itself stored
 * as an {@link IconData}.
 *
 * @author cygnus_x-1
 */
public class ConstellationIcon {

    private static final Logger LOGGER = Logger.getLogger(ConstellationIcon.class.getName());

    /**
     * A separator used when concatenating elements of a ConstellationIcon name.
     */
    protected static final String DEFAULT_ICON_SEPARATOR = SeparatorConstants.PERIOD;

    /**
     * The default file format for a ConstellationIcon.
     */
    protected static final String DEFAULT_ICON_FORMAT = "png";

    /**
     * The default size (ie. height and width) of a ConstellationIcon.
     */
    protected static final int DEFAULT_ICON_SIZE = 256;
    
    private static final String BUILDING_ICON_FORMAT =  "Building icon: {0}";

    /**
     * A cache to store icons
     */
    private static final Map<ThreeTuple<IconData, Integer, Color>, Object> ICON_CACHE = new HashMap<>();
    private static final Map<ThreeTuple<IconData, Integer, Color>, Object> IMAGE_CACHE = new HashMap<>();
    private static final Map<ThreeTuple<IconData, Integer, Color>, Object> BUFFERED_IMAGE_CACHE = new HashMap<>();

    private final String name;
    private final IconData iconData;
    private final List<String> aliases;
    private final List<String> categories;
    private boolean editable;

    private final String extendedName;

    private ConstellationIcon(final String name, final IconData iconData, final List<String> aliases, final List<String> categories, final boolean editable) {
        this.name = name;
        this.iconData = iconData;
        this.aliases = aliases;
        this.categories = categories;
        this.editable = editable;

        // Cache the extended name for performance
        this.extendedName = createExtendedName();
    }

    /**
     * Get the name of this ConstellationIcon.
     *
     * @return A {@link String} representing the name of this ConstellationIcon.
     */
    public String getName() {
        return name;
    }

    private String createExtendedName() {
        StringBuilder fullyQualifiedname = new StringBuilder();
        if (categories != null) {
            categories.forEach(category -> {
                fullyQualifiedname.append(category).append(DEFAULT_ICON_SEPARATOR);
            });
        }
        fullyQualifiedname.append(name);
        return fullyQualifiedname.toString();
    }

    /**
     * Get a name for this ConstellationIcon made up of each of its categories
     * in the order they were inserted into the categories property and its name
     * property, separated by the {@link #DEFAULT_ICON_SEPARATOR} constant.
     *
     * @return a {@link String} representing the extended name of this
     * ConstellationIcon.
     */
    public String getExtendedName() {
        return extendedName;
    }

    /**
     * Get the {@link IconData} object holding the byte data of this
     * ConstellationIcon.
     *
     * @return An {@link IconData} object holding the byte data of this
     * ConstellationIcon.
     */
    public IconData getIconData() {
        return iconData;
    }

    /**
     * Get the aliases for this ConstellationIcon, that is any alternate names
     * which might be used to reference this ConstellationIcon.
     *
     * @return A {@link List} of {@link String} representing aliases for this
     * ConstellationIcon.
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Get the categories for this ConstellationIcon, that is an ordered series
     * of strings for grouping this ConstellationIcon.
     *
     * @return A {@link List} of {@link String} representing categories for this
     * ConstellationIcon.
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Check if this ConstellationIcon can be edited. This is usually used to
     * determine if a ContellationIcon is a built-in icon (and hence should be
     * unmodifiable) or a user icon (and hence can be altered by the user).
     *
     * @return A boolean value representing whether this ConstellationIcon is
     * editable or not.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Set a ConstellationIcon as editable. This is usually used to determine if
     * a ContellationIcon is a built-in icon (and hence should be unmodifiable)
     * or a user icon (and hence can be altered by the user).
     *
     * @param editable True if this ConstellationIcon should be editable, false
     * otherwise.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Build an array of bytes representing this ConstellationIcon's data.
     *
     * @return An array of bytes representing this ConstellationIcon's data.
     */
    public byte[] buildByteArray() {
        return iconData.getData();
    }

    /**
     * Build a {@link BufferedImage} object representing this
     * ConstellationIcon's data.
     *
     * @return A {@link BufferedImage} representing this ConstellationIcon's
     * data.
     */
    public BufferedImage buildBufferedImage() {
        return buildBufferedImage(DEFAULT_ICON_SIZE, null);
    }

    /**
     * Build a {@link BufferedImage} object representing this
     * ConstellationIcon's data.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link BufferedImage}.
     * @return A {@link BufferedImage} representing this ConstellationIcon's
     * data.
     */
    public BufferedImage buildBufferedImage(final int size) {
        return buildBufferedImage(size, null);
    }

    /**
     * Build a {@link BufferedImage} object representing this
     * ConstellationIcon's data.
     *
     * @param color A {@link Color} representing the color of the icon.
     * @return A {@link BufferedImage} representing this ConstellationIcon's
     * data.
     */
    public BufferedImage buildBufferedImage(final Color color) {
        return buildBufferedImage(DEFAULT_ICON_SIZE, color);
    }

    /**
     * Build a {@link BufferedImage} object representing this
     * ConstellationIcon's data, scaled to the specified size.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link BufferedImage}.
     * @param color A {@link Color} representing the color of the icon.
     * @return A {@link BufferedImage} of the specified size.
     */
    public BufferedImage buildBufferedImage(final int size, final Color color) {
        // build the cache key
        final ThreeTuple<IconData, Integer, Color> key = buildCacheKey(iconData, size, color);

        BufferedImage icon;
        if (BUFFERED_IMAGE_CACHE.containsKey(key)) {
            icon = (BufferedImage) BUFFERED_IMAGE_CACHE.get(key);
        } else {
            // build the icon
            LOGGER.log(Level.FINE, BUILDING_ICON_FORMAT, name);
            final byte[] data = retrieveIconData(iconData, size, color);
            try {
                icon = ImageIO.read(new ByteArrayInputStream(data));

                // cache the icon data
                BUFFERED_IMAGE_CACHE.put(key, icon);
            } catch (IOException ex) {
                LOGGER.severe(ex.getLocalizedMessage());
                icon = null;
            }
        }

        return icon;
    }

    /**
     * Build an {@link Icon} object representing this ConstellationIcon's data
     * for use with Swing GUI elements.
     *
     * @return An {@link Icon} representing this ConstellationIcon's data.
     */
    public Icon buildIcon() {
        return buildIcon(DEFAULT_ICON_SIZE, null);
    }

    /**
     * Build an {@link Icon} object representing this ConstellationIcon's data
     * for use with Swing GUI elements.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link Icon}.
     * @return An {@link Icon} representing this ConstellationIcon's data.
     */
    public Icon buildIcon(final int size) {
        return buildIcon(size, null);
    }

    /**
     * Build an {@link Icon} object representing this ConstellationIcon's data
     * for use with Swing GUI elements.
     *
     * @param color A {@link Color} representing the color of the icon.
     * @return An {@link Icon} representing this ConstellationIcon's data.
     */
    public Icon buildIcon(final Color color) {
        return buildIcon(DEFAULT_ICON_SIZE, color);
    }

    /**
     * Build an {@link Icon} object representing this ConstellationIcon's data,
     * scaled to the specified size, for use with Swing GUI elements.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link Icon}.
     * @param color A {@link Color} representing the color of the icon.
     * @return An {@link Icon} of the specified size.
     */
    public Icon buildIcon(final int size, final Color color) {
        // build the cache key
        final ThreeTuple<IconData, Integer, Color> key = buildCacheKey(iconData, size, color);

        final ImageIcon icon;
        if (ICON_CACHE.containsKey(key)) {
            icon = (ImageIcon) ICON_CACHE.get(key);
        } else {
            // build the icon
            LOGGER.log(Level.FINE, BUILDING_ICON_FORMAT, name);
            final byte[] data = retrieveIconData(iconData, size, color);
            icon = new ImageIcon(data);

            // cache the icon data
            ICON_CACHE.put(key, icon);
        }

        return icon;
    }

    /**
     * Build an {@link Image} object representing this ConstellationIcon's data
     * for use with JavaFX GUI elements.
     *
     * @return An {@link Image} representing this ConstellationIcon's data.
     */
    public Image buildImage() {
        return buildImage(DEFAULT_ICON_SIZE, null);
    }

    /**
     * Build an {@link Image} object representing this ConstellationIcon's data
     * for use with JavaFX GUI elements.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link Icon}.
     * @return An {@link Image} representing this ConstellationIcon's data.
     */
    public Image buildImage(final int size) {
        return buildImage(size, null);
    }

    /**
     * Build an {@link Image} object representing this ConstellationIcon's data
     * for use with JavaFX GUI elements.
     *
     * @param color A {@link Color} representing the color of the icon.
     * @return An {@link Image} representing this ConstellationIcon's data.
     */
    public Image buildImage(final Color color) {
        return buildImage(DEFAULT_ICON_SIZE, color);
    }

    /**
     * Build an {@link Icon} object representing this ConstellationIcon's data,
     * scaled to the specified size, for use with JavaFX GUI elements.
     *
     * @param size An integer value representing both the height and width of
     * the output {@link Icon}.
     * @param color A {@link Color} representing the color of the icon.
     * @return An {@link Icon} of the specified size.
     */
    public Image buildImage(final int size, final Color color) {
        // build the cache key
        final ThreeTuple<IconData, Integer, Color> key = buildCacheKey(iconData, size, color);

        final Image image;
        if (IMAGE_CACHE.containsKey(key)) {
            image = (Image) IMAGE_CACHE.get(key);
        } else {
            // build the image
            LOGGER.log(Level.FINE, BUILDING_ICON_FORMAT, name);
            final byte[] data = retrieveIconData(iconData, size, color);
            image = new Image(new ByteArrayInputStream(data));

            // cache the icon data
            IMAGE_CACHE.put(key, image);
        }

        return image;
    }

    /**
     * Build a key that can be used to index the icon byte array in the icon
     * cache.
     *
     * @param iconData The icon data object
     * @param size The size of the icon
     * @param color The colour of the icon
     * @return The byte array of the icon ready for conversion
     */
    private ThreeTuple<IconData, Integer, Color> buildCacheKey(final IconData iconData, final int size, final Color color) {
        return ThreeTuple.create(iconData, size, color);
    }

    /**
     * Retrieve the icon byte array from cache or create it.
     *
     * @param iconData The icon data object
     * @param size The size of the icon
     * @param color The colour of the icon
     * @return The icon data byte array
     */
    private byte[] retrieveIconData(final IconData iconData, final int size, final Color color) {
        return iconData.getData(size, color);
    }

    @Override
    public String toString() {
        return getExtendedName();
    }

    /**
     * A Builder class for creating a ConstellationIcon.
     */
    public static class Builder {

        private final String name;
        private final IconData iconData;
        private final List<String> aliases;
        private final List<String> categories;
        private boolean editable;

        public Builder(final String name, final IconData iconData) {
            this.name = name;
            this.iconData = iconData;
            this.aliases = new ArrayList<>();
            this.categories = new ArrayList<>();
            this.editable = false;
        }

        public Builder addAlias(final String alias) {
            if (alias != null && !alias.isEmpty()) {
                this.aliases.add(alias);
            }
            return this;
        }

        public Builder addAliases(final List<String> aliases) {
            if (aliases != null && !aliases.isEmpty()) {
                this.aliases.addAll(aliases);
            }
            return this;
        }

        public Builder addCategory(final String category) {
            if (category != null && !category.isEmpty()) {
                this.categories.add(category);
            }
            return this;
        }

        public Builder addCategories(final List<String> categories) {
            if (categories != null && !categories.isEmpty()) {
                this.categories.addAll(categories);
            }
            return this;
        }

        public Builder setEditable(final boolean editable) {
            this.editable = editable;
            return this;
        }

        public ConstellationIcon build() {
            return new ConstellationIcon(name, iconData, aliases, categories, editable);
        }
    }
}
