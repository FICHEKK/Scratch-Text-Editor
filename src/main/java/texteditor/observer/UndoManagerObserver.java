package texteditor.observer;

public interface UndoManagerObserver
{
    void onUndoStackEmpty();
    void onUndoStackNotEmpty();
    void onRedoStackEmpty();
    void onRedoStackNotEmpty();
}
