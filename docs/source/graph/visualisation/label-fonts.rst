Label fonts
```````````

The "Label fonts" panel lets you configure the fonts used to draw node labels. This help describes label drawing when the "Use multiple font labels" box is checked.

CONSTELLATION uses the fonts installed on your workstation to draw node and connection labels on graphs. More than one font can be used, depending on the text being displayed.

To render a label, CONSTELLATION uses the following steps.

Divide the text into runs of a single direction.
::::::::::::::::::::::::::::::::::::::::::::::::

English text is read left-to-right. However, other text (such as Arabic and Hebrew) is read right-to-left. The text is scanned to see which direction it belongs to, and split into runs, such that each run contains text of a single direction.

Note that the Unicode bidirectional algorithm is not used to determine direction. Because the context is CONSTELLATION labels rather than general text, a much simpler algorithm is used.

Divide the direction runs into runs of a single font.
:::::::::::::::::::::::::::::::::::::::::::::::::::::

Text that runs in a single direction can require more than one font to be rendered. For example, text that consists of an English word followed by a Chinese word is entirely left-to-right, but the font used to draw the English text may not contain any Chinese characters. If this is the case, and assuming the correct fonts are available, the text will be split into two runs: one for each font.

Font selection is described below.

Type placement
::::::::::::::

If there is more than one run, and the final run is left-to-right and ends with a type (that is, ends with "<...>"), the text of the type is moved to the beginning of the text.

Font selection
``````````````

Use the text area in the "Label Fonts" panel to specify fonts, one per line, with more specific fonts first. The menus below the text area contain the available options.

Fonts can be specified using either their font name, or the name of the file containing the font. OpenType font files (``.otf``) and some TrueType font files (``.ttf``) must be specified using the filename.

On Windows, if an OTF font file name such as ``NotoSerifSC-Regular.otf`` is specified, the file will first be looked for in the user's local profile font directory ``%LOCALAPPDATA%/Microsoft/Windows/Fonts/NotoSerifSC-Regular.otf``, then the windows directory ``%WINDIR%/Fonts/NotoSerifSC-Regular.otf``.

An absolute filename (``C:/MyFonts/font1.ttf`` or ``/fonts/font1.ttf``) can be used to specify a font file.

Blank lines and lines beginning with ``#`` are ignored.

An example:

.. code-block:: text
  
              Courier New
              Noto Sans Arabic
              C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
  
This configuration first attempts to use the installed font ``Courier New``, then the installed font ``Noto Sans Arabic``, and finally the font in the file ``C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf``.

Font tags
:::::::::

Fonts can be tagged with additional information. Adding one of the tags ``BOLD``, ``ITALIC``, or ``BOLD_ITALIC`` will have the corresponding effect on the font.

.. code-block:: text
  
              Courier New,BOLD
              Noto Sans Arabic
              C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
  
This configuration is the same as the previous one, but uses ``Courier New Bold``.

Script tags
:::::::::::

Each Unicode codepoint has an associated script: for example, English uses Latin script, and Russian uses Cyrillic script.

The Arial font contains both Latin and Arabic script, but you may want use Arial for latin characters, and a different font (say, Noto Sans Arabic) for Arabic characters. You can do this by tagging a font with the scripts it should use or not use.

.. code-block:: text
  
              Arial,LATIN,COMMON
              Noto Sans Arabic
              C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
  
This configuration specifies that Arial will be used only for LATIN (ABC...) and COMMON (punctuation and other symbols) codepoints. This excludes Arabic codepoints, which will therefore be drawn using Noto Sans Arabic.

.. code-block:: text
  
              Arial,BOLD,!ARABIC
              Noto Sans Arabic
              C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
  
This is an alternative to the previous configuration: the "!" signifies a negation, so Arial Bold will not be used for Arabic codepoints, which will therefore be drawn using Noto Sans Arabic.

Fallback font
:::::::::::::

Regardless of the fonts that are specified, CONSTELLATION appends the Java font ``SansSerif`` to the font list. (On Windows this is typically Arial combined with some other fonts.) Even if the list is empty, or the fonts in the list cannot display all of the codepoints required for labels, CONSTELLATION will always attempt to use ``SansSerif``. This does not guarantee that all codepoints can be displayed

Validation
::::::::::

To ensure that your font configuration is correct, use the Validate button. A message will describe the problem if the configuration is incorrect.


.. help-id: au.gov.asd.tac.constellation.visual.opengl.labelfonts
