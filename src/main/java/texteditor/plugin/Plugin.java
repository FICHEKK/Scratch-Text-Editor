package texteditor.plugin;

import texteditor.ClipboardStack;
import texteditor.TextEditorModel;
import texteditor.UndoManager;

public interface Plugin
{
    String getName();
    String getDescription();
    void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack);
}
