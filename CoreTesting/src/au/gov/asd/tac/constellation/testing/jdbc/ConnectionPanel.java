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
package au.gov.asd.tac.constellation.testing.jdbc;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 * @author algol
 */
public class ConnectionPanel extends JPanel {
//    private static final String USERNAME_KEY = "username";
//    private static final String CONNECTION_KEY = "connection";
//    private static final String JAR_KEY = "jar";
//    private static final String DRIVER_KEY = "driver";

    private static final String EXT = ".jar";

    private final Graph graph;

    // If the user has loaded a saved set of parameters, store it here.
    // The controller can fetch it and update the settings for the other panels.
    private JdbcData data;

    /**
     * Creates new form Panel1
     */
    public ConnectionPanel(final Graph graph) {
        initComponents();

        findJarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/testing/jdbc/driver.png"))); // NOI18N

        this.graph = graph;
        data = null;
    }

    public String getUsername() {
        return usernameText.getText().trim();
    }

    public void setUsername(final String username) {
        usernameText.setText(username);
    }

    public char[] getPassword() {
        return passwordText.getPassword();
    }

    public void setPassword(final char[] password) {
        passwordText.setText(new String(password));
    }

    public String getConnectionUrl() {
        return connectionText.getText().trim();
    }

    public void setConnectionUrl(final String url) {
        connectionText.setText(url);
    }

    public String getJarFile() {
        return jarText.getText().trim();
    }

    public void setJarFile(final String jarfile) {
        jarText.setText(jarfile);
        getDrivers(jarfile);
    }

    public String getDriverName() {
        return (String) driverCombo.getSelectedItem();
    }

    public void setDriverName(final String driverName) {
        driverCombo.setSelectedItem(driverName);
    }

    public JdbcData getData() {
        final JdbcData tmp = data;
        data = null;

        return tmp;
    }

    @Override
    public String getName() {
        return "JDBC connection";
    }

    private void getDrivers(final String jarfile) {
        try {
            final ArrayList<String> driverList = new ArrayList<>();
            if (jarfile != null && !jarfile.isEmpty()) {
                try (final JarFile jf = new JarFile(jarfile)) {
                    final ZipEntry ze = jf.getEntry("META-INF/services/java.sql.Driver");
                    if (ze != null) {
                        // Find the possible JDBC driver classes the nice way.
                        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(jf.getInputStream(ze), StandardCharsets.UTF_8.name()))) {
                            while (true) {
                                final String line = reader.readLine();
                                if (line == null) {
                                    break;
                                }

                                driverList.add(line);
                            }
                        }
                    } else {
                        // The JAR file hasn't told us what the possible driver classes are,
                        // so do it the hard way.
                        final URL[] searchPath = new URL[]{new URL("file:///" + jarfile)};
                        final ClassLoader clloader = new URLClassLoader(searchPath);
                        for (final Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
                            final JarEntry je = e.nextElement();
                            final String classname = je.getName();
                            if (classname.endsWith(".class")) {
                                try {
                                    // Remove ".class", convert '/' to '.' to create a proper class name.
                                    final int len = classname.length();
                                    final String name = classname.substring(0, len - 6).replace('/', '.');
                                    final Class<?> cl = clloader.loadClass(name);
                                    if (Driver.class.isAssignableFrom(cl)) {
                                        driverList.add(name);
                                    }
                                } catch (ClassNotFoundException ex) {
                                    // Not a valid class; ignore it.
                                }
                            }
                        }
                    }
                }
            }

