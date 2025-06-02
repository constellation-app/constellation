/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * JSONImportFileParser implements ImportFileParser and is responsible for
 * extracting tables of data to import from JSON files. Because the intention is
 * to import lists of data, the received file is parsed looking for lists of
 * data embedded within it. The following rules are applied to determine valid
 * lists: 1. List must have 1 or more rows of data. 2. All rows within the list
 * must contain 'complex' JSON objects. These objects are characterized with a
 * set of one or more 'field' key/value pairs contained within braces '{}'.
 * Integers, Strings etc are not permitted as these do not translate to columns.
 * Only one list will be extracted from any JSAON file, if a file contains
 * multiple 'valid' lists (see above), then an algorithm determines which one to
 * use, this is essentially the list closest to the root node of the JSON
 * structure. If multiple valid lists are found at the same depth, the first one
 * encountered is used. A Future enhancement may be to allow the user to specify
 * the path to the list to use, however this first requires an input mechanism
 * on the file importer dialog.
 *
 * @author serpens24
 */
@ServiceProvider(service = ImportFileParser.class)
public class JSONImportFileParser extends ImportFileParser {

    private static final String WARN_PARSING_PREFIX
            = """
              Extracting data from JSON file failed.
              """;
    private static final String WARN_INVALID_JSON
            = WARN_PARSING_PREFIX + "Unable to parse file, invalid JSON.";
    private static final String WARN_NO_VALID_LIST
            = WARN_PARSING_PREFIX + """
                                    No valid list found. Valid lists are not empty and will be one of the following:
                                    * A list of equal size lists, containing basic data types only (no nested lists or objects)
                                    * A list of objects. These objects may contain different data, with field names and path used to determine column names.""";

    // Flag to indicate that no suitable list has been found to extract via the importer.
    public static final int NO_LIST_LEVEL = Integer.MAX_VALUE;

    // The list to extract values from. If null, no lists were found. Refer to header comments for logic in selecting list.
    private JsonNode selectedList = null;
    // Depth of the selectedList in JSON structure, used in determining best placed list.
    private int selectedListDepth = NO_LIST_LEVEL;
    
    private static final Pattern START_END_QUOTES_REGEX = Pattern.compile("(^\")|(\"$)");

    /**
     * Construct a new JSONImportFileParser with "JSON" label at position 4.
     */
    public JSONImportFileParser() {
        super("JSON", 4);
    }

