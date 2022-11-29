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
package au.gov.asd.tac.constellation.plugins.logging;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * A helper class to set properties that can be used to enrich logs and
 * retrieved using an implementor of {@link ConstellationLogger}
 *
 * @author arcturus
 */
public class ConstellationLoggerHelper {

    // log status
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";

    // attributes
    public static final String PLUGIN_TYPE = "PLUGIN TYPE";
    public static final String SOURCE = "SOURCE";
    public static final String DESTINATION = "DESTINATION";
    public static final String FILE_NAME = "FILE NAME";
    public static final String FILE_SIZE = "FILE SIZE";
    public static final String FILE_HASH = "FILE HASH";
    public static final String FILES_COUNT = "FILES COUNT";
    public static final String DATASOURCE = "DATASOURCE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String CONTENT = "CONTENT";
    public static final String TERMS = "TERMS";
    public static final String VALUE_BEFORE = "VALUE_BEFORE";
    public static final String VALUE_AFTER = "VALUE_AFTER";
    public static final String COUNT = "COUNT";
    public static final String STATUS = "STATUS";

    private static final String STRING_NUMBER_FORMAT = "%s %d";

    /**
     * Enrich the view log with property values when opening a file
     *
     * @param plugin The plugin run
     * @param file The file being opened
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     * @throws PluginException if an anticipated exception occurs.
     */
    public static Properties viewPropertyBuilder(final Plugin plugin, final File file, final String status) throws PluginException {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.VIEW.toString());
        properties.setProperty(STATUS, status);

