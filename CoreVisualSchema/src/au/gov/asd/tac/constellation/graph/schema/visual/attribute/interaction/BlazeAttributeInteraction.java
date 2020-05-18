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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class BlazeAttributeInteraction extends AbstractAttributeInteraction<Blaze> {

    private static final int DEFAULT_NODE_SIZE = 50;

    @Override
    public String getDataType() {
        return BlazeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object attrVal) {
        if (attrVal == null) {
            return null;
        }

        final Blaze blazeValue = ((Blaze) attrVal);
        final StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("Colour: ").append(blazeValue.getColor());
        sBuilder.append("; Angle: ").append(blazeValue.getAngle());

        return sBuilder.toString();
    }

    @Override
    public List<Node> getDisplayNodes(final Object attrVal, final double width, final double height) {
        final Blaze blazeValue = ((Blaze) attrVal);
        final double rectWidth = width == -1 ? height == -1 ? DEFAULT_NODE_SIZE : height : width;
        final double rectHeight = height == -1 ? rectWidth : height;
        final Rectangle rect = new Rectangle(rectWidth, rectHeight);
        rect.setFill(blazeValue.getColor().getJavaFXColor());
        rect.setStroke(Color.LIGHTGREY);

        return Arrays.asList(rect);
    }

    @Override
    protected Class<Blaze> getValueType() {
        return Blaze.class;
    }
}
