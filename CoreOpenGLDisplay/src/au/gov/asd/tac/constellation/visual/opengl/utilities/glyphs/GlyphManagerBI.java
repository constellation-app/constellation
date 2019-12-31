package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import au.gov.asd.tac.constellation.visual.opengl.utilities.GlyphManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Convert text into images that can be passed to OpenGL.
 * <p>
 * TODO Reset everything when the font changes.
 *
 * @author algol
 */
public final class GlyphManagerBI implements GlyphManager {
    private static final Logger LOGGER = Logger.getLogger(GlyphManagerBI.class.getName());

    /**
     * This logical font is always present.
     */
    public static final String DEFAULT_FONT_NAME = "Arial";//Font.SANS_SERIF;
    public static final int DEFAULT_FONT_SIZE = 64;
    public static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, DEFAULT_FONT_SIZE);

    /**
     * For Windows user-installed fonts (typically "%LOCALAPPDATA%/Microsoft/Windows/Fonts").
     * TODO Figure out something for non-Windows systems.
     */
    private static final String LOCAL_APP_DATA = "LOCALAPPDATA";

    public static final int DEFAULT_BUFFER_TYPE = BufferedImage.TYPE_BYTE_GRAY;

    // Where do we draw the text?
    //
    private static final int BASEX = 60;
