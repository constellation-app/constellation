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
package au.gov.asd.tac.constellation.utilities.file;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;

/**
 * The ConstellationInstalledFileLocator is a wrapper for
 * {@link InstalledFileLocator} because
 * {@link org.​openide.​modules.InstalledFileLocator} only works inside a
 * NetBeans environment and so running unit tests causes
 * {@link org.​openide.​modules.InstalledFileLocator} to fail.
 *
 * @author arcturus
 */
public class ConstellationInstalledFileLocator {
    
    private static final Logger LOGGER = Logger.getLogger(ConstellationInstalledFileLocator.class.getName());

    /**
     * A wrapper for {@code InstalledFileLocator.locate()}
     *
     * @param relativePath path from install root, e.g.
     * {@code docs/OpenAPIs.zip} or {@code modules/ext/somelib.jar} (always
     * using {@code / } as a separator, regardless of platform)
     * @param codeNameBase name of the supplying module, e.g.
     * {@code org.netbeans.modules.foo}; may be {@code null} if unknown
     * @param protectedDomain the {@code ProtectionDomain} of the class used to
     * find the location of the JAR.
     * @return the requested {@code File}, if it can be found, else {@code null}
     */
    public static File locate(final String relativePath, final String codeNameBase, final ProtectionDomain protectedDomain) {
        //LOGGER.log(Level.WARNING, "TRYING {0} {1}", new Object[]{relativePath, codeNameBase});
        File locatedFile = InstalledFileLocator.getDefault().locate(relativePath, codeNameBase, false);
        if (locatedFile == null) {
            LOGGER.log(Level.WARNING, "THIS ONE FAILS {0} {1}", new Object[]{relativePath, codeNameBase});
            
            //The below code causes the aplication to hang due to fialing quietly when a file is not at the expected location.
            //Have deactivated the code when looking for .svg file as svg equivlents to raster images is not ensured.
            if (!relativePath.contains(".svg")) {
                // InstalledFileLocator only works in th NetBeans environment.
                // If we're not there (because we're running unit tests in development, for example), we won't find anything.
                // Instead, we'll hack our way to what we want. If there's a nicer way, tell me.
                //
                // Which JAR are we in?
                final URL url = protectedDomain.getCodeSource().getLocation();

                try {
                    locatedFile = Paths.get(url.toURI()).toFile();
                } catch (final URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }

                // Go up two levels to the build\cluster directory so the relative path is correct.
                locatedFile = locatedFile.getParentFile().getParentFile();
                locatedFile = new File(locatedFile, relativePath);

                if (!locatedFile.exists()) {
                    throw new RuntimeException(String.format("Couldn't find file %s at %s in module %s", relativePath, locatedFile, codeNameBase));
                }
            }
        }

        return locatedFile;
    }
}
