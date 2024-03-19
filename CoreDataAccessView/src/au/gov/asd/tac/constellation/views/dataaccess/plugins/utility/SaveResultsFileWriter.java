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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * A helper to write result data to a file when the "Save Results" option is checked
 *
 * @author arcturus
 */
public class SaveResultsFileWriter {

    /**
     * Save a RecordStore to a CSV file
     *
     * @param plugin the plugin that generated the {@link RecordStore}.
     * @param recordstore the {@link RecordStore} to save.
     *
     * @throws PluginException if an anticipated exception occurs during plugin execution.
     */
    public static void writeRecordStore(final Plugin plugin, final RecordStore recordstore) throws PluginException {
        final File outputDir = DataAccessPreferenceUtilities.getDataAccessResultsDir();
        if (outputDir != null) {
            final String fnam = generateFilename(plugin, "csv");
            final File file = new File(outputDir, fnam);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                RecordStoreUtilities.toCsv(recordstore, outputStream);
            } catch (Exception ex) {
                ConstellationLoggerHelper.exportPropertyBuilder(plugin, Collections.emptyList(), file, ConstellationLoggerHelper.FAILURE);
                throw new PluginException(PluginNotificationLevel.ERROR, ex);
            }

            ConstellationLoggerHelper.exportPropertyBuilder(plugin, Collections.emptyList(), file, ConstellationLoggerHelper.SUCCESS);
        }
    }

    /**
     * Generate a unique filename incorporating the date time and plugin name
     *
     * @param plugin the plugin being run
     * @param filenameSuffix file suffix
     * @return A unique filename for the plugin run
     */
    protected static String generateFilename(final Plugin plugin, final String filenameSuffix) {
        return String.format("%s-%s.%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS")), plugin.getClass().getSimpleName(), filenameSuffix);
    }

    private SaveResultsFileWriter() {
    }

}
