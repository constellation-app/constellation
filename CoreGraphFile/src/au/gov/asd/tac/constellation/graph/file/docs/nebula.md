# Nebula

A nebula is a loosely related collection of graphs that can be opened,
saved, and closed together.

## Creating and Opening a Nebula

To create a nebula, save or copy some .star files into a directory of
your choice. Then select the menu item Experimental -> Tools -> New
Nebula, which will ask you for a directory and name for a nebula file,
and a colour to represent the nebula. The nebula will automatically be
added to the File -> Open Recent list. Alternatively, you can manually
create a text file called "name.nebula" in that directory, where "name"
can be any name you like.

You can open the nebula in the same way that you open a .star file. When
opened, Constellation will look for .star files in the same directory as
the nebula file, and open them.

If a nebula does not have a colour assigned, it will be assigned a
random colour. This colour will appear next to the graph icon in the tab
at the top of the graph window for each graph that is a member of the
nebula. This serves to distinguish the members of multiple nebulae from
each other. Each graph's tab tooltip will also be prepended with the
name of the nebula file: the graph "MyGraph" that is in the "shiny"
nebula will have the tooltip "shiny - \<PATH>/MyGraph".

## Functionality

If you right-click on the tab of any graph in the nebula, there are a
few options for nebulas in the resulting popup menu that may be
available:

-   "Save nebula" will be available if any graphs in the nebula have
    been modified. Selecting this will save each modified graph.
-   "Close nebula" will be available if all graphs in the nebula have
    been saved. Selecting this will close all of the graphs in the
    nebula.
-   "Discard nebula" will always be available. Selecting this will close
    all graphs in the nebula, whether they have been saved or not.

After a nebula has been opened, the individual graphs in that nebula
remain individual graphs; there is no connection between them other than
belonging to the same nebula. Any of the actions above could be done by
individually acting on each graph; a nebula just makes these actions
more efficient. Any of the .star files can still be opened individually
without opening the nebula file.

## Colour Assignment

You can assign your own colour to the nebula. Edit the nebula file using
your favourite text editor and add a "colour = \<COLOUR>" line (or
change the existing line). \<COLOUR> can be a colour name (e.g. red or
teal), a HTML colour (e.g. #ff0000 or #008080), or an RGB colour with
each element having a value between 0 and 1 (e.g. 1,0,0 or 0,0.5,0.5).

NOTE: "color" as a spelling also works
