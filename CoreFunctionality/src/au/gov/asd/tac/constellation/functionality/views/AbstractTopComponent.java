/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.views;

import au.gov.asd.tac.constellation.pluginframework.logging.ConstellationLogger;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

/**
 * A generic top component.
 *
 * @param <P> The class used by this {@link TopComponent} to display content.
 *
 * @author cygnus_x-1
 */
public abstract class AbstractTopComponent<P> extends TopComponent {

    protected P content;

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

    @Override
    protected void componentOpened() {
        super.componentOpened();
        ConstellationLogger.getDefault().viewOpened(this);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        ConstellationLogger.getDefault().viewClosed(this);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        ConstellationLogger.getDefault().viewShowing(this);
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        ConstellationLogger.getDefault().viewHidden(this);
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        ConstellationLogger.getDefault().viewActivated(this);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        ConstellationLogger.getDefault().viewDeactivated(this);
    }

    @Override
    public final HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }
}