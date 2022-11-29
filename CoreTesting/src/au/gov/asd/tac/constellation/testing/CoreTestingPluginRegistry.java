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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.DnaGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.ImageGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.PreferentialAttachmentGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.SleepEditPlugin;
import au.gov.asd.tac.constellation.testing.construction.SleepReadPlugin;
import au.gov.asd.tac.constellation.testing.construction.SmallWorldGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.SphereGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.StructuredGraphBuilderPlugin;
import au.gov.asd.tac.constellation.testing.construction.SudokuGraphBuilderPlugin;

/**
 * Core Testing Plugin Registry
 *
 * @author arcturus
 * @author canis_majoris
 */
public class CoreTestingPluginRegistry {

    public static final String FIVE_SECOND_READ_LOCK = FiveSecondReadLockPlugin.class.getName();
    public static final String FIVE_SECOND_WRITE_LOCK = FiveSecondWriteLockPlugin.class.getName();
    public static final String PLUGIN_EXCEPTION = PluginExceptionPlugin.class.getName();
    public static final String PLUGIN_RUNTIME_EXCEPTION = RuntimeExceptionPlugin.class.getName();

    public static final String COMPLETE_GRAPH_BUILDER = CompleteGraphBuilderPlugin.class.getName();
    public static final String DNA_GRAPH_BUILDER = DnaGraphBuilderPlugin.class.getName();
    public static final String IMAGE_GRAPH_BUILDER = ImageGraphBuilderPlugin.class.getName();
    public static final String PREFERENTIAL_ATTACHMENT_GRAPH_BUILDER = PreferentialAttachmentGraphBuilderPlugin.class.getName();
    public static final String SLEEP_EDIT_PLUGIN = SleepEditPlugin.class.getName();
    public static final String SLEEP_READ_PLUGIN = SleepReadPlugin.class.getName();
    public static final String SMALL_WORLD_GRAPH_BUILDER = SmallWorldGraphBuilderPlugin.class.getName();
    public static final String SPHERE_GRAPH_BUILDER = SphereGraphBuilderPlugin.class.getName();
    public static final String STRUCTURED_GRAPH_BUILDER = StructuredGraphBuilderPlugin.class.getName();
    public static final String SUDOKU_GRAPH_BUILDER = SudokuGraphBuilderPlugin.class.getName();
    public static final String NULL_PLUGIN = NullPlugin.class.getName();
}
