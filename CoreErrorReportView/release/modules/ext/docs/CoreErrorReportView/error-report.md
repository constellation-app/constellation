# Error Report

<table class="table table-striped">
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>Keyboard Shortcut</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Error Report</td>
<td></td>
<td>Help -&gt; Error Report</td>
<td style="text-align: center;"><img src="../ext/docs/CoreErrorReportView/resources/error-report-default.png" alt="Error Report Icon" /></td>
</tr>
</tbody>
</table>

The error report view shows a log of the runtime errors experienced by 
Constellation. It also allows a user to disable popup notifications of errors 
at different severity levels.

Example:
<br />
<div style="text-align: center">
    <img height=500 src="../ext/docs/CoreErrorReportView/errorreport/resources/errorReport.png" alt="Error Report" />
</div>
<br />

## Report Settings
<div style="text-align: center">
    <img width=400 src="../ext/docs/CoreErrorReportView/resources/ReportSettings.png" alt="Report Settings" />
</div>
<br />

The "Allow Popups" options, when checked, allow errors message popups of the 
respective severity. If unchecked, an error of the corresponding severity will 
not appear as a popup.
The popup mode set in the Error Report View 
is still enforced, even if the view is closed. However, if the Error Report View 
has not been opened, the default popup settings will be used. The default 
settings allow SEVERE and WARNING level popups.

- Allow SEVERE Popups
- Allow WARNING Popups
- Allow INFO Popups
- Allow FINE Popups

The "Display Reports" options, when checked, allows error messages of the 
corresponding severity to appear in the Error Report Window. If unchecked, 
the message is simply hidden and can be brought back into view by checking the 
box again.

- Display SEVERE Reports
- Display WARNING Reports
- Display INFO Reports
- Display FINE Reports

<br />

## Popup Mode
<div style="text-align: center">
    <img width=400 src="../ext/docs/CoreErrorReportView/resources/PopupMode.png" alt="Popup Mode" />
</div>
<br />

The "Popup Mode" options dictate how popups are displayed and redisplayed.
When closing any popup, there is a 10 second "grace period" before another 
popup will be able to be displayed. 

- 0 : Never Show Popups
- 1 : Show one popup only - Only shows one popup at a time. When closed, reoccurences of that error wont be redisplayed
- 2 : Show one popup, redisplayable - Only shows one popup at a time. When closed, reoccurences of that error will be displayed
- 3 : Show one popup per source (max 5) - Shows one popup per source, with a maximum of 5 sources showing popups at a time. Reoccurences of closed errors will not be displayed again.
- 4 : Show one per source, redisplayable - Shows one popup per source, with a maximum of 5 sources showing popups at a time. Reoccurences of closed errors are displayed again.

<br />

## Minimise All Error Reports

<img src="../ext/docs/CoreErrorReportView/resources/minimize.png" alt="Minimize Icon" />

Minimises all items in the Error Report View.

<br />

## Maximise All Error Reports

<img src="../ext/docs/CoreErrorReportView/resources/maximize.png" alt="Maximize Icon" />

Maxmises all items in the Error Report View.

<br />

## Clear All Reports

Clears all items in the Error Report View. This cannot be undone.
