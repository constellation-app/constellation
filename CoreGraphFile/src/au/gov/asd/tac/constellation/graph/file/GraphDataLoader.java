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
package au.gov.asd.tac.constellation.graph.file;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;

/**
 *
 * @author cygnus_x-1
 */
public class GraphDataLoader extends UniFileLoader {

    private static final String DATA_OBJECT_CLASS = "au.gov.asd.tac.constellation.graph.file.GraphDataObject";
    private static final String MIME_TYPE = "application/x-graph";
    private static final String ACTIONS_CONTEXT = "Loaders/" + MIME_TYPE + "/Actions";

    public GraphDataLoader() {
        super(DATA_OBJECT_CLASS);
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }

    @Override
    protected String defaultDisplayName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected MultiDataObject createMultiObject(final FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new GraphDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return ACTIONS_CONTEXT;
    }
}