        addFileStatistics(properties, Arrays.asList(file));

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the search log with property values when searching a repository
     * for a collection of terms
     *
     * @param plugin The plugin run
     * @param terms The collection of terms being searched
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @param resultCount The count of results returned
     * @param datasource The name of the data source
     * @param description A description field which for example could be used to
     * capture the error message during a failure
     * @return Enriched properties
     */
    public static Properties searchPropertyBuilder(final Plugin plugin, final Collection<String> terms, final String status, final long resultCount, final String datasource, final String description) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.SEARCH.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(TERMS, terms.toString());
        properties.setProperty(COUNT, Long.toString(resultCount));
        properties.setProperty(DATASOURCE, datasource);
        properties.setProperty(DESCRIPTION, StringUtils.defaultString(description));

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * ### THIS IS TEMPORARY ###
     *
     * @param plugin The plugin run
     * @param terms The collection of terms being searched
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @param resultCount The count of results returned
     * @param datasource The name of the data source
     * @param description A description field which for example could be used to
     * capture the error message during a failure
     * @param customProperties any extra properties that should be added.
     * @return Enriched properties
     */
    public static Properties searchPropertyBuilder(final Plugin plugin, final Collection<String> terms, final String status, final long resultCount, final String datasource, final String description, final Properties customProperties) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.SEARCH.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(TERMS, terms.toString());
        properties.setProperty(COUNT, Long.toString(resultCount));
        properties.setProperty(DATASOURCE, datasource);
        properties.setProperty(DESCRIPTION, StringUtils.defaultString(description));
        if (customProperties != null) {
            for (Entry<Object, Object> entry : customProperties.entrySet()) {
                properties.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the import log with property values when importing a collection of
     * files
     *
     * @param plugin The plugin run
     * @param terms The collection of terms being searched
     * @param files The import files
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     * @throws PluginException if an anticipated exception occurs.
     */
    public static Properties importPropertyBuilder(final Plugin plugin, final Collection<String> terms, final Collection<File> files, final String status) throws PluginException {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.IMPORT.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(TERMS, terms.toString());

        if (files != null) {
            addFileStatistics(properties, files);
        }

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the export log with property values when exporting a file
     *
     * @param plugin The plugin run
     * @param terms The collection of terms being searched
     * @param file The export file
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     * @throws PluginException if an anticipated exception occurs.
     */
    public static Properties exportPropertyBuilder(final Plugin plugin, Collection<String> terms, final File file, final String status) throws PluginException {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.EXPORT.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(TERMS, terms.toString());

        if (file != null) {
            addFileStatistics(properties, Arrays.asList(file));
        }

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the create log with property values when inserting text from
     * direct input
     *
     * @param plugin The plugin run
     * @param terms A collection of terms created
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     */
    public static Properties createPropertyBuilder(final Plugin plugin, final Collection<String> terms, final String status) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.CREATE.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(TERMS, terms.toString());

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the copy log with property values when inserting text from direct
     * input
     *
     * @param plugin The plugin run
     * @param count A count to quantify how many objects are copied
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     */
    public static Properties copyPropertyBuilder(final Plugin plugin, final long count, final String status) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.COPY.toString());
        properties.setProperty(STATUS, status);
        properties.setProperty(COUNT, Long.toString(count));

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the delete log with property values when deleting elements
     *
     * @param plugin The plugin run
     * @param terms A collection of terms created
     * @param status The status of the operation which could be a SUCCESS or
     * FAILURE
     * @return Enriched properties
     */
    public static Properties deletePropertyBuilder(final Plugin plugin, final Collection<String> terms, final String status) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.DELETE.toString());
        properties.setProperty(TERMS, terms.toString());
        properties.setProperty(STATUS, status);

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich the update log with property values when updating text
     *
     * @param plugin The plugin run
     * @param beforeValue the before value.
     * @param afterValue the after value.
     * @return Enriched properties.
     */
    public static Properties updatePropertyBuilder(final Plugin plugin, final String beforeValue, final String afterValue) {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_TYPE, PluginType.UPDATE.toString());
        properties.setProperty(VALUE_BEFORE, beforeValue);
        properties.setProperty(VALUE_AFTER, afterValue);

        ConstellationLogger.getDefault().pluginProperties(plugin, properties);
        return properties;
    }

    /**
     * Enrich with file information
     *
     * @param properties the properties to enrich.
     * @param files a collection of files to include in the properties.
     * @throws PluginException if an anticipated exception occurs.
     */
    private static void addFileStatistics(final Properties properties, final Collection<File> files) throws PluginException {
        if (files != null) {
            Integer counter = files.size();
            for (File file : files) {
                properties.setProperty(String.format(STRING_NUMBER_FORMAT, FILE_NAME, counter), file.getAbsolutePath());
                properties.setProperty(String.format(STRING_NUMBER_FORMAT, FILE_SIZE, counter), Long.toString(file.length()));
                try {
                    final String hash = createHash(file);
                    if (hash != null) {
                        properties.setProperty(String.format(STRING_NUMBER_FORMAT, FILE_HASH, counter), hash);
                    }
                } catch (final IOException | NoSuchAlgorithmException ex) {
                    throw new PluginException(PluginNotificationLevel.FATAL, "Error creating hash: " + ex.getMessage());
                }
                counter++;
            }
            properties.setProperty(FILES_COUNT, Integer.toString(files.size()));
        }
    }

    /**
     * Create an SHA-256 hash for a file
     *
     * @param file The file being opened
     * @return The SHA-256 hash or null if the file does not exist
     * @throws Exception if an error occurs creating the hash.
     */
    private static String createHash(final File file) throws IOException, NoSuchAlgorithmException {
        if (!file.exists()) {
            return null;
        }

        final MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
        sha256Digest.reset();

        final byte[] buffer = new byte[1024];
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            int bytesRead = in.read(buffer);
            while (bytesRead >= 0) {
                sha256Digest.update(buffer, 0, bytesRead);
                bytesRead = in.read(buffer);
            }
        }

        final StringBuilder result = new StringBuilder();
        final byte[] hash = sha256Digest.digest();
        for (byte b : hash) {
            int i = b;
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                result.append('0');
            }
            result.append(Integer.toHexString(i));
        }
        return result.toString();
    }
}
