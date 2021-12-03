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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter;

/**
 * A holder for the parameters of a Scatter3d arrangement.
 *
 * @author CrucisGamma
 */
public class Scatter3dChoiceParameters {

    private String xDimension;
    private String yDimension;
    private String zDimension;
    private boolean xLogarithmic = false;
    private boolean yLogarithmic = false;
    private boolean zLogarithmic = false;
    private Boolean doNotScale = false;

    public Scatter3dChoiceParameters() {
        // Method intentionally left blank
    }

    public static Scatter3dChoiceParameters getDefaultParameters() {
        return new Scatter3dChoiceParameters();
    }

    @Override
    public String toString() {
        return "";
    }

    public void setXDimension(final String xDimension) {
        this.xDimension = xDimension;
    }

    public void setYDimension(final String yDimension) {
        this.yDimension = yDimension;
    }

    public void setZDimension(final String zDimension) {
        this.zDimension = zDimension;
    }

    public void setLogarithmicX(final Boolean xLogarithmic) {
        this.xLogarithmic = xLogarithmic;
    }

    public void setLogarithmicY(final Boolean yLogarithmic) {
        this.yLogarithmic = yLogarithmic;
    }

    public void setLogarithmicZ(final Boolean zLogarithmic) {
        this.zLogarithmic = zLogarithmic;
    }

    public void setDoNotScale(final Boolean doNotScale) {
        this.doNotScale = doNotScale;
    }

    public String getXDimension() {
        return this.xDimension;
    }

    public String getYDimension() {
        return this.yDimension;
    }

    public String getZDimension() {
        return this.zDimension;
    }

    public boolean isLogarithmicX() {
        return xLogarithmic;
    }

    public boolean isLogarithmicY() {
        return yLogarithmic;
    }

    public boolean isLogarithmicZ() {
        return zLogarithmic;
    }

    public boolean isDoNotScale() {
        return doNotScale;
    }
}
