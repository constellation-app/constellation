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
package au.gov.asd.tac.constellation.graph.utilities.widgets;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.FileIconData;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 * @author algol
 */
public final class IconChooser extends javax.swing.JPanel implements TreeSelectionListener, ListSelectionListener {

    private final Set<ConstellationIcon> icons;
    private final boolean iconAdded = false;

    public IconChooser(final Set<ConstellationIcon> icons, final String selectedIconName) {
        initComponents();

        // Make a copy of the iconMap so we don't alter the original.
        this.icons = new HashSet<>();
        this.icons.addAll(icons);
        init(selectedIconName);
        iconsList.getSelectionModel().addListSelectionListener(this);
    }

    public Set<ConstellationIcon> getIconMap() {
        return icons;
    }

    /**
     * Initialise the icon tree.
     *
     * @param selectedIconName The initially selected icon.
     */
    private void init(final String selectedIconName) {
        ArrayList<IconTreeFolder> selectedPath = null;
        IconTreeFolder selectedFolder = null;
        String selectedPart = null;

        final IconFoldersTreeModel treeModel = new IconFoldersTreeModel();
        for (ConstellationIcon icon : icons) {
            ArrayList<IconTreeFolder> path = new ArrayList<>();
            String[] parts = icon.getExtendedName().split("\\.");

            IconTreeFolder tf = (IconTreeFolder) treeModel.getRoot();
            path.add(tf);
            for (int i = 0; i < parts.length - 1; i++) {
                final String part = parts[i];

                int childIx = treeModel.getIndexOfChild(tf, part);
                if (childIx == -1) {
                    final IconTreeFolder l = new IconTreeFolder(part);
                    tf.children.add(l);
                    Collections.sort(tf.children);
                }

                childIx = treeModel.getIndexOfChild(tf, part);
                tf = (IconTreeFolder) treeModel.getChild(tf, childIx);
                path.add(tf);
            }

            tf.icons.put(parts[parts.length - 1], icon);

            // If this is the selected icon, remember the details so we can present this icon as the default choice.
            // If the selected icon's name is an alias, then since an alias is just the last part of the dotted path,
            // the path will end with "."+alias.
            if (selectedIconName != null && (icon.getName().equals(selectedIconName)
                    || icon.getExtendedName().endsWith(SeparatorConstants.PERIOD + selectedIconName))) {
                selectedPath = path;
                selectedFolder = tf;
                selectedPart = parts[parts.length - 1];
            }
        }

        iconFolders.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        iconFolders.addTreeSelectionListener(this);

        iconFolders.setModel(treeModel);
        if (selectedFolder == null) {
            selectedFolder = (IconTreeFolder) treeModel.getRoot();
            selectedPath = new ArrayList<>();
            selectedPath.add(selectedFolder);
        }
        if (selectedFolder != null && selectedPath != null) {
            final TreePath path = new TreePath(selectedPath.toArray(new IconTreeFolder[selectedPath.size()]));
            iconFolders.expandPath(path);
            iconFolders.scrollPathToVisible(path);
            iconFolders.setSelectionPath(path);

            iconsList.setModel(new IconListModel(selectedFolder.icons));
            iconsList.setSelectedValue(new IconListElement(selectedPart, null), true);
        }
    }

    public String getSelectedIconName() {
        final String name;
        final int index = iconsList.getSelectedIndex();
        if (index != -1) {
            final IconListModel listModel = (IconListModel) iconsList.getModel();
            final IconListElement element = listModel.getElementAt(index);
            if (element.iconValue.buildByteArray() == null) {
                name = null;
            } else {
                StringBuilder fnam = new StringBuilder();
                final TreePath path = iconFolders.getSelectionPath();
                for (int i = 0; i < path.getPathCount(); i++) {
                    if (fnam.length() > 0) {
                        fnam.append(SeparatorConstants.PERIOD);
                    }

                    // Don't include the root name: it's just there to be selected in the JTree.
                    fnam.append(i > 0 ? path.getPathComponent(i) : "");
                }

                if (fnam.length() > 0) {
                    fnam.append(SeparatorConstants.PERIOD);
                }
                name = fnam.toString() + element.name;
            }
        } else {
            name = null;
        }

        return name;
    }

