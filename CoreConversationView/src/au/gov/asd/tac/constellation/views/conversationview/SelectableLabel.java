/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities;
import java.lang.reflect.Field;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;

/**
 * A SelectableLabel is a TextArea that has been enhanced to provide similar
 * layout behaviour to a Label.
 * 
 * TODO: {@link TextArea#populateContextMenu) no longer exists, fix it.
 *
 * @author sirius
 */
public class SelectableLabel extends TextArea {

    private Node content = null;
    private TextAreaSkin skin = null;
    private List<MenuItem> contextMenuItems = null;

    private double cachedWidth = Double.MIN_VALUE;
    private double cachedPrefHeight = Double.MIN_VALUE;

    private double cachedHeight = Double.MIN_VALUE;
    private double cachedPrefWidth = Double.MIN_VALUE;

    private class SelectableLabelSkin extends TextAreaSkin {

        public SelectableLabelSkin(final TextArea textArea) {
            super(textArea);
        }

//        @Override
//        public void populateContextMenu(final ContextMenu contextMenu) {
//            super.populateContextMenu(contextMenu);
//            if (contextMenuItems != null) {
//                contextMenu.getItems().addAll(contextMenuItems);
//            }
//        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectableLabelSkin(this);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        if (cachedHeight != height) {
            if (skin == null) {
                stopScrolling();
            }
            cachedPrefWidth = content.prefWidth(height);
            cachedHeight = height;
        }

        return cachedPrefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        if (cachedWidth != width) {
            if (skin == null) {
                stopScrolling();
            }
            cachedPrefHeight = content.prefHeight(width);
            cachedWidth = width;
        }

        return cachedPrefHeight;
    }

    public void setSelectableText(String text) {
        cachedHeight = cachedWidth = Double.MIN_VALUE;
        super.setText(text);
    }

    /**
     * Constructor.
     *
     * @param text the label to use for the text. Styles will be copied from
     * this label.
     * @param wrapText specifies whether text wrapping is allowed in this label.
     * @param style the CSS style of the label. This can be null.
     * @param tipsPane the tooltip for the label. This can be null.
     * @param contextMenuItems a list of menu items to add to the context menu
     * of the label. This can be null.
     */
    public SelectableLabel(final String text, boolean wrapText, String style, final TooltipPane tipsPane, final List<MenuItem> contextMenuItems) {
        getStyleClass().add("selectable-label");
        setText(text == null ? "" : text);
        setWrapText(wrapText);
        setEditable(false);
        setPadding(Insets.EMPTY);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setMinHeight(USE_PREF_SIZE);

        if (style != null) {
            setStyle(style);
        }

        if (tipsPane != null) {
            TooltipUtilities.activateTextInputControl(this, tipsPane);
        }

        this.contextMenuItems = contextMenuItems;
    }

    private void stopScrolling() {
        try {
            content = lookup(".content");
            skin = (TextAreaSkin) getSkin();

            Field scrollPaneField = skin.getClass().getSuperclass().getDeclaredField("scrollPane");
            scrollPaneField.setAccessible(true);
            final ScrollPane scrollPane = (ScrollPane) scrollPaneField.get(skin);

            scrollPane.setPadding(Insets.EMPTY);

            Field hbarPolicyField = scrollPane.getClass().getDeclaredField("hbarPolicy");
            hbarPolicyField.setAccessible(true);
            hbarPolicyField.set(scrollPane, new NeverScrollBarPolicyProperty(scrollPane, HBAR_POLICY));

            Field vbarPolicyField = scrollPane.getClass().getDeclaredField("vbarPolicy");
            vbarPolicyField.setAccessible(true);
            vbarPolicyField.set(scrollPane, new NeverScrollBarPolicyProperty(scrollPane, VBAR_POLICY));

            Field hvalueField = scrollPane.getClass().getDeclaredField("hvalue");
            hvalueField.setAccessible(true);
            hvalueField.set(scrollPane, new ZeroDoubleProperty(scrollPane, "hvalue"));

            Field vvalueField = scrollPane.getClass().getDeclaredField("vvalue");
            vvalueField.setAccessible(true);
            vvalueField.set(scrollPane, new ZeroDoubleProperty(scrollPane, "vvalue"));

            Field viewportBoundsField = scrollPane.getClass().getDeclaredField("viewportBounds");
            viewportBoundsField.setAccessible(true);
            viewportBoundsField.set(scrollPane, new EmptyBoundingBoxProperty(scrollPane, "viewportBounds"));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private static final CssMetaData<ScrollPane, ScrollBarPolicy> HBAR_POLICY
            = new CssMetaData<ScrollPane, ScrollBarPolicy>("-fx-hbar-policy",
                    new EnumConverter<>(ScrollBarPolicy.class), ScrollBarPolicy.NEVER) {

        @Override
        public boolean isSettable(ScrollPane n) {
            return true;
        }

        @Override
        public StyleableProperty<ScrollBarPolicy> getStyleableProperty(ScrollPane n) {
            return (StyleableProperty<ScrollBarPolicy>) n.hbarPolicyProperty();
        }
    };

    private static final CssMetaData<ScrollPane, ScrollBarPolicy> VBAR_POLICY
            = new CssMetaData<ScrollPane, ScrollBarPolicy>("-fx-vbar-policy",
                    new EnumConverter<>(ScrollBarPolicy.class), ScrollBarPolicy.NEVER) {

        @Override
        public boolean isSettable(ScrollPane n) {
            return true;
        }

        @Override
        public StyleableProperty<ScrollBarPolicy> getStyleableProperty(ScrollPane n) {
            return (StyleableProperty<ScrollBarPolicy>) n.vbarPolicyProperty();
        }
    };

    private class NeverScrollBarPolicyProperty extends StyleableObjectProperty<ScrollBarPolicy> {

        private final ScrollPane bean;
        private final CssMetaData<ScrollPane, ScrollBarPolicy> css;

        public NeverScrollBarPolicyProperty(ScrollPane bean, CssMetaData<ScrollPane, ScrollBarPolicy> css) {
            super(ScrollBarPolicy.NEVER);
            this.bean = bean;
            this.css = css;
        }

        @Override
        public Object getBean() {
            return bean;
        }

        @Override
        public String getName() {
            return "hbarPolicy";
        }

        @Override
        public void set(ScrollBarPolicy newValue) {
            super.set(ScrollBarPolicy.NEVER);
        }

        @Override
        public ScrollBarPolicy get() {
            return ScrollBarPolicy.NEVER;
        }

        @Override
        public CssMetaData<? extends Styleable, ScrollBarPolicy> getCssMetaData() {
            return css;
        }
    }

    private class ZeroDoubleProperty extends SimpleDoubleProperty {

        public ZeroDoubleProperty(Object bean, String name) {
            super(bean, name);
        }

        @Override
        public double get() {
            return 0.0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(double newValue) {
            super.set(0.0);
        }
    }

    private static final BoundingBox EMPTY_BOUNDING_BOX = new BoundingBox(0, 0, 0, 0);

    private class EmptyBoundingBoxProperty extends SimpleObjectProperty<BoundingBox> {

        public EmptyBoundingBoxProperty(Object bean, String name) {
            super(bean, name, EMPTY_BOUNDING_BOX);
        }

        @Override
        public BoundingBox get() {
            return EMPTY_BOUNDING_BOX;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(BoundingBox newValue) {
            super.set(EMPTY_BOUNDING_BOX);
        }
    }
}
