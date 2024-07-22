# Add Hashmod

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
<td>Add Hashmod</td>
<td></td>
<td>Experimental -&gt; Tools -&gt; Add Hashmod</td>
<td style="text-align: center;"></td>
</tr>
</tbody>
</table>

## Introduction

This is an experimental utility that enables you to import nodes and 
attributes through .csv files. Importing of transactions do not currently work.

The .csv files should be in the following format:

(row 1) Header 1, Header 2, Header 3, Header 4, Header 5

(row 2) MyKey, MyValue1, MyValue2, MyValue3, MyValue4

etc..

Eg.

Geo.Country,Lat,Long,flag,Identity

albania,41.3,19.81666667,alb,3

american_samoa,-14.26666667,-170.7166667,ame,5


## Loading the file/files

To load a single csv file, click the “Single” button. A File chooser dialog 
will be displayed. Select the .csv file that you wish to load and open.
To load multiple files, enter a list of filepaths as a comma delimited string
into the textbox next to the option “CSV Chain Hashmod (list of CSV files)” and 
click the “Chained” button.

## Add vertices and attributes

The columns will be added as attributes (if not already existing).
You will be able to preview the first 3 headers/columns 
which will be automatically displayed in the Key Attribute, Value Attribute, 
and Value2 Attribute textbox once a file has been loaded.

Select what you wish to do with the data loaded from the files by checking on 
the checkbox next to the relevant option.

Create new Vertices if no match found: will add new a node/vertex for each 
new key if it does not already exist in the graph.
Create new attributes from Column Headers: will add a new attribute if it does
not already exist in the graph.

