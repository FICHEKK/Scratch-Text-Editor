package texteditor;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import texteditor.observer.UndoManagerObserver;
import texteditor.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextEditorFrame extends JFrame
{
    private static final String PLUGIN_PATH = "texteditor.plugin";
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    private JLabel cursorLocationLabel;
    private JLabel rowCountLabel;

    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem pasteAndTakeMenuItem;
    private JMenuItem deleteSelectionMenuItem;
    private JMenuItem clearDocumentMenuItem;

    private TextEditor editor;
    private TextEditorModel model;

    private JButton undoButton;
    private JButton redoButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JButton cutButton;
    private JButton pasteAndTakeButton;

    public TextEditorFrame()
    {
        initMenuBar();

        add(createToolbar(), BorderLayout.PAGE_START);
        add(createTextEditor());
        add(createStatusBar(), BorderLayout.PAGE_END);

        UndoManager.getInstance().addObserver(new UndoManagerObserver()
        {
            @Override
            public void onUndoStackEmpty()
            {
                undoMenuItem.setEnabled(false);
                undoButton.setEnabled(false);
            }

            @Override
            public void onUndoStackNotEmpty()
            {
                undoMenuItem.setEnabled(true);
                undoButton.setEnabled(true);
            }

            @Override
            public void onRedoStackEmpty()
            {
                redoMenuItem.setEnabled(false);
                redoButton.setEnabled(false);
            }

            @Override
            public void onRedoStackNotEmpty()
            {
                redoMenuItem.setEnabled(true);
                redoButton.setEnabled(true);
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                editor.requestFocusInWindow();
            }
        });

        setTitle("Scratch! Text Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocationRelativeTo(null);
    }

    private JScrollPane createTextEditor()
    {
        model = new TextEditorModel("");
        editor = new TextEditor(model);

        JScrollPane editorScrollPane = new JScrollPane(editor);
        editorScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        editorScrollPane.getActionMap().put("unitScrollUp", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) { }});
        editorScrollPane.getActionMap().put("unitScrollDown", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) { }});

        model.addCursorObserver(location ->
        {
            cursorLocationLabel.setText(stringifyCursorLocation(location.row, location.column));
        });

        model.addTextObserver(() ->
        {
            clearDocumentMenuItem.setEnabled(!model.isEmpty());
            rowCountLabel.setText(stringifyRowCount(model.getLines().size()));
        });

        model.addSelectionObserver(() ->
        {
            boolean hasSelection = !model.getSelectedText().isEmpty();
            copyButton.setEnabled(hasSelection);
            copyMenuItem.setEnabled(hasSelection);
            cutButton.setEnabled(hasSelection);
            cutMenuItem.setEnabled(hasSelection);
            deleteSelectionMenuItem.setEnabled(hasSelection);
        });

        editor.getClipboard().addObserver(() ->
        {
            boolean hasSomethingToPaste = !editor.getClipboard().isEmpty();
            pasteMenuItem.setEnabled(hasSomethingToPaste);
            pasteButton.setEnabled(hasSomethingToPaste);
            pasteAndTakeMenuItem.setEnabled(hasSomethingToPaste);
            pasteAndTakeButton.setEnabled(hasSomethingToPaste);
        });

        return editorScrollPane;
    }

    private JToolBar createToolbar()
    {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setVisible(true);

        tb.add(undoButton = new JButton("Undo"));
        tb.add(redoButton = new JButton("Redo"));
        tb.add(copyButton = new JButton("Copy"));
        tb.add(pasteButton = new JButton("Paste"));
        tb.add(cutButton = new JButton("Cut"));
        tb.add(pasteAndTakeButton = new JButton("Paste and take"));

        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        copyButton.setEnabled(false);
        pasteButton.setEnabled(false);
        cutButton.setEnabled(false);
        pasteAndTakeButton.setEnabled(false);

        undoButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UndoManager.getInstance().undo();
            }
        });

        redoButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UndoManager.getInstance().redo();
            }
        });

        copyButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.copySelectedText();
            }
        });

        pasteButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.pasteText();
            }
        });

        cutButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.cutSelectedText();
            }
        });

        pasteAndTakeButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.pasteAndTakeText();
            }
        });

        return tb;
    }

    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu move = new JMenu("Move");

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(move);

        file.add(createOpen());
        file.add(createSave());
        file.addSeparator();
        file.add(createExit());

        edit.add(undoMenuItem = createUndo());
        edit.add(redoMenuItem = createRedo());
        edit.add(copyMenuItem = createCopy());
        edit.add(pasteMenuItem = createPaste());
        edit.add(cutMenuItem = createCut());
        edit.add(pasteAndTakeMenuItem = createPasteAndTake());
        edit.add(deleteSelectionMenuItem = createDeleteSelection());
        edit.add(clearDocumentMenuItem = createClearDocument());

        move.add(createMoveCursorToStart());
        move.add(createMoveCursorToEnd());

        undoMenuItem.setEnabled(false);
        redoMenuItem.setEnabled(false);
        copyMenuItem.setEnabled(false);
        pasteMenuItem.setEnabled(false);
        cutMenuItem.setEnabled(false);
        pasteAndTakeMenuItem.setEnabled(false);
        deleteSelectionMenuItem.setEnabled(false);
        clearDocumentMenuItem.setEnabled(false);

        initPlugins(menuBar);

        setJMenuBar(menuBar);
    }

    private void initPlugins(JMenuBar menuBar)
    {
        Reflections reflections = new Reflections(PLUGIN_PATH, new SubTypesScanner(false));
        var pluginClasses = reflections.getSubTypesOf(Plugin.class);

        if(pluginClasses.isEmpty()) return;

        JMenu pluginMenu = new JMenu("Plug-in");
        menuBar.add(pluginMenu);

        try
        {
            for (var pluginClass : pluginClasses)
            {
                Plugin plugin = pluginClass.getConstructor().newInstance();

                JMenuItem pluginItem = new JMenuItem();
                pluginItem.setAction(new AbstractAction()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        plugin.execute(model, UndoManager.getInstance(), editor.getClipboard());
                    }
                });
                pluginItem.setText(plugin.getName());
                pluginItem.setToolTipText(plugin.getDescription());

                pluginMenu.add(pluginItem);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private JPanel createStatusBar()
    {
        JPanel statusBarPanel = new JPanel(new GridLayout(1, 2));

        statusBarPanel.add(cursorLocationLabel = new JLabel(stringifyCursorLocation(0, 0)));
        statusBarPanel.add(rowCountLabel = new JLabel(stringifyRowCount(1)));
        cursorLocationLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        rowCountLabel .setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return statusBarPanel;
    }

    private JMenuItem createOpen()
    {
        JMenuItem item = new JMenuItem();

        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser jfc = new JFileChooser();

                if(jfc.showSaveDialog(TextEditorFrame.this) != JFileChooser.APPROVE_OPTION) return;
                Path path = jfc.getSelectedFile().toPath();

                try
                {
                    model.modifyLines(Files.readAllLines(path));
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        item.setText("Open");
        return item;
    }

    private JMenuItem createSave()
    {
        JMenuItem item = new JMenuItem();

        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser jfc = new JFileChooser();

                if(jfc.showSaveDialog(TextEditorFrame.this) != JFileChooser.APPROVE_OPTION) return;
                Path path = jfc.getSelectedFile().toPath();

                try
                {
                    Files.write(path, model.getLines());
                    JOptionPane.showMessageDialog(TextEditorFrame.this, "File saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(TextEditorFrame.this, "Could not save.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        item.setText("Save");
        return item;
    }

    private JMenuItem createExit()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        item.setText("Exit");
        return item;
    }

    private JMenuItem createUndo()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UndoManager.getInstance().undo();
            }
        });
        item.setText("Undo");
        return item;
    }

    private JMenuItem createRedo()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UndoManager.getInstance().redo();
            }
        });
        item.setText("Redo");
        return item;
    }

    private JMenuItem createCopy()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.copySelectedText();
            }
        });
        item.setText("Copy");
        return item;
    }

    private JMenuItem createPaste()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.pasteText();
            }
        });
        item.setText("Paste");
        return item;
    }

    private JMenuItem createCut()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.cutSelectedText();
            }
        });
        item.setText("Cut");
        return item;
    }

    private JMenuItem createPasteAndTake()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editor.pasteAndTakeText();
            }
        });
        item.setText("Paste and take");
        return item;
    }

    private JMenuItem createDeleteSelection()
    {
        JMenuItem item = new JMenuItem();

        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.deleteSelectedRange();
            }
        });

        item.setText("Delete selection");
        return item;
    }

    private JMenuItem createClearDocument()
    {
        JMenuItem item = new JMenuItem();

        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.clear();
            }
        });

        item.setText("Clear document");
        return item;
    }

    private JMenuItem createMoveCursorToStart()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.moveCursorToStart();
            }
        });

        item.setText("Cursor to document start");
        return item;
    }

    private JMenuItem createMoveCursorToEnd()
    {
        JMenuItem item = new JMenuItem();
        item.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.moveCursorToEnd();
            }
        });

        item.setText("Cursor to document end");
        return item;
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            new TextEditorFrame().setVisible(true);
        });
    }

    private String stringifyCursorLocation(int row, int column)
    {
        return "Cursor | R: " + row + " C: " + column;
    }

    private String stringifyRowCount(int rowCount)
    {
        return "Number of rows: " + rowCount;
    }
}
