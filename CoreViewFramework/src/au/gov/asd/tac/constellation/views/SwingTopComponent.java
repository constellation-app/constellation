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
package au.gov.asd.tac.constellation.views;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javafx.application.Platform;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/**
 * A Generic Swing top component with graph listening enabled.
 *
 * @param <P> The class of {@link Component} used by this {@link TopComponent}
 * to display content.
 *
 * @author cygnus_x-1
 */
public abstract class SwingTopComponent<P extends Component> extends ListeningTopComponent<P> {

    protected JScrollPane scrollPane;

    /**
     * A SwingTopComponent will have a JScrollPane by default, as it cannot know
     * the expected layout of the given content. If you wish to remove the
     * horizontal scroll bar, you can override this method to return
     * JScrollPane.HORIZONTAL_SCROLLBAR_NEVER.
     *
     * @return an integer representing the desired horizontal scroll bar policy.
     */
    protected int getHorizontalScrollPolicy() {
        return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
    }

    /**
     * A SwingTopComponent will have a JScrollPane by default, as it cannot know
     * the expected layout of the given content. If you wish to remove the
     * vertical scroll bar, you can override this method to return
     * JScrollPane.HORIZONTAL_SCROLLBAR_NEVER.
     *
     * @return an integer representing the desired vertical scroll bar policy.
     */
    protected int getVerticalScrollPolicy() {
        return ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
    }

    @Override
    protected final void initContent() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(500, 500));
        Platform.setImplicitExit(false);
        SwingUtilities.invokeLater(() -> {
            this.content = createContent();

            scrollPane = new JScrollPane(content);
            scrollPane.setHorizontalScrollBarPolicy(getHorizontalScrollPolicy());
            scrollPane.setVerticalScrollBarPolicy(getVerticalScrollPolicy());

            updateFont();

            add(scrollPane, BorderLayout.CENTER);
        });
    }

    @Override
    protected void updateFont() {
        if (content != null) {
            SwingUtilities.invokeLater(() -> content.setFont(FontUtilities.getApplicationFont()));
        }
    }
}