    public boolean isIconAdded() {
        return iconAdded;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        iconsList = new javax.swing.JList<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        iconFolders = new javax.swing.JTree();
        saveButton = new javax.swing.JButton();

        addButton.setText(org.openide.util.NbBundle.getMessage(IconChooser.class, "IconChooser.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText(org.openide.util.NbBundle.getMessage(IconChooser.class, "IconChooser.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        iconsList.setBackground(new java.awt.Color(0, 0, 0));
        iconsList.setForeground(new java.awt.Color(255, 255, 255));
        iconsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        iconsList.setCellRenderer(new IconListCellRenderer());
        jScrollPane2.setViewportView(iconsList);

        jScrollPane1.setViewportView(iconFolders);

        saveButton.setText(org.openide.util.NbBundle.getMessage(IconChooser.class, "IconChooser.saveButton.text")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeButton)
                    .addComponent(addButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add an icon to the icon list.
        String addedIcon = null;

        final FileChooserBuilder fChooser = new FileChooserBuilder(IconChooser.class)
                .setTitle("Add icons")
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final int extlen = 4;
                        final String name = pathName.getName().toLowerCase();
                        if (pathName.isFile() && StringUtils.endsWithAny(name, new String[]{FileExtensionConstants.JPG_EXTENSION, FileExtensionConstants.PNG_EXTENSION})) {
                            final String label = name.substring(0, name.length() - extlen);

                            // The name must contain at least one category (a '.' in position 1 or greater).
                            return label.indexOf('.') > 0;
                        }

                        return pathName.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Graph Icon";
                    }
                });

        final File[] files = fChooser.showMultiOpenDialog();
        if (files != null) {
            for (File file : files) {
                // The name must contain at least one category (a '.' in position 1 or greater).
                final int extlen = 4;
                final String fnam = file.getName();
                final String label = fnam.substring(0, fnam.length() - extlen);
                if (label.indexOf('.') < 1) {
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(String.format("Icon name %s must contain categories separated by '.'", fnam), NotifyDescriptor.ERROR_MESSAGE);
                    nd.setTitle("Bad icon file name");
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    ConstellationIcon customIcon = new ConstellationIcon.Builder(fnam, new FileIconData(file)).build();
                    if (IconManager.addIcon(customIcon)) {
                        addedIcon = label;
                    }
                }
            }
        }

        if (addedIcon != null) {
            icons.clear();
            icons.addAll(IconManager.getIcons());
            init(addedIcon);
        }
}//GEN-LAST:event_addButtonActionPerformed

private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Remove an icon from the icon list.
        final int index = iconsList.getSelectedIndex();
        if (index != -1) {
            final IconListModel listModel = (IconListModel) iconsList.getModel();
            listModel.remove(index);
            final IconListElement element = listModel.getElementAt(index);
            final TreePath path = iconFolders.getSelectionPath();
            if (path != null) {
//            final IconFoldersTreeModel treeModel = (IconFoldersTreeModel)iconFolders.getModel();
                final IconTreeFolder folder = (IconTreeFolder) path.getLastPathComponent();
                folder.removeChild(new IconTreeFolder(element.name));
            }
        }
}//GEN-LAST:event_removeButtonActionPerformed

