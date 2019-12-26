package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import au.gov.asd.tac.constellation.visual.opengl.utilities.GlyphManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;

/**
 * Convert text into images that can be passed to OpenGL.
 * <p>
 * TODO Reset everything when the font changes.
 *
 * @author algol
 */
public final class GlyphsBuffer implements GlyphManager {
    public static final float SCALING_FACTOR = 7f;

    // Where do we draw the text?
    //
    public static final int BASEX = 60;
    public static final int BASEY = 180;

    private final BufferedImage drawing;

    /**
     * This logical font is always present.
     */
    public static final String DEFAULT_FONT = Font.SANS_SERIF;
    public static final int DEFAULT_FONT_SIZE = 64;

    public static final int DEFAULT_BUFFER_TYPE = BufferedImage.TYPE_BYTE_GRAY;

    /**
     * The size of the rectangle buffer.
     * An arbitrary number, not too small that we need lots of buffers,
     * but not too large that OpenGL can't cope.
     */
    public static final int DEFAULT_TEXTURE_BUFFER_SIZE = 512 + 256;

    private Font[] fonts;

    /**
     * The glyphs must be scaled down to be rendered at a reasonable size.
     * The font height seems to be a reasonable scale.
     */
    private int maxFontHeight;
    private String line;

    // Which boundaries do we draw?
    //
    private boolean drawRuns, drawIndividual, drawCombined;

    private final GlyphRectangleBuffer textureBuffer;

    /**
     * A default no-op GlyphStream to use when the user specifies null.
     */
    private static final GlyphStream DEFAULT_GLYPH_STREAM = new GlyphStream() {
        @Override
        public void newLine(final float width) {
            System.out.printf("GlyphStream newLine %f\n", width);
        }

        @Override
        public void addGlyph(final int glyphPosition, final float x, final float y) {
            System.out.printf("GlyphStream addGlyph %d %f %f\n", glyphPosition, x, y);
        }
    };

    public GlyphsBuffer() {
        this(null, Font.PLAIN, DEFAULT_FONT_SIZE, DEFAULT_TEXTURE_BUFFER_SIZE, DEFAULT_BUFFER_TYPE);
    }

    public GlyphsBuffer(final String[] fontNames) {
        this(fontNames, Font.PLAIN, DEFAULT_FONT_SIZE, DEFAULT_TEXTURE_BUFFER_SIZE, DEFAULT_BUFFER_TYPE);
    }

    public GlyphsBuffer(final String[] fontNames, final int style, final int fontSize, final int textureBufferSize, final int bufferType) {

        // TODO Ensure that the BufferedImage is wide enough to draw into.
        // TODO Can we get away with using BufferedImage.TYPE_BYTE_GRAY?
        //
//        drawing = new BufferedImage(2048, 256, BufferedImage.TYPE_INT_ARGB);
//        drawing = new BufferedImage(2048, 256, BufferedImage.TYPE_BYTE_GRAY);
        drawing = new BufferedImage(2048, 256, bufferType);

        textureBuffer = new GlyphRectangleBuffer(textureBufferSize, textureBufferSize, bufferType);

        if(fontNames!=null && fontNames.length>0) {
            setFonts(fontNames, style, fontSize);
        } else {
            setFonts(new String[]{DEFAULT_FONT}, style, fontSize);
        }

        drawRuns = false;
        drawIndividual = false;
        drawCombined = false;
    }

    public BufferedImage getImage() {
        return drawing;
    }

    /**
     * Remove codepoints that can ruin layouts.
     *
     * @param s
     * @return The string with the forbidden characters removed.
     */
    static String cleanString(final String s) {
        return s
            .trim()
            .codePoints()
            .filter(cp -> !((cp>=0x202a && cp<=0x202e) || (cp>=0x206a && cp<=0x206f)))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString()
        ;
    }

    public final void setLine(final String line) {
        this.line = cleanString(line);
        renderTextAsLigatures(line, null);
    }

    public void setBoundaries(final boolean drawRuns, final boolean drawIndividual, final boolean drawCombined) {
        this.drawRuns = drawRuns;
        this.drawIndividual = drawIndividual;
        this.drawCombined = drawCombined;
        renderTextAsLigatures(line, null);
    }

