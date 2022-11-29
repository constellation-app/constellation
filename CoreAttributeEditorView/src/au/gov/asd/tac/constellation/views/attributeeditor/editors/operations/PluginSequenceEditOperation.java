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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;

/**
 *
 * @author twilight_sparkle
 */
public abstract class PluginSequenceEditOperation implements EditOperation {

    private Plugin preEdit = null;
    private Plugin postEdit = null;

    public void setPreEdit(final Plugin preEdit) {
        this.preEdit = preEdit;
    }

    public void setPostEdit(final Plugin postEdit) {
        this.postEdit = postEdit;
    }

    @Override
    public void performEdit(final Object value) {
        PluginExecutor executor = preEdit == null ? PluginExecutor.startWith(mainEdit(value)) : PluginExecutor.startWith(preEdit).followedBy(mainEdit(value));
        if (postEdit != null) {
            executor = executor.followedBy(postEdit);
        }
        executor.executeWriteLater(GraphManager.getDefault().getActiveGraph());
    }

    public abstract Plugin mainEdit(final Object value);

}
