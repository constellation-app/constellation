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
package au.gov.asd.tac.constellation.utilities.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Extended buffer manager allowing streaming to/from a buffer structure which in fact is a collection
 * of fixed size buffers stored in a blocking queue.
 *
 * @author sirius
 */
public class ExtendedBuffer {

    /**
     * Define individual buffer used by <code>ExtendedBuffer</code>. Each buffer will have
     * fixed maximum length, defined by value of <code>bufferSize</code> passed to
     * <code>ExtendedBuffer</code> constructor and maintain awareness of number of bytes
     * populated/read.
     */
    private static class Buffer {
        private int length;
        private int position = 0;
        private byte[] data;
    }

    // Marker to denote end of file.
    private static final Buffer END_OF_FILE_MARKER = new Buffer();
    static {
        END_OF_FILE_MARKER.length = 0;
        END_OF_FILE_MARKER.data = null;
    }

    // Maximum size of data to store in each buffer instance
    private final int bufferSize;

    // queue containing all buffers, each added buffer will have max size of
    // <code>bufferSize</code>.
    private final BlockingQueue<Buffer> queue = new LinkedBlockingQueue<>();

    // Buffer objects used for input/output of 'next' buffer to be added/removed from queue.
    private Buffer outputBuffer;
    private Buffer inputBuffer;

    // Maintain atomically count of how many bytes are availalbe in the buffer(s) to read.
    private final AtomicLong available = new AtomicLong(0L);

    /**
     * constructor to create ExtendedBuffer with defined maximum size of individual buffers and
     * set up input/output streams to the buffers.
     *
     * @param bufferSize The size of each individual buffer.
     */
    public ExtendedBuffer(final int bufferSize) {
        this.bufferSize = bufferSize;

        outputBuffer = new Buffer();
        outputBuffer.data = new byte[bufferSize];

        inputBuffer = new Buffer();
        inputBuffer.length = inputBuffer.position = bufferSize;
    }

    /**
     * Return InputStream handle.
     *
     * @return Handle to the input stream that manages reads from <code>ExtendedBuffer</code>.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Return OutputStream handle.
     *
     * @return Handle to the output stream that manages writes to <code>ExtendedBuffer</code>.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Return number of bytes available to read in the <code>ExtendedBuffer</code>
     *
     * @return Number of bytes available to read.
     */
    public long getAvailableSize() {
        return available.get();
    }

