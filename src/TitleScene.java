import javafx.application.*;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TitleScene extends Application {

    Pane currentPane;

    //method to change the current Pane
    public void setCurrentPane(Pane pane){
        currentPane = pane;
    }

    public Pane getCurrentPane(){
        return currentPane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //elements
        Button browse = new Button("Upload your file for processing...");
        browse.setStyle("-fx-base: #FF7F50; -fx-text-fill:white;");
        browse.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                browse.setEffect(new DropShadow());
            }
        });
        browse.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                browse.setEffect(null);
            }
        });
        browse.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Text Files", "*.pdf"),
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
                );
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    // extract table from pdf files
                    ImageProcess ip = new ImageProcess(selectedFile);
                    List<Product> productList = ip.extractFromTable(selectedFile);
                }
            }
        });

        Button browse2 = new Button("Upload your invoices for text recognition...");
        browse2.setStyle("-fx-base: #FF7F50; -fx-text-fill:white;");
        browse2.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                browse.setEffect(new DropShadow());
            }
        });
        browse2.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                browse.setEffect(null);
            }
        });
        browse2.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose the invoice");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
                );
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    // pass file to convert to jpg then perform image processing
                    ImageProcess ip = new ImageProcess(selectedFile);

                    //another Panel for cropping the images

                    //Stage as a pop up window
                    final Stage dialog = new Stage();
                    BorderPane cropper = new BorderPane();
                    Text text = new Text ("Crop the image");
                    text.setUnderline(true);
                    text.setStyle("-fx-font-size: 25px; -fx-background-color: #FF7F50; -fx-text-fill: white;");

                    cropper.setTop(text);

                    //pane for image cropper
                    Pane imageCropper = new Pane();
                    ImageView image = new ImageView();
                    javafx.scene.image.Image img = new javafx.scene.image.Image(selectedFile.toURI().toString());
                    image.setFitHeight(550);
                    image.setFitWidth(700);
                    image.setPreserveRatio(true);
                    image.setImage(img);

                    javafx.scene.image.Image preserved = new javafx.scene.image.Image(selectedFile.toURI().toString(), 700, 550, true, true, false);

                    if (img != null) {
                        double w = 0;
                        double h = 0;

                        double ratioX = image.getFitWidth() / img.getWidth();
                        double ratioY = image.getFitHeight() / img.getHeight();

                        double reducCoeff = 0;
                        if(ratioX >= ratioY) {
                            reducCoeff = ratioY;
                        } else {
                            reducCoeff = ratioX;
                        }

                        w = img.getWidth() * reducCoeff;
                        h = img.getHeight() * reducCoeff;

                        image.setX((image.getFitWidth() - w) / 2);
                        image.setY((image.getFitHeight() - h) / 2);
                    }

                    imageCropper.getChildren().add(image);

                    Rectangle frame = createDraggableRectangle(image.getX()+image.getLayoutX(), image.getY()+image.getLayoutY(), preserved.getWidth(), preserved.getHeight());
                    frame.opacityProperty().set(0.5);

                    imageCropper.getChildren().add(frame);

                    cropper.setCenter(imageCropper);

                    //the buttons to crop and cancel
                    HBox buttons = new HBox();
                    Button buttonCancel = new Button("Cancel");
                    Button buttonCrop = new Button("Crop");
                    buttonCancel.setStyle("-fx-base: #FF7F50; -fx-text-fill:white;");
                    buttonCrop.setStyle("-fx-base: #FF7F50; -fx-text-fill:white;");

                    buttonCrop.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            // first crop the image using the rectangle and corner position
                            Circle topleftcorner = corners.get(0);
                            Circle bottomrightcorner = corners.get(1);
                            double x = topleftcorner.getCenterX();
                            double y = topleftcorner.getCenterY();
                            double width = bottomrightcorner.getCenterX() - x;
                            double height = bottomrightcorner.getCenterY() - y;
                            double image_startx = image.getLayoutX() + image.getX();
                            double image_starty = image.getLayoutY() + image.getY();
                            x = x - image_startx; //x-coordinates in resized image
                            x = x < 0? 0 : x;
                            y = y - image_starty; //y-coordinates in resized image
                            y = y < 0? 0 : y;

                            double ratioX = image.getFitWidth() / img.getWidth();
                            double ratioY = image.getFitHeight() / img.getHeight();

                            double reducCoeff = 0;
                            if(ratioX >= ratioY) {
                                reducCoeff = ratioY;
                            } else {
                                reducCoeff = ratioX;
                            }
                            if (reducCoeff != 0){
                                x = x/reducCoeff;
                                y = y/reducCoeff;
                                width = width/reducCoeff;
                                height = height/reducCoeff;
                            }

                            try {
                                BufferedImage image = ImageIO.read(selectedFile);
                                image = image.getSubimage((int)Math.round(x), (int)Math.round(y), (int)Math.round(width), (int)Math.round(height));
                                System.out.println(ip.getImageRecognition(image, "invoice"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog.close();
                        }
                    });

                    buttonCancel.setOnMouseClicked(new EventHandler<MouseEvent>(){

                        @Override
                        public void handle(MouseEvent event) {
                            dialog.close();
                        }
                    });

                    buttons.getChildren().add(buttonCrop);
                    buttons.getChildren().add(buttonCancel);
                    buttons.setSpacing(20);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.setMinHeight(50);

                    cropper.setBottom(buttons);

                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    dialog.setResizable(false);
                    Scene dialogScene = new Scene(cropper, 700, 650);
                    dialog.setScene(dialogScene);
                    dialog.show();

                }
            }
        });



        //Pane for uploading pdf files for conversion
        BorderPane borderPane = new BorderPane();
        HBox hbox = new HBox();
        Text text_product = new Text("Uploading product details to database...");
        text_product.setStyle("-fx-font-size: 25; -fx-underline: True;");
        hbox.getChildren().add(text_product);
       // hbox.setStyle("-fx-background-color: #FF8362; -fx-text-fill: white;");
        borderPane.setTop(hbox);

        VBox vbox = new VBox();
        vbox.getChildren().add(browse);
        vbox.getChildren().add(new Text("Please either upload pdf file or images in the format of png/jpg/tiff"));
        borderPane.setCenter(vbox);
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER);

        currentPane = borderPane;

        //Pane for uploading invoices for conversion
        BorderPane borderPaneI = new BorderPane();
        HBox hboxI = new HBox();
        Text text_invoice = new Text("Uploading scanned invoices for text recognition...");
        text_invoice.setStyle("-fx-font-size: 25; -fx-underline: True;");
        hboxI.getChildren().add(text_invoice);
       // hboxI.setStyle("-fx-background-color: #FF8362; -fx-text-fill: white;");
        borderPaneI.setTop(hboxI);

        VBox vboxI = new VBox();
        vboxI.getChildren().add(browse2);
        vboxI.getChildren().add(new Text("Please either upload pdf file or images in the format of png/jpg/tiff"));
        borderPaneI.setCenter(vboxI);
        vboxI.setSpacing(5);
        vboxI.setAlignment(Pos.CENTER);

        //Pane for displaying other panes and switching between invoice / product
        BorderPane mainPane = new BorderPane();
        HBox invoiceOrProduct = new HBox();
        invoiceOrProduct.setStyle("-fx-background-color: #FF8362; -fx-text-fill: white");
        Button text1 = new Button("Product Details");
        Button text2 = new Button("Invoice");
        text1.setStyle("-fx-background-color: #CD5B45; -fx-text-fill: white");
        text2.setStyle("-fx-background-color: #FF8362; -fx-text-fill: white");

        text1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainPane.setCenter(borderPane);
                text1.setStyle("-fx-background-color:#CD5B45; -fx-text-fill: white;");
                text2.setStyle("-fx-background-color:#FF8362; -fx-text-fill: white;");
                mainPane.requestLayout();
            }
        });

        text2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainPane.setCenter(borderPaneI);
                text1.setStyle("-fx-background-color:#FF8362; -fx-text-fill: white;");
                text2.setStyle("-fx-background-color:#CD5B45; -fx-text-fill: white;");
                mainPane.requestLayout();
            }
        });

        invoiceOrProduct.getChildren().add(text1);
        invoiceOrProduct.getChildren().add(text2);
        invoiceOrProduct.setSpacing(10);

        mainPane.setTop(invoiceOrProduct);
        mainPane.setCenter(getCurrentPane());

        Group root = new Group();
        Scene scene = new Scene(mainPane, 600, 300);
        primaryStage.setTitle("PDF Reader");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    List<Circle> corners = null;

    private Rectangle createDraggableRectangle(double x, double y, double width, double height) {
        final double handleRadius = 5 ;

        Rectangle rect = new Rectangle(x, y, width, height);

        // top left resize handle:
        Circle resizeHandleNW = new Circle(handleRadius, Color.GOLD);
        // bind to top left corner of Rectangle:
        resizeHandleNW.centerXProperty().bind(rect.xProperty());
        resizeHandleNW.centerYProperty().bind(rect.yProperty());

        // bottom right resize handle:
        Circle resizeHandleSE = new Circle(handleRadius, Color.GOLD);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.centerXProperty().bind(rect.xProperty().add(rect.widthProperty()));
        resizeHandleSE.centerYProperty().bind(rect.yProperty().add(rect.heightProperty()));

        // move handle:
        Circle moveHandle = new Circle(handleRadius, Color.ROYALBLUE);

        // bind to bottom center of Rectangle:
        moveHandle.centerXProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)));
        moveHandle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty()));

        corners = Arrays.asList(resizeHandleNW, resizeHandleSE, moveHandle);
        // force circles to live in same parent as rectangle:
        rect.parentProperty().addListener((obs, oldParent, newParent) -> {
            for (Circle c : corners) {
                Pane currentParent = (Pane)c.getParent();
                if (currentParent != null) {
                    currentParent.getChildren().remove(c);
                }
                ((Pane)newParent).getChildren().add(c);
            }
        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        setUpDragging(resizeHandleNW, mouseLocation) ;
        setUpDragging(resizeHandleSE, mouseLocation) ;
        setUpDragging(moveHandle, mouseLocation) ;

        resizeHandleNW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                if (newX >= handleRadius
                        && newX <= rect.getX() + rect.getWidth() - handleRadius) {
                    rect.setX(newX);
                    rect.setWidth(rect.getWidth() - deltaX);
                }
                double newY = rect.getY() + deltaY ;
                if (newY >= handleRadius
                        && newY <= rect.getY() + rect.getHeight() - handleRadius) {
                    rect.setY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxX = rect.getX() + rect.getWidth() + deltaX ;
                if (newMaxX >= rect.getX()
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    rect.setWidth(rect.getWidth() + deltaX);
                }
                double newMaxY = rect.getY() + rect.getHeight() + deltaY ;
                if (newMaxY >= rect.getY()
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    rect.setHeight(rect.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        moveHandle.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                double newMaxX = newX + rect.getWidth();
                if (newX >= handleRadius
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    rect.setX(newX);
                }
                double newY = rect.getY() + deltaY ;
                double newMaxY = newY + rect.getHeight();
                if (newY >= handleRadius
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    rect.setY(newY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }

        });

        return rect ;
    }

    private void setUpDragging(Circle circle, Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            circle.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            circle.getParent().setCursor(Cursor.DEFAULT);
            mouseLocation.value = null ;
        });
    }

    static class Wrapper<T> { T value ; }



    public static void main(String args[]){
        launch(args);
    }
}
