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

import au.gov.asd.tac.constellation.graph.file.open.OpenFileDialogFilter;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide an OpenFileDialogFilter to NetBeans.
 * <p>
 * The default NetBeans open file dialog filters by default on "All Files", with
 * ".java" and ".txt" filters. This is silly.
 *
 * @author algol
 */
@ServiceProvider(service = OpenFileDialogFilter.class,
        supersedes = {"au.gov.asd.tac.constellation.graph.file.open.FileChooser$JavaFilesFilter", "au.gov.asd.tac.constellation.graph.file.open.FileChooser$TxtFileFilter"})
public class GraphOpenFileDialogFilter extends OpenFileDialogFilter {

    @Override
    public String getDescriptionString() {
        return String.format("%s Files", BrandingUtilities.APPLICATION_NAME);
    }

    @Override
    public String[] getSuffixes() {
        return new String[]{FileExtensionConstants.STAR_EXTENSION, FileExtensionConstants.NEBULA_EXTENSION};
    }
}
