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
package au.gov.asd.tac.constellation.views.mapview2;

import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

/**
 * Store details of maps available for selection within the MapView.
 * 
 * @author serpens24
 */
public class MapDetails {
    
    private static final Stop[] STOPS = new Stop[] {new Stop(0, Color.LIGHTGREEN), new Stop(1, Color.BLACK)};

    // For now define these values as static, but potentially allow diferent maps to adjust
    public static final double MARKER_LINE_WIDTH = 0.5;
    public static final double MARKER_OPACITY = 0.55;
    public static final double MARKER_DRAWING_OPACITY = 0.25;
    public static final Paint MARKER_STROKE_PAINT_GRADIENT = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop[] {new Stop(0, Color.LIMEGREEN), new Stop(1, Color.BLACK)});
    public static final Paint MARKER_STROKE_PAINT_SELECTED_GRADIENT = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop[] {new Stop(0, Color.GOLD), new Stop(1, Color.ORANGE)});
    public static final Paint MARKER_STROKE_PAINT_HIGHLIGHT_GRADIENT = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop[] {new Stop(0, Color.KHAKI), new Stop(1, Color.FIREBRICK)});
    public static final Color MARKER_STROKE_COLOUR =  Color.web("#000000");
    public static final Color MARKER_MULTI_FILL_COLOUR = Color.web("#FFFFFF", MARKER_OPACITY);
    public static final Color MARKER_DEFAULT_FILL_COLOUR = Color.web("#3F7FFF", MARKER_OPACITY);
    public static final Color MARKER_SELECTED_FILL_COLOUR = Color.web("#FF0000", MARKER_OPACITY);
    public static final Color MARKER_HIGHLIGHTED_FILL_COLOUR = Color.web("#FFFF00", MARKER_OPACITY);
    public static final Color MARKER_USER_DRAWN_LINE_COLOUR = Color.web("#FF8C00");
    public static final Color MARKER_USER_DRAWN_LINE_SELECTED_COLOUR = Color.web("#FF0000");
    public static final Color MARKER_USER_DRAWING_FILL_COLOUR = Color.web("#000000", MARKER_DRAWING_OPACITY);
    public static final Color MARKER_USER_DRAWN_FILL_COLOUR = Color.web("#FF8C00", MARKER_OPACITY);

    private final MapType type;  // The type of map layer that is being described
    private final double width;  // Width in "map units" of the map. Units may vary based on map type.
    private final double height;  // Height in "map units" of the map. Units may vary based on map type.
    private final double topLat;  // Latitude of the top edge of the map.
    private final double bottomLat;  // Latitude of the bottom edge of the map.
    private final double leftLon;  // Longitude of the left edge of the map.
    private final double rightLon;  // Longitude of the right edge of the map.
    private final Insets borderMilliInsets; // how much space around the outside of the map image is decorative border
    private final String name;  // Name to identify the map by.
    private final File mapFile;  // File object containing the map.
    
    /**
     * Create a MapDetails object corresponding to the supplied values.
     * 
     * @param type  // The type of map layer being described
     * @param width  // Width in "map units" of the map. Units may vary based on map type.
     * @param height  // Height in "map units" of the map. Units may vary based on map type.
     * @param topLat  // Latitude of the top edge of the map.
     * @param leftLon  // Longitude of the left edge of the map.
     * @param bottomLat  // Latitude of the bottom edge of the map.
     * @param rightLon  // Longitude of the right edge of the map.
     * @param name  // Name to identify the map by.
     * @param mapFile  // File object containing the map.
     */
    public MapDetails(final MapType type, final double width, final double height,
                      final double topLat, final double bottomLat, final double leftLon, final double rightLon,
                      final Insets borderInsets, final String name, final File mapFile) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.topLat = topLat;
        this.leftLon = leftLon;
        this.bottomLat = bottomLat;
        this.rightLon = rightLon;
        this.borderMilliInsets = borderInsets;
        this.name = name;
        this.mapFile = mapFile;
    }
    
    /**
     * Get the width in "map units" of the map. Units may vary based on map type.
     * 
     * @return Width in in "map units" of the map. Units may vary based on map type.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Get the height in "map units" of the map. Units may vary based on map type.
     * 
     * @return Height in in "map units" of the map. Units may vary based on map type.
     */
    public double getHeight() {
        return height;
    }
    
    /**
     * Get the latitude of the top edge of the map.
     * 
     * @return Latitude of the top edge of the map.
     */
    public double getTopLat() {
        return topLat;
    }
    
    /**
     * Get the latitude of the bottom edge of the map.
     * 
     * @return Latitude of the bottom edge of the map.
     */
    public double getBottomLat() {
        return bottomLat;
    }
    
    /**
     * Get the longitude of the left edge of the map.
     * 
     * @return Longitude of the left edge of the map.
     */
    public double getLeftLon() {
        return leftLon;
    }
    
    /**
     * Get the longitude of the right edge of the map.
     * 
     * @return Longitude of the right edge of the map.
     */
    public double getRightLon() {
        return rightLon;
    }
    
    /**
     * Get any border space around the map edges to exclude from any lat/long calculations
     * top, right, bottom, left
     * values to be divided by 1000 to get equivalent coordinate offsets.
     * @return 
     */
    public Insets getBorderInsets() {
        return borderMilliInsets;
    }
    
    /**
     * Get the name of the map.
     * @return Name of the map.
     */
    public String getMapName() {
        return name;
    }
    
    /**
     * Get the File object containing the map.
     * @return File object containing the map.
     */
    public File getMapFile() {
        return mapFile;
    }
    
    /**
     * Provides future scope to display base map layers of formats other than SVG paths.
     */
    public enum MapType {
        SVG,
        RASTER  // ie JPG, PNG etc. THese may provide greater details of a specific area but are unable to scale well
    }
}