private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // Save an icon from the icon list.
        final String iconName = getSelectedIconName();
        if (iconName != null) {
            final FileChooserBuilder fChooser = new FileChooserBuilder(IconChooser.class)
                    .setTitle("Save icon");
//        final File file = fChooser.showSaveDialog();

            // We need to get a JFileChooser because FileChooserBuilder doesn't have setSelectedFile().
            final JFileChooser chooser = fChooser.createFileChooser();

            chooser.setSelectedFile(new File(iconName + FileExtensionConstants.PNG_EXTENSION));
            final int result = chooser.showSaveDialog(this);
            final File file = result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;

            if (file != null) {
                try {
                    final IconListModel listModel = (IconListModel) iconsList.getModel();
                    final int index = iconsList.getSelectedIndex();
                    final IconListElement element = listModel.getElementAt(index);

                    try (final FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(element.iconValue.buildByteArray());
                        fos.flush();
                    }
                } catch (IOException ex) {
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(String.format("Error writing icon file %s:%n%s", file.toString(), ex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                    nd.setTitle("Icon file error");
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        }
}//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTree iconFolders;
    private javax.swing.JList<IconListElement> iconsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        final IconTreeFolder tf = (IconTreeFolder) iconFolders.getLastSelectedPathComponent();
        if (tf != null) {
            iconsList.setModel(new IconListModel(tf.icons));
        }

        // Don't allow the icon list at the root to be modified.
        // Allow any other icon lists to be modified.
        final boolean isRoot = tf == iconFolders.getModel().getRoot();
        final boolean hasIcons = iconsList.getModel().getSize() > 0;
        saveButton.setEnabled(false);
        removeButton.setEnabled(!isRoot && hasIcons);
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        saveButton.setEnabled(true);
        removeButton.setEnabled(true);
    }
}

class IconTreeFolder implements Comparable<IconTreeFolder> {

    protected String name;
    protected ArrayList<IconTreeFolder> children;
    protected TreeMap<String, ConstellationIcon> icons;

    public IconTreeFolder(final String name) {
        this.name = name;
        children = new ArrayList<>();
        icons = new TreeMap<>();
    }

    public void removeChild(final IconTreeFolder child) {
        children.remove(child);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        return this.getClass() == other.getClass() && ((IconTreeFolder) other).name.equals(name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(final IconTreeFolder o) {
        return name.compareTo(o.name);
    }
}

class IconFoldersTreeModel implements TreeModel {

    final IconTreeFolder root;

    public IconFoldersTreeModel() {
        root = new IconTreeFolder("(Built-in)");
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        final IconTreeFolder level = (IconTreeFolder) parent;

        return level.children.get(index);
    }

    @Override
    public int getChildCount(final Object parent) {
        final IconTreeFolder level = (IconTreeFolder) parent;

        return level.children.size();
    }

    @Override
    public boolean isLeaf(final Object node) {
        return false;
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // required for implementation of TreeModel
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        if (parent == null || child == null) {
            return -1;
        }

        final IconTreeFolder level = (IconTreeFolder) parent;
        final String levelChild = (String) child;
        for (int i = 0; i < level.children.size(); i++) {
            if (level.children.get(i).name.equals(levelChild)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        // required for implementation of TreeModel
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        // required for implementation of TreeModel
    }

}

class IconListElement {

    public final String name;
    public final ConstellationIcon iconValue;

    public IconListElement(final String name, final ConstellationIcon iconValue) {
        this.name = name;
        this.iconValue = iconValue;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        return this.getClass() == other.getClass() && ((IconListElement) other).name.equals(name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}

class IconListModel implements ListModel<IconListElement> {

    private final ArrayList<String> names;
    private final ArrayList<ConstellationIcon> iconValue;
    private final ArrayList<ListDataListener> listeners;

    public IconListModel(final TreeMap<String, ConstellationIcon> icons) {
        names = new ArrayList<>(icons.size());
        iconValue = new ArrayList<>(icons.size());
        for (String part : icons.navigableKeySet()) {
            names.add(part);
            iconValue.add(icons.get(part));
        }

        listeners = new ArrayList<>();
    }

    public void remove(final int index) {
        names.remove(index);
        iconValue.remove(index);
        for (ListDataListener l : listeners) {
            l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
        }
    }

    @Override
    public int getSize() {
        return names.size();
    }

    @Override
    public IconListElement getElementAt(final int index) {
        return new IconListElement(names.get(index), iconValue.get(index));
    }

    @Override
    public void addListDataListener(final ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(final ListDataListener l) {
        listeners.remove(l);
    }
}

class IconListCellRenderer extends JLabel implements ListCellRenderer<IconListElement> {

    @Override
    public Component getListCellRendererComponent(final JList<? extends IconListElement> list, final IconListElement value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        final IconListElement el = value;
        final boolean isIcon = el.iconValue != null && el.iconValue.buildByteArray().length > 0;
        final ImageIcon ii = isIcon ? new ImageIcon(el.iconValue.buildByteArray()) : null;
        setText(isIcon ? String.format("%s (%dx%d)", el.name, ii.getIconWidth(), ii.getIconHeight()) : el.name);
        setIcon(ii);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);

        return this;
    }
}
