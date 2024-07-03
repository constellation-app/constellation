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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.gui.DateChooserPanel;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ShortcutSupport;

/**
 * A {@link ConstellationInput} for managing {@link LocalDate} selection. 
 * This input provides the following {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Triggers a popup menu to select a {@link LocalDate} from a {@link DateChoserPannel}.</li>
 * <li>{@link ShortcutSupport} - Increments and decrements the data chronologically with up and down arrow.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * 
 * TODO: The {@link DateChooserPannel} is a little dated and could be updated to match the Constellation look and feel. 
 * Use SingleChoiceInput(ChoiceType.SINGLE_SPINNER) for month and year selection and a grid of dates to select a date.
 * 
 * @author capricornunicorn123
 */
public final class DateInput extends ConstellationInput<LocalDate> implements RightButtonSupport, ShortcutSupport {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @Override
            public String toString(final LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(final String s) {
                return StringUtils.isNotBlank(s) ? LocalDate.parse(s, DATE_FORMATTER) : null;
            }
        };
    
    public DateInput(){
        initialiseDepedantComponents();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Shortcut Support">   
    @Override
    public EventHandler<KeyEvent> getShortcuts() {
        return event -> {
            switch (event.getCode()){
                case UP -> this.nextDate();
                case DOWN -> this.prevDate();
            }
        };
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    /**
     * Gets the LocalDate represented by this input filed.
     * Achieved by parsing the text from the input field to a LocalDate
     * @return LocalDate representing the value of the input field
     */
    private LocalDate getLocalDate(){
        if (this.getText().isBlank()){
            return null;
        }
        return LocalDate.parse(this.getText(), DATE_FORMATTER);
    }
    
    /**
     * Gets the Date represented by this input filed.
     * Achieved by parsing the text from the input field to a Date
     * @return Date representing the value of the input field
     */
    private Date getDate(){
        return TemporalUtilities.localDateToDate(LocalDate.parse(this.getText(), DATE_FORMATTER));
    }
    
    /**
     * Sets this input filed based on a LocalDate object
     * @param date 
     */
    private void setDate(final LocalDate date){
        this.setText(converter.toString(date)); 
    }
    
    /**
     * Sets this input filed based on a Date object
     * @param date 
     */
    private void setDate(final Date date){
        this.setDate(TemporalUtilities.dateToLocalDate(date)); 
    }
    
    private void nextDate() {
        this.setValue(this.getValue().plusDays(1));
    }

    private void prevDate() {
        this.setValue(this.getValue().minusDays(1));
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public LocalDate getValue() {
        return getLocalDate();
    }

    @Override
    public void setValue(final LocalDate value) {
        this.setDate(value);
    }
    
    @Override
    public boolean isValid(){
        try{
            converter.fromString(getText());
            return true;
        } catch (DateTimeParseException ex){
           return false; 
        }
    }   
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final MenuItem format = new MenuItem("Select Date");
        format.setOnAction(value -> executeRightButtonAction());
        return Arrays.asList(format);
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public RightButton getRightButton() {
        return new RightButton(new Label(ConstellationInputConstants.SELECT_BUTTON_LABEL), Button.ButtonType.POPUP) {
                    @Override
                    public EventHandler<? super MouseEvent> action() {
                        return event -> executeRightButtonAction();
                    }
                };
    }
    
    @Override
    public void executeRightButtonAction() {
        final DateChooserPanel dc = new DateChooserPanel(this.getDate());
        final DialogDescriptor dialog = new DialogDescriptor(dc, "Select Date", true, null);
        final Integer result = (Integer) DialogDisplayer.getDefault().notify(dialog);
        if (result == 0) {
            setDate(dc.getSelectedDate()); 
        }       
    }
    // </editor-fold>  
}

