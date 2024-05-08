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
package au.gov.asd.tac.constellation.functionality.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 *
 * @author algol
 */
public class MostRecentModules {

    private static final Logger LOGGER = Logger.getLogger(MostRecentModules.class.getName());

    // Cache the most recent version for efficiency.
    // It won't change while the application is running.
    private static String mostRecentVersion = null;
    private static boolean found = false;

    /**
     * The relevant section of the version number of the most recent module.
     * <p>
     * The build process modifies the version number of modules so that the
     * first two parts are maintained and the next two parts are the yyyymmdd
     * date and hhmmss time of the build as integers.
     * <p>
     * The format of the returned string is "yyyymmdd.hhmmss".
     * <p>
     * If the build process hasn't updated the version number (when running from
     * the IDE, for example), null will be returned.
     *
     * @return The relevant section of the version number of the most recent
     * module.
     */
    public static synchronized String getMostRecentVersion() {
        if (!found && !isRunningUnitTest()) {
            final List<ModuleInfo> modules = getModules();
            final String[] versionParts = modules.get(0).getSpecificationVersion().toString().split("\\.");
            if (versionParts.length == 4) {
                final String yyyymmdd = versionParts[2];
                final StringBuilder hhmmss = new StringBuilder(String.valueOf(versionParts[3]));
                while (hhmmss.length() < 6) {
                    hhmmss.insert(0, "0");
                }

                mostRecentVersion = String.format("%s.%s", yyyymmdd, hhmmss.toString());

                // Just in case...
                if (mostRecentVersion.length() > 15) {
                    mostRecentVersion = mostRecentVersion.substring(0, 15);
                }
            }

            found = true;
        }

        return mostRecentVersion;
    }

    /**
     * True if we're running in the development environment, false otherwise.
     * <p>
     * This is for things that a developer might want to see, but users
     * shouldn't (extra GUI elements, for instance) because code is still being
     * developed. As a reminder to the developer, a message is printed to
     * stderr.
     *
     * @return True if we're running in the development environment, false
     * otherwise.
     */
    public static boolean isUnderDevelopment() {
        final String v = getMostRecentVersion();
        final boolean isdev = v == null;
        if (isdev) {
            LOGGER.info("*** isUnderDevelopment ***");
        }

        return isdev;
    }

    /**
     * Return a list of modules ordered by descending version number.
     * <p>
     * The build process modifies the version number of modules so that the
     * first two parts are maintained and the next two parts are the yyyymmdd
     * date and hhmmss time of the build as integers.
     * <p>
     * The module list is ordered by just the build part of the version if the
     * SpecificationVersion has four elements.
     *
     * @return A list of modules ordered by most recent version.
     */
    public static List<ModuleInfo> getModules() {
        final Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        final List<ModuleInfo> moduleList = new ArrayList<>();
        for (final ModuleInfo mi : modules) {
            if (!mi.getCodeNameBase().startsWith("org.netbeans")
                    && !mi.getCodeNameBase().startsWith("org.apache")
                    && !mi.getCodeNameBase().startsWith("org.openide")
                    && !mi.getCodeNameBase().startsWith("org.jdesktop")
                    && !mi.getCodeNameBase().startsWith("net.java.html")) {
                moduleList.add(mi);
            }
        }

        moduleList.sort((final ModuleInfo mi1, final ModuleInfo mi2) -> {
            final SpecificationVersion specver1 = mi1.getSpecificationVersion();
            final SpecificationVersion specver2 = mi2.getSpecificationVersion();

            final String[] v1 = specver1.toString().split("\\.");
            final String[] v2 = specver2.toString().split("\\.");
            if (v1.length == 4 && v2.length == 4) {
                int comp = Integer.compare(Integer.parseInt(v2[2]), Integer.parseInt(v1[2]));
                if (comp == 0) {
                    comp = Integer.compare(Integer.parseInt(v2[3]), Integer.parseInt(v1[3]));
                }

                return comp;
            }

            return specver2.compareTo(specver1);
        });

        return moduleList;
    }

    /**
     * If getMostRecentVersion() is executed inside a unit test then a NetBeans
     * module dependency window is shown causing strange issues with unit tests
     * failing to run or being hung. This is not a great check but avoids a lot
     * of problems.
     *
     * @return True if running in a unit test, False otherwise.
     */
    private static boolean isRunningUnitTest() {
        for (final StackTraceElement stackTrace : Thread.currentThread().getStackTrace()) {
            if (stackTrace.getClassName().startsWith("org.testng.") || stackTrace.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
