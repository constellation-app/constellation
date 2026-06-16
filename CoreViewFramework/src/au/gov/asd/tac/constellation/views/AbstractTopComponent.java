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
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.preferences.ViewOptionsPanelController;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    protected P content;
    private boolean isVisible;
    private PropertyChangeListener pcl;
    private final Preferences prefs = NbPreferences.forModule(ViewOptionsPanelController.class);

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
        final WindowManager windowManager = WindowManager.getDefault();
        final ViewOptionsPanelController controller = new ViewOptionsPanelController();

        pcl = (final PropertyChangeEvent evt) -> { // Fires when a view is floated or docked manually via the context menu.
            prefs.putBoolean(this.getName(), windowManager.isTopComponentFloating(this));
            controller.update();
        };

        this.addPropertyChangeListener(pcl);
        super.componentOpened();

        final Map<String, Boolean> defaultPrefs = controller.getPanel().getDefaultPrefs();

        if (defaultPrefs.containsKey(this.getName())) {
            final boolean isFloating = prefs.getBoolean(this.getName(), defaultPrefs.get(this.getName()));
            windowManager.setTopComponentFloating(this, isFloating);

            if (isFloating) {
                // This loops through all the current windows and compares this top component's top level ancestor
                // with the window's parent. Sets the size and location for the floating component if a match is found.
                for (final Window window : Window.getWindows()) {
                    if (this.getTopLevelAncestor() != null && this.getTopLevelAncestor().getName().equals(window.getName())) {
                        final Frame mainWindow = windowManager.getMainWindow();
                        final String mode = getModeName();
                        final Dimension size = createFloatingSize(mainWindow, mode);
                        final Point location = createFloatingLocation(mainWindow, mode, size);
                        window.setSize(size);
                        window.setLocation(location);
                    }
                }

                this.setRequestFocusEnabled(true);
            }
        }

        isVisible = true;
        ConstellationLogger.getDefault().viewStarted(this);
    }

    @Override
    protected void componentClosed() {
        this.removePropertyChangeListener(pcl);
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
     * Creates a dimension for the floating top component based on the size of Constellation's window and the mode of
     * the top component.
     *
     * @param window
     * @param mode
     * @return a dimension for the floating top component.
     */
    protected Dimension createFloatingSize(final Frame window, final String mode) {
        final int windowWidth = window.getWidth();
        final int windowHeight = window.getHeight();

        final Dimension size;

        switch (mode) {
            case "output", "bottomSlidingSide", "isSliding" -> { // Bottom opening top components.
                size = new Dimension(
                        windowWidth,
                        Math.round(windowHeight * 0.3F)
                );
            }
            default -> { // Side opening top components; "leftSlidingSide", "explorer", "navigator", "commonpalette", "properties", "rightSlidingSide", and any other modes by default.
                size = new Dimension(
                        Math.round(windowWidth * 0.3F),
                        Math.round(windowHeight * (windowWidth > windowHeight ? 0.892F : 0.94F))
                );
            }
        }

        return size;
    }

    /**
     * Creates a point for the floating top component to open from based on the size of Constellation's window, and the
     * mode and size of the top component.
     *
     * @param window
     * @param mode
     * @param size
     * @return a point for the floating top component to open from.
     */
    protected Point createFloatingLocation(final Frame window, final String mode, final Dimension size) {
        final int locationX;

        switch (mode) {
            case "commonpalette", "properties", "rightSlidingSide" -> { // Right side opening top components.
                locationX = window.getX() + window.getWidth() - size.width;
            }
            default -> { // Left side opening top components; "leftSlidingSide", "explorer", "navigator", "output", "bottomSlidingSide", "isSliding", and any other modes by default.
                locationX = window.getX();
            }
        }

        return new Point(locationX, window.getY() + window.getHeight() - size.height);
    }

    public abstract Tuple<String, Boolean> getDefaultFloatingInfo();

    protected abstract String getModeName();
}
