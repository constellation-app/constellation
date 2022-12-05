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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

/**
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer extends AbstractMapLayer {

    private final Group layer;

    private ImageView imageView;
    private Image img;

    public ThiessenPolygonsLayer(MapView parent, int id, ArrayList<Node> nodes) {
        super(parent, id);

        imageView = new ImageView();
        imageView.setOpacity(0.5);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        /*imageView.prefWidth(MapView.mapWidth);
        imageView.prefHeight(MapView.mapHeight);
        imageView.maxWidth(MapView.mapWidth);
        imageView.maxHeight(MapView.mapHeight);
        imageView.minWidth(MapView.mapWidth);
        imageView.minHeight(MapView.mapHeight);*/
        imageView.setFitHeight(MapView.mapHeight);
        imageView.setFitWidth(MapView.mapWidth);
        //imageView.
        img = new Image("C:\\Users\\pmazumder\\OneDrive - DXC Production\\Documents\\Work\\cryingMeme.png");
        imageView.setImage(img);

        layer = new Group();
    }

    private void sortNodes() {

    }

    private void calculateBisectors() {

    }

    @Override
    public void setUp() {
        Line line = new Line();
        line.setStartX(20);
        line.setStartY(20);

        line.setScaleX(0.1);

        layer.getChildren().addAll(imageView);
    }

    @Override
    public Group getLayer() {
        return layer;
    }
}
