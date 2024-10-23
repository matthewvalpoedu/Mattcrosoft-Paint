package com.example.paint;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import static java.lang.Math.abs;

/** Defines many methods for drawing shapes and lines */
public class DrawingTool {
    //declare variables to be used throughout the class
    private final Canvas canvas;
    private final GraphicsContext g;
    private double startX, startY, endX, endY;
    PaintController controller = PaintController.getInstance();

    /** DrawingTool constructor method */
    public DrawingTool(Canvas canvas) {
        this.canvas = canvas;
        this.g = canvas.getGraphicsContext2D();
    }

    /** Allows user to freely draw using mouse */
    public void freeDrawing(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) { //draw tool
        canvas.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText()); //gets brush size from the textField
            startX = ex.getX();
            startY = ex.getY();
            endX = ex.getX();
            endY = ex.getY();
            controller.saveState();
            if (dashed.isSelected()) {
                dashTheLine();
            } else { unDashTheLine(); }
            g.setStroke(colorPicker.getValue()); //draws based on the colorPicker color value
            g.setLineWidth(size); //sets the line width based on the textField
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(endX, endY, startX, startY);
            controller.logUpdate(" Line stroked");
            controller.markAsUnsaved();
        });
        canvas.setOnMouseDragged(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue()); //sets fill color of draw tool based on color picker
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(endX, endY, startX, startY);
            endX = startX;
            endY = startY;
            controller.markAsUnsaved();
        });
    }

    /** Allows user to freely erase using mouse */
    public void useEraser(TextField brushSize) {
        canvas.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText()); //gets brush size from the textField
            startX = ex.getX();
            startY = ex.getY();
            endX = ex.getX();
            endY = ex.getY();
            g.setStroke(Color.WHITE); //draws based on the colorPicker color value
            g.setLineWidth(size); //sets the line width based on the textField
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(endX, endY, startX, startY);
            controller.saveState();
            controller.markAsUnsaved();
        });
        canvas.setOnMouseDragged(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(Color.WHITE); //sets fill color of draw tool based on color picker
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(endX, endY, startX, startY);
            endX = startX;
            endY = startY;
            controller.markAsUnsaved();
        });
    }

    /** Allows user to select a color from the canvas and automatically selects that color in the color picker */
    public void useEyedrop(ColorPicker colorPicker) {
        canvas.setOnMousePressed(ex -> {
            startX = ex.getX();
            startY = ex.getY();
            WritableImage snap = canvas.snapshot(null, null);
            PixelReader pixelReader = snap.getPixelReader();
            Color color = pixelReader.getColor((int) startX, (int) startY);
            colorPicker.setValue(color);
        });
    }

    private WritableImage tempCanvas;
    /** Calls shapeInit in context */
    private void dToolSetup(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        tempCanvas = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.setOnMousePressed(ex -> {
            shapeInit(brushSize, colorPicker, dashed, ex);
            controller.saveState();
            controller.markAsUnsaved();
            canvas.snapshot(null, tempCanvas);
        });
    }
    /** Initializes shapes and lines with the line width, color, and whether it has dashes */
    private void shapeInit(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed, MouseEvent ex) {
        double size = Double.parseDouble(brushSize.getText());
        startX = ex.getX();
        startY = ex.getY();
        if (dashed.isSelected()) {
            dashTheLine();
        } else {unDashTheLine();}
        g.setStroke(colorPicker.getValue());
        g.setLineWidth(size);
        g.setLineCap(StrokeLineCap.ROUND);
    }

    /** Allows user to draw a straight line */
    public void drawLine(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            g.strokeLine(startX, startY, endX, endY);
        });
        canvas.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            g.strokeLine(startX, startY, endX, endY);
            controller.markAsUnsaved();
        });
    }

    /** Allows user to draw a square */
    public void drawSquare(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            squareMath(ex);
        });
        canvas.setOnMouseReleased(ex -> {
            squareMath(ex);
            controller.markAsUnsaved();
        });
    }
    /** Math for drawSquare method */
    private void squareMath(MouseEvent ex) {
        endX = ex.getX();
        endY = ex.getY();
        double sideLength = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));
        double squareX = startX;
        double squareY = startY;
        if (endX < startX) squareX = startX - sideLength;
        if (endY < startY) squareY = startY - sideLength;
        g.strokeRect(squareX, squareY, sideLength, sideLength);
    }

    /** Allows user to draw a rectangle */
    public void drawRectangle(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            rectangleMath(ex);
        });
        canvas.setOnMouseReleased(ex -> {
            rectangleMath(ex);
            controller.markAsUnsaved();
            canvas.snapshot(null, tempCanvas);
        });
    }
    /** Math for drawRectangle method */
    private void rectangleMath(MouseEvent ex) {
        endX = ex.getX();
        endY = ex.getY();
        double width = abs(endX - startX);
        double height = abs(endY - startY);
        double rectX = Math.min(startX, endX);
        double rectY = Math.min(startY, endY);
        g.strokeRect(rectX, rectY, width, height);
    }

    private WritableImage[] tempCanvasArray;
    /** Calculates the center point for the polygon */
    private void centerCalc(MouseEvent ex, int numSides) {
        endX = ex.getX();
        endY = ex.getY();
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)) / 2;
        polygonMath(numSides, centerX, centerY, radius);
    }
    /** Sets up an array of temporary canvases to be used by drawPolygon and drawStar */
    private void arrayShapeSetup(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        tempCanvasArray = new WritableImage[] {new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight())};
        SnapshotParameters params = new SnapshotParameters();
        canvas.setOnMousePressed(ex -> {
            shapeInit(brushSize, colorPicker, dashed, ex);
            tempCanvasArray[0] = canvas.snapshot(params, tempCanvasArray[0]);
            controller.saveState();
            controller.markAsUnsaved();
        });
    }

    /** Allows the user to draw a polygon with a user specified number of sides from a text field */
    public void drawPolygon(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed, TextField sides) {
        arrayShapeSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            int numSides = Integer.parseInt(sides.getText());
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvasArray[0], 0, 0);
            centerCalc(ex, numSides);
        });
        canvas.setOnMouseReleased(ex -> {
            int numSides = Integer.parseInt(sides.getText());
            centerCalc(ex, numSides);
            controller.markAsUnsaved();
        });
    }
    /** Math for drawPolygon method */
    private void polygonMath(int numSides, double centerX, double centerY, double radius) {
        double[] xPoints = new double[numSides];
        double[] yPoints = new double[numSides];

        double angleStep = 2 * Math.PI / numSides;

        for (int i = 0; i < numSides; i++) {
            double angle = i * angleStep;
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }
        for (int i = 0; i < numSides; i++) {
            int nextIndex = (i + 1) % numSides;
            g.strokeLine(xPoints[i], yPoints[i], xPoints[nextIndex], yPoints[nextIndex]);
        }
    }

    /** Allows the user to draw a star with a user specified number of points from a text field */
    public void drawStar(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed, TextField points) {
        arrayShapeSetup(brushSize, colorPicker, dashed);
        // As the mouse is dragged, calculate star points dynamically
        canvas.setOnMouseDragged(ex -> {
            int numPoints = Integer.parseInt(points.getText());
            if (numPoints < 5) return; // Star needs at least 5 points
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvasArray[0], 0, 0);
            starMath(ex, numPoints);
        });
        // Finalize the star drawing when the mouse is released
        canvas.setOnMouseReleased(ex -> {
            int numPoints = Integer.parseInt(points.getText());
            if (numPoints < 5) return; // Avoid invalid stars
            starMath(ex, numPoints);
        });
    }
    /** Math for drawStar method */
    private void starMath(MouseEvent ex, int numPoints) {
        endX = ex.getX();
        endY = ex.getY();
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double outerRadius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)) / 2;
        double innerRadius = outerRadius / 2.5;  // Adjust inner radius
        double[] xPoints = new double[numPoints * 2];
        double[] yPoints = new double[numPoints * 2];
        double angleStep = Math.PI / numPoints;  // Half the angle for the star shape
        for (int i = 0; i < numPoints * 2; i++) {
            double angle = i * angleStep;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + radius * Math.cos(angle - Math.PI / 2);
            yPoints[i] = centerY + radius * Math.sin(angle - Math.PI / 2);
        }
        for (int i = 0; i < numPoints * 2; i++) {
            int nextIndex = (i + 1) % (numPoints * 2);
            g.strokeLine(xPoints[i], yPoints[i], xPoints[nextIndex], yPoints[nextIndex]);
        }
    }

    /** Allows the user to draw a parallelogram */
    public void drawParallelogram(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        arrayShapeSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvasArray[0], 0, 0);
            parallelogramMath(ex);
        });
        canvas.setOnMouseReleased(this::parallelogramMath);
    }
    /** Math for drawParallelogram method */
    private void parallelogramMath(MouseEvent ex) {
        endX = ex.getX();
        endY = ex.getY();
        double width = endX - startX;
        double bx = startX + width * 0.5;
        double by = startY;
        double cx = endX + width * 0.5;
        double cy = endY;
        double dx = startX + width * 0.5;
        double dy = endY;
        g.strokeLine(startX, startY, bx, by);  // Line AB
        g.strokeLine(bx, by, cx, cy);          // Line BC
        g.strokeLine(cx, cy, dx, dy);          // Line CD
        g.strokeLine(dx, dy, startX, startY);  // Li/ Li Line D
    }

    /** Initializes setup for copyAndMove and deselectCopy */
    private void copyInit(WritableImage tempCanvas) {
        canvas.setOnMousePressed(ex -> {
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(Color.ALICEBLUE);
            g.setLineWidth(3);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            canvas.snapshot(null, tempCanvas);
        });
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            rectangleMath(ex);
        });
    }
    /** Math for copyAndMove and deselectCopy */
    private void copyMath(WritableImage tempCanvas, WritableImage[] selectedArea, double[] offsetX, double[] offsetY) {
        canvas.setOnMouseDragged(dragEvent -> {
            if (selectedArea[0] != null) {
                controller.fillCanvasWithBackground();
                g.drawImage(tempCanvas, 0, 0);
                double newX = dragEvent.getX() - offsetX[0];
                double newY = dragEvent.getY() - offsetY[0];
                g.drawImage(selectedArea[0], newX, newY);
            }
        });
        canvas.setOnMouseReleased(releaseEvent -> {
            if (selectedArea[0] != null) {
                controller.fillCanvasWithBackground();
                g.drawImage(tempCanvas, 0, 0);
                double finalX = releaseEvent.getX() - offsetX[0];
                double finalY = releaseEvent.getY() - offsetY[0];
                g.drawImage(selectedArea[0], finalX, finalY);
                selectedArea[0] = null;
            }
        });
    }
    /** Finalizes the copied portion of the graphicsContext onto the canvas */
    private void copyFinalize(WritableImage tempCanvas, WritableImage[] selectedArea, double[] offsetX, double[] offsetY, MouseEvent ex, double rectX, double rectY, double width, double height) {
        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new Rectangle2D(rectX, rectY, width, height));
        selectedArea[0] = new WritableImage((int) width, (int) height);
        canvas.snapshot(params, selectedArea[0]);
        offsetX[0] = ex.getX() - rectX;
        offsetY[0] = ex.getY() - rectY;
        controller.fillCanvasWithBackground();
        g.drawImage(tempCanvas, 0, 0);
    }

    /** Allows the user to copy a portion of their graphicsContext and move it anywhere on the canvas */
    public void copyAndMove() {
        WritableImage tempCanvas = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        final WritableImage[] selectedArea = new WritableImage[1];
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        copyInit(tempCanvas);
        canvas.setOnMouseReleased(ex -> {
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            if (width > 0 && height > 0) {
                copyFinalize(tempCanvas, selectedArea, offsetX, offsetY, ex, rectX, rectY, width, height);
                g.fillRect(rectX, rectY, selectedArea[0].getWidth(), selectedArea[0].getHeight());
            }
            copyMath(tempCanvas, selectedArea, offsetX, offsetY);
        });
    }
    /** Allows the user to select and move a portion of their graphicsContext and move it anywhere on the canvas */
    public void deselectCopy() {
        WritableImage tempCanvas = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        final WritableImage[] selectedArea = new WritableImage[1];
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        copyInit(tempCanvas);
        canvas.setOnMouseReleased(ex -> {
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            if (width > 0 && height > 0) {
                copyFinalize(tempCanvas, selectedArea, offsetX, offsetY, ex, rectX, rectY, width, height);
            }
            copyMath(tempCanvas, selectedArea, offsetX, offsetY);
        });
    }

    /** Allows the user to draw a circle */
    public void drawCircle(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            circleMath();
        });
        canvas.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            circleMath();
            controller.markAsUnsaved();
        });
    }
    /** Math for drawCircle method */
    private void circleMath() {
        double radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
        double centerX = Math.min(startX, endX) + radius;
        double centerY = Math.min(startY, endY) + radius;
        g.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /** Allows the user to draw an ellipse */
    public void drawEllipse(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            ellipseMath();
        });
        canvas.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            ellipseMath();
            controller.markAsUnsaved();
        });
    }
    /** Math for drawEllipse method */
    private void ellipseMath() {
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        double topLeftX = Math.min(startX, endX);
        double topLeftY = Math.min(startY, endY);
        g.strokeOval(topLeftX, topLeftY, width, height);
    }

    /** Allows the user to draw a triangle */
    public void drawTriangle(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            triangleMath();

        });
        canvas.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            triangleMath();
            controller.markAsUnsaved();
        });
    }
    /** Math for drawTriangle method */
    private void triangleMath() {
        double baseX1 = startX;
        double baseX2 = endX;
        double baseY = endY;
        double topX = (startX + endX) / 2;
        double topY = startY;
        g.strokePolygon(new double[]{baseX1, baseX2, topX}, new double[]{baseY, baseY, topY}, 3);
    }

    /** Allows the user to draw a diamond */
    public void drawDiamond(TextField brushSize, ColorPicker colorPicker, ToggleButton dashed) {
        dToolSetup(brushSize, colorPicker, dashed);
        canvas.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            diamondMath();
        });
        canvas.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            diamondMath();
            controller.markAsUnsaved();
        });
    }
    /** Math for drawDiamond method */
    private void diamondMath() {
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double halfWidth = Math.abs(endX - startX) / 2;
        double halfHeight = Math.abs(endY - startY) / 2;
        double topY = centerY - halfHeight;
        double rightX = centerX + halfWidth;
        double bottomY = centerY + halfHeight;
        double leftX = centerX - halfWidth;
        g.strokePolygon(new double[]{centerX, rightX, centerX, leftX}, new double[]{topY, centerY, bottomY, centerY}, 4);
    }

    /** Makes the line or shape being drawn dashed */
    public void dashTheLine() {
        g.setLineDashes(10);
    }
    /** Makes the line or shape being drawn solid */
    public void unDashTheLine() {
        g.setLineDashes(0);
    }
}
