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
package au.gov.asd.tac.constellation.graph.utilities.widgets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * GUI Class that is used to handle input of DateTime based queries in
 * conjunction with the <code>FindTopComponent</code> /
 * <code>FindCriteriaPanel</code> classes.
 *
 * @author betelgeuse
 * @see au.gov.asd.tac.constellation.views.find.gui.FindTopComponent
 * @see au.gov.asd.tac.constellation.views.find.gui.FindCriteriaPanel
 */
@Messages({
    "DateTimeFormat=yyyy-MM-dd HH:mm:ss"
})
public class DateTimeRangePanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(DateTimeRangePanel.class.getName());

    private final DateTimeListenerInterface parentPanel;
    private Calendar firstDate = null;
    private Calendar secondDate = null;
    private Date minDate = null;
    private Date maxDate = null;

    /**
     * Creates new form <code>DateTimeCriteriaPanel</code>.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public DateTimeRangePanel(final DateTimeListenerInterface parent) {
        this.parentPanel = parent;
        initialise();
    }

    private void initialise() {
        if (firstDate == null) {
            firstDate = new GregorianCalendar();
        }
        if (secondDate == null) {
            secondDate = new GregorianCalendar();
        }
        initComponents();

        final JSpinner.DateEditor de1 = new JSpinner.DateEditor(spnDateTime1, Bundle.DateTimeFormat());
        spnDateTime1.setEditor(de1);

        final JSpinner.DateEditor de2 = new JSpinner.DateEditor(spnDateTime2, Bundle.DateTimeFormat());
        spnDateTime2.setEditor(de2);

        presetDateRanges.removeAllItems();
        presetDateRanges.addItem(Bundle.PresetRange0());
        presetDateRanges.addItem(Bundle.PresetRange1());
        presetDateRanges.addItem(Bundle.PresetRange2());
        presetDateRanges.addItem(Bundle.PresetRange3());
        presetDateRanges.addItem(Bundle.PresetRange4());
        presetDateRanges.addItem(Bundle.PresetRange5());
        presetDateRanges.addItem(Bundle.PresetRange6());
        presetDateRanges.setSelectedItem(Bundle.PresetRange0());

    }

    /**
     * Constructs a new <code>DateTimeCriteriaPanel</code> with prefilled
     * content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param firstDateTime The value to place in the form's first input box.
     * @param secondDateTime The value to place in the form's second input box.
     * @param isBetween Whether or not the second input box should be shown.
     *
     * @see Calendar
     */
    public DateTimeRangePanel(final DateTimeListenerInterface parent, final Calendar firstDateTime,
            final Calendar secondDateTime, final boolean isBetween) {
        this(parent, firstDateTime, secondDateTime, null, null, isBetween);
    }

    /**
     * Constructs a new <code>DateTimeCriteriaPanel</code> with prefilled
     * content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param firstDateTime The value to place in the form's first input box.
     * @param secondDateTime The value to place in the form's second input box.
     * @param minDateTime minimum allowed date/time
     * @param maxDateTime minimum allowed date/tim
     * @param isBetween Whether or not the second input box should be shown.
     *
     * @see Calendar
     */
    public DateTimeRangePanel(final DateTimeListenerInterface parent, final Calendar firstDateTime,
            final Calendar secondDateTime, final Calendar minDateTime, final Calendar maxDateTime, final boolean isBetween) {
        this.parentPanel = parent;
        this.firstDate = firstDateTime;
        this.secondDate = secondDateTime;
        if (minDateTime == null || maxDateTime == null) {
            this.minDate = new Date(Long.MIN_VALUE);
            this.maxDate = new Date(Long.MAX_VALUE);
        } else {
            this.minDate = minDateTime.getTime();
            this.maxDate = maxDateTime.getTime();
        }
        initialise();
        this.setUIState(isBetween);
    }

    /**
     * Returns the current state of this panel's first input box.
     *
     * @return The value of the first input box.
     *
     * @see Calendar
     */
    public Calendar getFirstDateTime() {
        return fixDateTimeZoneError((Date) spnDateTime1.getValue());
    }

    /**
     * Returns the current state of this panel's second input box.
     *
     * @return The value of the first input box.
     *
     * @see Calendar
     */
    public Calendar getSecondDateTime() {
        return fixDateTimeZoneError((Date) spnDateTime2.getValue());
    }

    /**
     * Method that adjusts the form to handle multiple DateTime inputs.
     *
     * @param isBetween <code>true</code> if form should show the extended input
     * controls, <code>false</code> if it should not.
     */
    public void setUIState(final boolean isBetween) {
        lblDateTime1.setVisible(isBetween);
        spnDateTime2.setVisible(isBetween);
        calendarButton2.setVisible(isBetween);
        presetDateRanges.setVisible(isBetween);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spnDateTime1 = new javax.swing.JSpinner();
        calendarButton1 = new javax.swing.JButton();
        lblDateTime1 = new javax.swing.JLabel();
        spnDateTime2 = new javax.swing.JSpinner();
        calendarButton2 = new javax.swing.JButton();
        presetDateRanges = new javax.swing.JComboBox<>();

        setOpaque(false);

        spnDateTime1.setModel(new SpinnerDateModel(firstDate.getTime(), minDate, maxDate, Calendar.DAY_OF_MONTH));

        calendarButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/widgets/resources/calendar.png"))); // NOI18N
        calendarButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        calendarButton1.setPreferredSize(new java.awt.Dimension(24, 24));
        calendarButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarButton1ActionPerformed(evt);
            }
        });

        lblDateTime1.setText(org.openide.util.NbBundle.getMessage(DateTimeRangePanel.class, "DateTimeRangePanel.lblDate1.text")); // NOI18N

        spnDateTime2.setModel(new SpinnerDateModel(secondDate.getTime(), minDate, maxDate, Calendar.DAY_OF_MONTH));

        calendarButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/widgets/resources/calendar.png"))); // NOI18N
        calendarButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        calendarButton2.setPreferredSize(new java.awt.Dimension(24, 24));
        calendarButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarButton2ActionPerformed(evt);
            }
        });

        presetDateRanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetDateRangesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spnDateTime1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calendarButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblDateTime1)
                .addGap(18, 18, 18)
                .addComponent(spnDateTime2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calendarButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(presetDateRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(191, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spnDateTime1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(presetDateRanges)
                .addComponent(calendarButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(spnDateTime2)
                .addComponent(lblDateTime1)
                .addComponent(calendarButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void calendarButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarButton1ActionPerformed
        final Date date = this.getFirstDate();
        DateChooserPanel dc = new DateChooserPanel(date);
        final DialogDescriptor dialog = new DialogDescriptor(dc, "Select Date", true, null);
        Integer result = (Integer) DialogDisplayer.getDefault().notify(dialog);
        if (result == 0) {
            spnDateTime1.setValue(dc.getSelectedDate());
        }
    }//GEN-LAST:event_calendarButton1ActionPerformed

    private void calendarButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarButton2ActionPerformed
        final Date date = this.getSecondDate();
        DateChooserPanel dc = new DateChooserPanel(date);
        final DialogDescriptor dialog = new DialogDescriptor(dc, "Select Date", true, null);
        Integer result = (Integer) DialogDisplayer.getDefault().notify(dialog);
        if (result == 0) {
            spnDateTime2.setValue(dc.getSelectedDate());
        }
    }//GEN-LAST:event_calendarButton2ActionPerformed

    private void presetDateRangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetDateRangesActionPerformed
        String value = (String) (presetDateRanges.getSelectedItem());
        if (value.equals(Bundle.PresetRange0())) {
            spnDateTime1.setEnabled(true);
            spnDateTime2.setEnabled(true);
            calendarButton1.setEnabled(true);
            calendarButton2.setEnabled(true);
        } else if (value.equals(Bundle.PresetRange1())) {
            setPresetDates(Calendar.DAY_OF_MONTH, -3);
        } else if (value.equals(Bundle.PresetRange2())) {
            setPresetDates(Calendar.DAY_OF_MONTH, -7);
        } else if (value.equals(Bundle.PresetRange3())) {
            setPresetDates(Calendar.MONTH, -1);
        } else if (value.equals(Bundle.PresetRange4())) {
            setPresetDates(Calendar.MONTH, -3);
        } else if (value.equals(Bundle.PresetRange5())) {
            setPresetDates(Calendar.DAY_OF_MONTH, -6);
        } else if (value.equals(Bundle.PresetRange6())) {
            setPresetDates(Calendar.YEAR, -1);
        } else {
            // Do nothing
        }

    }//GEN-LAST:event_presetDateRangesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calendarButton1;
    private javax.swing.JButton calendarButton2;
    private javax.swing.JLabel lblDateTime1;
    private javax.swing.JComboBox<String> presetDateRanges;
    private javax.swing.JSpinner spnDateTime1;
    private javax.swing.JSpinner spnDateTime2;
    // End of variables declaration//GEN-END:variables

    /**
     * Helper method to ensure that DateTimes are handled in UTC time.
     *
     * @param date The date to modify to UTC time.
     * @return <code>Calendar</code> object representing the corrected DateTime.
     *
     * @see Date
     */
    private Calendar fixDateTimeZoneError(final Date date) {
        final Calendar cal = new GregorianCalendar();

        final SimpleDateFormat sdf = new SimpleDateFormat(Bundle.DateTimeFormat());
        sdf.setTimeZone(TimeZone.getDefault());

        final String formatted = sdf.format(date);

        try {
            cal.setTime(sdf.parse(formatted));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        return cal;
    }

    /**
     * Returns the current state of this panel's first input box.
     *
     * @return The value of the first input box.
     *
     * @see Date
     */
    public Date getFirstDate() {
        return (Date) spnDateTime1.getValue();
    }

    /**
     * Returns the current state of this panel's second input box.
     *
     * @return The value of the first input box.
     *
     * @see Date
     */
    public Date getSecondDate() {
        return (Date) spnDateTime2.getValue();
    }

    /**
     * disable and set the field range values
     *
     * @param field the field used to do the adjustment
     * @param amount the amount to change
     */
    private void setPresetDates(final int field, final int amount) {
        spnDateTime1.setEnabled(false);
        spnDateTime2.setEnabled(false);
        calendarButton1.setEnabled(false);
        calendarButton2.setEnabled(false);
        Calendar entry = new GregorianCalendar();
        Calendar start = new GregorianCalendar(entry.get(Calendar.YEAR), entry.get(Calendar.MONTH), entry.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Calendar end = new GregorianCalendar(entry.get(Calendar.YEAR), entry.get(Calendar.MONTH), entry.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        start.add(field, amount);
        spnDateTime1.setValue(start.getTime());
        spnDateTime2.setValue(end.getTime());
    }

    @Override
    public void setEnabled(final boolean flag) {
        calendarButton1.setEnabled(flag);
        calendarButton2.setEnabled(flag);
        lblDateTime1.setEnabled(flag);
        presetDateRanges.setEnabled(flag);
        spnDateTime1.setEnabled(flag);
        spnDateTime2.setEnabled(flag);
    }
}
