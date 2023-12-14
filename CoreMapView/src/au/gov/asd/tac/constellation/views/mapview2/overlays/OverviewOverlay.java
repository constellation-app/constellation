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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Overview overlay - NOT FULLY IMPLEMENTED
 *
 * @author altair1673
 */
public class OverviewOverlay extends AbstractOverlay {

    private static final double MAP_SCALE = 0.2;

    private final Rectangle borderRect = new Rectangle();  // A rectangle to wrap the overview pane in
    private final Rectangle panningRect = new Rectangle();  // Rectangle to show the viewPane with


    public OverviewOverlay(final Vec3 posVect, final Vec3 mapSizeVect, final Vec3 viewPanePosVect, final Vec3 viewPaneSizeVect, final List<SVGPath> countrySVGPaths) {
        super(posVect.getX(), posVect.getY());
        
        overlayPane.setMinWidth(mapSizeVect.getX() * MAP_SCALE);
        overlayPane.setMaxWidth(mapSizeVect.getX() * MAP_SCALE);
        overlayPane.setMinHeight(mapSizeVect.getY() * MAP_SCALE);
        overlayPane.setMaxHeight(mapSizeVect.getY() * MAP_SCALE);
                
        overlayPane.getChildren().clear();
        overlayPane.setBackground(new Background(new BackgroundFill(new Color(0.722, 0.871, 0.902, 1), null, null)));

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
        countryGroup.setScaleX(MAP_SCALE);
        countryGroup.setScaleY(MAP_SCALE);
        stackPane.getChildren().add(countryGroup);
        
        panningRect.setWidth(500 * MAP_SCALE);
        panningRect.setHeight(500 * MAP_SCALE);
        panningRect.setTranslateX(0 * MAP_SCALE);
        panningRect.setTranslateY(0 * MAP_SCALE);
        panningRect.setFill(Color.TRANSPARENT);
        panningRect.setStroke(Color.RED);
        panningRect.setStrokeWidth(2);
        panningRect.setMouseTransparent(true);
        stackPane.getChildren().add(panningRect);

        borderRect.setWidth(mapSizeVect.getX() * MAP_SCALE);
        borderRect.setHeight(mapSizeVect.getY() * MAP_SCALE);
        borderRect.setFill(Color.TRANSPARENT);
        borderRect.setStroke(Color.BLACK);
        borderRect.setStrokeWidth(5);
        borderRect.setMouseTransparent(true);
        stackPane.getChildren().add(borderRect);
        
        overlayPane.setCenter(stackPane);
    }
    
    @Override
    public Pane getOverlayPane() {
        return overlayPane;
    }
    
   public void update(final Vec3 viewPanePosVect, final Vec3 viewPaneSizeVect) {
       
       final double centerMapX = (overlayPane.getWidth() / 2);
       final double centerMapY = (overlayPane.getHeight() / 2);
       
       double width = viewPaneSizeVect.getX() * MAP_SCALE;
       double height = viewPaneSizeVect.getY() * MAP_SCALE;
       double x = -viewPanePosVect.getX() * MAP_SCALE;
       double y = -viewPanePosVect.getY() * MAP_SCALE;
       
       if (x < 0) {
           width = width + x;
           x = 0;
       } else if (x > overlayPane.getWidth()) {
           width = 0;
           x = overlayPane.getWidth();
       }
       if (x + width > overlayPane.getWidth()) {
           width = overlayPane.getWidth() - x;
       }
       
       if (y < 0) {
           height = height + y;
           y = 0;
       } else if (y > overlayPane.getHeight()) {
           height = 0;
           y = overlayPane.getHeight();
       }
       if (y + height > overlayPane.getHeight()) {
           height = overlayPane.getHeight() - y;
       }
       
       double centreViewPaneX = x + width/2;
       double centreViewPaneY = y + height/2;
       
        panningRect.setWidth(width);
        panningRect.setHeight(height);
        panningRect.setTranslateX(centreViewPaneX - centerMapX);
        panningRect.setTranslateY(centreViewPaneY - centerMapY);
   }
}
