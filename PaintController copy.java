package com.example.paint;

//import org.nanohttpd.protocols.http.nanoHTTPD;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Controller class for PaintApplication, includes button functionalities and other methods */
public class PaintController {
    //initialize variables;
    private static PaintController instance;
    public PaintController() {}
    public static PaintController getInstance() {return instance;}
    public static void setInstance(PaintController controllerInstance) {instance = controllerInstance;}

    @FXML private Stage stage = new Stage();
    @FXML private Canvas canvas;
    @FXML private Label label = new Label();
    @FXML private FileChooser fileChooser = new FileChooser();
    @FXML private Text text = new Text("test");
    @FXML private TextField brushSize;
    @FXML private ToggleButton eraser;
    @FXML private ToggleButton deselect;
    @FXML private ToggleButton draw;
    @FXML private ToggleButton line;
    @FXML private Button clearCanvas;
    @FXML private ToggleButton eyedrop;
    @FXML private ToggleButton square;
    @FXML private ToggleButton rectangle;
    @FXML private ToggleButton triangle;
    @FXML private ToggleButton circle;
    @FXML private ToggleButton ellipse;
    @FXML private ToggleButton textBox;
    @FXML private ToggleButton diamond;
    @FXML private ToggleButton copy;
    @FXML private ToggleButton parallelogram;
    @FXML private ToggleButton polygon;
    @FXML private Button newTab;
    @FXML private TextField sides;
    @FXML private ColorPicker colorPicker;
    @FXML private Image loadedImage;
    @FXML private ToggleButton dashed;
    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane rootPane1;
    @FXML private Label colorLabel;
    @FXML private TabPane tabPane;
    @FXML private Button undo;
    @FXML private Button redo;
    @FXML private ToggleButton autoSave;
    @FXML private Button uploadCanvas;
    private ScheduledExecutorService executorService;
    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();

