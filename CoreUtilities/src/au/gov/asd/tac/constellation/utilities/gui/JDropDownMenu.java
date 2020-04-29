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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * A drop down menu for use with Swing.
 *
 * @param <E>
 *
 * @author cygnus_x-1
 */
public class JDropDownMenu<E> extends JComponent implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(JDropDownMenu.class.getName());

    private final JButton button;
    private final JPopupMenu menu;
    private final Map<JMenuItem, E> menuItems;
    private final Set<ActionListener> listeners;

    public JDropDownMenu(final String text, final List<E> items) {
        this(text, null, items);
    }

    public JDropDownMenu(final Icon icon, final List<E> items) {
        this(null, icon, items);
    }

    private JDropDownMenu(final String text, final Icon icon, final List<E> items) {
        assert text == null || icon == null : "JDropDownMenu does not support both text and an icon";

        setLayout(new OverlayLayout(this));

        this.menu = new JPopupMenu();
        this.menuItems = new HashMap<>();

        Icon arrow = null;
        try {
            arrow = new ImageIcon(Utilities.toURI(InstalledFileLocator.getDefault().locate("modules/ext/icons/drop_down_arrow.png", "au.gov.asd.tac.constellation.utilities", false)).toURL());
        } catch (MalformedURLException ex) {
            LOGGER.warning("Could not create drop down arrow image");
        }

        assert arrow != null;

        if (icon != null) {
            final Icon iconWithArrow = new CompoundIcon(icon, arrow);
            this.button = new JButton(iconWithArrow);
        } else {
            this.button = new JButton(text, arrow);
        }
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.addActionListener(event -> {
            if (!menu.isVisible()) {
                final Point p = button.getLocationOnScreen();
                menu.setInvoker(button);
                menu.setLocation((int) p.getX(), (int) p.getY() + button.getHeight());
                menu.setVisible(true);
            } else {
                menu.setVisible(false);
            }
        });
        add(button);

        listeners = new HashSet<>();

        items.forEach(item -> {
            final JMenuItem menuItem = new JMenuItem(item.toString());
            menuItem.addActionListener(new OpenAction(menu, button));
            menuItem.addActionListener(event -> {
                final JMenuItem changedMenuItem = (JMenuItem) event.getSource();
                final E changedItem = menuItems.get(changedMenuItem);
                listeners.forEach(listener -> {
                    final Thread thread = new Thread("JDropDownMenu Worker") {
                        @Override
                        public void run() {
                            final ActionEvent actionEvent = new ActionEvent(changedItem, ActionEvent.ACTION_PERFORMED, null);
                            listener.actionPerformed(actionEvent);
                        }
                    };
                    thread.start();
                });
            });
            menuItems.put(menuItem, item);
            menu.add(menuItem);
        });
    }

    public final String getText() {
        return button.getText();
    }

    public final void setText(final String text) {
        button.setText(text);
    }

    public final Icon getIcon() {
        return button.getIcon();
    }

    public final void setIcon(final Icon icon) {
        button.setIcon(icon);
    }

    public final Set<E> getItems() {
        return new HashSet<>(menuItems.values());
    }

    public final void setSelectedItem(final E item) {
        if (item != null) {
            menuItems.forEach((mi, i) -> {
                if (i.equals(item)) {
                    final ActionEvent selectionEvent = new ActionEvent(i, ActionEvent.ACTION_PERFORMED, null);
                    actionPerformed(selectionEvent);
                }
            });
        }
    }

    public final void addActionListener(final ActionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        listeners.forEach(listener -> {
            final Thread thread = new Thread("JDropDownMenu Worker") {
                @Override
                public void run() {
                    listener.actionPerformed(event);
                }
            };
            thread.start();
        });
    }

    @Override
    public void setToolTipText(final String text) {
        button.setToolTipText(text);
    }

    public static class OpenAction implements ActionListener {

        private final JPopupMenu menu;
        private final JButton button;

        public OpenAction(final JPopupMenu menu, final JButton button) {
            this.menu = menu;
            this.button = button;
        }

        @Override
        public final void actionPerformed(final ActionEvent event) {
            menu.show(button, 0, button.getHeight());
        }
    }
}
