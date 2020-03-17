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

import javax.script.ScriptException;
import org.openide.util.lookup.ServiceProvider;
import org.python.core.PyLong;
import org.python.core.PyTuple;
import org.python.modules.time.Time;

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
public class TimeUtilities extends AbstractCalculatorUtilities {

    private static final String SCRIPTING_NAME = "times";
    private CalculatorContextManager context;

    @Override
    public void setContextManager(final CalculatorContextManager context) {
        this.context = context;
    }

    @Override
    public String getScriptingName() {
        return SCRIPTING_NAME;
    }

    public Object year(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(0);
    }

    public Object month(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(1);
    }

    public Object day(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(2);
    }

    public Object hour(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(3);
    }

    public Object minute(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(4);
    }

    public Object second(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(5);
    }

    public Object weekday(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return timeTuple.__getitem__(6);
    }

    public Object month_name(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return Time.strftime("%B", timeTuple);
    }

    public Object weekday_name(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return Time.strftime("%A", timeTuple);
    }

    public Object time_from_date(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return (timeTuple.__getitem__(3).asInt() * MINS_IN_HOUR * SECS_IN_MIN * MILLISECS_IN_SEC)
                + (timeTuple.__getitem__(4).asInt() * SECS_IN_MIN * MILLISECS_IN_SEC)
                + (timeTuple.__getitem__(5).asInt() * MILLISECS_IN_SEC);
    }

    public Object time_string_from_date(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return Time.strftime("%H:%M:%S", timeTuple);
    }

    public Object date_as_string(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        PyLong time = new PyLong(((Number) time_ms).longValue() / 1000);
        PyTuple timeTuple = Time.gmtime(time);
        return Time.strftime("%Y-%m-%d %H:%M:%S", timeTuple);
    }

    private static final int MILLISECS_IN_SEC = 1000;
    private static final int SECS_IN_MIN = 60;
    private static final int MINS_IN_HOUR = 60;
    private static final int HOURS_IN_DAY = 24;

    public Object days(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        return (int) (((Number) time_ms).longValue() / (1000 * SECS_IN_MIN * MINS_IN_HOUR * HOURS_IN_DAY));
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public Object hours(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        return (int) (((Number) time_ms).longValue() / (1000 * SECS_IN_MIN * MINS_IN_HOUR));
    }

    public Object minutes(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        return (int) (((Number) time_ms).longValue() / (1000 * SECS_IN_MIN));
    }

    public Object seconds(Object time_ms) throws ScriptException {
        if (nullCheck(time_ms)) {
            return time_ms;
        }
        return (int) (((Number) time_ms).longValue() / 1000);
    }

}
