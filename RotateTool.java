package com.example.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;

/** Class for rotation and other canvas mutators */
public class RotateTool {
    private final Canvas canvas;
    private final GraphicsContext g;

    /** RotateTool constructor method */
    public RotateTool(Canvas canvas) {
        this.canvas = canvas;
        this.g = canvas.getGraphicsContext2D();
    }

    /** Horizontally mirrors the canvas */
    public void mirrorCanvas() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.save();
        g.translate(canvas.getWidth(), 0);  // Translate to the right side of the canvas
        g.scale(-1, 1);  // Flip horizontally by scaling x-axis by -1
        g.drawImage(snapshot, 0, 0);
        g.restore();  // Restore to remove the transformation
    }

    /** Vertically flips the canvas */
    public void flipCanvas() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.save();
        g.translate(0, canvas.getHeight());  // Translate to the bottom of the canvas
        g.scale(1, -1);  // Flip vertically by scaling y-axis by -1
        g.drawImage(snapshot, 0, 0);
        g.restore();  // Restore to remove the transformation
    }

    /** Rotates the canvas the number of degrees as specified in a text field */
    public void rotateCanvasWithSnapshot(TextField degrees) {
        // Get the rotation degrees from the TextField
        String degreeText = degrees.getText();
        if (degreeText == null || degreeText.isEmpty()) {
            System.out.println("Please enter a valid degree value.");
            return; // Exit if input is invalid
        }
        double rotationDegrees;
        try {
            rotationDegrees = Double.parseDouble(degreeText);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter a valid number.");
            return; // Exit if parsing fails
        }
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.save();
        g.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        g.rotate(rotationDegrees);
        g.translate(-snapshot.getWidth() / 2, -snapshot.getHeight() / 2);
        g.drawImage(snapshot, 0, 0);
        g.restore();
    }
}
