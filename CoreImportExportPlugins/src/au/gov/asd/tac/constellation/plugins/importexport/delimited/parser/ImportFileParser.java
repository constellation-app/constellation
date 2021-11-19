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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.importexport.RefreshRequest;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * An ImportFileParser is responsible for converting a file into a table of
 * data. Current implementations include converting from CSV, TSV and SQLLite.
 * <p>
 * New parsers can be created by extending this class and registering the
 * subclass as a {@link ServiceProvider}.
 *
 * @author sirius
 */
public abstract class ImportFileParser {

    private static final Map<String, ImportFileParser> PARSERS = new LinkedHashMap<>();
    private static final Map<String, ImportFileParser> UNMODIFIABLE_PARSERS = Collections.unmodifiableMap(PARSERS);

    public static final ImportFileParser DEFAULT_PARSER = getParsers().values().iterator().next();

    private static synchronized void init() {
        if (PARSERS.isEmpty()) {
            final List<ImportFileParser> parsers = new ArrayList<>(Lookup.getDefault().lookupAll(ImportFileParser.class));
            Collections.sort(parsers, (ImportFileParser o1, ImportFileParser o2) -> Integer.compare(o1.position, o2.position));
            parsers.stream().forEach(parser -> PARSERS.put(parser.label, parser));
        }
    }

    /**
     * Returns instances of all registered ImportFileParser classes mapped by
     * their names.
     * <p>
     * The map returned is unmodifiable and its iterators will return the
     * ImportFileParser instances in order of position (highest first).
     *
     * @return Instances of all registered ImportFileParser classes mapped by
     * their names.
     */
    public static Map<String, ImportFileParser> getParsers() {
        init();
        return UNMODIFIABLE_PARSERS;
    }

    /**
     * Returns the ImportFileParser with the specified name or null if no
     * ImportFileParser has been registered with that name.
     *
     * @param label the label of a registered ImportFileParser.
     *
     * @return the ImportFileParser with the specified name.
     */
    public static ImportFileParser getParser(final String label) {
        return UNMODIFIABLE_PARSERS.get(label);
    }

    private final String label;
    private final int position;

    /**
     * Creates a new ImportFileParser with a specified label and position.
     *
     * @param label the label of the ImportFileParser (displayed in the UI).
     * @param position the position of the ImportFileParser used for sorting a
     * list of parsers.
     */
    public ImportFileParser(final String label, final int position) {
        this.label = label;
        this.position = position;
    }

    /**
     * Returns the label of this ImportFileParser.
     *
     * @return the label of this ImportFileParser.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Returns the position of this ImportFileParser. The position is used to
     * sort a list of ImportFileParsers when displayed in the UI.
     *
     * @return the position of this ImportFileParser.
     */
    public final int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Returns a {@link PluginParameter}s that specifies all the parameters
     * required to configure this ImportFileParser.
     *
     * @param refreshRequest a RefreshRequest object that can be used by the
     * parameters to refresh the UI in response to parameter changes.
     *
     * @return a {@link PluginParameter}s that specifies all the parameters
     * required to configure this ImportFileParser.
     */
    public PluginParameters getParameters(final RefreshRequest refreshRequest) {
        return null;
    }

    public void updateParameters(final PluginParameters parameters, final List<InputSource> inputs) {

    }

    /**
     * Returns the extension filter to use when browsing for files of this type.
     *
     * @return the extension filter to use when browsing for files of this type.
     */
    public ExtensionFilter getExtensionFilter() {
        return null;
    }

    /**
     * Reads the entire file and returns a List of String arrays, each of which
     * represents a row in the resulting table.
     *
     * @param input Input file
     * @param parameters the parameters that configure the parse operation.
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     * @throws IOException if an error occurred while reading the file.
     */
    public abstract List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException;

    /**
     * Reads only {@code limit} lines and returns a List of String arrays, each
     * of which represents a row in the resulting table.
     *
     * @param input Input file
     * @param parameters the parameters that configure the parse operation.
     * @param limit Row limit
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     * @throws IOException if an error occurred while reading the file.
     */
    public abstract List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException;

}