    private boolean hasUnsavedChanges = false;
    protected void markAsUnsaved() {
        hasUnsavedChanges = true;
    }
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    @FXML
    /** Runs on startup to set up the tabPane, undo and redo buttons, and icon images*/
    void initialize() {
        if (tabPane.getTabs().isEmpty()) {
            onNewTabButtonClick(null);  // Create a new default tab with a canvas
        }

        canvas.widthProperty().bind(rootPane.widthProperty()); //binds canvas to rootPane
        canvas.heightProperty().bind(rootPane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            fillCanvasWithBackground();
        });
        updateUndoRedoButtons();
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            fillCanvasWithBackground();
        });
        colorPicker.setOnAction(e -> {
            colorLabel.setText(ColorThing.colorToHex(colorPicker.getValue())); //prints hexcode to colorLabel
        });

        IconUploader uploader = new IconUploader();
        //UndoRedo ur = new UndoRedo(canvas);
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
        draw.setSelected(true);
        clearMouse();
    }
    /** Paints the canvas white to write over any existing color*/
    void fillCanvasWithBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        System.out.println("width: " + canvas.getWidth());
        System.out.println("height: " + canvas.getHeight());
    }
    void fillNewCanvasWithBackground() {
        Canvas tabCanvas = getCurrentTabCanvas();
        if (tabCanvas != null && tabCanvas.getWidth() > 0 && tabCanvas.getHeight() > 0) {
            GraphicsContext gc = tabCanvas.getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, tabCanvas.getWidth(), tabCanvas.getHeight());
            System.out.println("width: " + tabCanvas.getWidth());
            System.out.println("height: " + tabCanvas.getHeight());
        }
    }
    /** Saves the current state of the canvas in order to be restored if necessary */
    void saveState() {
        WritableImage snapshot = new WritableImage((int) getCurrentTabCanvas().getWidth(), (int) getCurrentTabCanvas().getHeight());
        getCurrentTabCanvas().snapshot(null, snapshot);
        if (undoStack.size() >= 5) {undoStack.removeFirst();}
        undoStack.push(snapshot);
        redoStack.clear();
        markAsUnsaved();
        updateUndoRedoButtons();
        //System.out.println("save state called");
    }

    void updateUndoRedoButtons() {
        undo.setDisable(undoStack.isEmpty());
        redo.setDisable(redoStack.isEmpty());
    }
    private WritableImage getCurrentCanvas() {
        WritableImage writableImage = new WritableImage((int) getCurrentTabCanvas().getWidth(), (int) getCurrentTabCanvas().getHeight());
        getCurrentTabCanvas().snapshot(null, writableImage);
        return writableImage;
    }
    private void restoreCanvas(WritableImage image) {
        GraphicsContext gc = getCurrentTabCanvas().getGraphicsContext2D();
        fillCanvasWithBackground();
        gc.drawImage(image, 0, 0);
    }
    @FXML void onUndoButtonClick() {
        undoRedoExtraction(undoStack, redoStack);
    }
    @FXML void onRedoButtonClick() {
        undoRedoExtraction(redoStack, undoStack);
    }
    private void undoRedoExtraction(Stack<WritableImage> fromStack, Stack<WritableImage> toStack) {
        if (!fromStack.isEmpty()) {
            toStack.push(getCurrentCanvas());
            if (toStack.size() > 5) {
                toStack.removeFirst();
            }
            WritableImage nextState = fromStack.pop();
            restoreCanvas(nextState);
            markAsUnsaved();
        }
        updateUndoRedoButtons();
    }

    @FXML
    void onDeselectButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        deselect.setSelected(true);
        if (deselect.isSelected()) {
            drawingTool.deselectCopy();
        }
    }
    @FXML void onCopyButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        copy.setSelected(true);
        if (copy.isSelected()) {
            drawingTool.copyAndMove();
        }
    }
    @FXML void onTextBoxButtonClick(ActionEvent e) {
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
    public void clear() {
        for (ToggleButton toggleButton : Arrays.asList(line, draw, eraser, square, rectangle, triangle, circle, eyedrop, ellipse, diamond, deselect, textBox, parallelogram, polygon, copy)) {
            toggleButton.setSelected(false);
        }
    }
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

    @FXML void onDrawButtonClick(ActionEvent e) {
        Canvas currentCanvas = getCurrentTabCanvas();
        if (currentCanvas != null) {
            DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
            clear();
            clearMouse();
            draw.setSelected(true);
            if (dashed.isSelected()) {
                drawingTool.dashTheLine();
            } else {
                drawingTool.unDashTheLine();
            }
            if (draw.isSelected()) {
                drawingTool.freeDrawing(brushSize, colorPicker);
            }
        } else {
            System.out.println("canvas is null");
        }
    }
    @FXML void onLineButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        line.setSelected(true);
        if (line.isSelected()) {
            drawingTool.drawLine(brushSize, colorPicker);
        }
    }
    @FXML void onEraserButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        eraser.setSelected(true);
        if (eraser.isSelected()) {
            drawingTool.useEraser(brushSize);
        }
    }
    @FXML void onEyedropButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        eyedrop.setSelected(true);
        if (eyedrop.isSelected()) {
            drawingTool.useEyedrop(colorPicker);
        }
    }
    @FXML void onClearCanvasButtonClick(ActionEvent e) {
        clear();
        clearMouse();
        hasUnsavedChanges = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear the Canvas?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            fillNewCanvasWithBackground();
        }
        else{

        }
    }
    @FXML void onParallelogramButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        parallelogram.setSelected(true);
        if (parallelogram.isSelected()) {
            drawingTool.drawParallelogram(brushSize, colorPicker);
        }
    }
    @FXML void onPolygonButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        polygon.setSelected(true);
        if (polygon.isSelected()) {
            drawingTool.drawPolygon(brushSize, colorPicker, sides);
        }
    }
    @FXML void onDiamondButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        diamond.setSelected(true);
        if (diamond.isSelected()) {
            drawingTool.drawDiamond(brushSize, colorPicker);
        }
    }
    @FXML void onTriangleButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        triangle.setSelected(true);
        if (triangle.isSelected()) {
            drawingTool.drawTriangle(brushSize, colorPicker);
        }
    }
    @FXML void onEllipseButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        ellipse.setSelected(true);
        if (ellipse.isSelected()) {
            drawingTool.drawEllipse(brushSize, colorPicker);
        }
    }
    @FXML void onCircleButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        circle.setSelected(true);
        if (circle.isSelected()) {
            drawingTool.drawCircle(brushSize, colorPicker);
        }
    }
    @FXML void onSquareButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        square.setSelected(true);
        if (square.isSelected()) {
            drawingTool.drawSquare(brushSize, colorPicker);
        }
    }
    @FXML void onRectangleButtonClick(ActionEvent e) {
        DrawingTool drawingTool = new DrawingTool(getCurrentTabCanvas());
        clear();
        clearMouse();
        if (dashed.isSelected()) {
            drawingTool.dashTheLine();
        } else {
            drawingTool.unDashTheLine();
        }
        rectangle.setSelected(true);
        if (rectangle.isSelected()) {
            drawingTool.drawRectangle(brushSize, colorPicker);
        }
    }
    @FXML void onUploadCanvasButtonClick(ActionEvent e) throws IOException {
        String serverUrl = "http://localhost:8000/paint";
        try {
            uploadCanvasToServer(serverUrl);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public BufferedImage canvasToBufferedImage(String format) {
        //converts entire canvas to a bufferedImage
        int width = (int) getCurrentTabCanvas().getWidth();
        int height = (int) getCurrentTabCanvas().getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        getCurrentTabCanvas().snapshot(null, writableImage);
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
    @FXML private void drawImageOnCanvas(Image image) {
        //draws the image on the canvas directly
        GraphicsContext g = getCurrentTabCanvas().getGraphicsContext2D();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double canvasWidth = getCurrentTabCanvas().getWidth();
        double canvasHeight = getCurrentTabCanvas().getHeight();
        //calculates aspect ratio to not stretch image
        double scaleFactor = Math.min(canvasWidth / imageWidth, canvasHeight / imageHeight);
        double newWidth = imageWidth * scaleFactor;
        double newHeight = imageHeight * scaleFactor;
        fillNewCanvasWithBackground();
        g.drawImage(image, (canvasWidth - newWidth) / 2, (canvasHeight - newHeight) / 2, newWidth, newHeight);
    }
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
                loadedImage = new Image(new FileInputStream(selectedFile));
                drawImageOnCanvas(loadedImage);//draws the copied image onto the canvas
                markAsUnsaved();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void onOpenButtonClick() {
        onOpenButtonClick(null);
    }
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
                BufferedImage bufferedImage = canvasToBufferedImage(format);
                //writes bufferedImage to file
                if (!ImageIO.write(bufferedImage, format, file)) {
                    System.out.println("Format not supported: " + format);
                } else {
                    System.out.println("Image saved successfully: " + file.getAbsolutePath());
                    hasUnsavedChanges = false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void onSaveAsButtonClick() {
        onSaveAsButtonClick(null);
    }
    @FXML void onSaveButtonClick(ActionEvent e) {
        //gets the value of the open file
        File selectedFile = new File(text.getText());
        try {
            String format = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1);
            BufferedImage bufferedImage = canvasToBufferedImage(format);
            ImageIO.write(bufferedImage, format, selectedFile);
            hasUnsavedChanges = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void onSaveButtonClick() {
        onSaveButtonClick(null);
    }
    @FXML void onAboutButtonClick(ActionEvent e) { //opens about pop-up
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
                             - Larger image accomodation
                             - Added help menu with 'about' dialogue
                
                             Known problems
                             - Cannot change the size of the image once uploaded
                
                             Upcoming Features
                             -""");
        alert.setWidth(500);
        alert.setHeight(465);
        alert.showAndWait();
    }
    @FXML
    void onNewTabButtonClick(ActionEvent e) {
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
        fillNewCanvasWithBackground();
    }
    public Canvas getCurrentTabCanvas() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof AnchorPane) {
            AnchorPane rootAnchorPane = (AnchorPane) selectedTab.getContent();
            // Check for the child AnchorPane
            for (Node node : rootAnchorPane.getChildren()) {
                if (node instanceof AnchorPane) {
                    AnchorPane childAnchorPane = (AnchorPane) node;
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
    private void uploadCanvasToServer(String serverUrl) throws IOException {
        byte[] imageBytes = CanvasUploader.canvasToImageBytes(getCurrentTabCanvas());
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "image/png");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(imageBytes);
            os.flush();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Canvas uploaded successfully.");
        } else {
            System.out.println("Canvas upload failed. Response code: " + responseCode);
        }
        connection.disconnect();
    }
    @FXML
    void onAutoSaveButtonClick(ActionEvent e) {
        if (autoSave.isSelected()) {
            startAutoSave();
        } else {
            stopAutoSave();
        }
    }
    private void startAutoSave() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(() -> Platform.runLater(this::onSaveButtonClick), 5, 5, TimeUnit.SECONDS);
        }
    }
    public void stopAutoSave() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
