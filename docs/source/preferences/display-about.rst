Display preferences
-------------------

The display preferences are located at Tools -> Options -> CONSTELLATION.

Default display mode
````````````````````

The default display mode that new graphs open in.

3D mode allows graphs to be rotated around the x, y, and z axes. 2D mode only allows rotation around the z axis.

2D mode does not change graphs that have non-zero z values; three-dimensional graphs will remain three-dimensional. It is only rotation that is constrained.

Maximum transactions drawn
``````````````````````````

If there are up to the maximum number of transactions between two nodes (by default eight), they will be drawn as individual transactions. If there are more than this, the transactions will be drawn as edges.

Label drawing
`````````````

There are three options for drawing labels on graphs: Fast, mixed, and pretty.

* Fast drawing works best for graphs that contain predominantly text with non-cursive script, such as English. When fast drawing is used, the characters are drawn individually without worrying too much about layout. If a label contains right-to-left text, the Unicode Bidirectional Algorithm is applied to the label so text is displayed in the correct order. However, the label may not look very nice if it contains a cursive script such as Arabic.
* Pretty drawing applies layout algorithms to the text, so both non-cursive script (such as English) and cursive script (such as Arabic) look their best. However, this takes time to process, so displaying a new graph, or redisplaying a graph after some labels have changed, can take longer.
* Mixed drawing provides a compromise between fast and pretty drawing. If a label is determined to consist entirely of left-to-right text, fast drawing is used; otherwise pretty drawing is used. For English labels, the difference between fast and pretty is generally not noticeable. For labels using cursive script (such as Arabic), pretty drawing is required to represent the label correctly.

When changing from one option to another, a restart of CONSTELLATION is required for the change to take effect.

Graph Visibility Threshold
``````````````````````````

As you open larger graphs, depending on your hardware specifications, CONSTELLATION will slow down. This threshold allows you to hide the graph entirely so that you can use other tools like the histogram or table view for instance to modify the graph before you visualise the data as a graph.

For this threshold to be used, you need to set the visibility button to hidden -> |resources-hidden.png| on the graph toolbar. To ignore this threshold value set the visibility button to visible -> |resources-visible.png|

.. |resources-hidden.png| image:: resources-hidden.png
   :alt: hidden

.. |resources-visible.png| image:: resources-visible.png
   :alt: visible


.. help-id: au.gov.asd.tac.constellation.functionality.display
