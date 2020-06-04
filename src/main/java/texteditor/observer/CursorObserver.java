package texteditor.observer;

import texteditor.location.Location;

public interface CursorObserver
{
    void updateCursorLocation(Location location);
}
