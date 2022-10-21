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

import javafx.scene.Group;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public abstract class AbstractHeatmapLayer extends AbstractMapLayer {

    protected Group layerGroup;

    public AbstractHeatmapLayer() {
        super();
        layerGroup = new Group();
    }

    @Override
    public void setUp() {
        String path = "";
        int x = 1;

        int lineCounter = 1;
        for (int i = 0; i < 256; ++i) {
            path += "l" + x + ",0";

            if ((i + 1) % 16 == 0) {
                path += "l0,0.01";
                ++lineCounter;
            }

            if (lineCounter % 2 == 0) {

                x = -1;
            } else {
                x = 1;
            }
        }

        path = "M100,100" + path;

        SVGPath box = new SVGPath();
        box.setStrokeWidth(15);
        box.setContent(path);

        layerGroup.getChildren().add(box);
    }

    @Override
    public Group getLayer() {
        return layerGroup;
    }

}
