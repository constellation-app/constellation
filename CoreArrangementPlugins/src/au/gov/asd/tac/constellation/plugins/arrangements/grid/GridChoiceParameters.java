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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

/**
 * A convenient holder for the parameters of a Grid arrangement.
 *
 * @author algol
 */
public class GridChoiceParameters {

    private GridChoice gridChoice;
    private float sizeGain;
    private int horizontalGap;
    private int verticalGap;
    private boolean rowOffsets;

    public GridChoiceParameters(final GridChoice gridChoice, final float sizeGain, final int horizontalGap, final int verticalGap, final boolean rowOffsets) {
        this.gridChoice = gridChoice;
        this.sizeGain = sizeGain;
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
        this.rowOffsets = rowOffsets;
    }

    public static GridChoiceParameters getDefaultParameters() {
        return new GridChoiceParameters(GridChoice.SQUARE, 1.25F, 1, 1, false);
    }

    @Override
    public String toString() {
        return String.format("[%s gridChoice:%s gain:%f hgap:%s vgap:%s offsets:%s]", this.getClass().getSimpleName(), gridChoice, sizeGain, horizontalGap, verticalGap, rowOffsets);
    }

    public GridChoice getGridChoice() {
        return this.gridChoice;
    }

    public float getSizeGain() {
        return this.sizeGain;
    }

    public int getVerticalGap() {
        return this.verticalGap;
    }

    public int getHorizontalGap() {
        return this.horizontalGap;
    }

    public void setSizeGain(final float value) {
        this.sizeGain = value;
    }

    public void setVerticalGap(final int value) {
        this.verticalGap = value;
    }

    public void setHorizontalGap(final int value) {
        this.horizontalGap = value;
    }

    public void setGridChoice(final GridChoice value) {
        this.gridChoice = value;
    }

    public boolean hasRowOffsets() {
        return rowOffsets;
    }

    /**
     * Offset even columns vertically so side-by-side labels don't overlap.
     *
     * @param rowOffsets the new row offsets value.
     */
    public void setRowOffsets(final boolean rowOffsets) {
        this.rowOffsets = rowOffsets;
    }
}
