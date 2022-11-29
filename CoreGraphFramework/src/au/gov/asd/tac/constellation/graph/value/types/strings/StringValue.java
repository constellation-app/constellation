/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value.types.strings;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.converters.Copyable;

/**
 *
 * @author sirius
 */
public class StringValue implements Copyable, StringReadable, StringWritable {

    static {
        final ConverterRegistry r = ConverterRegistry.getDefault();
        StringConverters.register(r, StringValue.class, StringValue.class);
    }

    private String value = null;

    @Override
    public Object copy() {
        final StringValue copy = new StringValue();
        copy.value = value;
        return copy;
    }

    @Override
    public String readString() {
        return value;
    }

    @Override
    public void writeString(String value) {
        this.value = value;
    }
}
