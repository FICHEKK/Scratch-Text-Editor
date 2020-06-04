package texteditor;

import texteditor.observer.UndoManagerObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoManager
{
    private static UndoManager instance = new UndoManager();
    private Stack<EditAction> undoStack = new Stack<>();
    private Stack<EditAction> redoStack = new Stack<>();

    private List<UndoManagerObserver> observers = new ArrayList<>();

    private UndoManager() {}

    public static UndoManager getInstance()
    {
        return instance;
    }

    public void undo()
    {
        if(undoStack.isEmpty()) return;

        EditAction action = undoStack.pop();
        redoStack.push(action);
        action.executeUndo();

        if(undoStack.isEmpty()) notifyObserversUndoStackEmpty();
        if(redoStack.size() == 1) notifyObserversRedoStackNotEmpty();
    }

    public void redo()
    {
        if(redoStack.isEmpty()) return;

        EditAction action = redoStack.pop();
        undoStack.push(action);
        action.executeDo();

        if(redoStack.isEmpty()) notifyObserversRedoStackEmpty();
        if(undoStack.size() == 1) notifyObserversUndoStackNotEmpty();
    }

    public void push(EditAction action)
    {
        redoStack.clear();
        undoStack.push(action);

        notifyObserversRedoStackEmpty();
        if(undoStack.size() == 1) notifyObserversUndoStackNotEmpty();
    }

    public void addObserver(UndoManagerObserver observer)
    {
        observers.add(observer);
    }

    public void removeObserver(UndoManagerObserver observer)
    {
        observers.remove(observer);
    }

    private void notifyObserversUndoStackEmpty()
    {
        for(var observer : observers)
            observer.onUndoStackEmpty();
    }

    private void notifyObserversUndoStackNotEmpty()
    {
        for(var observer : observers)
            observer.onUndoStackNotEmpty();
    }

    private void notifyObserversRedoStackEmpty()
    {
        for(var observer : observers)
            observer.onRedoStackEmpty();
    }

    private void notifyObserversRedoStackNotEmpty()
    {
        for(var observer : observers)
            observer.onRedoStackNotEmpty();
    }
}
