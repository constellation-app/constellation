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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Attribute;
import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds the state of an individual 'rule' used in Find services.
 * <p>
 * There are also several helper methods to construct <code>FindRule</code>s for
 * known attribute types such as <code>Boolean</code> or <code>String</code>.
 *
 * @author betelgeuse
 */
public class FindRule {

    private FindTypeOperators.Type type;
    private Attribute attribute;
    private FindTypeOperators.Operator operator;
    private boolean isAddToSelection;
    private Map<String, Object> args;
    // Default Map Keys:
    // Boolean:
    private static final String BLN_CONTENT = "boolean_content";
    // Color:
    private static final String COL_CONTENT = "color_content";
    // Date:
    private static final String DAT_FIRST = "date_first_item";
    private static final String DAT_SECOND = "date_second_item";
    // DateTime:
    private static final String DT_FIRST = "datetime_first_item";
    private static final String DT_SECOND = "datetime_second_item";
    // Float:
    private static final String FLT_FIRST = "float_first_item";
    private static final String FLT_SECOND = "float_second_item";
    // Int:
    private static final String INT_FIRST = "int_first_item";
    private static final String INT_SECOND = "int_second_item";
    // Icon:
    private static final String ICO_CONTENT = "icon_content";
    // String:
    private static final String STR_CONTENT = "string_content";
    private static final String STR_CASE = "string_case_sensitive";
    private static final String STR_LIST = "string_use_list";
    // Time:
    private static final String TIM_FIRST = "time_first_item";
    private static final String TIM_SECOND = "time_second_item";

    /**
     * Constructs a blank new FindRule.
     */
    public FindRule() {
        type = null;
        attribute = null;

        args = new HashMap<>();
    }

    /**
     * Constructs a <code>FindState</code> from a previously known/saved state.
     *
     * @param type The previous type.
     * @param attribute The previous attribute.
     * @param operator The previous operator.
     * @param args The previous arguments.
     */
    public FindRule(final FindTypeOperators.Type type, final Attribute attribute, final FindTypeOperators.Operator operator,
            final Map<String, Object> args) {
        this.type = type;
        this.attribute = attribute;
        this.operator = operator;
        this.args = args;
    }

    /**
     * Removes all stored values from the <code>FindState</code>.
     * <P>
     * Essentially resets the FindState.
     */
    public void clear() {
        type = null;
        attribute = null;
        operator = null;

        if (args != null) {
            args.clear();
        }
    }

    /**
     * Gets the operator enumeration corresponding to the current state's type.
     *
     * @return type The current type.
     */
    public FindTypeOperators.Type getType() {
        return type;
    }

    /**
     * Sets the type for the current state.
     *
     * @param type The type to be set for the current state.
     */
    public void setType(final FindTypeOperators.Type type) {
        this.type = type;
    }

