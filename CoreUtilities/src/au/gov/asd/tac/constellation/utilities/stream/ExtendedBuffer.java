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
package au.gov.asd.tac.constellation.utilities.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author sirius
 */
public class ExtendedBuffer {

    private static class Buffer {

        private int length;
        private int position = 0;
        private byte[] data;
    }

    private static final Buffer END_OF_FILE_MARKER = new Buffer();

    static {
        END_OF_FILE_MARKER.length = 0;
        END_OF_FILE_MARKER.data = null;
    }

    private final int bufferSize;

    private final BlockingQueue<Buffer> queue = new LinkedBlockingQueue<>();

    private Buffer outputBuffer, inputBuffer;

    private AtomicLong available = new AtomicLong(0L);

    public ExtendedBuffer(final int bufferSize) {
        this.bufferSize = bufferSize;

        outputBuffer = new Buffer();
        outputBuffer.data = new byte[bufferSize];

        inputBuffer = new Buffer();
        inputBuffer.length = inputBuffer.position = bufferSize;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public long getAvailableSize() {
        return available.get();
    }

    /**
     * Get the buffer data in a byte array.
     * <p>
     * This is a one-off read; when the data has been read, it is gone.
     *
     * @return The buffer data in a byte array.
     */
    public byte[] getData() {
        byte[] data = new byte[(int) available.get()];
        int position = 0;

        if (inputBuffer != null) {
            int bytesToCopy = inputBuffer.length - inputBuffer.position;
            if (bytesToCopy > 0) {
                System.arraycopy(inputBuffer, inputBuffer.position, data, position, bytesToCopy);
                position += bytesToCopy;
            }
        }

        Buffer buffer = queue.poll();
        while (buffer != null) {
            System.arraycopy(buffer.data, 0, data, position, buffer.length);
            position += buffer.length;
            buffer = queue.poll();
        }

        available.set(0L);

        return data;
    }

    private final InputStream inputStream = new InputStream() {

        @Override
        public int read() throws IOException {
            if (inputBuffer.position < inputBuffer.length) {
                available.getAndDecrement();
                return inputBuffer.data[inputBuffer.position++];
            } else if (inputBuffer.length < bufferSize) {
                return -1;
            } else {
                try {
                    inputBuffer = queue.take();
                    if (inputBuffer.length == 0) {
                        return -1;
                    }
                    available.getAndDecrement();
                    return inputBuffer.data[inputBuffer.position++];
                } catch (InterruptedException ex) {
                    throw new IOException(ex);
                }
            }
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {

            int byteCount = 0;

            while (len > 0) {

                // If this buffer has been exhausted then attempt to get the next buffer
                if (inputBuffer.position == inputBuffer.length) {

                    // If this buffer is the last buffer then return
                    if (inputBuffer.length < bufferSize) {
                        return byteCount == 0 ? -1 : byteCount;
                    } else {
                        try {
                            inputBuffer = queue.take();
                        } catch (InterruptedException ex) {
                            throw new IOException(ex);
                        }
                    }
                }

                int bytesToRead = Math.min(inputBuffer.length - inputBuffer.position, len);
                System.arraycopy(inputBuffer.data, inputBuffer.position, b, off, bytesToRead);

                byteCount += bytesToRead;
                inputBuffer.position += bytesToRead;
                available.getAndAdd(-bytesToRead);
                len -= bytesToRead;
                off += bytesToRead;
            }

            return byteCount;
        }
    };

    private final OutputStream outputStream = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
            outputBuffer.data[outputBuffer.position++] = (byte) b;
            if (outputBuffer.position == bufferSize) {
                addQueueToBuffer(outputBuffer);
                outputBuffer = new Buffer();
                outputBuffer.data = new byte[bufferSize];
            }
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {

            while (len > 0) {
                int bytesToCopy = Math.min(bufferSize - outputBuffer.position, len);

                System.arraycopy(b, off, outputBuffer.data, outputBuffer.position, bytesToCopy);
                outputBuffer.position += bytesToCopy;

                if (outputBuffer.position == bufferSize) {
                    addQueueToBuffer(outputBuffer);
                    outputBuffer = new Buffer();
                    outputBuffer.data = new byte[bufferSize];
                }

                len -= bytesToCopy;
                off += bytesToCopy;
            }
        }

        @Override
        public void close() {
            addQueueToBuffer(outputBuffer);
            if (outputBuffer.length == bufferSize) {
                queue.add(END_OF_FILE_MARKER);
            }
            outputBuffer = null;
        }

        private void addQueueToBuffer(Buffer buffer) {
            buffer.length = buffer.position;
            buffer.position = 0;
            available.addAndGet(buffer.length);
            queue.add(buffer);
        }
    };
}