    /**
     * Get the entire available extended buffer data in a byte array.
     * <p>
     * This is a one-off read, when the data has been read, it is gone.
     *
     * @return The buffer data in a byte array.
     */
    public byte[] getData() {
        final byte[] data = new byte[(int) available.get()];
        int position = 0;

        if (inputBuffer != null) {
            final int bytesToCopy = inputBuffer.length - inputBuffer.position;
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

        /**
         * Read values byte by byte from buffer. As buffers are read from <code>queue</code>
         * they are stored one at a time in <code>inputBuffer</code>.
         * Read values are converted to integers. -1 is returned when no data is available to read.
         * Read will block if buffer has been partially filled by outputStream but not yet added
         * to buffer queue and will remain blocked until the buffer is added to the queue.
         */
        @Override
        public int read() throws IOException {
            if (inputBuffer.position < inputBuffer.length) {
                // There are still bytes to read in inputBuffer, read the next one and reduce
                // the atomic available count
                available.getAndDecrement();
                return inputBuffer.data[inputBuffer.position++] & 0xFF;
            } else if (inputBuffer.length < bufferSize) {
                // nothing left to read
                return -1;
            } else {
                // see if another buffer can be taken from head of the queue of buffers, if there
                // are no queues available, block until queue becomes available. This occurs if a
                // buffer has partially been filled by outputStream, but is awaitying further
                // content.
                try {
                    inputBuffer = queue.take();
                    if (inputBuffer.length == 0) {
                        // retrieved buffer is empty, nothing left to read
                        return -1;
                    }
                    // Read the first byte from the newly taken buffer. inputBuffer now populated
                    // for subsequent take calls. Reduce the atomic available counter
                    // note - when buffers are added to thew queue their position is set to 0
                    available.getAndDecrement();
                    return inputBuffer.data[inputBuffer.position++] & 0xFF;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(ex);
                }
            }
        }

        /**
         * Read values from buffer into byte array. As buffers are read from <code>queue</code>
         * they are stored one at a time in <code>inputBuffer</code>.
         * Read values are converted to integers. -1 is returned when no data is available to read.
         * Read will block if buffer has been partially filled by outputStream but not yet added
         * to buffer queue and will remain blocked until the buffer is added to the queue.
         * 
         * @param b Byte array to read content into.
         * @param off Offset into content to start reading.
         * @param len Number of bytes to read.
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {

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
                            Thread.currentThread().interrupt();
                            throw new IOException(ex);
                        }
                    }
                }

                final int bytesToRead = Math.min(inputBuffer.length - inputBuffer.position, len);
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

    /**
     * Manage output stream as a queue of output buffers to ensure all content is handled.
     * Each individual buffer has a defined maximum size.
     * After stream is closed all content will have been written into ExtendedBuffer.queue
     * and will contain potentially multiple buffers of size bufferSize.
     */
    private final OutputStream outputStream = new OutputStream() {
        
        /**
         * Writes the 8 low-order bits (lowest order byte) of <code>b</code> to the end of
         * the currently active <code>outputBuffer</code>. The 24 high order bits are
         * ignored.
         * If as a result of the write <code>outputBuffer</code> has been filled, it is added
         * to <code>ExtendedBuffer.queue</code> and a new buffer created to handle future calls
         * to <code>write</code>.
         * 
         * @param b The integer to extract lowest 8 bits from.
         */
        @Override
        public void write(int b) throws IOException {
            
            // Append the lowest order byte from supplied integer to outputBuffer.
            outputBuffer.data[outputBuffer.position++] = (byte) b;
            
            // Handle outputBuffer becoming full by adding the buffer to ExtendedBuffer.queue
            // and preparing a new outPutbuffer fur subsequent writes.
            if (outputBuffer.position == bufferSize) {
                addBufferToQueue(outputBuffer);
                outputBuffer = new Buffer();
                outputBuffer.data = new byte[bufferSize];
            }
        }

        /**
         * Read a subset of bytes read from supplied byte array and write them into outputBuffer field.
         * Should size of the data to be read exceed outputBuffer size (as specified by bufferSize)
         * outputBuffer is iteratively filled and added to buffer queue, allowing full content to be read
         * into 1 or more buffers, but each buffer has a known maximum size.
         * 
         * @param b The byte array to read bytes from.
         * @param off The offset from start of source byte array to start reading from.
         * @param len The maximum number of bytes to read from the source byte array.
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            while (len > 0) {
                // Determine how many bytes from b (up to a maximum of len) can fit into the the output buffer
                // given the buffer size and current position.
                final int bytesToCopy = Math.min(bufferSize - outputBuffer.position, len);

                // Nibble off the number of bytes that can be read into the outputBuffer
                System.arraycopy(b, off, outputBuffer.data, outputBuffer.position, bytesToCopy);
                outputBuffer.position += bytesToCopy;

                // outputBuffer is full, add it to the buffer queue for future processing and continue reading
                if (outputBuffer.position == bufferSize) {
                    addBufferToQueue(outputBuffer);
                    outputBuffer = new Buffer();
                    outputBuffer.data = new byte[bufferSize];
                }

                len -= bytesToCopy;
                off += bytesToCopy;
            }
        }

        /**
         * Close the output stream, ensuring that all output buffers have been added to
         * ExtendedBuffer.queue.
         */
        @Override
        public void close() {
            addBufferToQueue(outputBuffer);
            if (outputBuffer.length == bufferSize) {
                queue.add(END_OF_FILE_MARKER);
            }
            outputBuffer = null;
        }

        /**
         * Add the supplied buffer to the buffer queue and increment the atomic 'available' counter
         * indicating how many bytes are queued in buffers ready to read.
         * 
         * @param buffer The buffer being added to <code>ExtendedBuffer.queue</code>.
         */
        private void addBufferToQueue(Buffer buffer) {
            // Set the size of content in the buffer and reset buffer position to start of buffer.
            // Add the buffer to the queue.
            buffer.length = buffer.position;
            buffer.position = 0;
            
            // Increment atomoic available value to include the total bytes in the buffer.
            available.addAndGet(buffer.length);
            
            // Add the buffer to the queue of buffers.
            queue.add(buffer);
        }
    };
}
