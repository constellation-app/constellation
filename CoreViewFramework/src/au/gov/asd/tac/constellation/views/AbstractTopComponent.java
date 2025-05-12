/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.preferences.ViewPreferenceKeys;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
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

    /**
     * Checks if the view will need an update when a graph changes based on if
     * the view is visible currently.
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
     * Builds and initialises the content for this top component. You should
     * call this method in the constructor of your TopComponent implementation
     * after calling the initComponents() method.
     */
    protected abstract void initContent();

    /**
     * This is where you pass in content which will be rendered within the
     * AbstractTopComponent.
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
     * Sets the view as floating based on the preference selection.
     *
     * @param floatingWidth
     * @param floatingHeight
     */
    protected final void setFloating(final int floatingWidth, final int floatingHeight) {
        final Preferences prefs = NbPreferences.forModule(ViewPreferenceKeys.class);
        final Boolean isFloating = prefs.getBoolean(this.getName(), ViewPreferenceKeys.DEFAULT_VIEW_OPTIONS.get(this.getName()));
        WindowManager.getDefault().setTopComponentFloating(this, isFloating);

        if (isFloating) {
            // This loops through all the current windows and compares this top component's top level ancestor
            // with the window's parent. Sets the size and location for the floating component if a match is found.
            for (final Window window : Window.getWindows()) {
                if (this.getTopLevelAncestor() != null && this.getTopLevelAncestor().getName().equals(window.getName())) {
                    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    final int orientationBasedWidth = mainWindow.getWidth() > mainWindow.getHeight()
                            ? Math.round(mainWindow.getWidth() * 0.3f)
                            : Math.round(mainWindow.getWidth() * 0.4f);
                    final Dimension size = new Dimension(
                            floatingWidth == 0 ? orientationBasedWidth : floatingWidth,
                            floatingHeight == 0 ? mainWindow.getHeight() - 110 : floatingHeight);
                    window.setMinimumSize(size);
                    window.setSize(size);
                    window.setLocation(new Point(mainWindow.getX(), mainWindow.getY() + 110));
                }
            }

            this.setRequestFocusEnabled(true);
        }
    }
}
