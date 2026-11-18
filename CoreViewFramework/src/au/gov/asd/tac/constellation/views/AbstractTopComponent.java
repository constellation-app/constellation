/*
 * Copyright 2010-2026 Australian Signals Directorate
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
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
    private ComponentListener cl;
    private final Preferences prefs = NbPreferences.forModule(ViewOptionsPanelController.class);

    private Dimension lastSize = null;
    private Point lastLocation = null;
    private Window thisWindow = null;

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
     * Builds and initializes the content for this top component. You should call this method in the constructor of your
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
        final ViewOptionsPanelController controller = new ViewOptionsPanelController();
        final Map<String, Boolean> defaultPrefs = controller.getPanel().getDefaultPrefs();

        if (defaultPrefs.containsKey(getName())) {
            final boolean isFloating = prefs.getBoolean(getName(), defaultPrefs.get(getName()));
            final WindowManager windowManager = WindowManager.getDefault();
            windowManager.setTopComponentFloating(this, isFloating);

            pcl = (final PropertyChangeEvent e) -> {
                if (!e.getPropertyName().equals("ancestor")) {
                    prefs.putBoolean(getName(), windowManager.isTopComponentFloating(this));
                    controller.update();
                }
            };

            this.addPropertyChangeListener(pcl);

            if (isFloating) {
                // This loops through all the current windows and compares this top component's top level ancestor
                // with the window's parent. Sets the size and location for the floating component if a match is found.
                for (final Window window : Window.getWindows()) {
                    if (getTopLevelAncestor() != null && getTopLevelAncestor().getName().equals(window.getName())) {
                        final Frame mainWindow = windowManager.getMainWindow();

                        thisWindow = window;

                        cl = new ComponentAdapter() {
                            @Override
                            public void componentResized(final ComponentEvent e) {
                                lastSize = new Dimension(e.getComponent().getWidth(), e.getComponent().getHeight());
                            }

                            @Override
                            public void componentMoved(final ComponentEvent e) {
                                lastLocation = new Point(e.getComponent().getX(), e.getComponent().getY());
                            }
                        };

                        thisWindow.addComponentListener(cl);

                        thisWindow.setSize(lastSize != null ? lastSize : createFloatingSize(mainWindow, getModeName()));
                        thisWindow.setLocation(lastLocation != null ? lastLocation : createFloatingLocation(mainWindow, thisWindow));
                    }
                }

                this.setRequestFocusEnabled(true);
            } else {
                windowManager.setTopComponentMinimized(this, List.of("leftSlidingSide", "bottomSlidingSide", "rightSlidingSide", "isSliding").contains(getModeName()));
            }
        }

        super.componentOpened();

        isVisible = true;
        ConstellationLogger.getDefault().viewStarted(this);
    }

    @Override
    protected void componentClosed() {
        this.removePropertyChangeListener(pcl);

        if (thisWindow != null) {
            thisWindow.removeComponentListener(cl);
        }

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
     * Creates a size dimension for the floating top component based on the size of Constellation's window and the mode
     * of the top component.
     *
     * @param mainWindow
     * @param mode
     * @return a size dimension for the floating top component.
     */
    protected Dimension createFloatingSize(final Frame mainWindow, final String mode) {
        final int windowWidth = mainWindow.getWidth();
        final int windowHeight = mainWindow.getHeight();

        final Dimension size;

        switch (mode) {
            case "output", "bottomSlidingSide", "isSliding" -> // Bottom opening top components.
                size = new Dimension(
                        Math.round(windowWidth * 0.8F),
                        Math.round(windowHeight * 0.3F)
                );
            default -> // Side opening top components; "leftSlidingSide", "explorer", "navigator", "commonpalette", "properties", "rightSlidingSide", and any other modes by default.
                size = new Dimension(
                        Math.round(windowWidth * 0.3F),
                        Math.round(windowHeight * 0.8F)
                );
        }

        return size;
    }

    /**
     * Creates a location point for the floating top component based on the size of Constellation's window and already
     * opened top components.
     *
     * @param mainWindow
     * @param thisWindow
     * @return a location point for the floating top component.
     */
    protected Point createFloatingLocation(final Frame mainWindow, final Window thisWindow) {
        thisWindow.setLocationRelativeTo(mainWindow);

        int floatingCount = 1;

        int locationX = thisWindow.getX();
        int locationY = thisWindow.getY();

        // For every floating top component that is currently opened,
        // apply an offset to the opening location of this top component.
        for (final TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (WindowManager.getDefault().isTopComponentFloating(tc)) {
                final int offsetMax = 5; // Maximum 5 offsets before returning to screen centre to avoid opening offscreen.
                final int offsetMultiplier = floatingCount % offsetMax >= 0 ? floatingCount % offsetMax : floatingCount;

                final int offsetX = mainWindow.getWidth() / 50;
                final int offsetY = mainWindow.getHeight() / 50;

                locationX = thisWindow.getX() + (offsetMultiplier * offsetX);
                locationY = thisWindow.getY() + (offsetMultiplier * offsetY);

                floatingCount++;
            }
        }

        return new Point(locationX, locationY);
    }

    public abstract Tuple<String, Boolean> getDefaultFloatingInfo();

    protected abstract String getModeName();
}
