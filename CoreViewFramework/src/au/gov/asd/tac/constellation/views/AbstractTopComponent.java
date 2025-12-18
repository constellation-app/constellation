/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views;

import au.gov.asd.tac.constellation.plugins.logging.ConstellationLogger;
import au.gov.asd.tac.constellation.views.preferences.ViewOptionsPanelController;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A generic top component.
 *
 * @param <P> The class used by this {@link TopComponent} to display content.
 *
 * @author cygnus_x-1
 */
public abstract class AbstractTopComponent<P> extends TopComponent {

    public enum Spawn {
        LEFT,
        RIGHT,
        BOTTOM,
        CENTRE
    }

    protected P content;

    private boolean isVisible;

    /**
     * Checks if the view will need an update when a graph changes based on if the view is visible currently.
     *
     * @return true if the view is visible and needs updating
     */
    protected boolean needsUpdate() {
        return isVisible;
    }

    protected void setComponentVisible(final boolean visibility) {
        isVisible = visibility;
    }

    /**
     * Builds and initialises the content for this top component. You should call this method in the constructor of your
     * TopComponent implementation after calling the initComponents() method.
     */
    protected abstract void initContent();

    /**
     * This is where you pass in content which will be rendered within the AbstractTopComponent.
     *
     * @return
     */
    protected abstract P createContent();

    /**
     * Returns the content given to this AbstractTopComponent.
     *
     * @return
     */
    public P getContent() {
        return content;
    }

    /**
     * Returns the visibility status of the concrete Top Component
     *
     * @return Boolean value true if the component is visible, false otherwise
     */
    public boolean getVisibility() {
        return isVisible;
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();

        isVisible = true;
        ConstellationLogger.getDefault().viewStarted(this);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();

        isVisible = false;
        ConstellationLogger.getDefault().viewStopped(this);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();

        isVisible = true;
        if (WindowManager.getDefault().isTopComponentFloating(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Showing / Floating");
        } else if (WindowManager.getDefault().isTopComponentMinimized(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Showing / Minimised");
        } else {
            ConstellationLogger.getDefault().viewInfo(this, "Showing / Docked");
        }
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();

        isVisible = true;
        if (WindowManager.getDefault().isTopComponentFloating(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Hidden / Floating");
        } else if (WindowManager.getDefault().isTopComponentMinimized(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Hidden / Minimised");
            isVisible = false;
        } else {
            ConstellationLogger.getDefault().viewInfo(this, "Hidden / Docked");

        }
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();

        isVisible = true;
        if (WindowManager.getDefault().isTopComponentFloating(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Activated / Floating");
        } else if (WindowManager.getDefault().isTopComponentMinimized(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Activated / Minimised");
            isVisible = false;
        } else {
            ConstellationLogger.getDefault().viewInfo(this, "Activated / Docked");
        }
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();

        isVisible = true;
        if (WindowManager.getDefault().isTopComponentFloating(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Deactivated / Floating");
        } else if (WindowManager.getDefault().isTopComponentMinimized(this)) {
            ConstellationLogger.getDefault().viewInfo(this, "Deactivated / Minimised");
            isVisible = false;
        } else {
            ConstellationLogger.getDefault().viewInfo(this, "Deactivated / Docked");
        }
    }

    @Override
    public final HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    /**
     * Sets whether the view is floating based on the preference selection.
     *
     * @param name
     * @param width
     * @param height
     * @param spawn
     */
    protected final void setFloating(final String name, final int width, final int height, final Spawn spawn) {
        final Preferences prefs = NbPreferences.forModule(ViewOptionsPanelController.class);
        final Boolean isFloating = prefs.getBoolean(name, ViewOptionsPanelController.getDefaultFloatingPreferences().getOrDefault(name, false));
        WindowManager.getDefault().setTopComponentFloating(this, isFloating);

        if (isFloating) {
            // This loops through all the current windows and compares this top component's top level ancestor
            // with the window's parent. Sets the size and location for the floating component if a match is found.
            for (final Window window : Window.getWindows()) {
                if (this.getTopLevelAncestor() != null && this.getTopLevelAncestor().getName().equals(window.getName())) {
                    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    final int mainWidth = mainWindow.getWidth();
                    final int mainHeight = mainWindow.getHeight();
                    final int mainX = mainWindow.getX();
                    final int mainY = mainWindow.getY();
                    final int offsetY = 117; // Offsets floating component so it doesn't overlap with top toolbar icons.

                    final Dimension sidesSize = new Dimension(
                            width == 0 ? Math.round(mainWidth * 0.3F) : width,
                            height == 0 ? mainHeight - offsetY : height);

                    final Dimension bottomSize = new Dimension(
                            width == 0 ? mainWidth : width,
                            height == 0 ? Math.round(mainHeight * 0.3F) : height);

                    final Dimension centreSize = new Dimension(
                            width == 0 ? mainWidth : width,
                            height == 0 ? mainHeight - offsetY : height);

                    final Dimension size;

                    switch (spawn) {
                        case LEFT -> {
                            size = sidesSize;
                            window.setLocation(mainX, mainY + offsetY);
                        }
                        case RIGHT -> {
                            size = sidesSize;
                            window.setLocation(mainX + mainWidth - sidesSize.width, mainY + offsetY);
                        }
                        case BOTTOM -> {
                            size = bottomSize;
                            window.setLocation(mainX, mainY + mainHeight - bottomSize.height);
                        }
                        default -> {
                            size = centreSize;
                            window.setLocation(mainX, mainY + offsetY);
                        }
                    }

                    window.setMinimumSize(size);
                    window.setSize(size);
                }
            }

            this.setRequestFocusEnabled(true);
        }
    }

    public abstract Map<String, Boolean> getFloatingPreference();
}
