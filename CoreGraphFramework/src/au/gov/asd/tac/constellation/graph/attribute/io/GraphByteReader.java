/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.utilities.stream.ExtendedBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provide a mechanism for GraphIOProviders to read data from ancillary files in
 * the zip graph file.
 * <p>
 * Each GraphIOProvider will be offered a GraphByteReader instance to receive
 * data from.
 *
 * @author algol
 */
public class GraphByteReader {

    private final Map<String, ExtendedBuffer> contents = new HashMap<>();

    public GraphByteReader() {
    }

    public GraphByteReader(final InputStream in) throws IOException {
        final ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            final ExtendedBuffer out = new ExtendedBuffer(1 << 16);
            try {
                GraphByteWriter.copy(zin, out.getOutputStream());
                contents.put(entry.getName(), out);
            } finally {
                out.getOutputStream().close();
            }
            entry = zin.getNextEntry();
        }
    }

    /**
     * Read the data from the specified file in the zip file and return a byte
     * array.
     *
     * @param reference The name of the ZipEntry to read.
     *
     * @return A byte[] containing the contents of the ZipEntry.
     *
     * @throws IOException If an I/O error occurs.
     */
    public ExtendedBuffer read(final String reference) throws IOException {
        return contents.get(reference);
    }
}
