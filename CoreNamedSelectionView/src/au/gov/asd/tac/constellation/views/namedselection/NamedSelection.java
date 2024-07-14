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
package au.gov.asd.tac.constellation.views.namedselection;

import org.openide.util.NbBundle.Messages;

/**
 * This class represents a Named Selection object.
 * <p>
 * Each instance of a <code>NamedSelection</code> contains (at the very least)
 * an ID which can be used to refer to a known set of graph elements from a
 * given graph.
 * <p>
 * Additional data and meta-data pertaining to the Named Selection is also
 * stored, and is persisted to the parent graph.
 * <p>
 * In order to fully support Undo/Redo (and the associated requirement of
 * immutable attributes), this class offers the ability to provide new clones of
 * the instance object. This allows the Undo/Redo management to flag changes to
 * the underlying graph structure when changes are made to this object type.
 *
 * @see Cloneable
 *
 * @author betelgeuse
 */
@Messages("NamedSelection_DefaultSelectionName=Selection ")
public class NamedSelection {

    private final int id;
    private String hotkey = null;
    private String name = null;
    private String description = "";
    private boolean isLocked = false;

    /**
     * Constructs a dummy named selection object which can be used for
     * place-holding, such as for 'Current Selections' at the top of the Named
     * Selection Browser.
     */
    public NamedSelection() {
        id = -1; // From 0 to 63
        setDefaultName();
    }

    /**
     * Constructs a new <code>NamedSelection</code> with a provided id number.
     *
     * @param id The id of the new named selection.
     */
    public NamedSelection(final int id) {
        this.id = id;
        setDefaultName();
    }

    /**
     * Constructs a new <code>NamedSelection</code> with both a provided id, and
     * a reference hotkey.
     *
     * @param id The id of the new named selection.
     * @param hotkey The hotkey that can be used to reference this object.
     */
    public NamedSelection(final int id, final String hotkey) {
        this.id = id;
        this.hotkey = hotkey;
        setDefaultName();
    }
    
    /**
     * Constructs a deep copy of an existing NamedSelection object
     * 
     * @param namedSelection the NamedSelection to copy
     */
    public NamedSelection(final NamedSelection namedSelection) {
        this.id = namedSelection.id;
        this.description = namedSelection.description;
        this.hotkey = namedSelection.hotkey;
        this.name = namedSelection.name;
        this.isLocked = namedSelection.isLocked;
    }

    /**
     * Returns the ID used to perform named selection operations on a known
     * subset of the graph.
     * <p>
     * The id is used to determine which elements are selected. The attribute
     * <tt>named_selection</tt> is a long representing a bit mask corresponding
     * to named selection ids. If the equivalent bit is set, then this element
     * is part of that named selection.
     * <p>
     * In other words, given <tt>id</tt> as the id of a named selection, then
     * <tt>final long mask = 1L &lt;&lt; id;</tt> gives a mask to check each
     * <tt>named_selection</tt> value. For each element, if((mask &amp;
     * value)!=0), then this element is in the named selection.
     *
     * @return The id of the named selection.
     */
    public int getID() {
        return id;
    }

    /**
     * Sets the name of the named selection.
     * <p>
     * The name would typically appear as a user-friendly method of identifying
     * a given named selection, though there is no mechanism for guaranteeing
     * uniqueness.
     *
     * @param name The new name of the named selection.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the named selection.
     * <p>
     * The name would typically appear as a user-friendly method of identifying
     * a given named selection, though there is no mechanism for guaranteeing
     * uniqueness.
     *
     * @return The name of this named selection.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the hotkey of the named selection.
     * <p>
     * The hotkey is used as a user-friendly method for quick retrieval of a
     * given named selection. There is guarantee of uniqueness.
     *
     * @param hotkey The key that can be used (in conjunction with 'ctrl') to
     * recall this named selection.
     */
    public void setHotkey(final String hotkey) {
        if (hotkey.length() == 1) {
            this.hotkey = hotkey;
        }
    }

    /**
     * Gets the hotkey of the named selection.
     * <p>
     * The hotkey is used as a user-friendly method for quick retrieval of a
     * given named selection. There is guarantee of uniqueness.
     *
     * @return The hotkey of this named selection.
     */
    public String getHotkey() {
        return hotkey;
    }

    /**
     * Determines whether there is a hotkey on this named selection.
     * <p>
     * The hotkey is used as a user-friendly method for quick retrieval of a
     * given named selection. There is guarantee of uniqueness.
     *
     * @return True if this named selection has a hotkey.
     */
    public boolean hasHotkey() {
        return hotkey != null;
    }

    /**
     * Sets the description of the named selection.
     * <p>
     * The description is used as a user-friendly method for further
     * identification or note taking purposes for a given named selection, and
     * appears in tooltips over the named selection in the browser UI.
     *
     * @param description The new description to be saved to the named
     * selection.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Gets the description of the named selection.
     * <p>
     * The description is used as a user-friendly method for further
     * identification or note taking purposes for a given named selection, and
     * appears in tooltips over the named selection in the browser UI.
     *
     * @return The description of this named selection.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the locked state of the named selection.
     * <p>
     * The lock is used to prevent edits being performed on the named selection.
     * Used as a safety mechanism.
     *
     * @param isLocked <code>true</code> to set the locked status for the named
     * selection.
     */
    public void setLocked(final boolean isLocked) {
        this.isLocked = isLocked;
    }

    /**
     * Gets the locked state of the named selection.
     * <p>
     * The lock is used to prevent edits being performed on the named selection.
     * Used as a safety mechanism.
     *
     * @return The locked state of this named selection.
     */
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        // Return either the set name, or a manufactured name with the friendly id:
        if (name != null) {
            sb.append(name);
        } else {
            sb.append(String.format("Selection %d", (id + 1)));
        }

        sb.append(String.format(" (id %d)", id));

        // Check if there is a hotkey and append this if there is:
        if (hotkey != null) {
            sb.append("  :  <Shortcut: 'Ctrl-");
            sb.append(hotkey);
            sb.append("'>");
        }

        return sb.toString();
    }

    /**
     * Helper function that generates default names using the default selection
     * name string (located in the bundle for localisation purposes, and the id
     * of the object.
     */
    private void setDefaultName() {
        name = Bundle.NamedSelection_DefaultSelectionName() + (id + 1);
    }
}
