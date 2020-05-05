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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ByteAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.DoubleAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ShortAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 * A DefaultBinCreatorProvider implements AttributeBinCreatorProvider to provide
 * all the basic BinCreators for the histogram. Other providers can augment
 * these BinCreators to expand the functionality of the histogram.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeBinCreatorProvider.class)
public class DefaultBinCreatorProvider extends AttributeBinCreatorProvider {

    @Override
    public void register() {
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.FLOAT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LONG_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DOUBLE_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(BooleanAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.BOOLEAN_ATTRIBUTE_BIN_CREATOR);
        AttributeType.ATTRIBUTE.registerAttributeBinCreator(StringAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.STRING_ATTRIBUTE_BIN_CREATOR);

        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_FLOAT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_LONG_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_DOUBLE_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(BooleanAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_BOOLEAN_ATTRIBUTE_BIN_CREATOR);
        AttributeType.SOURCE_VERTEX_ATTRIBUTE.registerAttributeBinCreator(StringAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.SOURCE_STRING_ATTRIBUTE_BIN_CREATOR);

        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_INT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_FLOAT_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_LONG_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_DOUBLE_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(BooleanAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_BOOLEAN_ATTRIBUTE_BIN_CREATOR);
        AttributeType.DESTINATION_VERTEX_ATTRIBUTE.registerAttributeBinCreator(StringAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.DESTINATION_STRING_ATTRIBUTE_BIN_CREATOR);

        // LINK AGGREGATE FUNCTIONS FOR TRANSACTION ATTRIBUTES
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.LINK_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.LINK_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.LINK_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(AttributeType.DEFAULT_ATTRIBUTE_TYPE, DefaultBinCreators.LINK_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER);

        // EDGE AGGREGATE FUNCTIONS FOR TRANSACTION ATTRIBUTES
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.EDGE_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.EDGE_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.EDGE_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(AttributeType.DEFAULT_ATTRIBUTE_TYPE, DefaultBinCreators.EDGE_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER);

        // VERTEX AGGREGATE FUNCTIONS FOR TRANSACTION ATTRIBUTES
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE.registerAttributeBinCreator(AttributeType.DEFAULT_ATTRIBUTE_TYPE, DefaultBinCreators.VERTEX_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER);

        // VERTEX AGGREGATE FUNCTIONS FOR NEIGHBOUR ATTRIBUTES
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_SUM_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MAX_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ByteAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ShortAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(IntegerAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(LongAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_LONG_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(FloatAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(DoubleAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER);
        AttributeType.VERTEX_MIN_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DefaultBinCreators.VERTEX_NEIGHBOUR_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER);

        AttributeType.VERTEX_UNIQUE_VALUES_NEIGHBOUR_ATTRIBUTE.registerAttributeBinCreator(AttributeType.DEFAULT_ATTRIBUTE_TYPE, DefaultBinCreators.VERTEX_NEIGHBOUR_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER);
    }
}
