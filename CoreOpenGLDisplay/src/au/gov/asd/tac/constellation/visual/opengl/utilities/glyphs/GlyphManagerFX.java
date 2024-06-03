///*
// * Copyright 2010-2024 Australian Signals Directorate
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;
//
//import com.sun.javafx.application.PlatformImpl;
//import com.sun.javafx.font.FontStrike;
//import com.sun.javafx.font.Glyph;
//import com.sun.javafx.font.PGFont;
//import com.sun.javafx.font.coretext.CTFactory;
//import com.sun.javafx.geom.BaseBounds;
//import com.sun.javafx.geom.PathIterator;
//import com.sun.javafx.geom.RectBounds;
//import com.sun.javafx.geom.Shape;
//import com.sun.javafx.geom.transform.BaseTransform;
//import com.sun.javafx.scene.text.GlyphList;
//import com.sun.javafx.scene.text.TextLayout;
//import com.sun.javafx.text.PrismTextLayout;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.application.Platform;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.scene.SnapshotParameters;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.PixelFormat;
//import javafx.scene.image.PixelReader;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import javafx.scene.paint.Color;
//import javax.imageio.ImageIO;
//
///**
// * Convert text into images that can be passed to OpenGL.
// *
// * @author sirius
// */
//public class GlyphManagerFX implements GlyphManager {
//
//    private static final Logger LOGGER = Logger.getLogger(GlyphManagerFX.class.getName());
//
//    private static final int BACKGROUND_PADDING = 1;
//
//    // The number of pixels of empty space to insert between glyphs
//    private static final float PADDING = 3.0f + BACKGROUND_PADDING;
//
//    private static final float BUFFER = 2.0f;
//
//    // The padding that is applied to the left and top edges of the glyph texture
//    private static final float INITIAL_PADDING = (float) Math.ceil(PADDING);
//
//    // The identity snapshot paramters to use when copying images from the glyph texture canvas
//    private static final SnapshotParameters DEFAULT_SNAPSHOT_PARAMETER_IDS = new SnapshotParameters();
//
//    // A list of code points for control characters that cause javafx to fail to layout a string  when it encounters them
//    private static final int FIRST_FORIDDEN_CHARACTER = 0x202a;
//    private static final int LAST_FORBIDDEN_CHARACTER = 0x206f;
//    private static final boolean[] FORBIDDEN_CONTROL_CHARACTERS = new boolean[LAST_FORBIDDEN_CHARACTER - FIRST_FORIDDEN_CHARACTER + 1];
//
//    static {
//        FORBIDDEN_CONTROL_CHARACTERS[0x202a - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x202b - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x202c - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x202d - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x202e - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206a - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206b - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206c - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206d - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206e - FIRST_FORIDDEN_CHARACTER] = true;
//        FORBIDDEN_CONTROL_CHARACTERS[0x206f - FIRST_FORIDDEN_CHARACTER] = true;
//    }
//
//    private static char[] cleanStringBuffer = new char[1024];
//
//    private static String cleanString(String original) {
//
//        if (original == null) {
//            return null;
//        }
//
//        final int length = original.length();
//        if (cleanStringBuffer.length < length) {
//            cleanStringBuffer = new char[length];
//        }
//
//        final char[] buffer = cleanStringBuffer;
//        final boolean[] forbidden = FORBIDDEN_CONTROL_CHARACTERS;
//
//        original.getChars(0, length, buffer, 0);
//
//        int dst = 0;
//        for (int src = 0; src < length; src++) {
//            final char c = buffer[src];
//            if (c < FIRST_FORIDDEN_CHARACTER || c > LAST_FORBIDDEN_CHARACTER || !forbidden[c - FIRST_FORIDDEN_CHARACTER]) {
//                buffer[dst++] = c;
//            }
//        }
//
//        return new String(buffer, 0, dst);
//    }
//
////    private static final int[] FORBIDDEN_CONTROL_CHARACTERS = new int[]{
////        0x202a, 0x202b, 0x202c, 0x202d, 0x202e, 0x206a, 0x206b, 0x206c, 0x206d, 0x206e, 0x206f
////    };
//    // A default no-op GlyphStream to use when the user specifies null
//    private static final GlyphStream DEFAULT_GLYPH_STREAM = new GlyphStream() {
//        @Override
//        public void newLine(float width) {
//        }
//
//        @Override
//        public void addGlyph(int glyphPosition, float x, float y) {
//        }
//    };
//
//    // A comparator that compares GlyphLocations in a way that ensures that their left boundaries are ascending
//    private static final Comparator<GlyphLocation> GLYPH_LOCATION_COMPARATOR = (GlyphLocation a, GlyphLocation b) -> {
//        return Float.compare(a.getLeft(), b.getLeft());
//    };
//
//    // A singleton layout instance
//    private final TextLayout layout = new PrismTextLayout();
//
//    // A singleton array that is used when rendering shape segments
//    private final float[] points = new float[6];
//
//    // A singleton WritableImage of the write size to receive the image from the glyph texture canvas
//    private final WritableImage copyImage;
//
//    private GlyphLocation[] glyphLocations = new GlyphLocation[0];
//
//    // Details of the font used to render the glyphs
//    private final String fontFamily;
//    private final float fontSize;
//    private final PGFont font;
//    private final FontStrike fontStrike;
//    private final float fontAscent;
//    private final float fontLineHeight;
//
//    // The width and height of each glyph texture page
//    private final int textureWidth;
//    private final int textureHeight;
//
//    // The scaling factors to convert glyph dimensions from texture coordinates into world coordinates
//    private final float widthScalingFactor;
//    private final float heightScalingFactor;
//
//    private final List<Canvas> glyphTextures = new ArrayList<>();
//    private Canvas currentGlyphTexture;
//    private GraphicsContext currentGraphicsContext;
//    private float currentGlyphPage = 0.0f;
//    private float currentGlyphLocationX = INITIAL_PADDING;
//    private float currentGlyphLocationY = INITIAL_PADDING;
//
//    // The number of unique glyphs currently stored
//    private int glyphCount = 0;
//
//    // The height of the tallest glyph in the current glyph row
//    // This allows the next row to be placed as high as possible to conserve space
//    private float currentGlyphRowHeight = 0.0f;
//
//    // A cache of all previously seen glyphs as GlyphInfo objects
//    private final GlyphInfo[] glyphInfoList = new GlyphInfo[2 ^ 16];
//    private final Map<Integer, GlyphInfo> glyphInfoMap = new HashMap<>();
//
//    // A cache of all previously seen glyph collections
//    private final Map<GlyphCollection, Integer> glyphCollectionPositions = new HashMap<>();
//
//    // The texture coordinates for each stored glyph
//    private float[] glyphTextureCoordinates = new float[1024];
//
//    private final int replacementGlyphCode;
//
//    /**
//     * Creates a new GlyphManager.
//     *
//     * @param fontFamily the family of the font used to render the glyphs.
//     * @param fontSize the size of the font used to render the glyphs.
//     * @param textureWidth the width in pixels of each glyph texture page.
//     * @param textureHeight the height in pixels of each glyph texture page.
//     */
//    public GlyphManagerFX(String fontFamily, float fontSize, int textureWidth, int textureHeight) {
//
//        // Save the constructor parameters for later
//        this.fontFamily = fontFamily;
//        this.fontSize = fontSize;
//        this.textureWidth = textureWidth;
//        this.textureHeight = textureHeight;
//
//        // Create the font that will render the glyphs
//        font = CTFactory.getFontFactory().createFont(fontFamily, fontSize);
//        fontStrike = font.getStrike(BaseTransform.IDENTITY_TRANSFORM);
//
//        // Cache some font metrics for efficiency
//        fontLineHeight = fontStrike.getMetrics().getLineHeight();
//        heightScalingFactor = textureHeight / fontLineHeight;
//        widthScalingFactor = textureWidth / fontLineHeight;
//        fontAscent = fontStrike.getMetrics().getAscent();
//
//        // Create a singleton image to use when copying pixel information out of
//        // the glyph texture pages
//        copyImage = new WritableImage(textureWidth, textureHeight);
//
//        // Create the first glyph texture page
//        createNewGlyphTexturePage();
//
//        // Ensure that JavaFX is running
//        PlatformImpl.startup(() -> {
//        });
//
//        replacementGlyphCode = fontStrike.getGlyph(' ').getGlyphCode();
//    }
//
//    @Override
//    public int getTextureWidth() {
//        return textureWidth;
//    }
//
//    @Override
//    public int getTextureHeight() {
//        return textureHeight;
//    }
//
//    /**
//     * Returns the number of glyphs.
//     *
//     * @return the number of glyphs.
//     */
//    @Override
//    public int getGlyphCount() {
//        return glyphCount;
//    }
//
//    /**
//     * Returns the number of glyph pages.
//     *
//     * @return the number of glyph pages.
//     */
//    @Override
//    public int getGlyphPageCount() {
//        return glyphTextures.size();
//    }
//
//    /**
//     * Returns an array holding the texture coordinates of each glyph. The
//     * coordinates of each glyph (x, y, width, height) are stored in 4
//     * continuous entries in the array starting at glyphPosition * 4. The
//     * returned array can be of any size but only the first glyphCount * 4
//     * entries are defined. As texture coordinates are always in the range 0..1,
//     * the whole number part of the x value is used to store the page that holds
//     * the glyph.
//     *
//     * @return an array holding the texture coordinates of each glyph.
//     */
//    @Override
//    public float[] getGlyphTextureCoordinates() {
//        return glyphTextureCoordinates;
//    }
//
//    /**
//     * Returns the scaling factor that should be applied to glyph texture widths
//     * to convert them to display widths.
//     *
//     * @return the scaling factor that should be applied to glyph texture widths
//     * to convert them to display widths.
//     */
//    @Override
//    public float getWidthScalingFactor() {
//        return widthScalingFactor;
//    }
//
//    /**
//     * Returns the scaling factor that should be applied to glyph texture
//     * heights to convert them to display heights.
//     *
//     * @return the scaling factor that should be applied to glyph texture
//     * heights to convert them to display heights.
//     */
//    @Override
//    public float getHeightScalingFactor() {
//        return heightScalingFactor;
//    }
//
//    /**
//     * Convenience method to add the full ascii character set.
//     */
//    public void addAscii() {
//        for (char c = 32; c < 127; c++) {
//            renderText(String.valueOf(c), null);
//        }
//    }
//
//    @Override
//    public int createBackgroundGlyph(float alpha) {
//        validateGlyphLocation(fontLineHeight, fontLineHeight);
//
//        int intensity = (int) (alpha * 255);
//        int color = (0xFF << 24) | (intensity << 16) | (intensity << 8) | intensity;
//        int[] colorArray = new int[]{color};
//
//        PixelWriter writer = currentGraphicsContext.getPixelWriter();
//
//        int left = (int) currentGlyphLocationX - BACKGROUND_PADDING;
//        int top = (int) currentGlyphLocationY - BACKGROUND_PADDING;
//        int right = (int) (currentGlyphLocationX + fontLineHeight) + BACKGROUND_PADDING;
//        int bottom = (int) (currentGlyphLocationY + fontLineHeight) + BACKGROUND_PADDING;
//
//        for (int x = left; x <= right; x++) {
//            for (int y = top; y <= bottom; y++) {
//                writer.setPixels(x, y, 1, 1, PixelFormat.getIntArgbInstance(), colorArray, 0, 1);
//            }
//        }
//
//        // Allocate the glyph a new position and store its texture coordinates
//        final int backgroundGlyphPosition = addTextureCoordinates(fontLineHeight, fontLineHeight, 0.0f);
//
//        advanceGlyphLocation(fontLineHeight, fontLineHeight);
//
//        return backgroundGlyphPosition;
//    }
//
//    /**
//     * Process a string of text, converting the characters into a stream of
//     * glyphs.
//     *
//     * @param text the text to be processed.
//     * @param glyphStream the stream where glyphs will be output.
//     */
//    public synchronized void renderText(String text, GlyphStream glyphStream) {
//
//        if (text.contains(SeparatorConstants.NEWLINE)) {
//            throw new RuntimeException("Cannot render a line of text containing '\n'");
//        }
//
//        // Use a no-op glyph stream if none supplied
//        if (glyphStream == null) {
//            glyphStream = DEFAULT_GLYPH_STREAM;
//        }
//
//        // Layout the text
//        layout.setContent(text, font);
//
//        // Offset the advance so that all x values are relative to the center of the text
//        BaseBounds layoutBounds = layout.getVisualBounds(TextLayout.TYPE_TEXT);
//        float advance = -layoutBounds.getMinX() - layoutBounds.getWidth() / 2.0f;
//
//        glyphStream.newLine(layoutBounds.getWidth() / fontLineHeight);
//
//        // The text may have several runs (eg mixture of left-to-right and right-to-left text)
//        GlyphList[] glyphLists = layout.getRuns();
//        for (GlyphList glyphList : glyphLists) {
//
//            // Each run will have a number of glyphs
//            int currentGlyphCount = glyphList.getGlyphCount();
//            for (int g = 0; g < currentGlyphCount; g++) {
//
//                // See if we have already seen this glyph before
//                GlyphInfo glyphInfo = getGlyphInfo(glyphList.getGlyphCode(g));
//
//                // Ensure that this glyph has been rendered to the texture
//                glyphInfo.ensureRenderedToTexture();
//
//                // Send the normalised glyph location to the glyph stream
//                glyphInfo.sendToGlyphStream(glyphStream, glyphList.getPosX(g) + advance, glyphList.getPosY(g));
//            }
//
//            // Adjust the advance by the width of this run
//            advance += glyphList.getWidth();
//        }
//    }
//
//    /**
//     * Performs an identical function to renderText() except that glyphs that
//     * overlap are combined into a single glyph containing the group of glyphs.
//     * This reduces the artifacts when that group of glyphs are rendered.
//     *
//     * @param text the text containing the characters to be rendered.
//     * @param glyphStream the glyph stream to output the characters to.
//     */
//    @Override
//    public synchronized void renderTextAsLigatures(String text, GlyphStream glyphStream) {
//
//        if (text.contains(SeparatorConstants.NEWLINE)) {
//            throw new RuntimeException("Cannot render a line of text containing '\n'");
//        }
//
////        for (int i=0; i<FORBIDDEN_CONTROL_CHARACTERS.length; i++) {
////            text = text.replace(String.valueOf(Character.toChars(FORBIDDEN_CONTROL_CHARACTERS[i])), "");
////        }
//        text = cleanString(text);
//
//        // Use a no-op glyph stream if none supplied
//        if (glyphStream == null) {
//            glyphStream = DEFAULT_GLYPH_STREAM;
//        }
//
//        // Layout the text
//        layout.setContent(text, font);
//        layout.setDirection(TextLayout.DIRECTION_LTR);
//
//        // Offset the advance so that all x values are relative to the center of the text
//        BaseBounds layoutBounds;
//        try {
//            layoutBounds = layout.getVisualBounds(TextLayout.TYPE_TEXT);
//        } catch (Exception ex) {
//            layout.setContent("### Error Rendering Label :( ###", font);
//            layout.setDirection(TextLayout.DIRECTION_LTR);
//            LOGGER.log(Level.WARNING, "Error Rendering Text {0}", text);
//            layoutBounds = layout.getVisualBounds(TextLayout.TYPE_TEXT);
//        }
//
//        float advanceCentre = -layoutBounds.getMinX() - layoutBounds.getWidth() / 2.0f;
//
//        glyphStream.newLine(layoutBounds.getWidth() / fontLineHeight);
//
//        // The text may have several runs (eg mixture of left-to-right and right-to-left text)
//        GlyphList[] glyphLists = layout.getRuns();
////        Arrays.sort(glyphLists, (o1, o2)->{return Float.compare(o1.getLocation().x, o2.getLocation().x);});
//        for (GlyphList glyphList : glyphLists) {
//            float advance = glyphList.getLocation().x + advanceCentre;
//
//            int currentGlyphCount = glyphList.getGlyphCount();
//            if (currentGlyphCount == 0) {
//                continue;
//            }
//
//            ensureGlyphLocationCapacity(currentGlyphCount);
//
//            boolean ordered = true;
//            boolean separated = true;
//
//            glyphLocations[0].init(glyphList.getGlyphCode(0), glyphList.getPosX(0), glyphList.getPosY(0));
//
//            for (int g = 1; g < currentGlyphCount; g++) {
//
//                glyphLocations[g].init(glyphList.getGlyphCode(g), glyphList.getPosX(g), glyphList.getPosY(g));
//
//                ordered &= glyphLocations[g].left >= glyphLocations[g - 1].left;
//                separated &= glyphLocations[g].left > glyphLocations[g - 1].right;
//            }
//
//            // If all the glyphs are separated from each other then just draw them individually
//            if (separated) {
//                for (int g = 0; g < currentGlyphCount; g++) {
//                    GlyphLocation glyphLocation = glyphLocations[g];
//                    glyphLocation.glyph.ensureRenderedToTexture();
//                    glyphLocation.sendToGlyphStream(glyphStream, advance);
//                }
//
//                // Otherwise find the glyphs that touch/overlap and draw then as single combined glyphs
//            } else {
//
//                // Sort the glyphs so that their left boundaries are in ascending order (if not already the case)
//                if (!ordered) {
//                    Arrays.sort(glyphLocations, 0, currentGlyphCount, GLYPH_LOCATION_COMPARATOR);
//                }
//
//                // Start with only the first glyph in a group
//                int firstGlyph = 0;
//                int currentGlyph = 0;
//                float groupRight = 0;
//
//                // Process each glyph from left to right
//                while (currentGlyph < currentGlyphCount) {
//
//                    // Extend the right edge of the current group to include the current glyph
//                    groupRight = Math.max(groupRight, glyphLocations[currentGlyph].right);
//
//                    // Move on to the next glyph
//                    currentGlyph++;
//
//                    // If the current glyph is the last glyph or does not overlap with those currently in the group
//                    // then render the group we have so far. The current glyph will start a new group.
//                    if (currentGlyph == currentGlyphCount || glyphLocations[currentGlyph].left > groupRight) {
//
//                        // If there is only 1 glyph in this group then render it as a single glyph
//                        if (firstGlyph == currentGlyph - 1) {
//                            GlyphLocation glyphLocation = glyphLocations[firstGlyph];
//                            glyphLocation.glyph.ensureRenderedToTexture();
//                            glyphLocation.sendToGlyphStream(glyphStream, advance);
//
//                            // If there are more than one glyphs is this group then render it as a combined glyph collection (ligature)
//                        } else {
//                            GlyphCollection glyphCollection = new GlyphCollection(glyphLocations, firstGlyph, currentGlyph);
//                            glyphCollection.ensureRenderedToTexure();
//                            glyphCollection.sendToGlyphStream(glyphStream, advance, 0);
//                        }
//
//                        // Start a new group beginning with the current glyph
//                        groupRight = 0.0f;
//                        firstGlyph = currentGlyph;
//                    }
//                }
//            }
//
//            // Adjust the advance by the width of this run
//            advance += glyphList.getWidth();
//        }
//    }
//
//    /**
//     * Moves the current glyph location to the next position that can
//     * accommodate a glyph with the specified bounds. In most cases, the current
//     * location has enough space to render the next glyph and no action is
//     * taken. If there is insufficient space, either horizontally or vertically,
//     * to render the next glyph, a new row is started which could involve moving
//     * on to a new page.
//     *
//     * @param width the width of the next glyph to be added.
//     * @param height the height of the next glyph to be added.
//     */
//    private void validateGlyphLocation(float width, float height) {
//
//        // If there is not enough width in the current row to add the new glyph then start a new row
//        if (currentGlyphLocationX + width > textureWidth - PADDING) {
//            currentGlyphLocationX = INITIAL_PADDING;
//            currentGlyphLocationY = (int) Math.ceil(currentGlyphLocationY + currentGlyphRowHeight + PADDING);
//            currentGlyphRowHeight = 0.0f;
//        }
//
//        // If there is not enough height in the current texture page for the new glyph then start a new page
//        if (currentGlyphLocationY + height > textureHeight - PADDING) {
//            currentGlyphLocationX = currentGlyphLocationY = INITIAL_PADDING;
//            currentGlyphRowHeight = 0.0f;
//            createNewGlyphTexturePage();
//        }
//    }
//
//    /**
//     * Moves the current glyph location to the right by the width of the last
//     * rendered glyph plus some padding.
//     *
//     * @param width the width of the last rendered glyph.
//     * @param height the height of the last rendered glyph.
//     */
//    private void advanceGlyphLocation(float width, float height) {
//        currentGlyphLocationX = (int) Math.ceil(currentGlyphLocationX + width + PADDING);
//        currentGlyphRowHeight = Math.max(height, currentGlyphRowHeight);
//    }
//
//    /**
//     * Reads the pixel data from a specified glyph texture page into a specified
//     * byte buffer.
//     *
//     * @param page the glyph texture page to read.
//     * @param buffer the buffer to store the pixel data in.
//     */
//    @Override
//    public void readGlyphTexturePage(int page, ByteBuffer buffer) {
//
//        CountDownLatch latch = new CountDownLatch(1);
//        Platform.runLater(() -> {
//            readGlyphTexturePageFX(page, buffer);
//            latch.countDown();
//        });
//        boolean waiting = true;
//        while (waiting) {
//            try {
//                latch.await();
//                waiting = false;
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    private void readGlyphTexturePageFX(int page, ByteBuffer buffer) {
//        // Get the glyph texture page to be written to the buffer
//        Canvas glyphTexture = glyphTextures.get(page);
//
//        // Snapshot the pixels from the canvas into an image where the pixel data will be available
//        // This seems inefficient but there does not seem to be any way to directly access pixel
//        // data from a Canvas.
//        glyphTexture.snapshot(DEFAULT_SNAPSHOT_PARAMETER_IDS, copyImage);
//
//        // Read all pixel values into the buffer. The image is greyscale so the blue
//        // intensity is used get the pixel intensity.
//        PixelReader reader = copyImage.getPixelReader();
//        for (int y = 0; y < textureHeight; y++) {
//            for (int x = 0; x < textureWidth; x++) {
//                buffer.put((byte) reader.getArgb(x, y));
//            }
//        }
//    }
//
//    /**
//     * Creates a new glyph texture page and updates the current graphics context
//     * to match.
//     */
//    private void createNewGlyphTexturePage() {
//        currentGlyphPage = glyphTextures.size();
//        currentGlyphTexture = new Canvas(textureWidth, textureHeight);
//        glyphTextures.add(currentGlyphTexture);
//        currentGraphicsContext = currentGlyphTexture.getGraphicsContext2D();
//        currentGraphicsContext.setFill(Color.BLACK);
//        currentGraphicsContext.fillRect(0, 0, textureWidth, textureHeight);
//        currentGraphicsContext.setFill(Color.WHITE);
//    }
//
//    /**
//     * Draws a glyph at the current glyph position in the glyph texture.
//     *
//     * @param graphicsContext the graphics context for the glyph texture.
//     * @param glyphShape the shape of the glyph.
//     * @param glyphBounds the bounds of the glyph.
//     */
//    private void drawGlyph(Shape glyphShape, float offsetX, float offsetY, boolean begin, boolean end) {
//
//        // Translate the shape to the correct position in the glyph texture
//        currentGraphicsContext.setTransform(
//                1.0, 0.0, 0.0, 1.0,
//                currentGlyphLocationX + offsetX,
//                currentGlyphLocationY + offsetY
//        );
//
//        // Draw each segment in the glyph shape
//        PathIterator glyphPathIterator = glyphShape.getPathIterator(BaseTransform.IDENTITY_TRANSFORM);
//        if (begin) {
//            currentGraphicsContext.beginPath();
//        }
//        while (!glyphPathIterator.isDone()) {
//            switch (glyphPathIterator.currentSegment(points)) {
//                case PathIterator.SEG_MOVETO:
//                    currentGraphicsContext.moveTo(points[0], points[1]);
//                    break;
//
//                case PathIterator.SEG_LINETO:
//                    currentGraphicsContext.lineTo(points[0], points[1]);
//                    break;
//
//                case PathIterator.SEG_QUADTO:
//                    currentGraphicsContext.quadraticCurveTo(points[0], points[1], points[2], points[3]);
//                    break;
//
//                case PathIterator.SEG_CUBICTO:
//                    currentGraphicsContext.bezierCurveTo(points[0], points[1], points[2], points[3], points[4], points[5]);
//                    break;
//
//                case PathIterator.SEG_CLOSE:
//                    currentGraphicsContext.closePath();
//                    break;
//                default:
//                    break;
//            }
//
//            glyphPathIterator.next();
//        }
//        if (end) {
//            currentGraphicsContext.fill();
//        }
//    }
//
//    /**
//     * Returns the glyph info for a specified glyph code. If this is the first
//     * time that an info object has been requested for this glyph code then a
//     * new GlyphInfo object is created.
//     */
//    private GlyphInfo getGlyphInfo(int glyphCode) {
//        // TODO: this needs to be reviewed as we are probably doing the
//        // wrong thing and missing glyphs in plan 2 onwards
//        if (glyphInfoList.length > glyphCode && glyphCode > -1) {
//            GlyphInfo glyphInfo = glyphInfoList[glyphCode];
//            if (glyphInfo == null) {
//                glyphInfo = glyphInfoList[glyphCode] = new GlyphInfo(glyphCode);
//            }
//            return glyphInfo;
//        }
//
//        GlyphInfo glyphInfo = glyphInfoMap.get(glyphCode);
//        if (glyphInfo == null) {
//            glyphInfoMap.put(glyphCode, glyphInfo = new GlyphInfo(glyphCode));
//        }
//        return glyphInfo;
//    }
//
//    private void ensureGlyphLocationCapacity(int capacity) {
//        if (glyphLocations.length < capacity) {
//            glyphLocations = new GlyphLocation[capacity];
//            for (int i = 0; i < capacity; i++) {
//                glyphLocations[i] = new GlyphLocation();
//            }
//        }
//    }
//
//    private int addTextureCoordinates(float width, float height, float buffer) {
//        if (glyphCount * 4 == glyphTextureCoordinates.length) {
//            glyphTextureCoordinates = Arrays.copyOf(glyphTextureCoordinates, glyphTextureCoordinates.length * 2);
//        }
//
//        int glyphBoundsPointer = glyphCount << 2;
//        glyphTextureCoordinates[glyphBoundsPointer++] = currentGlyphPage + (currentGlyphLocationX - buffer) / textureWidth;
//        glyphTextureCoordinates[glyphBoundsPointer++] = (currentGlyphLocationY - buffer) / textureHeight;
//        glyphTextureCoordinates[glyphBoundsPointer++] = (width + buffer * 2) / textureWidth;
//        glyphTextureCoordinates[glyphBoundsPointer++] = (height + buffer * 2) / textureHeight;
//
//        return glyphCount++;
//    }
//
////    /**
////     * A GlyphStream is provided to the GlyphManager when a string of text is
////     * rendered and receives a call-back for each glyph that needs to rendered
////     * in order to display that text.
////     *
////     * The call-back includes all the information needed to render the glyph
////     * when used in combination with data structures provided by the
////     * GlyphManager.
////     *
////     * The glyphPosition refers to the position of the glyph in the
////     * glyphTextureCoordinates buffer. This can be used to get the x, y, width
////     * and height of the glyph in texture coordinates.
////     *
////     * The x and y values provide the offset to the top-left corner of the glyph
////     * in render coordinates. Render coordinates have their origin at the top of
////     * the text, half way along the width of the rendered text. Render
////     * coordinates are proportionally scaled so that the line height of rendered
////     * font is 1.
////     */
////    public static interface GlyphStream {
////
////        public void newLine(float width);
////
////        /**
////         * Called to indicate that a glyph should be drawn at the specified x,y
////         * offset.
////         *
////         * @param glyphPosition the position of the glyph in the glyph registry.
////         * @param x the x location of the glyph.
////         * @param y the y location of the glyph.
////         */
////        public void addGlyph(int glyphPosition, float x, float y);
////    }
//    /**
//     * Writes a specified glyph texture page as a PNG image to the specified
//     * OutputStream.
//     *
//     * @param page the glyph texture page to be written.
//     * @param out the OutputStream to write the PNG image to.
//     *
//     * @throws IOException if an error occurs while writing the image.
//     */
//    @Override
//    public void writeGlyphBuffer(int page, OutputStream out) throws IOException {
//        WritableImage image = new WritableImage((int) currentGlyphTexture.getWidth(), (int) currentGlyphTexture.getHeight());
//        glyphTextures.get(page).snapshot(DEFAULT_SNAPSHOT_PARAMETER_IDS, image);
//        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", out);
//    }
//
//    /**
//     * Copies the pixels representing a specified glyph to a JavaFX
//     * GraphicsContext as a specified position and at a specified scale.
//     *
//     * This is very inefficient and is for testing purposes only.
//     *
//     * @param graphicsContext the JavaFX GraphicsContext to copy the glyph image
//     * to.
//     * @param glyphPosition the position of the glyph (as opposed to the glyph
//     * code)
//     * @param x the destination x coordinate.
//     * @param y the destination y coordinate.
//     * @param scale the scale of the destination image (height in pixels)
//     * @param widthScale the width of the character.
//     */
//    public void copyGlyph(GraphicsContext graphicsContext, int glyphPosition, double x, double y, double scale, double widthScale) {
//
//        // Get the bounds of the glyph in texture coordinates
//        int boundsPointer = glyphPosition * 4;
//        float glyphX = glyphTextureCoordinates[boundsPointer++];
//        float glyphY = glyphTextureCoordinates[boundsPointer++];
//        float glyphWidth = glyphTextureCoordinates[boundsPointer++];
//        float glyphHeight = glyphTextureCoordinates[boundsPointer++];
//
//        int page = (int) glyphX;
//        glyphX -= page;
//
//        WritableImage image = new WritableImage((int) currentGlyphTexture.getWidth(), (int) currentGlyphTexture.getHeight());
//        glyphTextures.get(page).snapshot(DEFAULT_SNAPSHOT_PARAMETER_IDS, image);
//
//        PixelWriter writer = image.getPixelWriter();
//        PixelReader reader = image.getPixelReader();
//        for (int pixelX = 0; pixelX < image.getWidth(); pixelX++) {
//            for (int pixelY = 0; pixelY < image.getHeight(); pixelY++) {
//                int pixel = reader.getArgb(pixelX, pixelY);
//                int intensity = pixel & 255;
//                pixel &= 0xFFFFFF;
//                pixel |= intensity << 24;
//                writer.setArgb(pixelX, pixelY, pixel);
//            }
//        }
//
//        graphicsContext.drawImage(
//                image,
//                glyphX * currentGlyphTexture.getWidth(),
//                glyphY * currentGlyphTexture.getHeight(),
//                glyphWidth * currentGlyphTexture.getWidth(),
//                glyphHeight * currentGlyphTexture.getHeight(),
//                x,
//                y,
//                glyphWidth * scale * widthScalingFactor * widthScale,
//                glyphHeight * scale * heightScalingFactor
//        );
//    }
//
//    private class GlyphCollection {
//
//        public final float width;
//        public final float height;
//        public final float originX;
//        public final float originY;
//        private final int[] glyphCodes;
//        private final float[] offsetX;
//        private final float[] offsetY;
//        private final int hashCode;
//
//        public GlyphCollection(GlyphLocation[] glyphLocations, int firstGlyph, int lastGlyph) {
//
//            final int glyphCount = lastGlyph - firstGlyph;
//            glyphCodes = new int[glyphCount];
//            offsetX = new float[glyphCount];
//            offsetY = new float[glyphCount];
//
//            float minX = Float.MAX_VALUE;
//            float minY = Float.MAX_VALUE;
//            float maxX = Float.MIN_VALUE;
//            float maxY = Float.MIN_VALUE;
//
//            int hash = 0;
//
//            for (int i = 0, glyphIndex = firstGlyph; i < glyphCount; i++, glyphIndex++) {
//                glyphCodes[i] = glyphLocations[glyphIndex].glyph.code;
//                offsetX[i] = glyphLocations[glyphIndex].x;
//                offsetY[i] = glyphLocations[glyphIndex].y;
//
//                GlyphInfo glyphInfo = getGlyphInfo(glyphCodes[i]);
//                final RectBounds bounds = glyphInfo.bounds;
//                minX = Math.min(minX, offsetX[i] + bounds.getMinX());
//                minY = Math.min(minY, offsetY[i] + bounds.getMinY());
//                maxX = Math.max(maxX, offsetX[i] + bounds.getMaxX());
//                maxY = Math.max(maxY, offsetY[i] + bounds.getMaxY());
//                hash = hash * 113 + glyphCodes[i];
//            }
//
//            for (int i = 0; i < glyphCount; i++) {
//                offsetX[i] -= minX;
//                offsetY[i] -= minY;
//                hash = hash * 113 + Float.hashCode(offsetX[i]);
//                hash = hash * 113 + Float.hashCode(offsetY[i]);
//            }
//
//            width = maxX - minX;
//            height = maxY - minY;
//            originX = minX;
//            originY = minY;
//            hashCode = hash;
//        }
//
//        public int ensureRenderedToTexure() {
//
//            Integer position = glyphCollectionPositions.get(this);
//            if (position != null) {
//                return position;
//            }
//
//            validateGlyphLocation(width, height);
//
//            for (int i = 0; i < glyphCodes.length; i++) {
//                drawGlyph(getGlyphInfo(glyphCodes[i]).shape, offsetX[i], offsetY[i], i == 0, i == glyphCodes.length - 1);
//            }
//
//            final int glyphPosition = addTextureCoordinates(width, height, BUFFER);
//
//            advanceGlyphLocation(width, height);
//
//            glyphCollectionPositions.put(this, glyphPosition);
//
//            return glyphPosition;
//        }
//
//        public void sendToGlyphStream(GlyphStream glyphStream, float x, float y) {
//            glyphStream.addGlyph(glyphCollectionPositions.get(this), (x + originX - BUFFER) / fontLineHeight, (y + originY - fontAscent - BUFFER) / fontLineHeight);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof GlyphCollection) {
//                final GlyphCollection other = (GlyphCollection) obj;
//                return this.hashCode == other.hashCode
//                        && Arrays.equals(this.glyphCodes, other.glyphCodes)
//                        && Arrays.equals(this.offsetX, other.offsetX)
//                        && Arrays.equals(this.offsetY, other.offsetY);
//            }
//            return false;
//        }
//
//        @Override
//        public int hashCode() {
//            return hashCode;
//        }
//    }
//
//    private class GlyphInfo {
//
//        public final int code;
//        public final Glyph glyph;
//        public final Shape shape;
//        public final RectBounds bounds;
//
//        private int position = -1;
//
//        public GlyphInfo(int glyphCode) {
//            // TODO: this needs to be reviewed as we are probably doing the
//            // wrong thing and missing glyphs in plan 2 onwards
//            if (glyphCode >= 65535 || glyphCode < 0) {
//                glyphCode = replacementGlyphCode;
//            }
//            code = glyphCode;
//            glyph = fontStrike.getGlyph(glyphCode);
//            shape = glyph.getShape();
//            bounds = shape.getBounds();
//        }
//
//        public void ensureRenderedToTexture() {
//            if (position < 0) {
//
//                validateGlyphLocation(bounds.getWidth(), bounds.getHeight());
//
//                // Draw the glyph to the glyph texture
//                drawGlyph(shape, -bounds.getMinX(), -bounds.getMinY(), true, true);
//
//                // Allocate the glyph a new position and store its texture coordinates
//                position = addTextureCoordinates(bounds.getWidth(), bounds.getHeight(), BUFFER);
//
//                advanceGlyphLocation(bounds.getWidth(), bounds.getHeight());
//            }
//        }
//
//        public void sendToGlyphStream(GlyphStream glyphStream, float x, float y) {
//            glyphStream.addGlyph(position, (x + bounds.getMinX() - BUFFER) / fontLineHeight, (y + bounds.getMinY() - fontAscent - BUFFER) / fontLineHeight);
//        }
//    }
//
//    private class GlyphLocation {
//
//        private GlyphInfo glyph;
//        private float x;
//        private float y;
//        private float left;
//        private float right;
//
//        public void init(int glyphCode, float x, float y) {
//            this.glyph = getGlyphInfo(glyphCode);
//            this.x = x;
//            this.y = y;
//            this.left = x + glyph.bounds.getMinX();
//            this.right = left + glyph.bounds.getWidth();
//        }
//
//        public void sendToGlyphStream(GlyphStream glyphStream, float advance) {
//            glyph.sendToGlyphStream(glyphStream, x + advance, y);
//        }
//
//        public GlyphInfo getGlyph() {
//            return glyph;
//        }
//
//        public void setGlyph(GlyphInfo glyph) {
//            this.glyph = glyph;
//        }
//
//        public float getX() {
//            return x;
//        }
//
//        public void setX(float x) {
//            this.x = x;
//        }
//
//        public float getY() {
//            return y;
//        }
//
//        public void setY(float y) {
//            this.y = y;
//        }
//
//        public float getLeft() {
//            return left;
//        }
//
//        public void setLeft(float left) {
//            this.left = left;
//        }
//
//        public float getRight() {
//            return right;
//        }
//
//        public void setRight(float right) {
//            this.right = right;
//        }
//
//    }
//}
