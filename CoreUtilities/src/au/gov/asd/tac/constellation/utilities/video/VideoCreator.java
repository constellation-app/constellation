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
package au.gov.asd.tac.constellation.utilities.video;

//import com.xuggle.mediatool.IMediaWriter;
//import com.xuggle.mediatool.ToolFactory;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Capture the actions of a graph to a WMV files
 *
 * @author algol
 */
public final class VideoCreator implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(VideoCreator.class.getName());

    private static final String WATERMARK = BrandingUtilities.APPLICATION_NAME;
    private static final AlphaComposite WM_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2F);
    private static final Font WM_FONT = new Font("Arial", Font.BOLD, 18);

    // Don't leave pauses longer than this in the video stream.
    private static final long MAX_TIMER_GAP = 2000;

    // Video won't record at greater sizes.
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    private final double wscale;
    private final double hscale;
    private final double scale;

    private final int width;
    private final int height;
    private final String out;
    private final long timestamp0;
    private final long prevTimestamp;
    private final long timerGap;

    private final BlockingQueue<VideoFrame> imageQueue;

    public VideoCreator(final int width, final int height, final String out) {
        wscale = width > MAX_WIDTH ? width / (double) MAX_WIDTH : 1;
        hscale = height > MAX_HEIGHT ? height / (double) MAX_HEIGHT : 1;
//        System.out.printf("@VC %dx%d wscale=%f hscale=%f\n", width, height, wscale, hscale);
        scale = wscale > 1 || hscale > 1 ? Math.max(wscale, hscale) : 1;

        int w = (int) (width / scale);
        this.width = w + w % 2;
        int h = (int) (height / scale);
        this.height = h + h % 2;
        this.out = out;
        timestamp0 = -1;
        prevTimestamp = -1;
        timerGap = 0;

        imageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        // TODO: removed the dependency to xuggle as the recording is not working - revisit this one day
//        // Get a dotted filename.
//        final File orig = new File(out);
//        final String name = orig.getName();
//        final File dottedFile = new File(orig.getParentFile(), "." + name);
//        final String dottedOut = dottedFile.getPath();
//
//        final IMediaWriter writer = ToolFactory.makeWriter(dottedOut);
//        writer.addVideoStream(0, 0, width, height);
//
//        try {
//            while (true) {
//                final VideoFrame img = imageQueue.take();
//                if (img.getTimestamp() < 0) {
//                    break;
//                }
//
//                if (timestamp0 == -1) {
//                    timestamp0 = img.getTimestamp();
//                    prevTimestamp = timestamp0;
//                }
//
//                BufferedImage bi = img.getVideoFrame();
//                if (scale > 1) {
////                    System.out.printf("@VC scale %f %dx%d to %dx%d\n", scale, bi.getWidth(), bi.getHeight(), (int)(bi.getWidth()/scale), (int)(bi.getHeight()/scale));
//                    final BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//                    final Graphics2D g2d = img2.createGraphics();
//                    g2d.scale(1 / scale, 1 / scale);
//                    g2d.drawImage(bi, 0, 0, null);
//                    g2d.dispose();
//                    bi = img2;
//                }
//
//                addWatermark(bi);
//
//                // If the previous frame was more than MAX_TIMER_GAP away, reduce the time in the video stream.
//                final long gap = img.getTimestamp() - prevTimestamp;
//                if (gap > MAX_TIMER_GAP) {
//                    timerGap += gap - MAX_TIMER_GAP;
//                }
//
//                final long t = img.getTimestamp() - timerGap;
//                writer.encodeVideo(0, bi, t, TimeUnit.MILLISECONDS);
////                System.out.printf("@VC t=%d\n", t);
//
//                prevTimestamp = img.getTimestamp();
//            }
//        } catch (InterruptedException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
//
//        writer.flush();
//        writer.close();
//
//        // Rename the file from the dotted name to the original name.
//        dottedFile.renameTo(new File(out));
//        final String msg = String.format("Video written to %s", out);
//        StatusDisplayer.getDefault().setStatusText(msg);
//        LOGGER.info(msg);
//
////        // TODO: change this class to a plugin as maybe you can't do a executePluginLater inside a thread
////        PluginEnvironment.getDefault().executePluginLater(null, new SimplePlugin() {
////            @Override
////            protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
////                ConstellationLoggerHelper.exportPropertyBuilder(this, orig, ConstellationLoggerHelper.SUCCESS);
////            }
////            @Override
////            public String getName() {
////                return "Record Vidoe";
////            }
////        }, false);
    }

    public BlockingQueue<VideoFrame> getQueue() {
        return imageQueue;
    }

    public static BufferedImage getImageFromFile(final File f) throws IOException {
        return ImageIO.read(f);
    }
// NOTE: Unused code below is referenced in commented code above, therefore it is commented out in case above code is revived
//    private static void addWatermark(final BufferedImage img) {
//        final Graphics2D g2d = (Graphics2D) img.getGraphics();
//        g2d.setComposite(WM_ALPHA);
//        g2d.setColor(Color.WHITE);
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2d.setFont(WM_FONT);
//        final FontMetrics fm = g2d.getFontMetrics();
//        final int x = img.getWidth() - fm.stringWidth(WATERMARK) - fm.getMaxDescent();
//        final int y = img.getHeight() - fm.getMaxDescent();
//        g2d.drawString(WATERMARK, x, y);
//        g2d.dispose();
//    }
}
