## Label fonts

The "Label fonts" panel lets you configure the fonts used to draw node
labels. This help describes label drawing when the "Use multiple font
labels" box is checked.

CONSTELLATION uses the fonts installed on your workstation to draw node
and connection labels on graphs. More than one font can be used,
depending on the text being displayed.

To render a label, CONSTELLATION uses the following steps.

### Divide the text into runs of a single direction.

English text is read left-to-right. However, other text (such as Arabic
and Hebrew) is read right-to-left. The text is scanned to see which
direction it belongs to, and split into runs, such that each run
contains text of a single direction.

Note that the Unicode bidirectional algorithm is not used to determine
direction. Because the context is CONSTELLATION labels rather than
general text, a much simpler algorithm is used.

### Divide the direction runs into runs of a single font.

Text that runs in a single direction can require more than one font to
be rendered. For example, text that consists of an English word followed
by a Chinese word is entirely left-to-right, but the font used to draw
the English text may not contain any Chinese characters. If this is the
case, and assuming the correct fonts are available, the text will be
split into two runs: one for each font.

Font selection is described below.

### Type placement

If there is more than one run, and the final run is left-to-right and
ends with a type (that is, ends with "&lt;...&gt;"), the text of the
type is moved to the beginning of the text.

## Font selection

Use the text area in the "Label Fonts" panel to specify fonts, one per
line, with more specific fonts first. The menus below the text area
contain the available options.

Fonts can be specified using either their font name, or the name of the
file containing the font. OpenType font files (<span
class="mono">.otf</span>) and some TrueType font files (<span
class="mono">.ttf</span>) must be specified using the filename.

On Windows, if an OTF font file name such as <span
class="mono">NotoSerifSC-Regular.otf</span> is specified, the file will
first be looked for in the user's local profile font directory <span
class="mono">%LOCALAPPDATA%/Microsoft/Windows/Fonts/NotoSerifSC-Regular.otf</span>,
then the windows directory <span
class="mono">%WINDIR%/Fonts/NotoSerifSC-Regular.otf</span>.

An absolute filename (<span class="mono">C:/MyFonts/font1.ttf</span> or
<span class="mono">/fonts/font1.ttf</span>) can be used to specify a
font file.

Blank lines and lines beginning with <span class="mono">\#</span> are
ignored.

An example:

                Courier New
                Noto Sans Arabic
                C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
            

This configuration first attempts to use the installed font <span
class="mono">Courier New</span>, then the installed font <span
class="mono">Noto Sans Arabic</span>, and finally the font in the file
<span
class="mono">C:\\Users\\User1\\Downloads\\Fonts\\NotoSerifCJKtc-Regular.otf</span>.

### Font tags

Fonts can be tagged with additional information. Adding one of the tags
<span class="mono">BOLD</span>, <span class="mono">ITALIC</span>, or
<span class="mono">BOLD\_ITALIC</span> will have the corresponding
effect on the font.

                Courier New,BOLD
                Noto Sans Arabic
                C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
            

This configuration is the same as the previous one, but uses <span
class="mono">Courier New Bold</span>.

### Script tags

Each Unicode codepoint has an associated script: for example, English
uses Latin script, and Russian uses Cyrillic script.

The Arial font contains both Latin and Arabic script, but you may want
use Arial for latin characters, and a different font (say, Noto Sans
Arabic) for Arabic characters. You can do this by tagging a font with
the scripts it should use or not use.

                Arial,LATIN,COMMON
                Noto Sans Arabic
                C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
            

This configuration specifies that Arial will be used only for LATIN
(ABC...) and COMMON (punctuation and other symbols) codepoints. This
excludes Arabic codepoints, which will therefore be drawn using Noto
Sans Arabic.

                Arial,BOLD,!ARABIC
                Noto Sans Arabic
                C:\Users\User1\Downloads\Fonts\NotoSerifCJKtc-Regular.otf
            

This is an alternative to the previous configuration: the "!" signifies
a negation, so Arial Bold will not be used for Arabic codepoints, which
will therefore be drawn using Noto Sans Arabic.

### Fallback font

Regardless of the fonts that are specified, CONSTELLATION appends the
Java font <span class="mono">SansSerif</span> to the font list. (On
Windows this is typically Arial combined with some other fonts.) Even if
the list is empty, or the fonts in the list cannot display all of the
codepoints required for labels, CONSTELLATION will always attempt to use
<span class="mono">SansSerif</span>. This does not guarantee that all
codepoints can be displayed

### Validation

To ensure that your font configuration is correct, use the Validate
button. A message will describe the problem if the configuration is
incorrect.
