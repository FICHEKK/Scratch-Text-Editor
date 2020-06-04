package texteditor;

import texteditor.location.Location;
import texteditor.location.LocationRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextEditorModel extends AbstractTextEditorModel
{
    public TextEditorModel(String text)
    {
        initializeLines(text);
    }

    private void initializeLines(String text)
    {
        if(text.equals(""))
        {
            lines.add(text);
            return;
        }

        int start = 0;
        int pointer = 0;
        for (char c : text.toCharArray())
        {
            if (c == '\n')
            {
                lines.add(text.substring(start, pointer));
                start = pointer + 1;
            }

            pointer++;
        }

        if(start < pointer)
        {
            lines.add(text.substring(start, pointer));
        }
    }

    //==================================================================
    //                       Cursor movement
    //==================================================================

    public void moveCursorLeft(boolean isSelecting)
    {
        if (cursorLocation.column == 0)
        {
            if (cursorLocation.row == 0) return;
            cursorLocation.row--;
            cursorLocation.column = lines.get(cursorLocation.row).length();
        }
        else
        {
            cursorLocation.column--;
        }

        updateSelection(isSelecting);
        notifyCursorObservers();
    }

    public void moveCursorRight(boolean isSelecting)
    {
        if (cursorLocation.column == lines.get(cursorLocation.row).length())
        {
            if (cursorLocation.row == lines.size() - 1) return;
            cursorLocation.row++;
            cursorLocation.column = 0;
        }
        else
        {
            cursorLocation.column++;
        }

        updateSelection(isSelecting);
        notifyCursorObservers();
    }

    public void moveCursorUp(boolean isSelecting)
    {
        if (cursorLocation.row == 0) return;
        cursorLocation.row--;

        cursorLocation.column = Math.min(cursorLocation.column, lines.get(cursorLocation.row).length());

        updateSelection(isSelecting);
        notifyCursorObservers();
    }

    public void moveCursorDown(boolean isSelecting)
    {
        if (cursorLocation.row == lines.size() - 1) return;
        cursorLocation.row++;

        cursorLocation.column = Math.min(cursorLocation.column, lines.get(cursorLocation.row).length());

        updateSelection(isSelecting);
        notifyCursorObservers();
    }

    public void moveCursorToStart()
    {
        cursorLocation.row = 0;
        cursorLocation.column = 0;
        notifyCursorObservers();
    }

    public void moveCursorToEnd()
    {
        cursorLocation.row = lines.size() - 1;
        cursorLocation.column = lines.get(lines.size() - 1).length();
        notifyCursorObservers();
    }

    //==================================================================
    //                      Selection modifiers
    //==================================================================

    private void updateSelection(boolean isSelecting)
    {
        if(isSelecting)
        {
            selectionRange.setEnd(cursorLocation.clone());
        }
        else
        {
            cancelSelection();
        }

        notifySelectionObservers();
    }

    private void cancelSelection()
    {
        selectionRange.setStart(cursorLocation.clone());
        selectionRange.setEnd(cursorLocation.clone());
    }

    //==================================================================
    //                      Utility methods
    //==================================================================

    public String getSelectedText()
    {
        if(selectionRange.isEmpty()) return "";

        int startRow = selectionRange.getStart().row;
        int endRow = selectionRange.getEnd().row;

        StringBuilder sb = new StringBuilder();

        for(int row = startRow; row <= endRow; row++)
        {
            int startIndex = 0;
            int endIndex = lines.get(row).length();

            if(row == startRow)
                startIndex = selectionRange.getStart().column;

            if(row == endRow)
                endIndex = selectionRange.getEnd().column;

            sb.append(lines.get(row).substring(startIndex, endIndex));
            if(row < endRow) sb.append('\n');
        }

        return sb.toString();
    }

    public void selectAllText()
    {
        selectionRange.setStart(new Location(0, 0));

        int row = lines.size() - 1;
        int column = lines.get(lines.size() - 1).length();
        selectionRange.setEnd(new Location(row, column));
        notifySelectionObservers();
    }

    public boolean isEmpty()
    {
        return lines.size() == 1 && lines.get(0).isEmpty();
    }

    //==================================================================
    //              Modifying the whole document at once
    //==================================================================

    public void clear()
    {
        List<String> newLines = new ArrayList<>();
        newLines.add("");
        modifyLines(newLines);
    }

    public void modifyLines(List<String> newLines)
    {
        modifyLines(newLines, true);
    }

    private void modifyLines(List<String> newLines, boolean shouldPush)
    {
        if(shouldPush)
        {
            UndoManager.getInstance().push(new ModifyLinesEditAction(lines, newLines));
        }

        moveCursorToStart();
        setLines(newLines);
        notifyTextObservers();
    }

    //==================================================================
    //           Deleting a character using BACKSPACE button
    //==================================================================

    public void deleteBefore()
    {
        deleteBefore(true);
    }

    private void deleteBefore(boolean shouldPush)
    {
        char deleted;

        if(cursorLocation.column == 0)
        {
            if(cursorLocation.row == 0) return;
            deleted = '\n';
            deleteBeforeLeftmost();
        }
        else
        {
            deleted = lines.get(cursorLocation.row).charAt(cursorLocation.column - 1);
            deleteBeforeStandard();
        }

        if(shouldPush)
        {
            UndoManager.getInstance().push(new DeleteCharacterEditAction(deleted, cursorLocation.clone()));
        }

        notifyTextObservers();
    }

    private void deleteBeforeLeftmost()
    {
        int row = cursorLocation.row;
        moveCursorLeft(false);

        String currentLine = lines.get(row);
        String previousLine = lines.get(row - 1);
        lines.set(row - 1, previousLine + currentLine);
        lines.remove(row);
    }

    private void deleteBeforeStandard()
    {
        String line = lines.get(cursorLocation.row);
        String modifiedLine = line.substring(0, cursorLocation.column - 1) + line.substring(cursorLocation.column);
        lines.set(cursorLocation.row, modifiedLine);
        moveCursorLeft(false);
    }

    //==================================================================
    //            Deleting a character using DELETE button
    //==================================================================

    public void deleteAfter()
    {
        deleteAfter(true);
    }

    private void deleteAfter(boolean shouldPush)
    {
        char deleted;

        if(cursorLocation.column == lines.get(cursorLocation.row).length())
        {
            if(cursorLocation.row == lines.size() - 1) return;

            deleted = '\n';
            deleteAfterRightmost();
        }
        else
        {
            deleted = lines.get(cursorLocation.row).charAt(cursorLocation.column);
            deleteAfterStandard();
        }

        if(shouldPush)
        {
            UndoManager.getInstance().push(new DeleteCharacterEditAction(deleted, cursorLocation.clone()));
        }

        notifyTextObservers();
    }

    private void deleteAfterRightmost()
    {
        String currentLine = lines.get(cursorLocation.row);
        String nextLine = lines.get(cursorLocation.row + 1);
        lines.set(cursorLocation.row, currentLine + nextLine);
        lines.remove(cursorLocation.row + 1);
    }

    private void deleteAfterStandard()
    {
        String line = lines.get(cursorLocation.row);
        String modifiedLine = line.substring(0, cursorLocation.column) + line.substring(cursorLocation.column + 1);
        lines.set(cursorLocation.row, modifiedLine);
    }

    //==================================================================
    //             Deleting a selected or an arbitrary range
    //==================================================================

    public void deleteSelectedRange()
    {
        deleteSelectedRange(true);
    }

    private void deleteSelectedRange(boolean shouldPush)
    {
        if(shouldPush)
        {
            String text = getSelectedText();
            Location textStart = selectionRange.getStart().clone();
            Location textEnd = selectionRange.getEnd().clone();
            UndoManager.getInstance().push(new DeleteTextEditAction(text, textStart, textEnd));
        }

        deleteRange(selectionRange);
        cursorLocation = selectionRange.getStart().clone();
        cancelSelection();
        notifySelectionObservers();
        notifyCursorObservers();
    }

    public void deleteRange(LocationRange range)
    {
        Location start = range.getStart();
        Location end = range.getEnd();

        String startLine = lines.get(start.row).substring(0, start.column);
        String endLine = lines.get(end.row).substring(end.column);
        lines.set(start.row, startLine + endLine);
        lines.subList(start.row + 1, end.row + 1).clear();

        notifyTextObservers();
    }

    //==================================================================
    //                Inserting a single character
    //==================================================================

    public void insert(char c)
    {
        insert(c, true);
    }

    private void insert(char c, boolean shouldPush)
    {
        if(shouldPush)
        {
            UndoManager.getInstance().push(new InsertCharacterEditAction(c, cursorLocation.clone()));
        }

        if(c == '\n')
        {
            insertNewLine();
            cursorLocation.row++;
            cursorLocation.column = 0;
            notifyCursorObservers();
        }
        else
        {
            insertStandard(c);
            moveCursorRight(false);
        }

        cancelSelection();
        notifySelectionObservers();
        notifyTextObservers();
    }

    private void insertNewLine()
    {
        String prefix = lines.get(cursorLocation.row).substring(0, cursorLocation.column);
        String suffix = lines.get(cursorLocation.row).substring(cursorLocation.column);
        lines.set(cursorLocation.row, prefix);
        lines.add(cursorLocation.row + 1, suffix);
    }

    private void insertStandard(char c)
    {
        String line = lines.get(cursorLocation.row);
        line = line.substring(0, cursorLocation.column) + c + line.substring(cursorLocation.column);
        lines.set(cursorLocation.row, line);
    }

    //==================================================================
    //                     Inserting a string
    //==================================================================

    public void insert(String text)
    {
        insert(text, true);
    }

    private void insert(String text, boolean shouldPush)
    {
        var continuousStrings = text.split("\n", -1);

        if(shouldPush)
        {
            Location textStart = cursorLocation.clone();
            Location textEnd = cursorLocation.clone();

            int nStrings = continuousStrings.length;
            textEnd.row += nStrings - 1;

            if(nStrings == 1)
            {
                textEnd.column += continuousStrings[0].length();
            }
            else
            {
                textEnd.column = continuousStrings[nStrings - 1].length();
            }

            UndoManager.getInstance().push(new InsertTextEditAction(text, textStart, textEnd));
        }

        // Ignore the last element as it should not produce '\n'.
        for(int i = 0; i < continuousStrings.length - 1; i++)
        {
            insertContinuous(continuousStrings[i]);
            insert('\n', false);
        }

        // Insert the last element.
        insertContinuous(continuousStrings[continuousStrings.length - 1]);
        notifyTextObservers();
    }

    private void insertContinuous(String text)
    {
        String line = lines.get(cursorLocation.row);
        line = line.substring(0, cursorLocation.column) + text + line.substring(cursorLocation.column);
        lines.set(cursorLocation.row, line);
        cursorLocation.column += text.length();
    }

    //==================================================================
    //                          Iterators
    //==================================================================

    public Iterator<String> allLines()
    {
        return lines.iterator();
    }

    public Iterator<String> linesRange(int start, int end)
    {
        return new RangeIterator(start, end);
    }

    private class RangeIterator implements Iterator<String>
    {
        private int current;
        private int end;

        private RangeIterator(int start, int end)
        {
            this.current = start;
            this.end = end;
        }

        @Override
        public boolean hasNext()
        {
            return current < end;
        }

        @Override
        public String next()
        {
            if(!hasNext()) throw new IndexOutOfBoundsException();
            return lines.get(current++);
        }
    }

    //==================================================================
    //                       Edit actions
    //==================================================================

    private class InsertCharacterEditAction implements EditAction
    {
        private char character;
        private Location location;

        private InsertCharacterEditAction(char character, Location cursorLocation)
        {
            this.character = character;
            this.location = cursorLocation;
        }

        @Override
        public void executeDo()
        {
            setCursorLocation(location.clone());
            insert(character, false);
        }

        @Override
        public void executeUndo()
        {
            setCursorLocation(location.clone());
            deleteAfter(false);
        }
    }

    private class InsertTextEditAction implements EditAction
    {
        private String text;
        private Location textStart;
        private Location textEnd;

        private InsertTextEditAction(String text, Location textStart, Location textEnd)
        {
            this.text = text;
            this.textStart = textStart;
            this.textEnd = textEnd;
        }

        @Override
        public void executeDo()
        {
            setCursorLocation(textStart.clone());
            insert(text, false);
        }

        @Override
        public void executeUndo()
        {
            setSelectionRange(new LocationRange(textStart.clone(), textEnd.clone()));
            deleteSelectedRange(false);
        }
    }

    private class DeleteCharacterEditAction implements EditAction
    {
        private char deleted;
        private Location deletedLocation;

        private DeleteCharacterEditAction(char deleted, Location deletedLocation)
        {
            this.deleted = deleted;
            this.deletedLocation = deletedLocation;
        }

        @Override
        public void executeDo()
        {
            setCursorLocation(deletedLocation.clone());
            deleteAfter(false);
        }

        @Override
        public void executeUndo()
        {
            setCursorLocation(deletedLocation.clone());
            insert(deleted, false);
        }
    }

    private class DeleteTextEditAction implements EditAction
    {
        private String text;
        private Location textStart;
        private Location textEnd;

        public DeleteTextEditAction(String text, Location textStart, Location textEnd)
        {
            this.text = text;
            this.textStart = textStart;
            this.textEnd = textEnd;
        }

        @Override
        public void executeDo()
        {
            setSelectionRange(new LocationRange(textStart.clone(), textEnd.clone()));
            deleteSelectedRange(false);
        }

        @Override
        public void executeUndo()
        {
            setCursorLocation(textStart.clone());
            insert(text, false);
        }
    }

    private class ModifyLinesEditAction implements EditAction
    {
        private List<String> oldLines;
        private List<String> newLines;

        public ModifyLinesEditAction(List<String> oldLines, List<String> newLines)
        {
            this.oldLines = oldLines;
            this.newLines = newLines;
        }

        @Override
        public void executeDo()
        {
            modifyLines(newLines, false);
        }

        @Override
        public void executeUndo()
        {
            modifyLines(oldLines, false);
        }
    }
}
