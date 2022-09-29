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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An IconData implementation allowing an icon to be built using a {@link File}.
 *
 * @author cygnus_x-1
 */
public class FileIconData extends IconData {

    private final File file;

    public FileIconData(final String relativePath, final String codeNameBase) {
        final File locatedFile = ConstellationInstalledFileLocator.locate(relativePath, codeNameBase, FileIconData.class.getProtectionDomain());
        this.file = locatedFile;
    }

    public FileIconData(final File file) {
        this.file = file;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }
}
