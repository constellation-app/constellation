package au.gov.asd.tac.constellation.visual.opengl.utilities;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.text.Element;

/**
 *
 * @author algol
 */
public final class NumberedTextArea extends JTextArea
{
    private final JTextArea textArea;

    public NumberedTextArea(final JTextArea textArea)
    {
        this.textArea = textArea;
        setBackground(Color.LIGHT_GRAY);
        setEditable(false);
    }

    public void updateLineNumbers()
    {
        setText(getLineNumbersText());
    }

    private String getLineNumbersText()
    {
        final int caretPosition = textArea.getDocument().getLength();
        final Element root = textArea.getDocument().getDefaultRootElement();
        final StringBuilder builder = new StringBuilder();
        builder.append("1").append(System.lineSeparator());

        for(int elementIndex=2; elementIndex<root.getElementIndex(caretPosition)+2; elementIndex++) {
            builder.append(elementIndex).append(System.lineSeparator());
        }

        return builder.toString();
    }
}