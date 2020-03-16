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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Lookup;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

/**
 * Abstract Calculator Utility. Note that calculator utilities with multiple
 * publicly accessible methods of the same name should all have different
 * numbers of parameters. This is a slight limitation, but is not significant
 * and allows everything to work nicely.
 *
 * @author twilight_sparkle
 */
public abstract class AbstractCalculatorUtilities {

    /**
     * Returns all calculator utilities
     *
     * @return all calculator utilities
     */
    public static Collection<AbstractCalculatorUtilities> getAllUtilities() {
        return (Collection<AbstractCalculatorUtilities>) Lookup.getDefault().lookupAll(AbstractCalculatorUtilities.class);
    }

    //  - it is not the java or python null values, nor the special obliterator object
    //  - it is not a python object with a __nonzero__ method that returns false
    //  - it is not a python object with a __len__ method that returns false
    protected static boolean isTrueValue(PyObject obj) {
        if (nullCheck(obj)) {
            return false;
        }
        if (__builtin__.hasattr(obj, new PyString("__nonzero__"))) {
            return obj.__nonzero__();
        }
        if (__builtin__.hasattr(obj, new PyString("__len__"))) {
            return obj.__len__() != 0;
        }
        return true;
    }

    // An object is true and contains non nulls if and only if:
    //  - it is not an iterable containing only objects which are null
    //  - is not itself false according to isTrueValue
    protected static boolean isTrueAndContainsNonNulls(PyObject obj) {
        if (obj instanceof PyList) {
            ((PyList) obj).removeIf((Object t) -> nullCheck(t));
            return !((PyList) obj).isEmpty();
        }
        return isTrueValue(obj);
    }

    // Checks to see whether an argument is null or the obliterator object. This should be called on any argument to
    // a utility function where that argument is permitted to be null (or the obliterator representation of null) and
    // a special result is returned in this case.
    // Note that any argument to a utility function could potentially have a null value; the author of a utility function
    // must make a design decision for each argument as to whether null should be handled gracefully or whether the end
    // user of the attribute calculator should be explicitly checking for null and hence an exception should be thrown.
    protected static boolean nullCheck(Object argument) {
        return argument == null || (argument instanceof PyObject && ((PyObject) argument).getType() == null) || argument.equals(AbstractCalculatorValue.the_obliterator);
    }

    public abstract void setContextManager(CalculatorContextManager manager);

    public abstract String getScriptingName();

    // This method is reflection city; dealwithit.jpg
    // Retrieves all public, non-inhereted method names from a concrete utility class so that these names can be bound in a script engine which intends to use the utilities.
    public Set<String> getUtilityMethodNames() {
        Set<String> methodNames = new HashSet<>();
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                methodNames.add(method.getName());
            }
        }
        return methodNames;
    }

    public Set<Integer> getFunctionalArgumentIndices(String methodName, int numArgs) {
        final Set<Integer> functionalArgumentIndices = new HashSet<>();
        Method[] methods = getClass().getDeclaredMethods();
        Method candidate = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName) && m.getParameterCount() == numArgs) {
                candidate = m;
                break;
            }
        }
        if (candidate != null) {
            Class<?>[] types = candidate.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(PyFunction.class)) {
                    functionalArgumentIndices.add(i);
                }
            }
        }
        return functionalArgumentIndices;
    }

}
