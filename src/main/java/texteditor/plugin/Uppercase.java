package texteditor.plugin;

import texteditor.ClipboardStack;
import texteditor.TextEditorModel;
import texteditor.UndoManager;

import java.util.ArrayList;
import java.util.List;

public class Uppercase implements Plugin
{
    @Override
    public String getName()
    {
        return "Uppercase";
    }

    @Override
    public String getDescription()
    {
        return "Converts the first letter of every word to uppercase.";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack)
    {
        if(model.isEmpty()) return;
        List<String> modifiedLines = new ArrayList<>();
        model.getLines().forEach(line -> modifiedLines.add(convertFirstLetterInWordsToUpper(line)));
        model.modifyLines(modifiedLines);
    }

    private String convertFirstLetterInWordsToUpper(String line)
    {
        char[] chars = line.toCharArray();

        boolean shouldConvertNextLetter = true;

        for(int i = 0; i < chars.length; i++)
        {
            char c = chars[i];

            if (Character.isWhitespace(c))
            {
                shouldConvertNextLetter = true;
                continue;
            }

            if (shouldConvertNextLetter && Character.isLetter(c))
            {
                shouldConvertNextLetter = false;
                chars[i] = Character.toUpperCase(c);
            }
        }

        return new String(chars);
    }
}