    /**
     * Private function with the sole purpose of determine that the supplied
     * parent node is an array and that all entries found within it are Objects
     * (i.e. isObject() returns true). This ensures all elements are built up of
     * key/value pairs which map well to column headings and values.
     *
     * @param parent Node to check children of as per description.
     * @return true if parent is an Array containing 1 or more Object JSON
     * nodes, and no other children. As this function is designed to only be
     * called by lookForChildArrays it can be assumed that parent is an array
     * and not empty as this is checked in lookForChildArrays.
     */
    private boolean checkAllArrayItemsAreObjects(final JsonNode parent) {

        // Get the first child of the list and ensure all children are same type.
        // We only want a list of lists or a list of objects.
        JsonNode firstChild = parent.get(0);

        // We now know we have an ArrayNode, check all entries in the array are
        // ObjectNodes or all entries are ArrayNodes, if any are not, return
        // false.
        if (firstChild.isObject()) {
            for (final JsonNode childNode : parent) {
                if (!childNode.isObject() || childNode.size() == 0) {
                    return false;
                }
            }
            return true;
        } else if (firstChild.isArray()) {
            for (final JsonNode childNode : parent) {
                if (!childNode.isArray()) {
                    return false;
                }

                if (firstChild.size() != childNode.size() || firstChild.size() == 0) {
                    return false;
                }

                // also confirm the list only contains primitive values, otherwise
                // due to list entries not being named, its not possible to align
                // content.
                for (final JsonNode grandchildNode : childNode) {
                    if (grandchildNode.isArray() || grandchildNode.isObject()) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            // Do nothing
        }
        return false;
    }

    /**
     * Private function that navigates the JSON tree looking for all array nodes
     * that meet the following criteria: 1. List must have 1 or more rows of
     * data. 2. All rows within the list must contain 'complex' JSON objects.
     * These objects are characterized with a set of one or more 'field'
     * key/value pairs contained within braces '{}'. Integers, Strings etc are
     * not permitted as these do not translate to columns. Function is
     * responsible for populating the private variables selectedList and
     * selectedListDepth to highlight the identified best list found within the
     * JSON structure.
     *
     * @param node The node to begin search at.
     * @param path The path to the given node in the overall JSON structure.
     * @param depth The depth into the overall JSON structure of the node.
     */
    private void lookForChildArrays(final JsonNode node, final String path, final int depth) throws IOException {
        if (node.isArray()) {
            if (node.size() > 0 && checkAllArrayItemsAreObjects(node)) {
                // Process node content. IF errors are detected an IOException is
                // thrown so only successful case needs to be handled. On success
                // update details of list to use.
                selectedList = node;
                selectedListDepth = 1;
            }
        } else if (node.size() > 0) {
            // Top level node is not an array, go searching
            for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
                final Map.Entry<String, JsonNode> entry = it.next();

                // We are only interested in arrays that contain at least one
                // ObjectNode entry, an ObjectNode contains one or more fields
                if (entry.getValue().isArray() && depth < selectedListDepth
                        && entry.getValue().size() > 0
                        && checkAllArrayItemsAreObjects(entry.getValue())) {

                    // Passed all checks and is current best candidate list, update
                    // globals capturing this information.
                    selectedList = entry.getValue();
                    selectedListDepth = depth;

                } else if (entry.getValue().isObject() && depth < selectedListDepth) {
                    // The node is not an array buy is another container, dive into
                    // it and see if there is any list
                    lookForChildArrays(entry.getValue(), path + "/" + entry.getKey(), (depth + 1));
                } else {
                    // Do nothing
                }
            }
        } else {
            // Do nothing
        }
    }

    /**
     * Private function designed to only be called by extractAllColNames. This
     * recursive function digs down through nested JSON objects to extract
     * further column names to add to existingColumns and return the updated
     * value.
     *
     * @param parent he parent node to start column name extraction from.
     * @param existingColumns List of already identified columns. This list is
     * added to if new column names are identified.
     * @param prefix Path prefix, this is used to help build up a fully
     * qualified column name relative to the list origin.
     * @return Updated list of known column headers.
     */
    private ArrayList<String> extractColNamesFromFields(final JsonNode node, final ArrayList<String> existingColumns, final String prefix) {
        if (node.isObject()) {
            // Iterate over each field in object and add its name if it doesnt already exist in results
            for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
                final Map.Entry<String, JsonNode> entry = it.next();

                if (entry.getValue().isObject()) {
                    // If the entry node is an Object node then apply recursion to
                    // it and progress its fields. Note the updated prefix string
                    // which allows column names in table to represent JSON path.
                    extractColNamesFromFields(entry.getValue(), existingColumns, prefix + entry.getKey() + SeparatorConstants.PERIOD);
                } else if (!existingColumns.contains(prefix + entry.getKey())) {
                    // The entry node is not an object, meaning it should be either
                    // a primitive type or a list. Either case is treated as a
                    // potential column title, with nested lists ultimately
                    // representing their data as a merged value string.
                    existingColumns.add(prefix + entry.getKey());
                } else {
                    // Do nothing
                }
            }
        }
        return existingColumns;
    }

    /**
     * Private wrapper function designed to create a comprehensive list of
     * column names within the JSON structure starting at the supplied parent
     * node. This function requires the list to have already been validated, as
     * is done by lookForChildArrays, which only identifies valid lists.
     *
     * @param parent The parent node to start column name extraction from.
     * @param existingColumns List of already identified columns. This list is
     * added to if new column names are identified. If null, then the list is
     * created, this would be the case upon the first call.
     * @param prefix Path prefix, this is used to help build up a fully
     * qualified column name relative to the list origin.
     * @return Updated list of known column headers.
     */
    private ArrayList<String> extractAllColNames(final JsonNode parent, ArrayList<String> existingColumns, final String prefix) {
        // Ensure existingColumns is created if it wasn't already.
        if (existingColumns == null) {
            existingColumns = new ArrayList<>();
        }

        // Check the first entry in the collection which will either be a list or
        // and object. All collection members will be of the same type due to
        // earlier validation.
        if (parent.get(0) != null && parent.get(0).isArray()) {
            // This is a list of lists (rows).
            // Treat first list as column headers.
            for (final JsonNode listNode : parent.get(0)) {
                // In future there may be an option to select if first row is
                // column names or actual data, for now use it as column
                // headings.
                existingColumns.add(listNode.toString());
            }
        } else {
            // The list is a list of complex objects, extract unique names from
            // each of these.
            for (final JsonNode node : parent) {
                extractColNamesFromFields(node, existingColumns, prefix);
            }
        }
        return existingColumns;
    }

