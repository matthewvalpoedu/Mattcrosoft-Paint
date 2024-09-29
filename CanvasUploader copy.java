package com.example.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
/** Class to convert canvas to a byte array to be uploaded to the web server */
public class CanvasUploader {
    // Method to convert the canvas to a byte array
    public static byte[] canvasToImageBytes(Canvas canvas) throws IOException {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);
        PixelReader pixelReader = writableImage.getPixelReader();
        byte[] buffer = new byte[(int) (canvas.getWidth() * canvas.getHeight() * 4)];
        pixelReader.getPixels(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(),
                PixelFormat.getByteBgraInstance(), buffer, 0, (int) canvas.getWidth() * 4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
            deflaterOutputStream.write(buffer);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
