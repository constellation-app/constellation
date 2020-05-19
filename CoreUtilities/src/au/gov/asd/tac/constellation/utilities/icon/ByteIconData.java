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
package au.gov.asd.tac.constellation.utilities.icon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An IconData implementation allowing an icon to be built using an array of
 * bytes.
 *
 * @author cygnus_x-1
 */
public class ByteIconData extends IconData {

    private final byte[] bytes;

    public ByteIconData(final byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }
}