    /**
     * Private function which constructs a line of data (based on column names)
     * starting at the selected node. The line of data will include all values
     * at the level of the supplied node as well as any nested values, using a
     * prefix notation to identify the column name to use - as generated by the
     * function extractAllColNames. Both this function and the column extraction
     * functions need to stay in synch in relation to how column names are
     * constructed.
     *
     * @param node Node to start line extraction from.
     * @param columnMap A map listing names of all available columns and
     * containing an index to the column number that should contain this info.
     * This map is built up from the list of extracted columns and allows data
     * at a given JSON path to be correctly matched with the column it belongs
     * in in the extracted line.
     * @param prefix Prefix string identifying the JSON path to the node from
     * the parent node of the list.
     * @param line The line to add content to, if null setup the line with
     * enough slots to cover all columns.
     * @return line of data. This is effectively an array of strings, one per
     * column.
     */
    private String[] getLineContent(final JsonNode node, final Map<String, Integer> columnMap, final String prefix, String[] line) {
        // Ensure the line is created if it wasn't already.
        if (line == null) {
            line = new String[columnMap.size()];
        }

        if (node.isArray()) {
            // The list is a list of lists, validation ensures lists are all of
            // equal size so just loop through and populate line with entries.
            int colNo = 0;
            for (final JsonNode listEntry : node) {
                line[colNo++] = listEntry.toString();
            }
        } else if (node.isObject()) {
            // Iterate over all child fields of the parent noode, for each one
            // determine if its a container Object node, if so recursively continue
            // to extract its values, if not, extract the value. Note that nested
            // lists will be converted to text, so if a list contains another list,
            // that second list is treated as a single object.
            for (final Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
                final Map.Entry<String, JsonNode> entry = it.next();

                if (entry.getValue().isObject()) {
                    line = getLineContent(entry.getValue(), columnMap, (prefix + entry.getKey() + SeparatorConstants.PERIOD), line);
                } else {
                    line[columnMap.get(prefix + entry.getKey())] = START_END_QUOTES_REGEX.matcher(entry.getValue().toString()).replaceAll("");
                }
            }
        } else {
            // Do nothing
        }
        return line;
    }

