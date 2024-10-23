package com.example.paint;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Controller class for PaintApplication, includes button functionalities and other methods */
public class PaintController {
    //initialize variables
    private static PaintController instance;
    public PaintController() {}
    public static PaintController getInstance() {return instance;}
    public static void setInstance(PaintController controllerInstance) {instance = controllerInstance;}

    @FXML private Stage stage = new Stage();
    @FXML private Canvas canvas;
    @FXML private Label label = new Label();
    @FXML private Label colorLabel;
    @FXML private FileChooser fileChooser = new FileChooser();
    @FXML private Text text = new Text("test");
    @FXML private TextField brushSize, sides, points, degrees;
    @FXML private ToggleButton eraser, deselect, draw, line, eyedrop, square, rectangle, triangle, circle, ellipse, textBox, diamond, copy, parallelogram, polygon, star, dashed, autoSave;
    @FXML private Button clearCanvas, rotate, mirror, flip, newTab, undo, redo;
    @FXML private ColorPicker colorPicker;
    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPane;
    @FXML private Label autoSaveLabel = new Label();

    private ScheduledExecutorService executorService;
    private boolean hasUnsavedChanges = false;
    private UndoRedoTool urt;
    private final CanvasModTool cmt = new CanvasModTool();

    protected void markAsUnsaved() {
        hasUnsavedChanges = true;
    }
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /** Runs on startup to set up the tabPane, undo and redo buttons, and icon images */
    @FXML void initialize() {
        urt = new UndoRedoTool(this);
        updateUndoRedoButtons();
        if (tabPane.getTabs().isEmpty()) {
            onNewTabButtonClick();  // Create a new default tab with a canvas
        }
        canvas.widthProperty().bind(rootPane.widthProperty()); //binds canvas to rootPane
        canvas.heightProperty().bind(rootPane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> fillCanvasWithBackground());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> fillCanvasWithBackground());
        colorPicker.setOnAction(e -> {
            colorLabel.setText(ColorThing.colorToHex(colorPicker.getValue())); //prints hex code to colorLabel
        });
        IconUploader uploader = new IconUploader();
        uploader.uploadImage(line, "/iconImages/line.png");
        uploader.uploadImage(draw, "/iconImages/draw.png");
        uploader.uploadImage(eraser, "/iconImages/eraser.png");
        uploader.uploadImage(clearCanvas, "/iconImages/clearCanvas.png");
        uploader.uploadImage(deselect, "/iconImages/deselect.png");
        uploader.uploadImage(eyedrop, "/iconImages/eyedrop.png");
        uploader.uploadImage(square, "/iconImages/square.png");
        uploader.uploadImage(rectangle, "/iconImages/rectangle.png");
        uploader.uploadImage(triangle, "/iconImages/triangle.png");
        uploader.uploadImage(circle, "/iconImages/circle.png");
        uploader.uploadImage(ellipse, "/iconImages/ellipse.png");
        uploader.uploadImage(diamond, "/iconImages/diamond.png");
        uploader.uploadImage(textBox, "/iconImages/textBox.png");
        uploader.uploadImage(copy, "/iconImages/copy.png");
        uploader.uploadImage(polygon, "/iconImages/polygon.png");
        uploader.uploadImage(dashed, "/iconImages/dashes.png");
        uploader.uploadImage(parallelogram, "/iconImages/parallelogram.png");
        uploader.uploadImage(undo, "/iconImages/undo.png");
        uploader.uploadImage(redo, "/iconImages/redo.png");
        uploader.uploadImage(autoSave, "/iconImages/autoSave.png");
        uploader.uploadImage(star, "/iconImages/star.png");
        uploader.uploadImage(rotate, "/iconImages/rotate.png");
        uploader.uploadImage(mirror, "/iconImages/mirror.png");
        uploader.uploadImage(flip, "/iconImages/flip.png");
        uploader.uploadImage(newTab, "/iconImages/newTab.png");
        clearMouse();
        clear();
        autoSaveLabel.setText("Auto Save is off");
        if (SystemTray.isSupported()) {
            System.out.println("System tray enabled");
        }
        String file = "/Users/matthewdemik/log.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))){} catch (IOException e) {e.printStackTrace();}
    }

    /** Paints the canvas white to write over any existing color */
    void fillCanvasWithBackground() {
        Canvas tabCanvas = getCurrentTabCanvas();
        if (tabCanvas != null && tabCanvas.getWidth() > 0 && tabCanvas.getHeight() > 0) {
            GraphicsContext gc = tabCanvas.getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, tabCanvas.getWidth(), tabCanvas.getHeight());
            System.out.println("width: " + tabCanvas.getWidth());
            System.out.println("height: " + tabCanvas.getHeight());
        }
    }

    /** Functionality for undo button */
    @FXML void onUndoButtonClick() {
        urt.undo();
        updateUndoRedoButtons();
    }
    /** Functionality for redo button */
    @FXML void onRedoButtonClick() {
        urt.redo();
        updateUndoRedoButtons();
    }
    /** If stacks are empty, disables undo and redo buttons respectively */
    public void updateUndoRedoButtons() {
        undo.setDisable(!urt.canUndo());
        redo.setDisable(!urt.canRedo());
    }
    /** Saves the current state of the canvas for an undo or a redo */
    public void saveState() {
        urt.saveState();
        updateUndoRedoButtons();
    }

    /** Calls the deselectCopy method from DrawingTool on the click of a toggle button */
    @FXML void onDeselectButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        deselect.setSelected(true);
        if (deselect.isSelected()) {
            drawingTool.deselectCopy();
        }
    }
    /** Calls the copyAndMove method from DrawingTool on the click of a toggle button */
    @FXML void onCopyButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        copy.setSelected(true);
        if (copy.isSelected()) {
            drawingTool.copyAndMove();
        }
    }

    /** Allows the user to create a textbox anywhere on the canvas */
    @FXML void onTextBoxButtonClick() {
        clear();
        clearMouse();
        getCurrentTabCanvas().setOnMousePressed(ex -> {
            TextArea textArea = new TextArea();
            double startX = ex.getX();
            double startY = ex.getY();
            textArea.setLayoutX(startX);
            textArea.setLayoutY(startY);
            rootPane.getChildren().add(textArea);
            textArea.requestFocus();
            markAsUnsaved();
            textArea.setOnKeyPressed(y -> {
                if (y.getCode() == KeyCode.ENTER) {
                    String text = textArea.getText();
                    GraphicsContext gc = getCurrentTabCanvas().getGraphicsContext2D();
                    gc.setFill(colorPicker.getValue());
                    gc.setFont(Font.font("Arial", 20));
                    gc.fillText(text, startX, startY);
                    rootPane.getChildren().remove(textArea);
                }
            });
        });
    }

    /** Method to clear the toggle button selections */
    public void clear() {
        for (ToggleButton toggleButton : Arrays.asList(line, draw, eraser, square, rectangle, triangle, circle, eyedrop, ellipse, diamond, deselect, textBox, parallelogram, polygon, star, copy)) {
            toggleButton.setSelected(false);
        }
    }
    /** Method to clear previously logged mouse events */
    void clearMouse() {
        Canvas currentCanvas = getCurrentTabCanvas();
        if (currentCanvas != null) {
            currentCanvas.setOnMousePressed(null);
            currentCanvas.setOnMouseDragged(null);
            currentCanvas.setOnMouseClicked(null);
            currentCanvas.setOnMouseEntered(null);
            currentCanvas.setOnMouseExited(null);
            currentCanvas.setOnMouseMoved(null);
            currentCanvas.setOnMouseReleased(null);
        }
    }

    /** Calls the freeDrawing method from DrawingTool on the click of a toggle button */
    @FXML void onDrawButtonClick() {
        Canvas currentCanvas = getCurrentTabCanvas();
        if (currentCanvas != null) {
            DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
            clear();
            clearMouse();
            draw.setSelected(true);
            logUpdate(" Draw button selected");
            if (draw.isSelected()) {
                drawingTool.freeDrawing(brushSize, colorPicker, dashed);
            }
        } else {
            System.out.println("canvas is null");
        }
    }
    /** Calls the drawLine method from DrawingTool on the click of a toggle button */
    @FXML void onLineButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        line.setSelected(true);
        logUpdate(" Line button selected");
        if (line.isSelected()) {
            drawingTool.drawLine(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the useEraser method from DrawingTool on the click of a toggle button */
    @FXML void onEraserButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        eraser.setSelected(true);
        logUpdate(" Eraser button selected");
        if (eraser.isSelected()) {
            drawingTool.useEraser(brushSize);
        }
    }
    /** Calls the useEyedrop method from DrawingTool on the click of a toggle button */
    @FXML void onEyedropButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        eyedrop.setSelected(true);
        logUpdate(" Eyedrop button selected");
        if (eyedrop.isSelected()) {
            drawingTool.useEyedrop(colorPicker);
        }
    }
    /** Clears the entire canvas of any changes after user confirmation */
    @FXML void onClearCanvasButtonClick() {
        clear();
        clearMouse();
        hasUnsavedChanges = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear the Canvas?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            fillCanvasWithBackground();
            logUpdate(" Canvas cleared");
        }
        else{

        }
    }
    /** Calls the drawParallelogram method from DrawingTool on the click of a toggle button */
    @FXML void onParallelogramButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        parallelogram.setSelected(true);
        logUpdate(" Parallelogram button selected");
        if (parallelogram.isSelected()) {
            drawingTool.drawParallelogram(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawPolygon method from DrawingTool on the click of a toggle button */
    @FXML void onPolygonButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        polygon.setSelected(true);
        logUpdate(" Polygon button selected");
        if (polygon.isSelected()) {
            drawingTool.drawPolygon(brushSize, colorPicker,dashed, sides);
        }
    }
    /** Calls the drawStar method from DrawingTool on the click of a toggle button */
    @FXML void onStarButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        star.setSelected(true);
        logUpdate(" Star button selected");
        if (star.isSelected()) {
            drawingTool.drawStar(brushSize, colorPicker, dashed, points);
        }
    }
    /** Calls the drawDiamond method from DrawingTool on the click of a toggle button */
    @FXML void onDiamondButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        diamond.setSelected(true);
        logUpdate(" Diamond button selected");
        if (diamond.isSelected()) {
            drawingTool.drawDiamond(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawTriangle method from DrawingTool on the click of a toggle button */
    @FXML void onTriangleButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        triangle.setSelected(true);
        logUpdate(" Triangle button selected");
        if (triangle.isSelected()) {
            drawingTool.drawTriangle(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawEllipse method from DrawingTool on the click of a toggle button */
    @FXML void onEllipseButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        ellipse.setSelected(true);
        logUpdate(" Ellipse button selected");
        if (ellipse.isSelected()) {
            drawingTool.drawEllipse(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawCircle method from DrawingTool on the click of a toggle button */
    @FXML void onCircleButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        circle.setSelected(true);
        logUpdate(" Circle button selected");
        if (circle.isSelected()) {
            drawingTool.drawCircle(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawSquare method from DrawingTool on the click of a toggle button */
    @FXML void onSquareButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        square.setSelected(true);
        logUpdate(" Square button selected");
        if (square.isSelected()) {
            drawingTool.drawSquare(brushSize, colorPicker, dashed);
        }
    }
    /** Calls the drawRectangle method from DrawingTool on the click of a toggle button */
    @FXML void onRectangleButtonClick() {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        rectangle.setSelected(true);
        logUpdate(" Rectangle button selected");
        if (rectangle.isSelected()) {
            drawingTool.drawRectangle(brushSize, colorPicker, dashed);
        }
    }

    /** Opens a file chooser and allows the user to select image files from their computer to upload to the canvas */
    @FXML void onOpenButtonClick(ActionEvent e) {
        //opens file explorer
        fileChooser.setTitle("Open Image File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll( //filters for only image file extensions in the file explorer
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG image", "*.jpg"), new FileChooser.ExtensionFilter("PNG image", "*.png"),
                new FileChooser.ExtensionFilter("GIF image", "*.gif"), new FileChooser.ExtensionFilter("BMP image", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            text.setText(selectedFile.toString());
            label.setText(selectedFile.getName());
            try {
                Image loadedImage = new Image(new FileInputStream(selectedFile));
                fillCanvasWithBackground();
                cmt.drawImageOnCanvas(getCurrentTabCanvas(), loadedImage);
                logUpdate(" Image " + selectedFile.getAbsolutePath() + " opened"); //draws the copied image onto the canvas
                markAsUnsaved();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /** Method needed to attach accelerators */
    public void onOpenButtonClick() {
        onOpenButtonClick(null);
    }

    /** Allows the user to save the current canvas anywhere on their computer */
    @FXML void onSaveAsButtonClick(ActionEvent e) {
        if (fileChooser.getExtensionFilters().isEmpty()) {
            fileChooser.getExtensionFilters().addAll( //filters for only image file extensions in the file explorer
                    new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.bmp"),
                    new FileChooser.ExtensionFilter("JPEG image", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG image", "*.png"),
                    new FileChooser.ExtensionFilter("GIF image", "*.gif"),
                    new FileChooser.ExtensionFilter("BMP image", "*.bmp")
            );
        }
        File file = fileChooser.showSaveDialog(stage); //opens save pop-up
        if (file != null) {
            try {
                String fileName = file.getName();
                String format = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "png";
                if (!fileName.toLowerCase().endsWith("." + format)) {
                    file = new File(file.getAbsolutePath() + "." + format);
                }
                //converts image to bufferedImage
                BufferedImage bufferedImage = cmt.canvasToBufferedImage(getCurrentTabCanvas(), format);
                //writes bufferedImage to file
                if (!ImageIO.write(bufferedImage, format, file)) {
                    System.out.println("Format not supported: " + format);
                } else {
                    System.out.println("Image saved successfully: " + file.getAbsolutePath());
                    logUpdate(" Image saved as " + file.getAbsolutePath());
                    hasUnsavedChanges = false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /** Method needed to attach accelerators */
    public void onSaveAsButtonClick() {
        onSaveAsButtonClick(null);
    }

    /** Allows the user to save the current canvas in the current file location */
    @FXML void onSaveButtonClick(ActionEvent e) {
        if (Objects.equals(text.getText(), "test")) {
            onSaveAsButtonClick(null);
        } else {
            File selectedFile = new File(text.getText());
            try {
                String format = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1);
                BufferedImage bufferedImage = cmt.canvasToBufferedImage(getCurrentTabCanvas(), format);
                ImageIO.write(bufferedImage, format, selectedFile);
                logUpdate(" Image " + selectedFile.getAbsolutePath() + " saved");
                hasUnsavedChanges = false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /** Method needed to attach accelerators */
    public void onSaveButtonClick() {
        onSaveButtonClick(null);
    }

    /** Pops up information about the current version of Mattcrosoft Pain(t) */
    @FXML void onAboutButtonClick() { //opens about pop-up
        logUpdate(" About button clicked");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(null);
        alert.setTitle("About");
        alert.setHeaderText("Mattcrosoft Pain(t) v1.1.0");
        alert.setContentText("""
                Welcome to Mattcrosoft Pain(t) v1.1.0!\s
                
                Added Features
                             - Opens an image and shows it
                             - Added save button
                             - Added save as button
                             - Added .jpg, .bmp, .png, and .gif support
                             - Allows the user to draw a line
                             - Allows the user to choose the size and color of their line
                             - Allows the user to save and save as an edited image in any of the image types
                             - Larger image accommodation
                             - Added help menu with 'about' dialogue
                
                             Known problems
                             - Cannot change the size of the image once uploaded
                
                             Upcoming Features
                             -""");
        alert.setWidth(500);
        alert.setHeight(465);
        alert.showAndWait();
    }

    /** Creates a new tab with a new canvas */
    @FXML void onNewTabButtonClick() {
        logUpdate(" New tab opened");
        Tab newTab = new Tab("Untitled Tab " + (tabPane.getTabs().size() + 1));
        AnchorPane newAnchorPane = new AnchorPane();
        newAnchorPane.setPrefHeight(817.0);
        newAnchorPane.setPrefWidth(1880.0);
        AnchorPane nestedAnchorPane = new AnchorPane();
        newAnchorPane.getChildren().add(nestedAnchorPane);
        AnchorPane.setTopAnchor(newAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(newAnchorPane, 0.0);
        AnchorPane.setRightAnchor(newAnchorPane, 0.0);
        AnchorPane.setBottomAnchor(newAnchorPane, 0.0);
        nestedAnchorPane.setPrefHeight(817.0);
        nestedAnchorPane.setPrefWidth(1880.0);
        Canvas newCanvas = new Canvas(1880, 817);
        nestedAnchorPane.getChildren().add(newCanvas);
        AnchorPane.setTopAnchor(newCanvas, 0.0);
        AnchorPane.setBottomAnchor(newCanvas, 0.0);
        AnchorPane.setLeftAnchor(newCanvas, 0.0);
        AnchorPane.setRightAnchor(newCanvas, 0.0);
        newTab.setContent(newAnchorPane);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        fillCanvasWithBackground();
    }
    /** Returns the canvas of the tab that is currently being viewed by the user */
    public Canvas getCurrentTabCanvas() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof AnchorPane rootAnchorPane) {
            // Check for the child AnchorPane
            for (Node node : rootAnchorPane.getChildren()) {
                if (node instanceof AnchorPane childAnchorPane) {
                    // Now look for the Canvas within this child AnchorPane
                    for (Node childNode : childAnchorPane.getChildren()) {
                        if (childNode instanceof Canvas) {
                            return (Canvas) childNode;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Pops up a label that notifies the user that autosave is enabled */
    private void autoSaveNotification() {
        Popup popup = new Popup();
        Label label = new Label("Automatically saved.");
        label.setStyle("-fx-font-size: 16px;");
        StackPane pane = new StackPane(label);
        pane.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");
        pane.setAlignment(Pos.TOP_RIGHT);
        popup.getContent().add(pane);
        Scene scene = canvas.getScene();
        if (scene != null) {
            popup.show(scene.getWindow());
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> popup.hide()));
        timeline.play();

    }
    /** Toggles on or off autosave using a toggle button */
    @FXML void onAutoSaveButtonClick() {
        if (autoSave.isSelected()) {
            logUpdate(" Autosave turned on");
            startAutoSave();
            autoSaveNotification();
        } else {
            logUpdate(" Autosave turned off");
            stopAutoSave();
        }
    }
    /** Automatically saves the current canvas every 5 seconds */
    private void startAutoSave() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(() -> Platform.runLater(this::onSaveButtonClick), 5, 5, TimeUnit.SECONDS);
        }
    }
    /** Shuts down autosave */
    public void stopAutoSave() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /** Calls rotateCanvasWithSnapshot method from RotateTool on the click of a button */
    @FXML void onRotateCanvasButtonClick() {
        RotateTool rotateTool = new RotateTool(getCurrentTabCanvas());
        rotateTool.rotateCanvasWithSnapshot(degrees);
        logUpdate(" Canvas rotated " + degrees);
    }
    /** Calls mirrorCanvas method from RotateTool on the click of a button */
    @FXML void onMirrorCanvasButtonClick() {
        RotateTool rotateTool = new RotateTool(getCurrentTabCanvas());
        rotateTool.mirrorCanvas();
        logUpdate(" Canvas mirrored");
    }
    /** Calls flipCanvas method from RotateTool on the click of a button */
    @FXML void onFlipCanvasButtonClick() {
        RotateTool rotateTool = new RotateTool(getCurrentTabCanvas());
        rotateTool.flipCanvas();
        logUpdate(" Canvas flipped");
    }
    /** Updates a log file with text showing the actions performed during runtime */
    public void logUpdate(String addThisText) {
        String file = "/Users/matthewdemik/log.txt";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))){
            writer.write("[" + dtf.format(now) + "]" + addThisText);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
