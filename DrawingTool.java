package com.example.paint;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import static java.lang.Math.abs;

public class DrawingTool {
    //declare variables to be used throughout the class
    private Canvas canvas2;
    private GraphicsContext g;
    private double startX, startY, endX, endY;
    WritableImage selectedImage;
    PaintController controller = PaintController.getInstance();

    public DrawingTool(Canvas canvas) {
        //initialize canvas and graphics context

        this.canvas2 = canvas;
        this.g = canvas.getGraphicsContext2D();
    }
    public void freeDrawing(TextField brushSize, ColorPicker colorPicker) { //draw tool
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText()); //gets brush size from the textField
            startX = ex.getX();
            startY = ex.getY();
            endX = ex.getX();
            endY = ex.getY();
            controller.saveState();
            g.setStroke(colorPicker.getValue()); //draws based on the colorPicker color value
            g.setLineWidth(size); //sets the line width based on the textField
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(endX, endY, startX, startY);
            controller.markAsUnsaved();
        });
        canvas2.setOnMouseDragged(ex -> {
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

    public void drawLine(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });

        canvas2.setOnMouseDragged(ex -> {
            //g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(tempCanvas, 0, 0);

            endX = ex.getX();
            endY = ex.getY();
            g.strokeLine(startX, startY, endX, endY);
        });

        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            g.strokeLine(startX, startY, endX, endY);
            controller.markAsUnsaved();
        });
    }
    public void drawSquare(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double sideLength = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));
            double squareX = startX;
            double squareY = startY;
            if (endX < startX) squareX = startX - sideLength;
            if (endY < startY) squareY = startY - sideLength;
            g.strokeRect(squareX, squareY, sideLength, sideLength);
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double sideLength = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));
            double squareX = startX;
            double squareY = startY;
            if (endX < startX) squareX = startX - sideLength;
            if (endY < startY) squareY = startY - sideLength;
            g.strokeRect(squareX, squareY, sideLength, sideLength);
            controller.markAsUnsaved();
        });
    }
    public void drawRectangle(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            g.strokeRect(rectX, rectY, width, height);
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            g.strokeRect(rectX, rectY, width, height);
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
    }
    public void drawPolygon(TextField brushSize, ColorPicker colorPicker, TextField sides) {
        final WritableImage[] tempCanvas = {new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight())};
        SnapshotParameters params = new SnapshotParameters();

        canvas2.setOnMousePressed(ex -> {

            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            tempCanvas[0] = canvas2.snapshot(params, tempCanvas[0]);
            controller.saveState();
            controller.markAsUnsaved();
        });

        canvas2.setOnMouseDragged(ex -> {
            int numSides = Integer.parseInt(sides.getText());
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas[0], 0, 0);

            endX = ex.getX();
            endY = ex.getY();

            double centerX = (startX + endX) / 2;
            double centerY = (startY + endY) / 2;
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)) / 2;

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
        });

        canvas2.setOnMouseReleased(ex -> {
            int numSides = Integer.parseInt(sides.getText());
            endX = ex.getX();
            endY = ex.getY();

            double centerX = (startX + endX) / 2;
            double centerY = (startY + endY) / 2;
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)) / 2;

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
        });
    }

    public void drawParallelogram(TextField brushSize, ColorPicker colorPicker) {
        final WritableImage[] tempCanvas = {new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight())};
        SnapshotParameters params = new SnapshotParameters();
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            tempCanvas[0] = canvas2.snapshot(params, tempCanvas[0]);
            controller.saveState();
            controller.markAsUnsaved();
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas[0], 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double width = endX - startX;
            double height = endY - startY;

            // Calculate the four points of the parallelogram
            double bx = startX + width * 0.5;
            double by = startY;
            double cx = endX + width * 0.5;
            double cy = endY;
            double dx = startX + width * 0.5;
            double dy = endY;

            // Draw the parallelogram
            g.strokeLine(startX, startY, bx, by);  // Line AB
            g.strokeLine(bx, by, cx, cy);          // Line BC
            g.strokeLine(cx, cy, dx, dy);          // Line CD
            g.strokeLine(dx, dy, startX, startY);  // Li// Line D
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double width = endX - startX;
            double height = endY - startY;

            // Calculate the four points of the parallelogram
            double bx = startX + width * 0.5;
            double by = startY;
            double cx = endX + width * 0.5;
            double cy = endY;
            double dx = startX + width * 0.5;
            double dy = endY;

            // Draw the parallelogram
            g.strokeLine(startX, startY, bx, by);  // Line AB
            g.strokeLine(bx, by, cx, cy);          // Line BC
            g.strokeLine(cx, cy, dx, dy);          // Line CD
            g.strokeLine(dx, dy, startX, startY);  // Li/ Li Line D
        });
    }
    public void copyAndMove() {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        final WritableImage[] selectedArea = new WritableImage[1];
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        canvas2.setOnMousePressed(ex -> {
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(Color.ALICEBLUE);
            g.setLineWidth(3);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            g.strokeRect(rectX, rectY, width, height);
        });
        canvas2.setOnMouseReleased(ex -> {
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            if (width > 0 && height > 0) {
                SnapshotParameters params = new SnapshotParameters();
                params.setViewport(new Rectangle2D(rectX, rectY, width, height));
                selectedArea[0] = new WritableImage((int) width, (int) height);
                canvas2.snapshot(params, selectedArea[0]);
                offsetX[0] = ex.getX() - rectX;
                offsetY[0] = ex.getY() - rectY;
                controller.fillCanvasWithBackground();
                g.drawImage(tempCanvas, 0, 0);
                g.fillRect(rectX, rectY, selectedArea[0].getWidth(), selectedArea[0].getHeight());
            }
            canvas2.setOnMouseDragged(dragEvent -> {
                if (selectedArea[0] != null) {
                    controller.fillCanvasWithBackground();
                    g.drawImage(tempCanvas, 0, 0);
                    double newX = dragEvent.getX() - offsetX[0];
                    double newY = dragEvent.getY() - offsetY[0];
                    g.drawImage(selectedArea[0], newX, newY);
                }
            });
            canvas2.setOnMouseReleased(releaseEvent -> {
                if (selectedArea[0] != null) {
                    controller.fillCanvasWithBackground();
                    g.drawImage(tempCanvas, 0, 0);
                    double finalX = releaseEvent.getX() - offsetX[0];
                    double finalY = releaseEvent.getY() - offsetY[0];
                    g.drawImage(selectedArea[0], finalX, finalY);
                    selectedArea[0] = null;
                }
            });
        });
    }
    public void deselectCopy() {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        final WritableImage[] selectedArea = new WritableImage[1];
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        canvas2.setOnMousePressed(ex -> {
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(Color.ALICEBLUE);
            g.setLineWidth(3);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            g.strokeRect(rectX, rectY, width, height);
        });

        canvas2.setOnMouseReleased(ex -> {
            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            double width = abs(endX - startX);
            double height = abs(endY - startY);
            if (width > 0 && height > 0) {
                SnapshotParameters params = new SnapshotParameters();
                params.setViewport(new Rectangle2D(rectX, rectY, width, height));
                selectedArea[0] = new WritableImage((int) width, (int) height);
                canvas2.snapshot(params, selectedArea[0]);
                offsetX[0] = ex.getX() - rectX;
                offsetY[0] = ex.getY() - rectY;

                controller.fillCanvasWithBackground();
                g.drawImage(tempCanvas, 0, 0);
            }
            canvas2.setOnMouseDragged(dragEvent -> {
                if (selectedArea[0] != null) {
                    controller.fillCanvasWithBackground();
                    g.drawImage(tempCanvas, 0, 0);
                    double newX = dragEvent.getX() - offsetX[0];
                    double newY = dragEvent.getY() - offsetY[0];
                    g.drawImage(selectedArea[0], newX, newY);
                }
            });
            canvas2.setOnMouseReleased(releaseEvent -> {
                if (selectedArea[0] != null) {
                    controller.fillCanvasWithBackground();
                    g.drawImage(tempCanvas, 0, 0);
                    double finalX = releaseEvent.getX() - offsetX[0];
                    double finalY = releaseEvent.getY() - offsetY[0];
                    g.drawImage(selectedArea[0], finalX, finalY);
                    selectedArea[0] = null;
                }
            });
        });
    }
    public void drawCircle(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
            double centerX = Math.min(startX, endX) + radius;
            double centerY = Math.min(startY, endY) + radius;
            g.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
            double centerX = Math.min(startX, endX) + radius;
            double centerY = Math.min(startY, endY) + radius;
            g.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            controller.markAsUnsaved();
        });
    }
    public void drawEllipse(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            double topLeftX = Math.min(startX, endX);
            double topLeftY = Math.min(startY, endY);
            g.strokeOval(topLeftX, topLeftY, width, height);
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            double topLeftX = Math.min(startX, endX);
            double topLeftY = Math.min(startY, endY);
            g.strokeOval(topLeftX, topLeftY, width, height);
            controller.markAsUnsaved();
        });
    }

    public void useEraser(TextField brushSize) {
        canvas2.setOnMousePressed(ex -> {
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
        canvas2.setOnMouseDragged(ex -> {
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
    public void useEyedrop(ColorPicker colorPicker) {
        canvas2.setOnMousePressed(ex -> {
            startX = ex.getX();
            startY = ex.getY();
            WritableImage snap = canvas2.snapshot(null, null);
            PixelReader pixelReader = snap.getPixelReader();
            Color color = pixelReader.getColor((int) startX, (int) startY);
            colorPicker.setValue(color);
        });
    }
    public void drawTriangle(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double baseX1 = startX;
            double baseX2 = endX;
            double baseY = endY;
            double topX = (startX + endX) / 2;
            double topY = startY;
            g.strokePolygon(new double[]{baseX1, baseX2, topX}, new double[]{baseY, baseY, topY}, 3);

        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double baseX1 = startX;
            double baseX2 = endX;
            double baseY = endY;
            double topX = (startX + endX) / 2;
            double topY = startY;
            g.strokePolygon(new double[]{baseX1, baseX2, topX}, new double[]{baseY, baseY, topY}, 3);
            controller.markAsUnsaved();
        });
    }
    public void drawDiamond(TextField brushSize, ColorPicker colorPicker) {
        WritableImage tempCanvas = new WritableImage((int) canvas2.getWidth(), (int) canvas2.getHeight());
        canvas2.setOnMousePressed(ex -> {
            double size = Double.parseDouble(brushSize.getText());
            startX = ex.getX();
            startY = ex.getY();
            g.setStroke(colorPicker.getValue());
            g.setLineWidth(size);
            g.setLineCap(StrokeLineCap.ROUND);
            controller.saveState();
            controller.markAsUnsaved();
            canvas2.snapshot(null, tempCanvas);
        });
        canvas2.setOnMouseDragged(ex -> {
            g.clearRect(0, 0, canvas2.getWidth(), canvas2.getHeight());
            g.drawImage(tempCanvas, 0, 0);
            endX = ex.getX();
            endY = ex.getY();
            double centerX = (startX + endX) / 2;
            double centerY = (startY + endY) / 2;
            double halfWidth = Math.abs(endX - startX) / 2;
            double halfHeight = Math.abs(endY - startY) / 2;
            double topX = centerX;
            double topY = centerY - halfHeight;
            double rightX = centerX + halfWidth;
            double rightY = centerY;
            double bottomX = centerX;
            double bottomY = centerY + halfHeight;
            double leftX = centerX - halfWidth;
            double leftY = centerY;
            g.strokePolygon(new double[]{topX, rightX, bottomX, leftX}, new double[]{topY, rightY, bottomY, leftY}, 4);
        });
        canvas2.setOnMouseReleased(ex -> {
            endX = ex.getX();
            endY = ex.getY();
            double centerX = (startX + endX) / 2;
            double centerY = (startY + endY) / 2;
            double halfWidth = Math.abs(endX - startX) / 2;
            double halfHeight = Math.abs(endY - startY) / 2;
            double topX = centerX;
            double topY = centerY - halfHeight;
            double rightX = centerX + halfWidth;
            double rightY = centerY;
            double bottomX = centerX;
            double bottomY = centerY + halfHeight;
            double leftX = centerX - halfWidth;
            double leftY = centerY;
            g.strokePolygon(new double[]{topX, rightX, bottomX, leftX}, new double[]{topY, rightY, bottomY, leftY}, 4);
            controller.markAsUnsaved();
        });
    }
    public void dashTheLine() {
        g.setLineDashes(10);
    }
    public void unDashTheLine() {
        g.setLineDashes(0);
    }
}
