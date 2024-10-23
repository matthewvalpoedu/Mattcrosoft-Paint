package com.example.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;

/** Class that contains methods for modifying the canvas */
public class CanvasModTool {
    /** Method to draw an image on a specific canvas */
    public void drawImageOnCanvas(Canvas canvas, Image image) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        // Calculates the aspect ratio to avoid stretching the image
        double scaleFactor = Math.min(canvasWidth / imageWidth, canvasHeight / imageHeight);
        double newWidth = imageWidth * scaleFactor;
        double newHeight = imageHeight * scaleFactor;
        // Draws the image centered on the canvas
        g.drawImage(image, (canvasWidth - newWidth) / 2, (canvasHeight - newHeight) / 2, newWidth, newHeight);
    }
    /** Converts a canvas to a bufferedImage to save it as an image */
    public BufferedImage canvasToBufferedImage(Canvas canvas, String format) {
        //converts entire canvas to a bufferedImage
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        canvas.snapshot(null, writableImage);
        BufferedImage bufferedImage;
        if (format.equals("jpg") || format.equals("bmp")) { //checks formatting to see if transparency is allowed or not; .jpg and .bmp do not allow transparent pixels
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        PixelReader pixelReader = writableImage.getPixelReader();
        for (int x = 0; x < width; x++) { //writes image
            for (int y = 0; y < height; y++) {
                Color fxColor = pixelReader.getColor(x, y);
                java.awt.Color awtColor;
                if (format.equals("jpg") || format.equals("bmp")) { //same transparency check as before
                    awtColor = new java.awt.Color((float) fxColor.getRed(),
                            (float) fxColor.getGreen(), (float) fxColor.getBlue());
                    bufferedImage.setRGB(x, y, awtColor.getRGB());
                } else {
                    awtColor = new java.awt.Color((float) fxColor.getRed(),
                            (float) fxColor.getGreen(), (float) fxColor.getBlue(),
                            (float) fxColor.getOpacity());
                    bufferedImage.setRGB(x, y, awtColor.getRGB());
                }
            }
        }

        return bufferedImage;
    }
}