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

import au.gov.asd.tac.constellation.utilities.gui.DateChooserPanel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * GUI Class that is used to handle input of <code>Date</code> based queries in
 * conjunction with the <code>FindTopComponent</code> /
 * <code>FindCriteriaPanel</code> classes.
 *
 * @author betelgeuse
 * @see au.gov.asd.tac.constellation.views.find.gui.FindTopComponent
 * @see au.gov.asd.tac.constellation.views.find.gui.FindCriteriaPanel
 */
@NbBundle.Messages({
    "DateFormat=yyyy-MM-dd",
    "PresetRange0=Custom",
    "PresetRange1=Last 3 days",
    "PresetRange2=Last week",
    "PresetRange3=Last month",
    "PresetRange4=Last 3 months",
    "PresetRange5=Last 6 months",
    "PresetRange6=Last year"
})
public class DateRangePanel extends javax.swing.JPanel {

    private JSpinner.DateEditor de1;
    private JSpinner.DateEditor de2;
    private Date firstDate = null;
    private Date secondDate = null;
    private Date minDate = null;
    private Date maxDate = null;

    /**
     * Creates a new form <code>DateCriteriaPanel</code> with no prefilled
     * content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public DateRangePanel(final DateTimeListenerInterface parent) {
        initialise();
    }

    private void initialise() {
        if (firstDate == null) {
            firstDate = new Date();
        }
        if (secondDate == null) {
            secondDate = new Date();
        }

        initComponents();

        de1 = new JSpinner.DateEditor(spnDate1, Bundle.DateFormat());
        spnDate1.setEditor(de1);

        de2 = new JSpinner.DateEditor(spnDate2, Bundle.DateFormat());
        spnDate2.setEditor(de2);

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

    public String getFirstRawData() {
        return de1.getTextField().getText();
    }

    public String getSecondRawData() {
        return de2.getTextField().getText();
    }

    public SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(Bundle.DateFormat());
    }

    /**
     * Constructs a new <code>DateCriteriaPanel</code> with prefilled content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param firstDate The value to place in the form's first input box.
     * @param secondDate The value to place in the form's second input box.
     * @param isBetween Whether or not the second input box should be shown.
     *
     * @see Date
     */
    public DateRangePanel(final DateTimeListenerInterface parent, final Date firstDate,
            final Date secondDate, final boolean isBetween) {
        this(parent, firstDate, secondDate, null, null, isBetween);
    }

    /**
     * Constructs a new <code>DateCriteriaPanel</code> with prefilled content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param firstDate The value to place in the form's first input box.
     * @param secondDate The value to place in the form's second input box.
     * @param minDate minimum allowed date
     * @param maxDate maximum allowed date
     * @param isBetween Whether or not the second input box should be shown.
     *
     * @see Date
     */
    public DateRangePanel(final DateTimeListenerInterface parent, final Date firstDate,
            final Date secondDate, final Date minDate, final Date maxDate, final boolean isBetween) {
        this.firstDate = firstDate;
        this.secondDate = secondDate;
        if (minDate == null || maxDate == null) {
            this.minDate = new Date(Long.MIN_VALUE);
            this.maxDate = new Date(Long.MAX_VALUE);
        } else {
            this.minDate = minDate;
            this.maxDate = maxDate;
        }
        initialise();
        this.setUIState(isBetween);
    }

    /**
     * Returns the current state of this panel's first input box.
     *
     * @return The value of the first input box.
     *
     * @see Date
     */
    public Date getFirstDate() {
        return (Date) spnDate1.getValue();
    }

    /**
     * Returns the current state of this panel's second input box.
     *
     * @return The value of the first input box.
     *
     * @see Date
     */
    public Date getSecondDate() {
        return (Date) spnDate2.getValue();
    }