    /**
     * Set the fonts to be used by the font renderer.
     * <p>
     * The most specific font (ie the font containing the fewest glyphs) should
     * be first. This allows a different font to be used for Latin characters.
     * <p>
     * Because setting new fonts implies a complete redraw,
     * the existing texture buffers are reset, so all strings have to be
     * rebuilt.
     *
     * @param fontNames An array of Font instances.
     * @param style The style to be used to render the fonts (Font.PLAIN, Font.BOLD).
     * @param fontSize The font size.
     */
    public void setFonts(final String[] fontNames, final int style, final int fontSize) {
        fonts = Arrays.stream(fontNames).map(fn -> new Font(fn, style, fontSize)).toArray(Font[]::new);
        maxFontHeight = Arrays.stream(fonts).map(f -> {
                final Graphics2D g2d = drawing.createGraphics();
                final FontMetrics fm = g2d.getFontMetrics(f);
                final int height = fm.getHeight();
                System.out.printf("@@font %s height %d\n", f, height);
                g2d.dispose();
                return height;
            }).mapToInt(i -> i).max().orElseThrow(NoSuchElementException::new);
        textureBuffer.reset();

//        createBackgroundGlyph(0.5f);

        renderTextAsLigatures(line, null);
    }

    public String[] getFonts() {
        return Arrays.stream(fonts).map(f -> f.getFontName()).toArray(String[]::new);
    }

    /**
     * Merge bounding boxes that overlap on the x axis.
     * <p>
     * This code feels a bit ugly. Have another look later.
     *
     * @param boxes A List<Rectangle> representing possibly overlapping glyph bounding boxes.
     *
     * @return A List<Rectangle> of non-overlapping bounding boxes.
     */
    private static List<Rectangle> mergeBoxes(final List<Rectangle> boxes) {
        final List<Rectangle> merged = new ArrayList<>();
        for(int i=boxes.size()-1;i>=0;) {
            Rectangle curr = boxes.get(i--);
            if(i==-1) {
                merged.add(curr);
                break;
            }
            while(i>=0) {
                final Rectangle prev = boxes.get(i);
                if((prev.x + prev.width) < curr.x) {
                    merged.add(curr);
                    break;
                }
                final int y = Math.min(prev.y, curr.y);
                curr = new Rectangle(
                    prev.x,
                    y,
                    Math.max(prev.x+prev.width, curr.x+curr.width)-prev.x,
                    Math.max(prev.y+prev.height, curr.y+curr.height)-y
                );
                i--;
                if(i==-1) {
                    merged.add(curr);
                    break;
                }
            }
        }

        return merged;
    }

    /**
     * Draw a String that may contain multiple directions and scripts.
     * <p>
     * This is not a general purpose text drawer. Instead, it caters to the
     * kind of string that are likely to be found in a CONSTELLLATION label;
     * short, lacking punctuation,but possibly containing multi-language characters.
     * <p>
     * A String is first broken up into sub-strings that consist of codepoints
     * of the same direction. These sub-strings are further broken into
     * sub-sub-strings that contain the same font. Each sub-sub-string can then
     * be drawn using TextLayout.draw(), and the associated glyphs can be
     * determined using Font.layoutGlyphVector().
     * <p>
     * The glyph images can then be drawn into an image buffer for use by OpenGL
     * to draw node and connection labels. Some glyphs (such as those used in
     * cursive Arabic script) will overlap: any overlapping glyphs will be
     * treated as a unit.
     * <p>
     * Hashes of the glyph images are used to determine if the image has already
     * been
     * @param text
     */
    @Override
    public void renderTextAsLigatures(final String text, GlyphStream glyphStream) {
        if(text==null || text.isEmpty()) {
            return;
        }

        System.out.printf("@@render [%s] %s\n", text, glyphStream);
        if(glyphStream==null) {
            glyphStream = DEFAULT_GLYPH_STREAM;
        }

        final Graphics2D g2d = drawing.createGraphics();
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, drawing.getWidth(), drawing.getHeight());
        g2d.setColor(Color.WHITE);

//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

//        if(text==null){
//            g2d.dispose();
//            return;
//        }

//        g2d.setColor(Color.ORANGE);
//        g2d.drawLine(BASEX, BASEY, BASEX+1000, BASEY);

        int x = BASEX;
        final int y0 = BASEY;

        final FontRenderContext frc = g2d.getFontRenderContext();

        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        final List<GlyphRectangle> glyphRectangles = new ArrayList<>();

