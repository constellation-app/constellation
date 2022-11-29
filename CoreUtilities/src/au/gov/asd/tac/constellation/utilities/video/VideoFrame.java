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
package au.gov.asd.tac.constellation.utilities.video;

import java.awt.image.BufferedImage;

/**
 * Data model for a video frame
 *
 * @author algol
 */
public final class VideoFrame {

    private final BufferedImage videoFrame;
    private final long timestamp;

    public VideoFrame(final BufferedImage videoFrame, final long timestamp) {
        this.videoFrame = videoFrame;
        this.timestamp = timestamp;
    }

    public BufferedImage getVideoFrame() {
        return videoFrame;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[VideoFrame %d %s]", timestamp, videoFrame);
    }
}