            final String[] drivers = driverList.toArray(new String[driverList.size()]);
            driverCombo.setModel(new DefaultComboBoxModel<>(drivers));

//            final Preferences prefs = NbPreferences.forModule(JdbcData.class);
//            final String currentDriver = prefs.get(JAR_KEY, "");
//            if(!currentDriver.isEmpty())
//            {
//                driverCombo.setSelectedItem(currentDriver);
//            }
        } catch (IOException ex) {
            driverCombo.setModel(new DefaultComboBoxModel<>(new String[0]));

            final String msg = String.format("Not a valid JAR file:%n%s", ex.getMessage());
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        findJarButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        driverCombo = new javax.swing.JComboBox<String>();
        loadButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        usernameText = new javax.swing.JTextField();
        connectionText = new javax.swing.JTextField();
        passwordText = new javax.swing.JPasswordField();
        jarText = new javax.swing.JTextField();
        txSqlButton = new javax.swing.JButton();
        vxSqlButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(400, 446));

        findJarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/testing/jdbc/driver.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(findJarButton, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.findJarButton.text")); // NOI18N
        findJarButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.findJarButton.toolTipText")); // NOI18N
        findJarButton.setIconTextGap(0);
        findJarButton.setPreferredSize(new java.awt.Dimension(49, 20));
        findJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findJarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jLabel5.text")); // NOI18N

        driverCombo.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.driverCombo.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(loadButton, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.loadButton.text")); // NOI18N
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jLabel4.text")); // NOI18N

        usernameText.setText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.usernameText.text")); // NOI18N
        usernameText.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.usernameText.toolTipText")); // NOI18N

        connectionText.setText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.connectionText.text")); // NOI18N
        connectionText.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.connectionText.toolTipText")); // NOI18N

        passwordText.setText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.passwordText.text")); // NOI18N
        passwordText.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.passwordText.toolTipText")); // NOI18N

        jarText.setEditable(false);
        jarText.setText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jarText.text")); // NOI18N
        jarText.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.jarText.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(txSqlButton, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.txSqlButton.text")); // NOI18N
        txSqlButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.txSqlButton.toolTipText")); // NOI18N
        txSqlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txSqlButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(vxSqlButton, org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.vxSqlButton.text")); // NOI18N
        vxSqlButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionPanel.class, "ConnectionPanel.vxSqlButton.toolTipText")); // NOI18N
        vxSqlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vxSqlButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectionText)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jarText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(findJarButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(passwordText)
                                    .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 168, Short.MAX_VALUE))
                            .addComponent(driverCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(txSqlButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(loadButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(vxSqlButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(connectionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jarText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findJarButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(driverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 248, Short.MAX_VALUE)
                .addComponent(vxSqlButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadButton)
                    .addComponent(txSqlButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void findJarButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_findJarButtonActionPerformed
    {//GEN-HEADEREND:event_findJarButtonActionPerformed
        final FileChooserBuilder fChooser = new FileChooserBuilder("ExportJDbc")
                .setTitle("JDBC JAR file")
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName().toLowerCase();
                        if (pathName.isFile() && name.toLowerCase().endsWith(EXT)) {
                            return true;
                        }

                        return pathName.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "JAR file";
                    }
                });

        final File file = fChooser.showOpenDialog();
        if (file != null) {
            getDrivers(file.getPath());
            final String fnam = file.getAbsolutePath();
            jarText.setText(fnam);

        }
    }//GEN-LAST:event_findJarButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadButtonActionPerformed
    {//GEN-HEADEREND:event_loadButtonActionPerformed
        data = new JdbcData();
        JdbcParameterIO.loadParameters(data);
        setUsername(data.username);
        setConnectionUrl(data.url);
        setJarFile(data.jar);
        setDriverName(data.driverName);

        passwordText.requestFocusInWindow();
    }//GEN-LAST:event_loadButtonActionPerformed

    private void vxSqlButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_vxSqlButtonActionPerformed
    {//GEN-HEADEREND:event_vxSqlButtonActionPerformed
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final Set<Integer> keys = new HashSet<>();
            for (final int k : rg.getPrimaryKey(GraphElementType.VERTEX)) {
                keys.add(k);
            }

            final String sql = createSql(rg, GraphElementType.VERTEX, "vertices", keys);
            final InfoTextPanel itp = new InfoTextPanel(sql);
            final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(itp);
            msg.setTitle("Create vertex SQL");
            DialogDisplayer.getDefault().notify(msg);
        } finally {
            rg.release();
        }
    }//GEN-LAST:event_vxSqlButtonActionPerformed

    private void txSqlButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txSqlButtonActionPerformed
    {//GEN-HEADEREND:event_txSqlButtonActionPerformed
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final Set<Integer> keys = new HashSet<>();
            for (final int k : rg.getPrimaryKey(GraphElementType.TRANSACTION)) {
                keys.add(k);
            }

            final String sql = createSql(rg, GraphElementType.TRANSACTION, "transactions", keys);
            final InfoTextPanel itp = new InfoTextPanel(sql);
            final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(itp);
            msg.setTitle("Create transaction SQL");
            DialogDisplayer.getDefault().notify(msg);
        } finally {
            rg.release();
        }
    }//GEN-LAST:event_txSqlButtonActionPerformed

    private static String createSql(final GraphReadMethods rg, final GraphElementType etype, final String tableName, final Set<Integer> keys) {
        final StringBuilder buf = new StringBuilder();
        buf.append(String.format("CREATE TABLE %s%n(%n", tableName));
        if (etype == GraphElementType.VERTEX) {
            buf.append(String.format("    %-16s %s,%n", "vx_id_", sqlType(IntegerAttributeDescription.ATTRIBUTE_NAME)));
        } else {
            buf.append(String.format("    %-16s %s,%n", "tx_id_", sqlType(IntegerAttributeDescription.ATTRIBUTE_NAME)));
            buf.append(String.format("    %-16s %s,%n", "vx_src_", sqlType(IntegerAttributeDescription.ATTRIBUTE_NAME)));
            buf.append(String.format("    %-16s %s,%n", "vx_dst_", sqlType(IntegerAttributeDescription.ATTRIBUTE_NAME)));
            buf.append(String.format("    %-16s %s,%n", "tx_dir_", sqlType(BooleanAttributeDescription.ATTRIBUTE_NAME)));
        }

        final ArrayList<String> lines = new ArrayList<>();
        final int attrCount = rg.getAttributeCount(etype);
        for (int position = 0; position < attrCount; position++) {
            final int attrId = rg.getAttribute(etype, position);
            final Attribute attr = new GraphAttribute(rg, attrId);
            final String line = String.format("    %-16s %s,%n", JdbcUtilities.canonicalLabel(attr.getName(), false), sqlType(attr.getAttributeType()));
            lines.add(line);
        }

        lines.sort(String::compareTo);
        lines.stream().forEach((line) -> {
            buf.append(line);
        });

        if (etype == GraphElementType.VERTEX) {
            buf.append("    PRIMARY KEY (vx_id_)%n");
        } else {
            buf.append("    PRIMARY KEY (tx_id_)%n");
        }
        buf.append(");");

        return buf.toString();
    }

    private static String sqlType(final String attrType) {
        switch (attrType) {
            case BooleanAttributeDescription.ATTRIBUTE_NAME:
                return "INT";
            case DateAttributeDescription.ATTRIBUTE_NAME:
                return "DATE";
            case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME:
                return "DATETIME";
            case FloatAttributeDescription.ATTRIBUTE_NAME:
                return "FLOAT";
            case IntegerAttributeDescription.ATTRIBUTE_NAME:
                return "INT";
            case TimeAttributeDescription.ATTRIBUTE_NAME:
                return "TIME";
            default:
                return "TEXT";
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JTextField connectionText;
    javax.swing.JComboBox<String> driverCombo;
    javax.swing.JButton findJarButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField jarText;
    private javax.swing.JButton loadButton;
    javax.swing.JPasswordField passwordText;
    private javax.swing.JButton txSqlButton;
    javax.swing.JTextField usernameText;
    javax.swing.JButton vxSqlButton;
    // End of variables declaration//GEN-END:variables

}