    /**
     * Method that adjusts the form to handle multiple <code>Date</code> input.
     *
     * @param isBetween <code>true</code> if form should show the extended input
     * controls, <code>false</code> if it should not.
     */
    public final void setUIState(final boolean isBetween) {
        lblDate1.setVisible(isBetween);
        spnDate2.setVisible(isBetween);
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

        spnDate1 = new javax.swing.JSpinner();
        calendarButton1 = new javax.swing.JButton();
        lblDate1 = new javax.swing.JLabel();
        spnDate2 = new javax.swing.JSpinner();
        calendarButton2 = new javax.swing.JButton();
        presetDateRanges = new javax.swing.JComboBox<>();

        setOpaque(false);

        spnDate1.setModel(new SpinnerDateModel(firstDate, minDate, maxDate, Calendar.DAY_OF_MONTH));

        calendarButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/widgets/resources/calendar.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(calendarButton1, org.openide.util.NbBundle.getMessage(DateRangePanel.class, "DateRangePanel.calendarButton1.text")); // NOI18N
        calendarButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        calendarButton1.setPreferredSize(new java.awt.Dimension(24, 24));
        calendarButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarButton1ActionPerformed(evt);
            }
        });

        lblDate1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lblDate1, org.openide.util.NbBundle.getMessage(DateRangePanel.class, "DateRangePanel.lblDate1.text")); // NOI18N

        spnDate2.setModel(new SpinnerDateModel(secondDate, minDate, maxDate, Calendar.DAY_OF_MONTH));

        calendarButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/au/gov/asd/tac/constellation/graph/utilities/widgets/resources/calendar.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(calendarButton2, org.openide.util.NbBundle.getMessage(DateRangePanel.class, "DateRangePanel.calendarButton2.text")); // NOI18N
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
                .addGap(0, 0, 0)
                .addComponent(spnDate1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calendarButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblDate1)
                .addGap(18, 18, 18)
                .addComponent(spnDate2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calendarButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(presetDateRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(175, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(presetDateRanges, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(calendarButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(calendarButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spnDate2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblDate1)))))
                    .addComponent(spnDate1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void calendarButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarButton1ActionPerformed
        final Date date = this.getFirstDate();
        DateChooserPanel dc = new DateChooserPanel(date);
        final DialogDescriptor dialog = new DialogDescriptor(dc, "Select Date", true, null);
        Integer result = (Integer) DialogDisplayer.getDefault().notify(dialog);
        if (result == 0) {
            spnDate1.setValue(dc.getSelectedDate());
        }
    }//GEN-LAST:event_calendarButton1ActionPerformed

    private void calendarButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarButton2ActionPerformed
        final Date date = this.getSecondDate();
        DateChooserPanel dc = new DateChooserPanel(date);
        final DialogDescriptor dialog = new DialogDescriptor(dc, "Select Date", true, null);
        Integer result = (Integer) DialogDisplayer.getDefault().notify(dialog);
        if (result == 0) {
            spnDate2.setValue(dc.getSelectedDate());
        }
    }//GEN-LAST:event_calendarButton2ActionPerformed

    private void presetDateRangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetDateRangesActionPerformed
        String value = (String) (presetDateRanges.getSelectedItem());
        if (value.equals(Bundle.PresetRange0())) {
            spnDate1.setEnabled(true);
            spnDate2.setEnabled(true);
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
    private javax.swing.JLabel lblDate1;
    private javax.swing.JComboBox<String> presetDateRanges;
    private javax.swing.JSpinner spnDate1;
    private javax.swing.JSpinner spnDate2;
    // End of variables declaration//GEN-END:variables

    /**
     * disable and set the field range values
     *
     * @param field the field used to do the adjustment
     * @param amount the amount to change
     */
    private void setPresetDates(final int field, final int amount) {
        spnDate1.setEnabled(false);
        spnDate2.setEnabled(false);
        calendarButton1.setEnabled(false);
        calendarButton2.setEnabled(false);
        Calendar entry = new GregorianCalendar();
        Calendar start = new GregorianCalendar(entry.get(Calendar.YEAR), entry.get(Calendar.MONTH), entry.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Calendar end = new GregorianCalendar(entry.get(Calendar.YEAR), entry.get(Calendar.MONTH), entry.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        start.add(field, amount);
        spnDate1.setValue(start.getTime());
        spnDate2.setValue(end.getTime());
    }

    @Override
    public void setEnabled(final boolean flag) {
        calendarButton1.setEnabled(flag);
        calendarButton2.setEnabled(flag);
        lblDate1.setEnabled(flag);
        presetDateRanges.setEnabled(flag);
        spnDate1.setEnabled(flag);
        spnDate2.setEnabled(flag);
    }
}