        for(final DirectionRun drun : DirectionRun.getDirectionRuns(text)) {
            for(final FontRun frun : FontRun.getFontRuns(drun.run, fonts)) {
//                // Draw an indicator line to show where the font run starts.
//                //
//                g2d.setColor(Color.LIGHT_GRAY);
//                g2d.drawLine(x, y0-128, x, y0+64);

                final String spart = frun.string;
                final int flags = drun.getFontLayoutDirection() | Font.LAYOUT_NO_START_CONTEXT | Font.LAYOUT_NO_LIMIT_CONTEXT;
                final GlyphVector gv = frun.font.layoutGlyphVector(frc, spart.toCharArray(), 0, spart.length(), flags);
//                final int ng = gv.getNumGlyphs();
//                System.out.printf("* %s %s\n", gv.getClass(), gv);
//                System.out.printf("* numGlyphs %d\n", gv.getNumGlyphs());

                // Some fonts are shaped such that the left edge of the pixel bounds is
                // to the left of the starting point, and the right edge of the pixel
                // bounds is to to the right of the pixel bounds (for example,
                // the word "Test" in font "Montez" from fonts.google.com).
                // Figure that out here.
                //
                final Rectangle pixelBounds = gv.getPixelBounds(null, x, y0);
                if(pixelBounds.x<x) {
                    System.out.printf("adjust %s %s %s\n", x, pixelBounds.x, x-pixelBounds.x);
                    x += x-pixelBounds.x;
                }

                System.out.printf("* font run %s %d->%s\n", frun, x, pixelBounds);
                g2d.setColor(Color.WHITE);
                g2d.setFont(frun.font);

                final Map<AttributedCharacterIterator.Attribute,Object> attrs = new HashMap<>();
                attrs.put(TextAttribute.RUN_DIRECTION, drun.direction);
                attrs.put(TextAttribute.FONT, frun.font);
                final TextLayout layout = new TextLayout(spart, attrs, frc);

//                System.out.printf("* isLTR %s\n", layout.isLeftToRight());
                layout.draw(g2d, x, y0);

//                // The font glyphs are drawn such that the reference point is at (x,y).
//                // (See the javadoc for FontMetrics.) However, the pixelBounds
//                // are where the glyphs are actually drawn. To place the glyphs
//                // accurately within the bounding box, we need to know the difference.
//                //
//                final int frontDiff = pixelBounds.x - x;
//                final int topDiff = pixelBounds.y - y0;
//                textureBuffer.drawGlyph(layout, pixelBounds, frun.font, frontDiff, topDiff);

                // Iterate through the glyphs to get the bounding boxes.
                //
                final List<Rectangle> boxes = new ArrayList<>();
                for(int glyphIx=0; glyphIx<gv.getNumGlyphs(); glyphIx++) {
                    final int gc = gv.getGlyphCode(glyphIx);
                    if(gc!=0) {
                        final Rectangle gr = gv.getGlyphPixelBounds(glyphIx, frc, x, y0);
                        if(gr.width>0) {
//                            System.out.printf("rec %s\n", gr);
                            boxes.add(gr);

                            left = Math.min(left, gr.x);
                            right = Math.max(right, gr.x+gr.width);
                        }
                    }
//                    else {
//                        System.out.printf("glyphcode %d\n", gc);
//                    }
                }

                // Sort them by x position.
                //
                Collections.sort(boxes, (Rectangle r0, Rectangle r1) -> r0.x - r1.x);

                final List<Rectangle> merged = mergeBoxes(boxes);
//                System.out.printf("merged: %s\n", merged);

                // Add each merged glyph rectangle to the texture buffer.
                // Remember the texture position and rectangle (see below).
                //
                final FontMetrics fm = g2d.getFontMetrics(frun.font);
                merged.forEach(r -> {
                    final int position = textureBuffer.addRectImage(drawing.getSubimage(r.x, r.y, r.width, r.height));
                    glyphRectangles.add(new GlyphRectangle(position, r, fm.getAscent()));
//                    glyphStream.addGlyph(position, x/maxFontHeight, (y0-fm.getAscent())/maxFontHeight);
                });

                if(drawRuns) {
                    g2d.setColor(Color.RED);
                    g2d.drawRect(pixelBounds.x, pixelBounds.y, pixelBounds.width, pixelBounds.height);
                }

                if(drawIndividual) {
                    for(int glyphIx=0; glyphIx<gv.getNumGlyphs(); glyphIx++) {
                        final int gc = gv.getGlyphCode(glyphIx);
                        if(gc!=0) {
                            final Rectangle gr = gv.getGlyphPixelBounds(glyphIx, frc, x, y0);
    //                        final Point2D pos = gv.getGlyphPosition(glyphIx);
//                            System.out.printf("* GV  %d %s %s %s\n", glyphIx, gv.getGlyphCode(glyphIx), gr, spart);
                            if(gr.width!=0 && gr.height!=0) {
                                g2d.setColor(Color.GREEN);
                                g2d.drawRect(gr.x, gr.y, gr.width, gr.height);
                            }

//                                final Shape shape = gv.getGlyphOutline(glyphIx, x, y0);
//                                g2d.setColor(Color.MAGENTA);
//                                g2d.fill(shape);
                        }
                        else {
                            System.out.printf("glyphcode %d\n", gc);
                        }
                    }
                }

                if(drawCombined) {
                    g2d.setColor(Color.MAGENTA);
                    merged.forEach(r -> {g2d.drawRect(r.x, r.y, r.width, r.height);});
                }

//                g2d.setColor(Color.LIGHT_GRAY);
//                g2d.drawLine(x, y0-2, (int)(x + layout.getAdvance()), y0+2);

                // Just like some fonts draw to the left of their start points (see above),
                // some fonts draw after their advance.
                // Figure that out here.
                //
                final int width = (int)Math.max(layout.getAdvance(), pixelBounds.width);
                x += width;
            }
        }

