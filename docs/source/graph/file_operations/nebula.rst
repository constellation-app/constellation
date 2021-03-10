|---nebula-resources-nebula.png|    Nebula
------------------------------------------

A nebula is a loosely related collection of graphs that can be opened, saved, and closed together.

Creating and opening a nebula
`````````````````````````````

To create a nebula, save or copy some ``.star`` files into a directory of your choice. Then select the menu item File → New nebula..., which will ask you for a directory and name for a nebula file, and a colour to represent the nebula. The nebula will automatically be added to the File → Open Recent list. Alternatively, you can manually create a text file called ``*name*.nebula`` in that directory, where *name* can be any name you like.

Now you can open the nebula in the same way that you open a star file, using File → Open, dragging the nebula file from a file browser to the bar above the graph window, or the recent files list if you used "New nebula...". Constellation will look for star files in the same directory as the nebula file, and open them.

If a nebula does not have a colour assigned, it will be assigned a random colour. This colour will appear next to the graph icon in the tab at the top of the graph window for each graph that is a member of the nebula. This serves to distinguish the members of multiple nebulæ from each other. Each graph's tab tooltip will also be prepended with the name of the nebula file: the graph "MyGraph" that is in the "shiny" nebula will have the tooltip "shiny - *path*/MyGraph".

Functionality
`````````````

As well as being opened collectively, graphs in a nebula can be saved collectively. Right-click on the tab of any graph in the nebula: if any graphs have been modified, there will be a "Save nebula" option in the popup menu; selecting this will save each modified graph. If all of the graphs in the nebula have been saved, there will be a "Close nebula" option; selecting this will close all of the graphs in the nebula. Finally, there is always a "Discard nebula" option; selecting this will close all of the graphs in the nebula, whether they have been saved or not.

After a nebula has been opened, the individual graphs in that nebula remain individual graphs; there is no connection between them other than belonging to the same nebula. Any of the actions above could be done by individually acting on each graph; a nebula just makes these actions more efficient. Any of the star files can still be opened individually without opening the nebula file.

Colour assignment
`````````````````

You can assign your own colour to the nebula. Edit the nebula file using your favourite text editor and add a "``colour = *colour*``" line (or change the existing line). Any one of the following lines will assign red as the nebula colour.

.. code-block:: text
  
              colour = red
              colour = #ff0000
              colour = 1,0,0
  
(These are, respectively, a known colour name, an HTML colour, and an RGB colour where each element has the range 0..1.)

Another example: use any one of these to make the nebula marker teal.

.. code-block:: text
  
              colour = teal
              colour = #008080
              colour = 0, 0.5, 0.5
  
(Note: for those with a broader acceptance of spelling, "color" also works.)

.. |---nebula-resources-nebula.png| image:: ---nebula-resources-nebula.png
   :alt: Nebula


.. help-id: au.gov.asd.tac.constellation.graph.file.nebula
