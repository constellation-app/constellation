Datetime range
--------------

The datetime range control allows you to enter a datetime range in either relative or absolute form. The relative section provides predetermined relative periods. The absolute section provides an unchanging from and to range.

A relative range indicates a period ending "now" and starting at the indicated time in the past. For example, if 2 days is selected, and a data access query is run at 2015-01-30 09:30, the range used for the query will be 2015-01-28 09:30:00 to 2015-01-30 09:30. If another query is run ten minutes later, the range will be 2015-01-28 09:40:00 to 2015-01-30 09:40.

This is also true if a relative range is saved and loaded at a later time: the datetime range will always end at "now".

Note that selecting a relative period will update the absolute range fields. However, this is done as an indication of the period when it was selected. Since the relative period ends "now", and is therefore continually changing, the values displayed in the absolute section are immediately obsolete.

An absolute range allows you to specify the start and end of the range as fixed instances in time. For example, if 2015-01-23 09:30 and 2015-01-30 09:30 are used, the same range will be used whenever a query is run.

The absolute range also allows to you select a timezone to be used to display the absolute time range. A drop-down list contains all available timezones, with convenience buttons for UTC and local timezones.

The *Use Maximum Time Range* button sets the time range to start at 00:00:00 and end at 23:59:59.

When the timezone changes, the time instant of each absolute datetime remains the same: only the timezone in which the datetimes are displayed is changed.

The date range is highlighted in green to indicate whether a relative or absolute range is being used.


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.GlobalCoreParameters
