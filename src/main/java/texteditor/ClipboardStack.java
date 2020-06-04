package texteditor;

import texteditor.observer.ClipboardObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ClipboardStack
{
    private Stack<String> texts = new Stack<>();
    private List<ClipboardObserver> observers = new ArrayList<>();

    public void push(String text)
    {
        texts.push(text);
        notifyObservers();
    }

    public String pop()
    {
        String top = texts.pop();
        notifyObservers();
        return top;
    }

    public String peek()
    {
        return texts.peek();
    }

    public boolean isEmpty()
    {
        return texts.isEmpty();
    }

    public void clear()
    {
        texts.clear();
        notifyObservers();
    }

    public void addObserver(ClipboardObserver observer)
    {
        observers.add(observer);
    }

    public void removeObserver(ClipboardObserver observer)
    {
        observers.remove(observer);
    }

    private void notifyObservers()
    {
        for (var observer : observers)
        {
            observer.updateClipboard();
        }
    }
}
