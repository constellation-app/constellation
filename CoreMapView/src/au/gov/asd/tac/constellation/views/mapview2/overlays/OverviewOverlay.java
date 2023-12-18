/*
 * Copyright 2010-2022 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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

    private static final double OVERLAY_SCALE = 0.1;  // Scaling factor to apply to map to generate the overview map
    private static final double OVERLAY_BORDER_THICKNESS = 2;  // Border thicknesses tp apply
    private static final Color COLOUR_BORDER = new Color(0.224, 0.239, 0.278, 1);  // Colour to use for the border
    private static final Color COLOUR_FOG = new Color(0.224, 0.239, 0.278, 0.5);  // Colour to display fox as
    private static final Color COLOUR_OCEAN = new Color(0.722, 0.871, 0.902, 1);  // Colour to display water/ocean as
    
    private final Rectangle borderRect = new Rectangle();  // A crisp border around the overview pane
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
    public OverviewOverlay(final double mapWidth, final double mapHeight, final List<SVGPath> countrySVGPaths) {
        super(0, 0);

        // Setup the overlay pane dimensions and style to align with displayed map
        this.mapWidth = mapWidth * OVERLAY_SCALE;
        this.mapHeight = mapHeight * OVERLAY_SCALE;
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
        final Group countryGroup = new Group();
        for (int i = 0; i < countrySVGPaths.size(); i++) {
            final SVGPath p = new SVGPath();
            p.setContent(countrySVGPaths.get(i).getContent());
            p.setStroke(Color.BLACK);
            p.setFill(Color.WHITE);
            p.setStrokeWidth(0.025);
            countryGroup.getChildren().add(p);
        }
        countryGroup.setScaleX(OVERLAY_SCALE);
        countryGroup.setScaleY(OVERLAY_SCALE);
        countryGroup.setLayoutX(0);
        countryGroup.setLayoutX(0);
        stackPane.getChildren().add(countryGroup);

        // Draw a clear border around the overview overlay
        borderRect.setWidth(this.mapWidth + OVERLAY_BORDER_THICKNESS);
        borderRect.setHeight(this.mapHeight + OVERLAY_BORDER_THICKNESS);
        borderRect.setFill(Color.TRANSPARENT);
        borderRect.setStroke(COLOUR_BORDER);
        borderRect.setStrokeWidth(2);
        borderRect.setMouseTransparent(true);
        stackPane.getChildren().add(borderRect);
        stackPane.getChildren().add(fogRegion);
               
        overlayPane.setCenter(stackPane);
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
    public void update(final double viewPortWidth, final double viewPortHeight, double hValue, double vValue) {
       
       // Store current viewport dimensions scaled to the size of the overlay
       this.viewPortWidth = viewPortWidth * OVERLAY_SCALE;
       this.viewPortHeight = viewPortHeight * OVERLAY_SCALE;   

       // Calculate how much of the map is hidden in the horizontal and vertical directions, this combined with the
       // hValue and vValue tell us how much of the map is not visible to the left and top and therefor to the right and
       // bottom
       final double leftFogWidth = hValue * (this.mapWidth - this.viewPortWidth);
       final double topFogHeight = vValue * (this.mapHeight - this.viewPortHeight);
       
       // Draw a rectangle representing the entire map and hten cut out the viewable area to create a clip to be applied
       // to the fogGregion.
       final Rectangle fogRect = new Rectangle(this.mapWidth, this.mapHeight);
       final Rectangle fogCutoutRect = new Rectangle(this.viewPortWidth, this.viewPortHeight);
       fogCutoutRect.setX(leftFogWidth);
       fogCutoutRect.setLayoutY(topFogHeight);
       this.fogRegion.setClip(Shape.subtract(fogRect, fogCutoutRect));
    
       // Populate the fog region on the layer
       this.fogRegion.setMinWidth(this.mapWidth);
       this.fogRegion.setMaxWidth(this.mapWidth);
       this.fogRegion.setMinHeight(this.mapHeight);
       this.fogRegion.setMaxHeight(this.mapHeight);
       this.fogRegion.setBackground(new Background(new BackgroundFill(COLOUR_FOG, null, null)));
   }
}
