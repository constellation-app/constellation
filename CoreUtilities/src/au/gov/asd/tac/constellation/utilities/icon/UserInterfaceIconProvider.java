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
package au.gov.asd.tac.constellation.utilities.icon;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining user interface icons.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class UserInterfaceIconProvider implements ConstellationIconProvider {

    public static final ConstellationIcon AXIS_X = new ConstellationIcon.Builder("Axis (x)", new FileIconData("modules/ext/icons/ui/axis_x.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon AXIS_X_NEGATIVE = new ConstellationIcon.Builder("Axis (-x)", new FileIconData("modules/ext/icons/ui/axis_x_negative.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon AXIS_Y = new ConstellationIcon.Builder("Axis (y)", new FileIconData("modules/ext/icons/ui/axis_y.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon AXIS_Y_NEGATIVE = new ConstellationIcon.Builder("Axis (-y)", new FileIconData("modules/ext/icons/ui/axis_y_negative.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon AXIS_Z = new ConstellationIcon.Builder("Axis (z)", new FileIconData("modules/ext/icons/ui/axis_z.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon AXIS_Z_NEGATIVE = new ConstellationIcon.Builder("Axis (-z)", new FileIconData("modules/ext/icons/ui/axis_z_negative.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon ADD = new ConstellationIcon.Builder("Add", new FileIconData("modules/ext/icons/ui/add.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon ADD_ALTERNATE = new ConstellationIcon.Builder("Add Alternate", new FileIconData("modules/ext/icons/ui/add_alternate.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon BLAZES = new ConstellationIcon.Builder("Blazes", new FileIconData("modules/ext/icons/ui/blazes.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon BOOKMARK = new ConstellationIcon.Builder("Bookmark", new FileIconData("modules/ext/icons/ui/bookmark.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHECK = new ConstellationIcon.Builder("Check", new FileIconData("modules/ext/icons/ui/check.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_DOWN = new ConstellationIcon.Builder("Chevron Down", new FileIconData("modules/ext/icons/ui/chevron_down.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_LEFT = new ConstellationIcon.Builder("Chevron Left", new FileIconData("modules/ext/icons/ui/chevron_left.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_LEFT_DOUBLE = new ConstellationIcon.Builder("Chevron Left Double", new FileIconData("modules/ext/icons/ui/chevron_left_double.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_RIGHT = new ConstellationIcon.Builder("Chevron Right", new FileIconData("modules/ext/icons/ui/chevron_right.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_RIGHT_DOUBLE = new ConstellationIcon.Builder("Chevron Right Double", new FileIconData("modules/ext/icons/ui/chevron_right_double.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CHEVRON_UP = new ConstellationIcon.Builder("Chevron Up", new FileIconData("modules/ext/icons/ui/chevron_up.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon COLUMNS = new ConstellationIcon.Builder("Columns", new FileIconData("modules/ext/icons/ui/columns.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CONNECTIONS = new ConstellationIcon.Builder("Connections", new FileIconData("modules/ext/icons/ui/connections.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CONNECTION_LABELS = new ConstellationIcon.Builder("Connection Labels", new FileIconData("modules/ext/icons/ui/connection_labels.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CONTRACT = new ConstellationIcon.Builder("Contract", new FileIconData("modules/ext/icons/ui/contract.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon COPY = new ConstellationIcon.Builder("Copy", new FileIconData("modules/ext/icons/ui/copy.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon CROSS = new ConstellationIcon.Builder("Cross", new FileIconData("modules/ext/icons/ui/cross.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DIRECTED = new ConstellationIcon.Builder("Directed", new FileIconData("modules/ext/icons/ui/directed.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DELETE = new ConstellationIcon.Builder("Delete", new FileIconData("modules/ext/icons/ui/delete.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DOWNLOAD = new ConstellationIcon.Builder("Download", new FileIconData("modules/ext/icons/ui/download.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DRAG_DROP = new ConstellationIcon.Builder("Drag Drop", new FileIconData("modules/ext/icons/ui/drag_drop.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DRAG_WORD = new ConstellationIcon.Builder("Drag Word", new FileIconData("modules/ext/icons/ui/drag_word.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon DRAW_MODE = new ConstellationIcon.Builder("Draw Mode", new FileIconData("modules/ext/icons/ui/draw_mode.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon EDGES = new ConstellationIcon.Builder("Edges", new FileIconData("modules/ext/icons/ui/edges.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon EDIT = new ConstellationIcon.Builder("Edit", new FileIconData("modules/ext/icons/ui/edit.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon ERROR = new ConstellationIcon.Builder("Error", new FileIconData("modules/ext/icons/ui/error.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon EXPAND = new ConstellationIcon.Builder("Expand", new FileIconData("modules/ext/icons/ui/expand.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HEART = new ConstellationIcon.Builder("Heart", new FileIconData("modules/ext/icons/ui/heart.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HELP = new ConstellationIcon.Builder("Help", new FileIconData("modules/ext/icons/ui/help.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HIDDEN = new ConstellationIcon.Builder("Hidden", new FileIconData("modules/ext/icons/ui/hidden.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HOP_FULL = new ConstellationIcon.Builder("Full Hop", new FileIconData("modules/ext/icons/ui/hop_full.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HOP_HALF = new ConstellationIcon.Builder("Half Hop", new FileIconData("modules/ext/icons/ui/hop_half.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon HOP_ONE = new ConstellationIcon.Builder("One Hop", new FileIconData("modules/ext/icons/ui/hop_one.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon INFORMATION = new ConstellationIcon.Builder("Information", new FileIconData("modules/ext/icons/ui/information.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon KEY = new ConstellationIcon.Builder("Key", new FileIconData("modules/ext/icons/ui/key.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon LABELS = new ConstellationIcon.Builder("Labels", new FileIconData("modules/ext/icons/ui/labels.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon LINKS = new ConstellationIcon.Builder("Links", new FileIconData("modules/ext/icons/ui/links.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon LOCK = new ConstellationIcon.Builder("Lock", new FileIconData("modules/ext/icons/ui/lock.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon MENU = new ConstellationIcon.Builder("Menu", new FileIconData("modules/ext/icons/ui/menu.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon MODE_2D = new ConstellationIcon.Builder("2D", new FileIconData("modules/ext/icons/ui/2d.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon MODE_3D = new ConstellationIcon.Builder("3D", new FileIconData("modules/ext/icons/ui/3d.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon NODES = new ConstellationIcon.Builder("Nodes", new FileIconData("modules/ext/icons/ui/nodes.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon NODE_LABELS = new ConstellationIcon.Builder("Node Labels", new FileIconData("modules/ext/icons/ui/node_labels.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon OPEN = new ConstellationIcon.Builder("Open", new FileIconData("modules/ext/icons/ui/open.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon PLAY = new ConstellationIcon.Builder("Play", new FileIconData("modules/ext/icons/ui/play.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon REFRESH = new ConstellationIcon.Builder("Refresh", new FileIconData("modules/ext/icons/ui/refresh.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon REMOVE = new ConstellationIcon.Builder("Remove", new FileIconData("modules/ext/icons/ui/remove.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon REMOVE_ALTERNATE = new ConstellationIcon.Builder("Remove Alternate", new FileIconData("modules/ext/icons/ui/remove_alternate.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon REPORT = new ConstellationIcon.Builder("Report", new FileIconData("modules/ext/icons/ui/report.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon SEARCH = new ConstellationIcon.Builder("Search", new FileIconData("modules/ext/icons/ui/search.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon SELECT_MODE = new ConstellationIcon.Builder("Select Mode", new FileIconData("modules/ext/icons/ui/select_mode.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon SHARE = new ConstellationIcon.Builder("Share", new FileIconData("modules/ext/icons/ui/share.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon SORT = new ConstellationIcon.Builder("Sort", new FileIconData("modules/ext/icons/ui/sort.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon TAG = new ConstellationIcon.Builder("Tag", new FileIconData("modules/ext/icons/ui/tag.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon TRANSACTIONS = new ConstellationIcon.Builder("Transactions", new FileIconData("modules/ext/icons/ui/transactions.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon UNDIRECTED = new ConstellationIcon.Builder("Undirected", new FileIconData("modules/ext/icons/ui/undirected.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon UNLOCK = new ConstellationIcon.Builder("Unlock", new FileIconData("modules/ext/icons/ui/unlock.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon UPLOAD = new ConstellationIcon.Builder("Upload", new FileIconData("modules/ext/icons/ui/upload.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon VISIBLE = new ConstellationIcon.Builder("Visible", new FileIconData("modules/ext/icons/ui/visible.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon WARNING = new ConstellationIcon.Builder("Warning", new FileIconData("modules/ext/icons/ui/warning.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon ZOOM_IN = new ConstellationIcon.Builder("Zoom In", new FileIconData("modules/ext/icons/ui/zoom_in.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();
    public static final ConstellationIcon ZOOM_OUT = new ConstellationIcon.Builder("Zoom Out", new FileIconData("modules/ext/icons/ui/zoom_out.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("User Interface")
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        final List<ConstellationIcon> userInterfaceIcons = new ArrayList<>();
        userInterfaceIcons.add(AXIS_X_NEGATIVE);
        userInterfaceIcons.add(AXIS_Y_NEGATIVE);
        userInterfaceIcons.add(AXIS_Z_NEGATIVE);
        userInterfaceIcons.add(AXIS_X);
        userInterfaceIcons.add(AXIS_Y);
        userInterfaceIcons.add(AXIS_Z);
        userInterfaceIcons.add(ADD);
        userInterfaceIcons.add(ADD_ALTERNATE);
        userInterfaceIcons.add(BLAZES);
        userInterfaceIcons.add(BOOKMARK);
        userInterfaceIcons.add(CHECK);
        userInterfaceIcons.add(CHEVRON_DOWN);
        userInterfaceIcons.add(CHEVRON_LEFT);
        userInterfaceIcons.add(CHEVRON_LEFT_DOUBLE);
        userInterfaceIcons.add(CHEVRON_RIGHT);
        userInterfaceIcons.add(CHEVRON_RIGHT_DOUBLE);
        userInterfaceIcons.add(CHEVRON_UP);
        userInterfaceIcons.add(COLUMNS);
        userInterfaceIcons.add(CONNECTIONS);
        userInterfaceIcons.add(CONNECTION_LABELS);
        userInterfaceIcons.add(CONTRACT);
        userInterfaceIcons.add(COPY);
        userInterfaceIcons.add(CROSS);
        userInterfaceIcons.add(DELETE);
        userInterfaceIcons.add(DIRECTED);
        userInterfaceIcons.add(DOWNLOAD);
        userInterfaceIcons.add(DRAG_DROP);
        userInterfaceIcons.add(DRAG_WORD);
        userInterfaceIcons.add(DRAW_MODE);
        userInterfaceIcons.add(EDGES);
        userInterfaceIcons.add(EDIT);
        userInterfaceIcons.add(ERROR);
        userInterfaceIcons.add(EXPAND);
        userInterfaceIcons.add(HEART);
        userInterfaceIcons.add(HELP);
        userInterfaceIcons.add(HIDDEN);
        userInterfaceIcons.add(HOP_FULL);
        userInterfaceIcons.add(HOP_HALF);
        userInterfaceIcons.add(HOP_ONE);
        userInterfaceIcons.add(INFORMATION);
        userInterfaceIcons.add(KEY);
        userInterfaceIcons.add(LABELS);
        userInterfaceIcons.add(LINKS);
        userInterfaceIcons.add(LOCK);
        userInterfaceIcons.add(MENU);
        userInterfaceIcons.add(MODE_2D);
        userInterfaceIcons.add(MODE_3D);
        userInterfaceIcons.add(NODES);
        userInterfaceIcons.add(NODE_LABELS);
        userInterfaceIcons.add(OPEN);
        userInterfaceIcons.add(PLAY);
        userInterfaceIcons.add(REFRESH);
        userInterfaceIcons.add(REMOVE);
        userInterfaceIcons.add(REMOVE_ALTERNATE);
        userInterfaceIcons.add(REPORT);
        userInterfaceIcons.add(SEARCH);
        userInterfaceIcons.add(SELECT_MODE);
        userInterfaceIcons.add(SHARE);
        userInterfaceIcons.add(SORT);
        userInterfaceIcons.add(TAG);
        userInterfaceIcons.add(TRANSACTIONS);
        userInterfaceIcons.add(UNDIRECTED);
        userInterfaceIcons.add(UNLOCK);
        userInterfaceIcons.add(UPLOAD);
        userInterfaceIcons.add(VISIBLE);
        userInterfaceIcons.add(WARNING);
        userInterfaceIcons.add(ZOOM_IN);
        userInterfaceIcons.add(ZOOM_OUT);
        return userInterfaceIcons;
    }
}
