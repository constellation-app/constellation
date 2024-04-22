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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provide a mechanism for GraphIOProviders to write data to ancillary files in
 * the graph zip file.
 * <p>
 * Each GraphIOProvider will be offered a GraphByteWriter instance to send data
 * to. The GraphByteWriter will accumulate the various data streams and
 * (eventually) write them to the graph zip file as separate ZipEntry files.
 *
 * @author algol
 */
public final class GraphByteWriter {

    static final String FILE_PREFIX = "CONSTELLATION_";
    private static final int BUFSIZ = 1024 * 1024;

    // Map reference to File.
    private final Map<String, File> fileMap;

    /**
     * Construct a new instance.
     */
    public GraphByteWriter() {
        fileMap = new HashMap<>();
    }

    /**
     * Reset the reference and fileMap; files in the fileMap will be deleted.
     */
    public void reset() {
        for (final File f : fileMap.values()) {
            if (f.exists()) {
                final boolean fIsDeleted = f.delete();
                if (!fIsDeleted) {
                    //TODO: Handle case where file not successfully deleted
                }
            }
        }

        fileMap.clear();
    }

    public Map<String, File> getFileMap() {
        return fileMap;
    }

    /**
     * Give an InputStream to the GraphWriter to be written to the graph file,
     * and return a String as a reference to the data.
     * <p>
     * The InputStream will be closed.
     *
     * @param in An InputStream.
     *
     * @return A label that refers to the data in the InputStream.
     *
     * @throws java.io.IOException If an I/O error occurs.
     */
    public String write(final InputStream in) throws IOException {
        final String reference = UUID.randomUUID().toString();

        // Store the data in a temporary file.
        final File temp = File.createTempFile(FILE_PREFIX, FileExtensionConstants.BINARY);
        try (final OutputStream out = new FileOutputStream(temp)) {
            copy(in, out);
            fileMap.put(reference, temp);
        } finally {
            in.close();
        }

        return reference;
    }

    /**
     * Copy the InputStream to the OutputStream.
     * <p>
     * Both streams will be closed.
     *
     * @param in Source.
     * @param out Destination.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buf = new byte[BUFSIZ];
        int len = in.read(buf);
        while (len >= 0) {
            out.write(buf, 0, len);
            len = in.read(buf);
        }
    }
}
