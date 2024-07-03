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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An IconData implementation allowing an icon to be built using a {@link File}.
 *
 * This class has been adapted to support dual sourced IconData, loading raster and vector images. 
 * Ideally this class would be rewritten to accept a file location and a file name and this class 
 * will dynamically create references to raster and vector files base on known extensions.
 * However due to the existence of other ConstellationIconProviders the below implementation was developed 
 * to avoid having to refactor current usage of the FileIconData class. 
 * 
 * @author cygnus_x-1
 * @author capricornunicorn123
 */
public class FileIconData extends IconData {

    private final File rasterFile;
    private final File vectorFile;

    public FileIconData(final String relativePath, final String codeNameBase) {
        final File locatedRasterFile = ConstellationInstalledFileLocator.locate(relativePath, codeNameBase, FileIconData.class.getProtectionDomain());
        this.rasterFile = locatedRasterFile;
        
        final File locatedVectorFile = ConstellationInstalledFileLocator.locate(relativePath.replaceAll(FileExtensionConstants.PNG, FileExtensionConstants.SVG), codeNameBase, FileIconData.class.getProtectionDomain());
        this.vectorFile = locatedVectorFile;
    }

    public FileIconData(final File file) {
        this.rasterFile = file;
        this.vectorFile = null;
    }

    @Override
    protected InputStream createRasterInputStream() throws IOException {
        return new FileInputStream(rasterFile);
    }
    
    @Override
    protected InputStream createVectorInputStream() throws IOException {
        //Not all files have SVG alternatives to only return a stream if the alternative was found.
        return vectorFile != null ? new FileInputStream(vectorFile) : null;
    }

    public String getFilePath() {
        return rasterFile.getAbsolutePath();
    }
}
