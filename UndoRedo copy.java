/*package com.example.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

import java.util.Stack;

public class UndoRedo {
    private static UndoRedo instance1;
    public UndoRedo() {}
    public static UndoRedo getInstance1() {return instance1;}
    public static void setInstance1(UndoRedo urInstance) {instance1 = urInstance;}
    public UndoRedo(Canvas canvas) {
        this.canvas3 = canvas;
    }
    public void initialize1(Canvas canvas) {
        instance1 = new UndoRedo(canvas);
    }
    private Canvas canvas3;
    PaintController controller = PaintController.getInstance();
    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();

    public Stack<WritableImage> getUndoStack() {
        return undoStack;
    }
    public Stack<WritableImage> getRedoStack() {
        return redoStack;
    }

    void saveState() {
        WritableImage snapshot = new WritableImage((int) canvas3.getWidth(), (int) canvas3.getHeight());
        canvas3.snapshot(null, snapshot);
        if (undoStack.size() >= 5) {undoStack.removeFirst();}
        undoStack.push(snapshot);
        redoStack.clear();
        controller.markAsUnsaved();
    }

    WritableImage getCurrentCanvasState() {
        WritableImage writableImage = new WritableImage((int) canvas3.getWidth(), (int) canvas3.getHeight());
        canvas3.snapshot(null, writableImage);
        return writableImage;
    }
    void restoreCanvasState(WritableImage image){
        GraphicsContext gc = canvas3.getGraphicsContext2D();
        controller.fillCanvasWithBackground();
        gc.drawImage(image, 0, 0);
    }
    void undoRedoExtraction(Stack<WritableImage> fromStack, Stack<WritableImage> toStack) {
        if (!fromStack.isEmpty()) {
            toStack.push(getCurrentCanvasState());
            if (toStack.size() > 5) {
                toStack.removeFirst();
            }
            WritableImage nextState = fromStack.pop();
            restoreCanvasState(nextState);
            controller.markAsUnsaved();
        }
    }
}*/
