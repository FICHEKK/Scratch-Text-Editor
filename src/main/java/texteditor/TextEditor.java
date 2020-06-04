package texteditor;

import texteditor.location.Location;
import texteditor.location.LocationRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

public class TextEditor extends JComponent
{
    private static final int OFFSET_X = 32;
    private static final int OFFSET_Y = 20;
    private TextEditorModel model;
    private ClipboardStack clipboard = new ClipboardStack();

    private static final Color BACKGROUND_COLOR = new Color(40, 44, 52);
    private static final Color CURRENT_LINE_BACKGROUND_COLOR = new Color(50, 54, 62);
    private static final Color TEXT_COLOR = Color.orange;
    private static final Color ROW_NUMBER_COLOR = Color.lightGray;
    private static final Color CURSOR_COLOR = Color.white;

    public TextEditor(TextEditorModel model)
    {
        this.model = model;

        this.model.addCursorObserver(location -> repaint());
        this.model.addSelectionObserver(this::repaint);
        this.model.addTextObserver(() ->
        {
            revalidate();
            repaint();
        });


        this.setFont(new Font("Calibri", Font.PLAIN, 14));

        this.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_UP:
                        model.moveCursorUp(e.isShiftDown());
                        break;
                    case KeyEvent.VK_DOWN:
                        model.moveCursorDown(e.isShiftDown());
                        break;
                    case KeyEvent.VK_LEFT:
                        model.moveCursorLeft(e.isShiftDown());
                        break;
                    case KeyEvent.VK_RIGHT:
                        model.moveCursorRight(e.isShiftDown());
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        if (model.getSelectionRange().isEmpty())
                            model.deleteBefore();
                        else
                            model.deleteSelectedRange();
                        break;
                    case KeyEvent.VK_DELETE:
                        if (model.getSelectionRange().isEmpty())
                            model.deleteAfter();
                        else
                            model.deleteSelectedRange();
                        break;
                    default:
                        processKeyboardInput(e);
                        break;
                }
            }
        });

        this.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                requestFocusInWindow();
            }
        });
    }

    private void processKeyboardInput(KeyEvent e)
    {
        if (e.isControlDown())
        {
            processControlActions(e);
        }
        else
        {
            processWriting(e);
        }
    }

    private void processControlActions(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_C)
        {
            copySelectedText();
        }
        else if (e.getKeyCode() == KeyEvent.VK_X)
        {
            cutSelectedText();
        }
        else if (e.getKeyCode() == KeyEvent.VK_V)
        {
            if (e.isShiftDown())
            {
                pasteAndTakeText();
            }
            else
            {
                pasteText();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_Z)
        {
            UndoManager.getInstance().undo();
        }
        else if (e.getKeyCode() == KeyEvent.VK_Y)
        {
            UndoManager.getInstance().redo();
        }
        else if (e.getKeyCode() == KeyEvent.VK_A)
        {
            model.selectAllText();
        }
    }

    private void processWriting(KeyEvent e)
    {
        char c = e.getKeyChar();

        byte code = (byte) c;

        // ASCII: 32 = space char, 126 = last special char
        if ((code >= 32 && code <= 126) || c == '\n')
        {
            if (!model.getSelectionRange().isEmpty())
                model.deleteSelectedRange();

            model.insert(c);
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        paintCurrentLineBackground(g2d);
        paintSelection(g2d);
        paintText(g2d);
        paintCursor(g2d);
    }

    private void paintCurrentLineBackground(Graphics2D g2d)
    {
        int lineHeight = g2d.getFontMetrics().getHeight();
        int y = model.getCursorLocation().row * lineHeight - lineHeight / 4 * 3;

        g2d.setColor(CURRENT_LINE_BACKGROUND_COLOR);
        g2d.fillRect(0, OFFSET_Y + y, getWidth(), lineHeight);
    }

    private void paintSelection(Graphics2D g2d)
    {
        LocationRange range = model.getSelectionRange();
        if(range.isEmpty()) return;

        int startRow = range.getStart().row;
        int endRow = range.getEnd().row;

        for(int row = startRow; row <= endRow; row++)
        {
            int startIndex = 0;
            int endIndex = model.getLines().get(row).length();

            if(row == startRow)
                startIndex = range.getStart().column;

            if(row == endRow)
                endIndex = range.getEnd().column;

            paintSelectionForRow(g2d, row, startIndex, endIndex);
        }
    }

    private void paintSelectionForRow(Graphics2D g2d, int row, int startIndex, int endIndex)
    {
        String line = model.getLines().get(row);
        String untilSelection = line.substring(0, startIndex);
        String selection = line.substring(startIndex, endIndex);

        int selectionOffsetX = g2d.getFontMetrics().stringWidth(untilSelection);
        int width = g2d.getFontMetrics().stringWidth(selection);

        int height = g2d.getFontMetrics().getHeight();
        int selectionOffsetY = height * row;

        g2d.setColor(Color.BLUE);
        g2d.fillRect(OFFSET_X + selectionOffsetX, OFFSET_Y + selectionOffsetY - height / 4 * 3, width, height);
    }

    private void paintText(Graphics2D g2d)
    {
        Iterator<String> iterator = model.allLines();
        int rowHeight = g2d.getFontMetrics().getHeight();
        for(int row = 0; iterator.hasNext(); row++)
        {
            g2d.setColor(ROW_NUMBER_COLOR);
            g2d.drawString(String.valueOf(row + 1), OFFSET_X / 3, OFFSET_Y + rowHeight * row);

            g2d.setColor(TEXT_COLOR);
            g2d.drawString(iterator.next(), OFFSET_X, OFFSET_Y + rowHeight * row);
        }
    }

    private void paintCursor(Graphics2D g2d)
    {
        Location cl = model.getCursorLocation();
        String lineUntilCursor = model.getLines().get(cl.row).substring(0, cl.column);

        int cursorX = g2d.getFontMetrics().stringWidth(lineUntilCursor);
        int letterHeight = g2d.getFontMetrics().getHeight();
        int cursorY = letterHeight * cl.row;

        int x = OFFSET_X + cursorX;
        int y0 = OFFSET_Y + cursorY  - letterHeight / 4 * 3;
        int y1 = y0 + letterHeight;

        g2d.setColor(CURSOR_COLOR);
        g2d.drawLine(x, y0 + 1, x, y1 - 1);
    }

    //==================================================================
    //                          API
    //==================================================================

    public ClipboardStack getClipboard()
    {
        return clipboard;
    }

    public void copySelectedText()
    {
        if(model.getSelectionRange().isEmpty()) return;
        clipboard.push(model.getSelectedText());
    }

    public void cutSelectedText()
    {
        if(model.getSelectionRange().isEmpty()) return;
        clipboard.push(model.getSelectedText());
        model.deleteSelectedRange();
    }

    public void pasteText()
    {
        if(clipboard.isEmpty()) return;
        model.insert(clipboard.peek());
    }

    public void pasteAndTakeText()
    {
        if(clipboard.isEmpty()) return;
        model.insert(clipboard.pop());
    }

    @Override
    public Dimension getPreferredSize()
    {
        var size = super.getPreferredSize();
        int height = getGraphics().getFontMetrics().getHeight() * (model.getLines().size() + 1);
        return new Dimension(size.width, Math.max(size.height, height));
    }
}
