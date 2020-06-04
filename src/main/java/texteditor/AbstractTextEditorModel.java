package texteditor;

import texteditor.location.Location;
import texteditor.location.LocationRange;
import texteditor.observer.CursorObserver;
import texteditor.observer.SelectionObserver;
import texteditor.observer.TextObserver;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTextEditorModel
{
    protected List<String> lines = new ArrayList<>();
    protected Location cursorLocation = new Location(0, 0);
    protected LocationRange selectionRange = new LocationRange(new Location(0, 0), new Location(0, 0));

    private List<CursorObserver> cursorObservers = new ArrayList<>();
    private List<TextObserver> textObservers = new ArrayList<>();
    private List<SelectionObserver> selectionObservers = new ArrayList<>();

    //==================================================================
    //                          Getters
    //==================================================================

    public List<String> getLines()
    {
        return lines;
    }

    public Location getCursorLocation()
    {
        return new Location(cursorLocation.row, cursorLocation.column);
    }

    public LocationRange getSelectionRange()
    {
        return selectionRange;
    }

    //==================================================================
    //                          Setters
    //==================================================================

    public void setLines(List<String> lines)
    {
        this.lines = lines;
    }

    public void setCursorLocation(Location location)
    {
        cursorLocation = location;
    }

    public void setSelectionRange(LocationRange range)
    {
        selectionRange = range;
    }

    //==================================================================
    //                       Cursor observers
    //==================================================================

    public void addCursorObserver(CursorObserver observer)
    {
        cursorObservers.add(observer);
    }

    public void removeCursorObserver(CursorObserver observer)
    {
        cursorObservers.remove(observer);
    }

    protected void notifyCursorObservers()
    {
        for (var observer : cursorObservers)
        {
            observer.updateCursorLocation(cursorLocation.clone());
        }
    }

    //==================================================================
    //                       Text observers
    //==================================================================

    public void addTextObserver(TextObserver observer)
    {
        textObservers.add(observer);
    }

    public void removeTextObserver(TextObserver observer)
    {
        textObservers.remove(observer);
    }

    protected void notifyTextObservers()
    {
        for (var observer : textObservers)
        {
            observer.updateText();
        }
    }

    //==================================================================
    //                       Selection observers
    //==================================================================

    public void addSelectionObserver(SelectionObserver observer)
    {
        selectionObservers.add(observer);
    }

    public void removeSelectionObserver(SelectionObserver observer)
    {
        selectionObservers.remove(observer);
    }

    protected void notifySelectionObservers()
    {
        for (var observer : selectionObservers)
        {
            observer.onSelectionChanged();
        }
    }
}
