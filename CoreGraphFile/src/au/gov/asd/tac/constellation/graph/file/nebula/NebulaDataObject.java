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
package au.gov.asd.tac.constellation.graph.file.nebula;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

/**
 * Nebula file type.
 * <p>
 * A nebula is a collection of graphs. Loading a nebula file will cause all of
 * the .star files in the same directory to be opened. The content of the nebula
 * file is irrelevant, it is just a marker file.
 * <p>
 * Created using right-click on project -&gt; New -&gt; File Type...; MIME Type
 * "application/x-nebula", extension "nebula".
 * <p>
 * Manually added "implements OpenCookie" so we can take over the open. Removed
 * "registerEditor" because we don't want a separate editor for nebula files.
 *
 * @author algol
 */
@Messages({"LBL_Nebula_LOADER=Files of Nebula"})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Nebula_LOADER",
        mimeType = "application/x-nebula",
        extension = {"nebula"},
        position = 0)
@DataObject.Registration(
        mimeType = "application/x-nebula",
        iconBase = "au/gov/asd/tac/constellation/graph/file/nebula/resources/nebula.png",
        displayName = "#LBL_Nebula_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/application/x-nebula/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class NebulaDataObject extends MultiDataObject implements OpenCookie {
    
    private static final Logger LOGGER = Logger.getLogger(NebulaDataObject.class.getName());

    /**
     * Filename extension for nebula files.
     */
    public static final String FILE_EXTENSION = FileExtensionConstants.NEBULA;

    // Remember nebula colors.
    private static final Map<String, Color> NEBULA_COLOR = new HashMap<>();

    public NebulaDataObject(final FileObject pf, final MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public void open() {
        final Properties props = new Properties();
        try (final FileReader reader = new FileReader(getPrimaryFile().getPath())) {
            props.load(reader);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        // Generate a color for the nebula marker.
        // First, see if the user has specified a color in the nebula file.
        Color c = null;
        final String cname = props.getProperty("colour") != null ? props.getProperty("colour") : props.getProperty("color");
        if (cname != null) {
            ConstellationColor cv = ConstellationColor.fromHtmlColor(cname);
            if (cv == null) {
                cv = ConstellationColor.getColorValue(cname);
            }
            if (cv != null) {
                c = cv.getJavaColor();
            }
        }

        // At this point, we should check to see if there are already any other graphs in this nebula open,
        // so we can use the existing color. However, we're a bit low-level here.
        // Therefore, we track the random colors as we create them,
        if (c == null) {
            c = NEBULA_COLOR.get(getPrimaryFile().getPath());
        }

        // Otherwise, create a random color for this nebula.
        if (c == null) {
            final float h = new SecureRandom().nextFloat();
            c = Color.getHSBColor(h, 0.5F, 0.95F);
            NEBULA_COLOR.put(getPrimaryFile().getPath(), c);
        }

        for (final Enumeration<DataObject> i = getFolder().children(); i.hasMoreElements();) {
            final DataObject dobj = i.nextElement();
            if (dobj instanceof GraphDataObject) {
                final GraphDataObject gdo = (GraphDataObject) dobj;
                gdo.setNebulaDataObject(this);
                gdo.setNebulaColor(c);
                GraphOpener.getDefault().openGraph(gdo);
            }
        }

        // Because we haven't registered an editor, a TopComponent won't open for this file (which is good).
        // However, the recent files stuff works by watching for opening TopComponents (which is bad).
        // So, do it manually.
        // FileObject.getPath() returns a path containing "/"; we need to convert it to local separators for RecentFiles.
        final String path = new File(getPrimaryFile().getPath()).getAbsolutePath();
        RecentFiles.addFile(path);
    }

    /**
     * Does the other NebulaDataObject have the same path as this?
     *
     * @param other Another NebulaDataObject.
     *
     * @return True if the other NebulaDataObject has the same path as this.
     */
    public boolean equalsPath(final NebulaDataObject other) {
        return other != null && other.getPrimaryFile().getPath().equals(getPrimaryFile().getPath());
    }

    /**
     * Convenience method to allow external callers to add a nebula to the
     * recent files list.
     *
     * @param f A File to be added to the recent files list.
     */
    public static void addRecent(final File f) {
        RecentFiles.addFile(f.getAbsolutePath());
    }
}
