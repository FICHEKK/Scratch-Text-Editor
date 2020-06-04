package texteditor.plugin;

import texteditor.ClipboardStack;
import texteditor.TextEditorModel;
import texteditor.UndoManager;

import javax.swing.*;

public class Statistics implements Plugin
{
    @Override
    public String getName()
    {
        return "Statistics";
    }

    @Override
    public String getDescription()
    {
        return "Displays the number of lines, words and letters in the document.";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack)
    {
        int numberOfLines = countLines(model);
        int numberOfWords = countWords(model);
        int numberOfLetters = countLetters(model);

        JOptionPane.showMessageDialog(null, "Lines: " + numberOfLines + "\n" +
                        "Words: " + numberOfWords + "\n" +
                        "Letters: " + numberOfLetters,
                "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private int countLines(TextEditorModel model)
    {
        return model.getLines().size();
    }

    private int countWords(TextEditorModel model)
    {
        int numberOfWords = 0;

        for(String line : model.getLines())
        {
            if(line.isBlank()) continue;
            numberOfWords += line.split("\\s+").length;
        }

        return numberOfWords;
    }

    private int countLetters(TextEditorModel model)
    {
        int numberOfLetters = 0;

        for(String line : model.getLines())
        {
            for (char c : line.toCharArray())
            {
                if(Character.isLetter(c)) numberOfLetters++;
            }
        }

        return numberOfLetters;
    }
}
