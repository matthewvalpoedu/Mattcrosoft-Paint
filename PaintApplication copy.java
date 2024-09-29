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
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
/** Main paint application, launches the GUI, sets keybindings, and starts the web server */
public class PaintApplication extends Application {
    public PaintApplication() throws IOException {
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Creates the GUI
        FXMLLoader fxmlLoader = new FXMLLoader(PaintApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        PaintController controller = fxmlLoader.getController();
        PaintController.setInstance(controller);
        initializeAccelerators(scene, controller);
        stage.setTitle("Mattcrosoft Pain(t)");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> { // Unsaved close alert
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
                        // Wait for input
                    }
                });
            }
        });
        stage.show();
    }

    public static void main(String[] args) throws IOException {
        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Context for uploading the canvas
        server.createContext("/paint", new UploadHandler());

        // Context for serving the uploaded image
        server.createContext("/uploaded_canvas.png", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                File file = new File("uploaded_canvas.png");
                if (file.exists()) {
                    exchange.getResponseHeaders().set("Content-Type", "image/png");
                    exchange.sendResponseHeaders(200, file.length());
                    try (OutputStream os = exchange.getResponseBody();
                         FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1); // File not found
                }
            }
        });

        server.setExecutor(null); // Default executor
        server.start(); // Start the server

        launch(); // Start the JavaFX application
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                byte[] imageBytes = exchange.getRequestBody().readAllBytes();
                try (OutputStream os = new FileOutputStream("uploaded_canvas.png")) {
                    os.write(imageBytes);
                }
                String response = "Canvas uploaded successfully!";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }
    /** Sets up keybinds to be used when the application is running*/
    public void initializeAccelerators(Scene scene, PaintController controller) { // Keybinds
        KeyCombination oc = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(oc, controller::onOpenButtonClick);

        KeyCombination sc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(sc, controller::onSaveButtonClick);

        KeyCombination sac = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        scene.getAccelerators().put(sac, controller::onSaveAsButtonClick);
    }
}