        g2d.dispose();

        // Add the background for this text.
        //
        glyphStream.newLine((right-left)/(float)maxFontHeight);

        // The glyphRectangles list contains the absolute positions of each glyph rectangle
        // in pixels as drawn above.
        // Our OpenGL shaders expect x in world units, where x is relative to the centre
        // of the entire line rather than the left.
        //
        final float centre = (left+right)/2f;
        for(final GlyphRectangle gr : glyphRectangles) {
            final float cx = (gr.rect.x-centre)/(float)maxFontHeight;
            final float cy = (gr.rect.y + gr.ascent)/(float)maxFontHeight - 1.5f;//maxFontHeight/SCALING_FACTOR;
//            System.out.printf("GlyphRectangle: %s %f %f\n", gr, cx, cy);
            glyphStream.addGlyph(gr.position, cx, cy);
        }
    }

    @Override
    public int getGlyphCount() {
        return textureBuffer.getRectangleCount();
    }

    @Override
    public int getGlyphPageCount() {
        return textureBuffer.size();
    }

    @Override
    public void readGlyphTexturePage(final int page, final ByteBuffer buffer) {
        textureBuffer.readRectangleBuffer(page, buffer);
    }

    @Override
    public float[] getGlyphTextureCoordinates() {
        return textureBuffer.getRectangleCoordinates();
    }

    @Override
    public int getTextureWidth() {
        return textureBuffer.width;
    }

    @Override
    public int getTextureHeight() {
        return textureBuffer.height;
    }

    @Override
    public float getWidthScalingFactor() {
        return SCALING_FACTOR;
    }

    @Override
    public float getHeightScalingFactor() {
        return SCALING_FACTOR;
    }

    BufferedImage getTextureBuffer() {
        return textureBuffer.get(textureBuffer.size()-1);
    }

    @Override
    public int createBackgroundGlyph(float alpha) {
        final BufferedImage bg = new BufferedImage(maxFontHeight, maxFontHeight, DEFAULT_BUFFER_TYPE);
        final Graphics2D g2d = bg.createGraphics();
        final int intensity = (int) (alpha * 255);
        g2d.setColor(new Color((intensity<<16) | (intensity<<8) | intensity));
        g2d.fillRect(0, 0, maxFontHeight, maxFontHeight);
        g2d.dispose();

        final int position = textureBuffer.addRectImage(bg);

        return position;
    }

    @Override
    public void writeGlyphBuffer(final int page, final OutputStream out) throws IOException {
        final BufferedImage img = textureBuffer.get(page);
        ImageIO.write(img, "png", out);
    }

    private static class GlyphRectangle {
        final int position;
        final Rectangle rect;
        final int ascent;

        GlyphRectangle(final int position, final Rectangle rect, final int ascent) {
            this.position = position;
            this.rect = rect;
            this.ascent = ascent;
        }

        @Override
        public String toString() {
            return String.format("[GlyphRectangle p=%d r=%s a=%d]", position, rect, ascent);
        }
    }
}
