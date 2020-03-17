/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d2;

import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d2.BoundingBox2D.Box2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author algol
 */
public final class CirclePanel extends JPanel {

    private static final Random RAND = new Random(99);

    private static final Color TREE_RUN_COLOR = new Color(0f, 0f, 0f, 0.5f);
    private static final Color TREE_END_COLOR = new Color(1f, 0.7843f, 0.1569f, 0.5f);

    private Orb2D[] orbs;
    private Color[] colors;
    private List<Box2D> boxes;
    private Color boxColor;

    public CirclePanel() {
        orbs = new Orb2D[0];
        colors = new Color[0];
        redo(0);
    }

    public void redo(final int n) {
        orbs = new Orb2D[n];
        colors = new Color[n];
        final int width = this.getWidth();
        final int height = this.getHeight();
        for (int i = 0; i < n; i++) {
            final float x = width / 8f + RAND.nextFloat() * width * 6 / 8f;
            final float y = height / 8f + RAND.nextFloat() * height * 6 / 8f;
//            final float r = i%100==0 ? 100 + rand.nextFloat()*50 : 5 + 3;
            final float r = i % 100 == 0 ? 50 + RAND.nextFloat() * 75 : 5 + RAND.nextFloat() * 5;
//            final float r = 3;
            final Color c = new Color(RAND.nextFloat() * 0.96f, RAND.nextFloat() * .96f, RAND.nextFloat() * 0.96f, 0.5f);
            orbs[i] = new Orb2D(x, y, r);
            colors[i] = c;
        }

        setBoxes(null, false);
    }

    public Orb2D[] getOrbs() {
        return orbs;
    }

    public void setOrbs(final Orb2D[] orbs) {
        this.orbs = orbs;
    }

    public void setBoxes(final List<Box2D> boxes, final boolean isEnd) {
        this.boxes = boxes;
        boxColor = isEnd ? TREE_END_COLOR : TREE_RUN_COLOR;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < orbs.length; i++) {
            final Orb2D orb = orbs[i];
            final Color c = colors[i];
            final float r = orb.r;
            final int d = Math.round(2 * r);
            g2d.setColor(c);
            g2d.fillOval(Math.round(orb.x - r), Math.round(orb.y - r), d, d);
        }

        if (boxes != null) {
            g2d.setColor(boxColor);
            for (final Box2D rec : boxes) {
                g2d.drawRect((int) rec.minx, (int) rec.miny, (int) (rec.maxx - rec.minx), (int) (rec.maxy - rec.miny));
            }
        }
    }
}
