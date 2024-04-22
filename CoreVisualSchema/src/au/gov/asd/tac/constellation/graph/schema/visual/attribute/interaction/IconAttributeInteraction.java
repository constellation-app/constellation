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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class IconAttributeInteraction extends AbstractAttributeInteraction<ConstellationIcon> {

    private static final int DEFAULT_NODE_SIZE = 50;

    @Override
    public String getDataType() {
        return IconAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }
        final ConstellationIcon icon = (ConstellationIcon) value;

        return icon.getExtendedName();
    }

    @Override
    public List<Node> getDisplayNodes(final Object value, final double width, final double height) {
        Image iconImage = null;

        final ConstellationIcon icon = (ConstellationIcon) value;
        if (icon != null) {
            iconImage = icon.buildImage();
        }

        final double rectWidth;
        if (width == -1) {
            rectWidth = height == -1 ? DEFAULT_NODE_SIZE : height;
        } else {
            rectWidth = width;
        }

        final double rectHeight = height == -1 ? rectWidth : height;

        final ImageView imageView = new ImageView(iconImage);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(rectHeight);
        return Arrays.asList(imageView);
    }

    @Override
    protected Class<ConstellationIcon> getValueType() {
        return ConstellationIcon.class;
    }
}