//    public static final int BASEY = 180;
    private final int basey;

    private final int bufferType;
    private final BufferedImage drawing;

    /**
     * The size of the rectangle buffer.
     * An arbitrary number, not too small that we need lots of buffers,
     * but not too large that OpenGL can't cope.
     */
    public static final int DEFAULT_TEXTURE_BUFFER_SIZE = 2048;

    // The fonts being used.
    // We can't derive the names from the fonts, because a .otf font may have been specified
    // (see setFonts()).
    //
    private String[] fontNames;
    private Font[] fonts;

    /**
     * The glyphs must be scaled down to be rendered at a reasonable size.
     * The font height seems to be a reasonable scale.
     */
    private int maxFontHeight;

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

    public GlyphManagerBI(final String[] fontNames, final int fontSize, final int textureSize) {
        this(fontNames, Font.PLAIN, fontSize, textureSize, DEFAULT_BUFFER_TYPE);
    }

    public GlyphManagerBI(final String[] fontNames, final int fontStyle, final int fontSize, final int textureBufferSize, final int bufferType) {

        this.bufferType = bufferType;
        textureBuffer = new GlyphRectangleBuffer(textureBufferSize, textureBufferSize, bufferType);

        if(fontNames!=null && fontNames.length>0) {
            setFonts(fontNames, fontStyle, fontSize);
        } else {
            setFonts(new String[]{DEFAULT_FONT_NAME}, fontStyle, fontSize);
        }

        // Make the drawing buffer twice the max font height.
        // Draw text at the mid y point.
        // TODO Ensure that the BufferedImage is wide enough to draw into.
        //
        drawing = new BufferedImage(4096, maxFontHeight*2, bufferType);
        basey = maxFontHeight;

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

    public void setBoundaries(final boolean drawRuns, final boolean drawIndividual, final boolean drawCombined) {
        this.drawRuns = drawRuns;
        this.drawIndividual = drawIndividual;
        this.drawCombined = drawCombined;
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
     * <p>
     * Java doesn't recognise OTF fonts; they have to be created and derived,
     * rather than just "new Font()". This means fonts such as Google's Noto
     * CJK fonts need special treatment.
     * Names ending in ".otf" are treated as filenames. If such names start with
     * "LOCALAPPDATA", replace it with the relevant Windows directory.
     *
     * @param fontNames An array of font names or .otf files.
     * @param fontStyle The font style (typically Font.PLAIN or Font.BOLD).
     * @param fontSize The font size.
     */
    public void setFonts(final String[] fontNames, final int fontStyle, final int fontSize) {
        // If the fontName array does not contain the default font, add it to the end.
        //
        final String[] tempNames;
        final Optional<String> hasDefault = Arrays.stream(fontNames).filter(fn -> fn.trim().toLowerCase().equals(DEFAULT_FONT_NAME.toLowerCase())).findFirst();
        if(hasDefault.isPresent()) {
            tempNames = Arrays.copyOf(fontNames, fontNames.length);
        } else {
            tempNames = Arrays.copyOf(fontNames, fontNames.length+1);
            tempNames[fontNames.length] = DEFAULT_FONT_NAME;
        }

        this.fontNames = tempNames;

        fonts = Arrays.stream(tempNames).map(fn -> {
            fn = fn.trim();
            if(fn.toLowerCase().endsWith(".otf")) {
                File otfFile = new File(fn);
                try {
                    // This appears to be an OTF font file.
                    // If it is relative, look in the user's local profile for
                    // the font file and create the font.
                    //
                    if(!otfFile.isAbsolute()) {
                        final String lap = System.getenv(LOCAL_APP_DATA);
                        if(lap!=null) {
                            otfFile = new File(String.format("%s/Microsoft/Windows/Fonts/%s", lap, fn));
                        }
                    }
                    LOGGER.info(String.format("Reading OTF font from %s", otfFile));
                    final Font otf = Font.createFont(Font.TRUETYPE_FONT, otfFile);
                    return otf.deriveFont(fontStyle, fontSize);
                } catch (final FontFormatException | IOException ex) {
                    LOGGER.log(Level.SEVERE, String.format("Can't load OTF font %s from %s", fn, otfFile), ex);
                    return null;
                }
            } else {
                return new Font(fn, fontStyle, fontSize);
            }
        }).filter(f -> f!=null).toArray(Font[]::new);

        for(int i=0; i<this.fonts.length; i++) {
            LOGGER.info(String.format("Font %d: %s", i, this.fonts[i]));
        }

        // Create a temporary BufferedImage so we can get the metrics we need.
        // Should we use getMaxCharBounds()?
        //
        final BufferedImage tmpBI = new BufferedImage(1, 1, bufferType);
        final Graphics2D g2d = tmpBI.createGraphics();
        maxFontHeight = Arrays.stream(fonts).map(f -> {
                final FontMetrics fm = g2d.getFontMetrics(f);
                final int height = fm.getHeight();
                return height;
            }).mapToInt(i -> i).max().orElseThrow(NoSuchElementException::new);
        g2d.dispose();

        textureBuffer.reset();

//        createBackgroundGlyph(0.5f);
    }

    /**
     * Return the names of the fonts being used (including the extra default font).
     *
     * @return The names of the fonts being used (including the extra default font).
     */
    public String[] getFonts() {
        return Arrays.copyOf(fontNames, fontNames.length);
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
     * Draw codepoints that may contain multiple directions and scripts.
     * <p>
     * This is not a general purpose text drawer. Instead, it caters to the
     * kind of string that are likely to be found in a CONSTELLLATION label;
     * short, lacking punctuation, but possibly containing
     * multi-language, multi-script, multi-directional characters.
     * <p>
     * A String is first broken up into sub-strings that consist of codepoints
     * of the same direction. These sub-strings are further broken into
     * sub-sub-strings that contain the same font. Each sub-sub-string can then
     * be drawn using TextLayout.draw(), and the associated glyphs can be
     * determined using Font.layoutGlyphVector().
     * <p>
     * The bounding box of each glyph is determined. Some glyphs (for example,
     * those used in cursive script such as Arabic and fancy Latin fonts) will
     * overlap; these bounding boxes are merged. From here on it's all about
     * rectangles that happen to contain useful images.
     * <p>
     * The rectangles can then be drawn into an image buffer that can be copied
     * directly to a texture buffer for use by OpenGL to draw node and
     * connection labels.
     * <p>
     * Hashes of the contents of the rectangles are used to determine if the
     * glyph image has already been drawn. If it has, the same rectangle is
     * reused.
     *
     * @param text The text top be rendered.
     */
    @Override
    public void renderTextAsLigatures(final String text, GlyphStream glyphStream) {
        if(text==null || text.isEmpty()) {
            return;
        }

        if(glyphStream==null) {
            glyphStream = DEFAULT_GLYPH_STREAM;
        }

        final Graphics2D g2d = drawing.createGraphics();
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, drawing.getWidth(), drawing.getHeight());
        g2d.setColor(Color.WHITE);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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
        final int y0 = basey;

        final FontRenderContext frc = g2d.getFontRenderContext();

        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
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

                // Some fonts are shaped such that the left edge of the pixel bounds is
                // to the left of the starting point, and the right edge of the pixel
                // bounds is to to the right of the pixel bounds (for example,
                // the word "Test" in font "Montez" from fonts.google.com).
                // Figure that out here.
                //
                final Rectangle pixelBounds = gv.getPixelBounds(null, x, y0);
                if(pixelBounds.x<x) {
//                    System.out.printf("adjust %s %s %s\n", x, pixelBounds.x, x-pixelBounds.x);
                    x += x-pixelBounds.x;
                }

//                System.out.printf("* font run %s %d->%s\n", frun, x, pixelBounds);
                g2d.setColor(Color.WHITE);
                g2d.setFont(frun.font);

                final Map<AttributedCharacterIterator.Attribute,Object> attrs = new HashMap<>();
                attrs.put(TextAttribute.RUN_DIRECTION, drun.direction);
                attrs.put(TextAttribute.FONT, frun.font);
                final TextLayout layout = new TextLayout(spart, attrs, frc);

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
                            top = Math.min(top, gr.y);
                            bottom = Math.max(bottom, gr.y+gr.height);
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
                            if(gr.width!=0 && gr.height!=0) {
                                g2d.setColor(Color.GREEN);
                                g2d.drawRect(gr.x, gr.y, gr.width, gr.height);
                            }
                        }
                    }
                }

                if(drawCombined) {
                    g2d.setColor(Color.MAGENTA);
                    merged.forEach(r -> {g2d.drawRect(r.x, r.y, r.width, r.height);});
                }

                if(drawRuns || drawIndividual || drawCombined) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawRect(0, 0, drawing.getWidth()-1, drawing.getHeight()-1);
                }

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
        // The OpenGL shaders expect x,y in world units, where x is relative to the centre
        // of the entire line rather than the left. See NodeLabel.vs etc.
        // [0] the index of the glyph in the glyphInfoTexture
        // [1..2] x and y offsets of this glyph from the top centre of the line of text
        //
        // cx centers the text horizontally. Subtracting 0.1f aligns the text
        //      with the background (which subtracts 0.2 for some reason, see
        //      ConnectionLabelBatcher and NodeLabelBatcher).
        // cy centers the top and bottom vertically
        //
        final float centre = (left+right)/2f;
        for(final GlyphRectangle gr : glyphRectangles) {
            final float cx = (gr.rect.x-centre)/(float)maxFontHeight - 0.1f;
//            final float cy = (gr.rect.y-top+((maxFontHeight-(bottom-top))/2f))/(float)maxFontHeight;
//            final float cy = (2*gr.rect.y-top+maxFontHeight-bottom)/(2f*maxFontHeight);
            final float cy = (gr.rect.y-(top+bottom)/2f)/(float)(maxFontHeight) + 0.5f;
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
        return textureBuffer.width/maxFontHeight;
    }

    @Override
    public float getHeightScalingFactor() {
        return textureBuffer.height/maxFontHeight;
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
