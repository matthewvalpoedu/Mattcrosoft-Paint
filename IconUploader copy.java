package com.example.paint;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;
/** Class for uploading icon images to the buttons and toggle buttons in the GUI */
public class IconUploader {
    public void uploadImage(ToggleButton toggleButton, String imagePath) { //uploads icon images to togglebuttons
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()); //grabs image from resource folder
            ImageView lineImage = new ImageView(icon);
            toggleButton.setGraphic(lineImage);
            toggleButton.setContentDisplay(ContentDisplay.CENTER);
            lineImage.fitWidthProperty().bind(toggleButton.widthProperty().subtract(10)); //sets alignment and borders properly
            lineImage.setPreserveRatio(true);
            toggleButton.setMaxWidth(Double.MAX_VALUE);
        } catch (NullPointerException e) {
            System.out.println("Image not found. Please check the file path.");
        }
    }
    public void uploadImage(Button button, String imagePath) { //uploads icon images to normal buttons
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()); //grabs image from resource folder
            ImageView lineImage = new ImageView(icon);
            button.setGraphic(lineImage);
            button.setContentDisplay(ContentDisplay.CENTER);
            lineImage.fitWidthProperty().bind(button.widthProperty().subtract(10)); //sets alignment and borders properly
            lineImage.setPreserveRatio(true);
            button.setMaxWidth(Double.MAX_VALUE);
        } catch (NullPointerException e) {
            System.out.println("Image not found. Please check the file path.");
        }
    }
}
