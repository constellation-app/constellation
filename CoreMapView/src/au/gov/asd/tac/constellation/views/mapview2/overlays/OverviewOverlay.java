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
package au.gov.asd.tac.constellation.views.mapview2.overlays;

import java.util.List;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

/**
 * 
 * @author serpens24
 */
public class OverviewOverlay extends AbstractOverlay {

    private static final double OVERLAY_SCALE = 0.125;  // Scaling factor to apply to map to generate the overview map
    private static final double OVERLAY_BORDER_THICKNESS = 2;  // Border thicknesses tp apply
    private static final Color COLOUR_BORDER = new Color(0.224, 0.239, 0.278, 1);  // Colour to use for the border
    private static final Color COLOUR_FOG = new Color(0.224, 0.239, 0.278, 0.5);  // Colour to display fox as
    private static final Color COLOUR_OCEAN = new Color(0.42, 0.74, 0.86, 1);  // Colour to display water/ocean as
    
    private final Region fogRegion = new Region();  // The region used to display fog over non visible parts of the map
    
    private final double mapWidth;  // The width of the map being viewed
    private final double mapHeight;  // The height of the map being viewed
    private double viewPortWidth;  // The width of current ScrollPane viewport viewing the map
    private double viewPortHeight;  // The height of current ScrollPane viewport viewing the map
    
    /**
     * Construct the overview overlay, setting it up based on the currently selected map.
     * 
     * @param mapWidth The width of the main map
     * @param mapWidth The height of the main map
     * @param countrySVGPaths List of country SVG paths to display in the map
     */
    public OverviewOverlay(final double mapWidth, final double mapHeight, final List<SVGPath> countrySVGPaths, final Group minimapGroup) {
        super(0, 0);

        // Setup the overlay pane dimensions and style to align with displayed map
        this.mapWidth = (mapWidth) * OVERLAY_SCALE;
        this.mapHeight = (mapHeight) * OVERLAY_SCALE;
        this.viewPortWidth = 0;
        this.viewPortHeight = 0;
        overlayPane.getChildren().clear();
        overlayPane.setMinWidth(this.mapWidth + OVERLAY_BORDER_THICKNESS);
        overlayPane.setMaxWidth(this.mapWidth + OVERLAY_BORDER_THICKNESS);
        overlayPane.setMinHeight(this.mapHeight + OVERLAY_BORDER_THICKNESS);
        overlayPane.setMaxHeight(this.mapHeight + OVERLAY_BORDER_THICKNESS);    
        overlayPane.setBackground(new Background(new BackgroundFill(COLOUR_OCEAN, null, null)));

        // Setup a stackpane and fill it with the map based on country SVG paths
        final StackPane stackPane = new StackPane();
        if (minimapGroup == null) {
            final Group countryGroup = new Group();
            boolean firstLineDone = false;
            for (int i = 0; i < countrySVGPaths.size(); i++) {
                final SVGPath p = new SVGPath();
                p.setContent(countrySVGPaths.get(i).getContent());
                p.setStroke(Color.BLACK);
                if (!firstLineDone) { // the first line is generally the bounding border rectangle, not map data
                    p.setFill(Color.rgb(128,140,255,0.2));
                    firstLineDone = true;
                } else {
                    p.setFill(Color.rgb(255,255,255,0.8));
                }                
                p.setStrokeWidth(0.005);
                countryGroup.getChildren().add(p);
            }
            countryGroup.setScaleX(OVERLAY_SCALE);
            countryGroup.setScaleY(OVERLAY_SCALE);
            countryGroup.setLayoutX(0);
            countryGroup.setLayoutY(0);

            stackPane.getChildren().add(countryGroup);
        } else {
            stackPane.getChildren().add(minimapGroup);
        }
        
        stackPane.getChildren().add(fogRegion);               
        overlayPane.setCenter(stackPane);

        // Draw a clear border around the overview overlay
        stackPane.setMinWidth(this.mapWidth + OVERLAY_BORDER_THICKNESS * 2);
        stackPane.setMaxWidth(this.mapWidth + OVERLAY_BORDER_THICKNESS * 2);
        stackPane.setMinHeight(this.mapHeight + OVERLAY_BORDER_THICKNESS * 2);
        stackPane.setMaxHeight(this.mapHeight + OVERLAY_BORDER_THICKNESS * 2);
        stackPane.setBorder(new Border(new BorderStroke(COLOUR_BORDER, BorderStrokeStyle.SOLID, null, null)));
    }
    
    /**
     * Return handle to the overlay pane.
     * @return Handle to the overlay pane.
     */
    @Override
    public Pane getOverlayPane() {
        return overlayPane;
    }
    
    /**
     * Update the display of fog in the overview overlay. The fog grays out parts of the mini map that correspond to
     * areas of the full map that cannot currently be seen. These areas are drawn with a semi transparent overlay over
     * them to show the areas that can't be seen.
     * 
     * @param viewPortWidth The current width of the ScrollPane containing the map being viewed with a mini map.
     * @param viewPortHeight The current height of the ScrollPane containing the map being viewed with a mini map.
     * @param hValue The current hValue of the ScrollPane containing the map being viewed, which indicates its current
     *               horizontal scroll.
     * @param vValue  The current vValue of the ScrollPane containing the map being viewed, which indicates its current
     *               vertical scroll.
     */
    public void update(final double currentViewPortWidth, final double currentViewPortHeight, double hValue, double vValue) {
       
       // Store current viewport dimensions scaled to the size of the overlay
       viewPortWidth = currentViewPortWidth * OVERLAY_SCALE;
       viewPortHeight = currentViewPortHeight * OVERLAY_SCALE;   

       // Calculate how much of the map is hidden in the horizontal and vertical directions, this combined with the
       // hValue and vValue tell us how much of the map is not visible to the left and top and therefor to the right and
       // bottom
       final double leftFogHvalue = hValue * (mapWidth - viewPortWidth);
       final double topFogVvalue = vValue * (mapHeight - viewPortHeight);
       
       // Draw a rectangle representing the entire map and hten cut out the viewable area to create a clip to be applied
       // to the fogGregion.
       final Rectangle fogRect = new Rectangle(mapWidth, mapHeight);
       final Rectangle fogCutoutRect = new Rectangle(viewPortWidth, viewPortHeight);
       fogCutoutRect.setX(leftFogHvalue);
       fogCutoutRect.setLayoutY(topFogVvalue);
       fogRegion.setClip(Shape.subtract(fogRect, fogCutoutRect));
    
       // Populate the fog region on the layer
       fogRegion.setMinWidth(mapWidth);
       fogRegion.setMaxWidth(mapWidth);
       fogRegion.setMinHeight(mapHeight);
       fogRegion.setMaxHeight(mapHeight);
       fogRegion.setBackground(new Background(new BackgroundFill(COLOUR_FOG, null, null)));
   }
}