    /**
     * Top level processing function. This function is called by both the
     * overloaded base class functions parse and preview to avoid code
     * duplication. This function uses the other private functions found in this
     * class to do the following: 1. identify the best available list within the
     * overall JSON file to use 2. for the identified list, recursively build up
     * a list of candidate columns 3. loop through each list entry and extract
     * values into a list of data to return.
     *
     * Key Considerations: Refer to the following sample JSON example to
     * demonstrate key processing considerations.
     *
     * {
     * "parent": { "description": "this is example JSON", "badlist": ["this",
     * "list", "contains", "strings", "only"], "goodlist": [ { "name":
     * "record1", "age": 184, "address": { "city": "Adelaide", "state": "SA",
     * "postcode": 5001, "commerical": ["pub", "shop"], "history": { "est":
     * 2025, "population": 600 } } }, { "name": "record2", "description":
     * "example description", "address": { "city": "Canberra", "state": "ACT",
     * "postcode": 2601, "latitude": -32.2809, "longitude": 149.13 } } ] } }
     *
     * 1. The list parent/badlist will be ignored as it doesn't contain only
     * objects. 2. The list parent/goodlist will be considered as all of the
     * following conditions are met: - it has at least one entry - all list
     * entries are complex objects - it is not nested under another list 3. The
     * list parent/goodlist/1/address/commercial will be ignored as: - it
     * doesn't contain only objects - it is deeper than parent/goodlist - it is
     * contained within another list 4. Ultimately list parent/goodlist is
     * chosen as its the shallowest list found. The only way it would have been
     * overlooked was if parent/badlist was valid, or if another list node was
     * added at the same level as parent. 5. The following column names and data
     * would be extracted (under the parent/goodlist list node). Note, first
     * column is column name, columns 2 and 3 are records. - name record1
     * record2 - age 184             <null>
     * - address.city Adelaide Canberra - address.state SA SA - address.postcode
     * 5001 2601 - address.commerical ["pub", "shop"] <null>
     * - address.history.est 2025            <null>
     * - address.history.population 600             <null>
     * - description                    <null> example description - address.latitude               <null>
     * -32.2809 - address.longitude              <null> 149.13 As can be seen above, if a
     * filed doesn't exist in a particular row it is nulled. This handles the
     * case with individual rows have incomplete or differing fields.
     *
     * @param input Input file
     * @param limit How many rows to return
     * @return a List of String arrays, each of which represents a row in the
     * resulting table.
     */
    private List<String[]> getResults(final InputSource input, final int limit) throws IOException {
        try {
            final ArrayList<String[]> results = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            InputStream in = input.getInputStream();

            // Get root node and try to find a valid candidate list. If no list
            // is found there will be no data to import.
            selectedList = null;
            selectedListDepth = NO_LIST_LEVEL;
            final JsonNode root;
            final ArrayNode childNode = mapper.createArrayNode();
            JsonNode node = null;
            int counter = 0;

            // read in all JSON object from input
            final MappingIterator<JsonNode> it = mapper.readerFor(JsonNode.class)
                    .readValues(in);
            while (it.hasNextValue()) {
                node = it.nextValue();
                childNode.add(node);
                counter++;
            }
            // Maps newline delimited JSON to valid JSON in the format
            // {"results": [<ndjson>]}
            switch(counter){
                case 0 -> throw new IOException(WARN_NO_VALID_LIST);
                case 1 -> root = node;
                default -> {
                    // Changes the ndJSON to valid JSON
                    final ObjectMapper newJSON = new ObjectMapper();
                    final ObjectNode rootNode = newJSON.createObjectNode();
                    rootNode.set("results", childNode);
                    root = rootNode;
                }
            }
            lookForChildArrays(root, "", 0);

            if (selectedList != null) {

                // A valid list is found, extract its fully qualified column
                // names and store them in a dictionary mapping them to column
                // number.
                ArrayList<String> columns = extractAllColNames(selectedList, null, "");
                Map<String, Integer> columnMap = new HashMap<>();
                columns.forEach(column -> columnMap.put(column, columnMap.size()));

                // Add a heading row to the return data
                String[] headings = new String[columns.size()];
                headings = columns.toArray(headings);
                results.add(headings);

                // Now process all records and add them to the return data based
                // on column indexes
                for (final JsonNode listNode : selectedList) {
                    if (listNode.isObject() || listNode != selectedList.get(0)) {

                        // If we are dealing with a list of lists, the first row is used
                        // as column headings, so skip over it.
                        String[] line = getLineContent(listNode, columnMap, "", null);
                        results.add(line);

                        if (results.size() > limit && limit > 0) {
                            break;
                        }
                    }
                }
            } else {
                throw new IOException(WARN_NO_VALID_LIST);
            }
            return results;
        } catch (final JsonParseException ex) {
            // Catch case whre invalid JSON file has been supplied gracefully
            throw new IOException(WARN_INVALID_JSON);
        } catch (final Exception ex) {
            // Catch case whre invalid file content has been supplied gracefully (IOException)
            // along with any unexpected exceptions
            throw ex;
        }
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
    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        return getResults(input, 0);
    }

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
    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        return getResults(input, limit);
    }

    /**
     * Returns the file filter to use when browsing for files of this type.
     *
     * @return the file filter to use when browsing for files of this type.
     */
    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(final File file) {
                final String name = file.getName();
                return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.JSON)) || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "JSON Files (" + FileExtensionConstants.JSON + ")";
            }
        };
    }
}
