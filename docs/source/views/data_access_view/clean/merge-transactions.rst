Merge transactions
------------------

Merge transactions in your graph together
`````````````````````````````````````````

This plugin is used to merge groups of transactions between the same two nodes which all occur within a specified time of each other. Transactions must have the same type (and, if it exists, activity) in order to be merged.

Note that the DateTime, Activity and Type attributes must be defined for transactions for this work.

Parameters
``````````

An explanation of the options are:

*Merge By*: Transactions will be merged based on this. Currently only Date/Time is supported

*Threshold*: Threshold (seconds)

*Merging Rule*: The rule deciding how attributes are merged

*Lead Transaction*: The rule deciding how to choose the lead transaction based on the following option:

* Latest Time - Once merged, the latest transaction will remain
* Earliest Time - Once merged, the earliest transaction will remain

*Selected Only*: Merge Only Selected Transactions


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin
