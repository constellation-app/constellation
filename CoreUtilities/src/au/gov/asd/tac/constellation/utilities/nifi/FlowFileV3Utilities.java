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
package au.gov.asd.tac.constellation.utilities.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utilities for generating Flow Files (v3) for use with Niagara Files.
 *
 * @author cygnus_x-1
 */
public class FlowFileV3Utilities {

    private static final byte[] MAGIC_HEADER = {'N', 'i', 'F', 'i', 'F', 'F', '3'};
    private static final int MAX_VALUE_2_BYTES = 65535;

    private FlowFileV3Utilities() {
        throw new IllegalStateException("Utility class");
    }
    
    public static void packageFlowFile(final InputStream in, final OutputStream out, final Map<String, String> attributes, final long fileSize) throws IOException {
        out.write(MAGIC_HEADER);

        final byte[] writeBuffer = new byte[8];

        if (attributes == null) {
            writeFieldLength(writeBuffer, out, 0);
        } else {
            // write out the number of attributes
            writeFieldLength(writeBuffer, out, attributes.size());

            // write out each attribute key/value pair
            for (final Map.Entry<String, String> entry : attributes.entrySet()) {
                writeString(writeBuffer, entry.getKey(), out);
                writeString(writeBuffer, entry.getValue(), out);
            }
        }

        // write out length of data
        writeLong(writeBuffer, out, fileSize);

        // write out the actual flow file payload
        copy(in, out);
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[65536];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    private static void writeString(final byte[] writeBuffer, final String value, final OutputStream out) throws IOException {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeFieldLength(writeBuffer, out, bytes.length);
        out.write(bytes);
    }

    private static void writeFieldLength(final byte[] writeBuffer, final OutputStream out, final int numBytes) throws IOException {
        // If the value is less than the max value that can be fit into 2 bytes, just use the
        // actual value. Otherwise, we will set the first 2 bytes to 255/255 and then use the next
        // 4 bytes to indicate the real length.
        if (numBytes < MAX_VALUE_2_BYTES) {
            writeBuffer[0] = (byte) (numBytes >>> 8);
            writeBuffer[1] = (byte) (numBytes);
            out.write(writeBuffer, 0, 2);
        } else {
            writeBuffer[0] = (byte) 0xff;
            writeBuffer[1] = (byte) 0xff;
            writeBuffer[2] = (byte) (numBytes >>> 24);
            writeBuffer[3] = (byte) (numBytes >>> 16);
            writeBuffer[4] = (byte) (numBytes >>> 8);
            writeBuffer[5] = (byte) (numBytes);
            out.write(writeBuffer, 0, 6);
        }
    }

    private static void writeLong(final byte[] writeBuffer, final OutputStream out, final long value) throws IOException {
        writeBuffer[0] = (byte) (value >>> 56);
        writeBuffer[1] = (byte) (value >>> 48);
        writeBuffer[2] = (byte) (value >>> 40);
        writeBuffer[3] = (byte) (value >>> 32);
        writeBuffer[4] = (byte) (value >>> 24);
        writeBuffer[5] = (byte) (value >>> 16);
        writeBuffer[6] = (byte) (value >>> 8);
        writeBuffer[7] = (byte) (value);
        out.write(writeBuffer, 0, 8);
    }
}