    /**
     * Gets the current state's attribute.
     *
     * @return type
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the current state's attribute.
     *
     * @param attribute The attribute to set the current state to.
     */
    public void setAttribute(final Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * Gets the operator for the current state.
     *
     * @return operator
     */
    public FindTypeOperators.Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator for the current state.
     *
     * @param operator The operator to set the current state to.
     */
    public void setOperator(final FindTypeOperators.Operator operator) {
        this.operator = operator;
    }

    /**
     * Gets the current state for whether or not current selections on the graph
     * should be 'held', or overridden.
     *
     * @return <code>true</code> to keep current selection, <code>false</code>
     * to override.
     */
    public boolean isHeld() {
        return isAddToSelection;
    }

    public void setHeld(final boolean isHeld) {
        this.isAddToSelection = isHeld;
    }

    /**
     * Sets the arguments for the current state.
     *
     * @param args Map of arguments.
     */
    public void setArgs(final Map<String, Object> args) {
        this.args = args;
    }

    /**
     * Returns the map of arguments for the current state.
     *
     * @return args; Map of arguments.
     */
    public Map<String, Object> getArgs() {
        return args;
    }

    // <editor-fold defaultstate="collapsed" desc="Default Rule Builder / Retriever Functions">
    /**
     * Helper method to retrieve <code>Boolean</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Boolean</code> representing the saved Boolean value in this
     * <code>FindRule</code>
     */
    public boolean getBooleanContent() {
        return args != null && (Boolean) args.get(BLN_CONTENT);
    }

    /**
     * Helper method to save <code>Boolean</code> content to this
     * <code>FindRule</code>.
     *
     * @param content The value to save to this <code>FindRule</code>.
     */
    public void addBooleanBasedRule(final boolean content) {
        type = FindTypeOperators.Type.BOOLEAN;

        args = new HashMap<>();
        args.put(BLN_CONTENT, content);
    }

    /**
     * Helper method to retrieve <code>Color</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Color</code> representing the saved Color value in this
     * <code>FindRule</code>
     * @see Color
     */
    public Color getColorContent() {
        return args != null ? (Color) args.get(COL_CONTENT) : null;
    }

    /**
     * Helper method to save <code>Color</code> content to this
     * <code>FindRule</code>.
     *
     * @param content The value to save to this <code>FindRule</code>.
     * @see Color
     */
    public void addColorBasedRule(final Color content) {
        type = FindTypeOperators.Type.COLOR;

        args = new HashMap<>();
        args.put(COL_CONTENT, content);
    }

    /**
     * Helper method to retrieve the first <code>Date</code> content saved to
     * this <code>FindRule</code>.
     *
     * @return <code>Date</code> representing the first saved Date value in this
     * <code>FindRule</code>
     * @see Date
     */
    public Date getDateFirstArg() {
        return args != null ? (Date) args.get(DAT_FIRST) : null;
    }

    /**
     * Helper method to retrieve the second <code>Date</code> content saved to
     * this <code>FindRule</code>.
     *
     * @return <code>Date</code> representing the second saved Date value in
     * this <code>FindRule</code>
     * @see Date
     */
    public Date getDateSecondArg() {
        return args != null ? (Date) args.get(DAT_SECOND) : null;
    }

    /**
     * Helper method to save <code>Date</code> content to this
     * <code>FindRule</code>.
     *
     * @param first the first date.
     * @param second the second date.
     *
     * @see Date
     */
    public void addDateBasedRule(final Date first, final Date second) {
        type = FindTypeOperators.Type.DATE;

        args = new HashMap<>();
        args.put(DAT_FIRST, first);
        args.put(DAT_SECOND, second);
    }

    /**
     * Helper method to retrieve the first <code>Calendar</code> content saved
     * to this <code>FindRule</code>.
     *
     * @return <code>Calendar</code> representing the first saved DateTime value
     * in this <code>FindRule</code>
     * @see Calendar
     */
    public Calendar getDateTimeFirstArg() {
        return args != null ? (Calendar) args.get(DT_FIRST) : null;
    }

    /**
     * Helper method to retrieve the second <code>Calendar</code> content saved
     * to this <code>FindRule</code>.
     *
     * @return <code>Calendar</code> representing the second saved DateTime
     * value in this <code>FindRule</code>
     * @see Calendar
     */
    public Calendar getDateTimeSecondArg() {
        return args != null ? (Calendar) args.get(DT_SECOND) : null;
    }

    /**
     * Helper method to save <code>Calendar</code> content (representing a
     * DateTime value) to this <code>FindRule</code>.
     *
     * @param first the first calendar.
     * @param second the second calendar.
     *
     * @see Calendar
     */
    public void addDateTimeBasedRule(final Calendar first, final Calendar second) {
        type = FindTypeOperators.Type.DATETIME;

        args = new HashMap<>();
        args.put(DT_FIRST, first);
        args.put(DT_SECOND, second);
    }

    /**
     * Helper method to save <code>Float</code> content to this
     * <code>FindRule</code>.
     *
     * @param first the first float.
     * @param second the second float.
     */
    public void addFloatBasedRule(final float first, final float second) {
        type = FindTypeOperators.Type.FLOAT;

        args = new HashMap<>();
        args.put(FLT_FIRST, first);
        args.put(FLT_SECOND, second);
    }

    /**
     * Helper method to retrieve <code>Float</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Float</code> representing the first saved Float value in
     * this <code>FindRule</code>
     */
    public float getFloatFirstArg() {
        return args != null ? (Float) args.get(FLT_FIRST) : 0.0F;
    }

    /**
     * Helper method to retrieve <code>Float</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Float</code> representing the second saved Float value in
     * this <code>FindRule</code>
     */
    public float getFloatSecondArg() {
        return args != null ? (Float) args.get(FLT_SECOND) : 0.0F;
    }

    /**
     * Helper method to save <code>Float</code> content to this
     * <code>FindRule</code>.
     *
     * @param first the first int.
     * @param second the second int.
     */
    public void addIntegerBasedRule(final int first, final int second) {
        type = FindTypeOperators.Type.INTEGER;

        args = new HashMap<>();
        args.put(INT_FIRST, first);
        args.put(INT_SECOND, second);
    }

    /**
     * Helper method to retrieve <code>Float</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Float</code> representing the first saved Float value in
     * this <code>FindRule</code>
     */
    public int getIntFirstArg() {
        return args != null ? (Integer) args.get(INT_FIRST) : 0;
    }

    /**
     * Helper method to retrieve <code>Float</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Float</code> representing the second saved Float value in
     * this <code>FindRule</code>
     */
    public int getIntSecondArg() {
        return args != null ? (Integer) args.get(INT_SECOND) : 0;
    }

    /**
     * Helper method to save <code>String</code> content (representing an Icon's
     * value) to this <code>FindRule</code>.
     *
     * @param content The value to save to this <code>FindRule</code>.
     */
    public void addIconBasedRule(final String content) {
        type = FindTypeOperators.Type.ICON;

        args = new HashMap<>();
        args.put(ICO_CONTENT, content);
    }

    /**
     * Helper method to retrieve <code>Icon</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>String</code> representing the saved Icon value in this
     * <code>FindRule</code>
     */
    public String getIconContent() {
        return args != null ? (String) args.get(ICO_CONTENT) : null;
    }

    /**
     * Helper method to save <code>String</code> content to this
     * <code>FindRule</code>.
     *
     * @param content The value to save to this <code>FindRule</code>.
     * @param isCaseSensitive Matches case sensitivity option on the form.
     * @param isUsingList Matches using list option on the form.
     */
    public void addStringBasedRule(final String content,
            final boolean isCaseSensitive, final boolean isUsingList) {
        type = FindTypeOperators.Type.STRING;

        args = new HashMap<>();
        args.put(STR_CONTENT, content);
        args.put(STR_CASE, isCaseSensitive);
        args.put(STR_LIST, isUsingList);
    }

    /**
     * Helper method to retrieve <code>String</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>String</code> representing the saved String value in this
     * <code>FindRule</code>
     */
    public String getStringContent() {
        return args != null ? (String) args.get(STR_CONTENT) : null;
    }

    /**
     * Helper method to retrieve <code>String</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Boolean</code> representing the saved 'String Case
     * Sensitivity' value in this <code>FindRule</code>
     */
    public boolean isStringCaseSensitivity() {
        return args.containsKey(STR_CASE) && (boolean) args.get(STR_CASE);
    }

    /**
     * Helper method to retrieve <code>String</code> content saved to this
     * <code>FindRule</code>.
     *
     * @return <code>Boolean</code> representing the saved 'Use List' value in
     * this <code>FindRule</code>
     */
    public boolean isStringUsingList() {
        return args.containsKey(STR_LIST) && (boolean) args.get(STR_LIST);
    }

    /**
     * Helper method to retrieve the first <code>Calendar</code> content saved
     * to this <code>FindRule</code>.
     *
     * @return <code>Calendar</code> representing the first saved Time value in
     * this <code>FindRule</code>
     * @see Calendar
     */
    public Calendar getTimeFirstArg() {
        return args != null ? (Calendar) args.get(TIM_FIRST) : null;
    }

    /**
     * Helper method to retrieve the second <code>Calendar</code> content saved
     * to this <code>FindRule</code>.
     *
     * @return <code>Calendar</code> representing the second saved Time value in
     * this <code>FindRule</code>
     * @see Calendar
     */
    public Calendar getTimeSecondArg() {
        return args != null ? (Calendar) args.get(TIM_SECOND) : null;
    }

    /**
     * Helper method to save <code>Calendar</code> content (representing a Time
     * value) to this <code>FindRule</code>.
     *
     * @param first the first calendar.
     * @param second the second calendar.
     *
     * @see Calendar
     */
    public void addTimeBasedRule(final Calendar first, final Calendar second) {
        type = FindTypeOperators.Type.TIME;

        args = new HashMap<>();
        args.put(TIM_FIRST, first);
        args.put(TIM_SECOND, second);
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "FindRule{" + "type=" + type + ", attribute=" + attribute + ", operator=" + operator + ", isAddToSelection=" + isAddToSelection + ", args=" + args + '}';
    }
}
