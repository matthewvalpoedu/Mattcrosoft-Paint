package com.example.paint;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;

public class PaintApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //creates the gui
        FXMLLoader fxmlLoader = new FXMLLoader(PaintApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        PaintController controller = fxmlLoader.getController();
        PaintController.setInstance(controller);
        initializeAccelerators(scene, controller);
        stage.setTitle("Mattcrosoft Pain(t)");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> { //unsaved close alert
            if (controller.hasUnsavedChanges()) {
                e.consume();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes");
                alert.setContentText("Do you want to save your changes before exiting?");
                ButtonType saveButton = new ButtonType("Save");
                ButtonType dontSaveButton = new ButtonType("Don't Save");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
                alert.showAndWait().ifPresent(response -> {
                    if (response == saveButton) {
                        controller.onSaveButtonClick();
                        stage.close();
                    } else if (response == dontSaveButton) {
                        stage.close();
                    } else {
                        //wait for input
                    }
                });
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        //start gui
        launch();
    }
    public void initializeAccelerators(Scene scene, PaintController controller) { //keybinds
        KeyCombination oc = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(oc, controller::onOpenButtonClick);

        KeyCombination sc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(sc, controller::onSaveButtonClick);

        KeyCombination sac = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        scene.getAccelerators().put(sac, controller::onSaveAsButtonClick);
    }
}
