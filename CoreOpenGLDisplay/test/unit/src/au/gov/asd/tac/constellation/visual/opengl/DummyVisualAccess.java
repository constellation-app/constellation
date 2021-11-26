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
package au.gov.asd.tac.constellation.visual.opengl;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 * @author antares
 */
public class DummyVisualAccess implements VisualAccess {

    final Camera camera = new Camera();
    final Vector3f zoomOutVector = new Vector3f(0, 0, -1f);

    @Override
    public void updateInternally() {
    }

    @Override
    public List<VisualChange> getIndigenousChanges() {
        return Collections.emptyList();
    }

    @Override
    public void beginUpdate() {
    }

    @Override
    public void endUpdate() {
    }

    public void zoomOut() {
        camera.lookAtEye.subtract(zoomOutVector);
    }

    @Override
    public ConstellationColor getBackgroundColor() {
        return ConstellationColor.BLACK;
    }

    @Override
    public ConstellationColor getHighlightColor() {
        return ConstellationColor.RED;
    }

    @Override
    public DrawFlags getDrawFlags() {
        return DrawFlags.ALL;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public int getVertexCount() {
        return 2;
    }

    @Override
    public int getConnectionCount() {
        return 4;
    }

    @Override
    public float getBlazeSize() {
        return 0.5f;
    }

    @Override
    public float getBlazeOpacity() {
        return 1;
    }

    @Override
    public float getConnectionOpacity() {
        return 0.7f;
    }

    @Override
    public int getTopLabelCount() {
        return 2;
    }

    @Override
    public int getBottomLabelCount() {
        return 2;
    }

    @Override
    public int getConnectionAttributeLabelCount() {
        return 1;
    }

    @Override
    public int getConnectionLabelCount(final int connection) {
        return 1;
    }

    @Override
    public boolean isLabelSummary(int connection) {
        return false;
    }

    @Override
    public ConstellationColor getTopLabelColor(int labelNum) {
        return ConstellationColor.BLUE;
    }

    @Override
    public ConstellationColor getBottomLabelColor(int labelNum) {
        return ConstellationColor.BLUE;
    }

    @Override
    public ConstellationColor getConnectionLabelColor(int labelNum) {
        return ConstellationColor.BLUE;
    }

    @Override
    public float getTopLabelSize(int labelNum) {
        return 1;
    }

    @Override
    public float getBottomLabelSize(int labelNum) {
        return 1;
    }

    @Override
    public float getConnectionLabelSize(int labelNum) {
        return 1;
    }

    @Override
    public int getConnectionId(int connection) {
        return connection;
    }

    @Override
    public int getVertexId(int vertex) {
        return vertex;
    }

    @Override
    public float getX(int vertex) {
        return vertex * 4 - 2;
    }

    @Override
    public float getY(int vertex) {
        return 0;
    }

    @Override
    public float getZ(int vertex) {
        return -10;
    }

    @Override
    public float getX2(int vertex) {
        return 0;
    }

    @Override
    public float getY2(int vertex) {
        return 0;
    }

    @Override
    public float getZ2(int vertex) {
        return 0;
    }

    @Override
    public ConstellationColor getVertexColor(int vertex) {
        return ConstellationColor.CARROT;
    }

    @Override
    public String getBackgroundIcon(int vertex) {
        return "Background.Flat Square";
    }

    @Override
    public String getForegroundIcon(int vertex) {
        return "Character.Question Mark";
    }

    @Override
    public boolean isVertexSelected(int vertex) {
        return vertex == 1;
    }

    @Override
    public float getVertexVisibility(int vertex) {
        return 1;
    }

    @Override
    public boolean isVertexDimmed(int vertex) {
        return vertex == 0;
    }

    @Override
    public float getRadius(int vertex) {
        return 1;
    }

    @Override
    public boolean isBlazed(int vertex) {
        return vertex == 0;
    }

    @Override
    public int getBlazeAngle(int vertex) {
        return 45;
    }

    @Override
    public ConstellationColor getBlazeColor(int vertex) {
        return ConstellationColor.BANANA;
    }

    @Override
    public String getNWDecorator(int vertex) {
        return vertex == 1 ? "Background.Flat Square" : null;
    }

    @Override
    public String getNEDecorator(int vertex) {
        return null;
    }

    @Override
    public String getSEDecorator(int vertex) {
        return null;
    }

    @Override
    public String getSWDecorator(int vertex) {
        return null;
    }

    @Override
    public ConstellationColor getConnectionColor(int connection) {
        return ConstellationColor.WHITE;
    }

    @Override
    public boolean isConnectionSelected(int connection) {
        return connection == 2;
    }

    @Override
    public float getConnectionVisibility(int connection) {
        return 1;
    }

    @Override
    public boolean isConnectionDimmed(int connection) {
        return connection == 3;
    }

    @Override
    public LineStyle getConnectionLineStyle(int connection) {
        return LineStyle.SOLID;
    }

    @Override
    public float getConnectionWidth(int connection) {
        return 1 + (connection / 2f);
    }

    @Override
    public int getConnectionLowVertex(int connection) {
        return 0;
    }

    @Override
    public int getConnectionHighVertex(int connection) {
        return connection == 0 ? 0 : 1;
    }

    @Override
    public int getLinkCount() {
        return 2;
    }

    @Override
    public int getLinkLowVertex(int link) {
        return 0;
    }

    @Override
    public int getLinkHighVertex(int link) {
        return link;
    }

    @Override
    public int getLinkSource(int link) {
        return 0;
    }

    @Override
    public int getLinkDestination(int link) {
        return link;
    }

    @Override
    public int getLinkConnectionCount(int link) {
        return link == 0 ? 1 : 3;
    }

    @Override
    public int getLinkConnection(int link, int pos) {
        return link + pos;
    }

    @Override
    public String getVertexTopLabelText(int vertex, int labelNum) {
        return "hello" + (labelNum == 1 ? "2" : "");
    }

    @Override
    public String getVertexBottomLabelText(int vertex, int labelNum) {
        return "world" + (labelNum == 1 ? "2" : "");
    }

    @Override
    public String getConnectionLabelText(int connection, int labelNum) {
        return String.valueOf(connection);
    }

    @Override
    public ConnectionDirection getConnectionDirection(int connection) {
        return (connection == 3) ? ConnectionDirection.LOW_TO_HIGH : ConnectionDirection.UNDIRECTED;
    }

    @Override
    public boolean isConnectionDirected(int connection) {
        return connection != 2;
    }
}
