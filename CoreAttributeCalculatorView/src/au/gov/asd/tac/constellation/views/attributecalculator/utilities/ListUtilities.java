/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributecalculator.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptException;
import org.openide.util.lookup.ServiceProvider;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.__builtin__;

/**
 * Used to allow neighbour vertex analysis in the attribute calculator.
 *
 * The attribute calculator plugin constructs a VertexNeighbourContext object
 * with the relevant graph, engine and bindings. This object is then bound to
 * the name 'neighbours' from the perspective of users coding in python. The
 * current element id being processed by the attribute calculator must be
 * updated in this object. Users can then call neighbours.has_neighbour() for
 * example to perform analysis on the neighbours of each node in the graph. The
 * public methods in this class are intentionally named with underscores against
 * convention as these names must match the names visible to the user which
 * should be pythonic.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractCalculatorUtilities.class)
public class ListUtilities extends AbstractCalculatorUtilities {

    private static final String SCRIPTING_NAME = "lists";
    private CalculatorContextManager context;

    @Override
    public void setContextManager(final CalculatorContextManager context) {
        this.context = context;
    }

    @Override
    public String getScriptingName() {
        return SCRIPTING_NAME;
    }

    public Object median(final Collection<Object> collection) throws ScriptException {
        final PyList pylist = new PyList(collection);
        if (pylist.__len__() == 0) {
            return null;
        }
        return pylist.get(pylist.__len__() / 2);
    }

    public Object mean(final Collection<Object> collection) throws ScriptException {
        final PyList pylist = new PyList(collection);
        if (pylist.__len__() == 0) {
            return null;
        }
        PyObject acc = __builtin__.sum(pylist);
        acc = acc.__div__(new PyInteger(pylist.__len__()));
        return acc;
    }

    public Object mode(final Collection<Object> collection) throws ScriptException {
        final PyList pylist = new PyList(collection);
        Map<PyObject, Integer> counts = new HashMap<>();
        int maxCount = 0;
        PyObject mode = null;
        if (pylist.__len__() == 0) {
            return null;
        }
        for (int i = 0; i < pylist.__len__(); i++) {
            PyObject current = pylist.__getitem__(i);
            int currentCount = 1;
            if (counts.containsKey(current)) {
                currentCount = counts.get(current) + 1;
            }
            if (currentCount > maxCount) {
                maxCount = currentCount;
                mode = current;
            }
            counts.put(current, currentCount);
        }
        return mode;
    }

    public Object defined_values(final Collection<Object> collection) throws ScriptException {
        collection.removeIf((Object t) -> nullCheck(t));
        return collection;
    }

}
