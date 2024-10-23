package com.example.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import java.util.Stack;

/** Class that contains functionality for the undo and redo buttons */
public class UndoRedoTool {
    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();
    PaintController pc;

    /** Constructor method for UndoRedoTool */
    public UndoRedoTool(PaintController pc) {
        this.pc = pc;
    }

    /** Saves the current state of the canvas to the stack to be reverted using undo or redone with redo */
    public void saveState() {
        WritableImage snapshot = new WritableImage((int) pc.getCurrentTabCanvas().getWidth(), (int) pc.getCurrentTabCanvas().getHeight());
        pc.getCurrentTabCanvas().snapshot(null, snapshot);
        if (undoStack.size() >= 5) {
            undoStack.removeFirst();
        }
        undoStack.push(snapshot);
        redoStack.clear();  // Clear redo stack on new action
        pc.markAsUnsaved(); // Notify unsaved state
    }

    /** Undoes the most recent change */
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(getCurrentCanvas());
            WritableImage previousState = undoStack.pop();
            restoreCanvas(previousState);
            pc.markAsUnsaved();
        }
    }
    /** Redoes the most recent change */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(getCurrentCanvas());
            WritableImage nextState = redoStack.pop();
            restoreCanvas(nextState);
            pc.markAsUnsaved();
        }
    }

    /** Creates a writable image for the stack to be used when undoing or redoing */
    private WritableImage getCurrentCanvas() {
        WritableImage writableImage = new WritableImage((int) pc.getCurrentTabCanvas().getWidth(), (int) pc.getCurrentTabCanvas().getHeight());
        pc.getCurrentTabCanvas().snapshot(null, writableImage);
        return writableImage;
    }

    /** Draws the WritableImage from the stack */
    private void restoreCanvas(WritableImage image) {
        GraphicsContext gc = pc.getCurrentTabCanvas().getGraphicsContext2D();
        pc.fillCanvasWithBackground();
        gc.drawImage(image, 0, 0);
    }

    /** Functionality for disabling the undo button when there is nothing to be undone */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    /** Functionality for disabling the redo button when there is nothing to be redone */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}