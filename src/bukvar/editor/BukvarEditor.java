/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.editor;

import bukvar.structs.Bukvar;
import bukvar.structs.ImgContainer;
import bukvar.structs.Lession;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author jovan
 */
public class BukvarEditor extends Application {
    
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 1000;
    private static final int DEFAULT_SPACING = 10;
    
    private static final int GROUP_HEADER_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_HEADER_HEIGHT = 80;
    
    private static final int GROUP_TABLE_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_TABLE_HEIGHT = 200;
    private static final int CANVAS_WIDTH = GROUP_TABLE_WIDTH - 2 * DEFAULT_SPACING;
    private static final int CANVAS_HEIGHT = 120;
    private static final int ERASER = 5;
    
    private static final int GROUP_IMAGES_WIDTH = 400;
    private static final int GROUP_IMAGES_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT - GROUP_TABLE_HEIGHT;
    private static final int BTN_NEW_IMAGE_WIDTH = 150;
    private static final int BTN_NEW_IMAGE_HEIGHT = 40;
    private static final int GROUP_IMAGE_LIST_WIDTH = GROUP_IMAGES_WIDTH - 2 * DEFAULT_SPACING;
    private static final int GROUP_IMAGE_LIST_HEIGHT = GROUP_IMAGES_HEIGHT - 3 * DEFAULT_SPACING - BTN_NEW_IMAGE_HEIGHT;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = IMAGE_WIDTH;
    
    private static final int GROUP_PARAMETERS_WIDTH = 600;
    private static final int GROUP_PARAMETERS_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT - GROUP_TABLE_HEIGHT;
    private static final int GROUP_PARAM_1_HEIGHT = 80;
    
    private static final Set<String> alphabet = new HashSet<>(Arrays.asList(
            "А", "Б", "В", "Г", "Д", "Ђ", "Е"," Ж", "З", "И", "Ј", "К", "Л", "Љ", "М", 
            "Н", "Њ", "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ", "Ш"
    ));
    
    private static final Map<String, Color> colors = new HashMap<String, Color>(){{
        put("Red", Color.RED);
        put("Green", Color.GREEN);
        put("Blue", Color.BLUE);
    }};
    
    private Stage stage;
    private Scene scene;
    private Group root;
    
    private Group groupHeader;
    private ComboBox<String> comboLessions;
    private Button btnSave;
    private Button btnDelete;
    private Button btnDefault;
    private TextField fieldNewName;
    private Button btnNewLession;
    
    private Group groupTable;
    private ComboBox<String> comboColor;
    private Spinner<Integer> spinTick;
    private CheckBox checkStraightLine;
    private CheckBox checkEraser;
    private Group groupCanvas;
    private DrawableCanvas canvas;
    private Line tempLine;
    
    private Group groupImages;
    private Button btnNewImage;
    private Group groupImageList;
    private ScrollPane spImageList;
     
    private Group groupParams;
    private CheckBox checkTimeIndef;
    private Spinner<Integer> spinTime;
    private Spinner<Integer> spinImgsToShow;
    private Spinner<Integer> spinMinMatchingImgs;
    
    private FileChooser fileChooser;
    
    private Bukvar bukvar;
    private Lession selectedLession;
    private double rememberedScrollPane = 0.0;
    
    class LetterChangeListener implements ChangeListener<String> {
        
        private TextField fieldLetter;
        private int imageId;
        
        public LetterChangeListener(TextField fieldLetter, int imageId) {
            this.fieldLetter = fieldLetter;
            this.imageId = imageId;
        }
        
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (fieldLetter.getText().length() > 0) {
                String l = fieldLetter.getText().substring(0, 1).toUpperCase();
                if (alphabet.contains(l)) { 
                    fieldLetter.textProperty().removeListener(this);
                    fieldLetter.setText(l);
                    fieldLetter.textProperty().addListener(this);
                    selectedLession.images.get(imageId).letter = l;
                } else { 
                    fieldLetter.textProperty().removeListener(this);
                    fieldLetter.setText(""); 
                    fieldLetter.textProperty().addListener(this);
                }
            }
        }
    }
    
    class DrawableCanvas extends Canvas {
        public DrawableCanvas(double width, double height) {
            super(width, height);
            final GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.setStroke(colors.get(comboColor.getValue()));
            gc.setLineWidth(spinTick.getValue());
            addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                    if (checkStraightLine.isSelected()) {
                        gc.setStroke(colors.get(comboColor.getValue()));
                        tempLine.setStartX(event.getX());
                        tempLine.setStartY(event.getY());
                        tempLine.setEndX(event.getX());
                        tempLine.setEndY(event.getY());
                        tempLine.setStroke(colors.get(comboColor.getValue()));
                    } else if (checkEraser.isSelected()) {
                        gc.setStroke(Color.WHITE);
                        int width = ERASER * spinTick.getValue();
                        gc.fillRect(event.getX() - width / 2, event.getY() - width / 2, width, width); gc.stroke();
                    } else {
                        gc.setStroke(colors.get(comboColor.getValue()));
                        gc.beginPath(); gc.moveTo(event.getX(), event.getY()); gc.stroke();
                    }
                }
            });
            addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                    if (checkStraightLine.isSelected()) {
                        tempLine.setEndX(event.getX());
                        tempLine.setEndY(event.getY());
                    } else if (checkEraser.isSelected()) {
                        int width = ERASER * spinTick.getValue();
                        gc.rect(event.getX() - width / 2, event.getY() - width / 2, width, width); gc.stroke();
                    } else {
                        gc.lineTo(event.getX(), event.getY()); gc.stroke();
                    }
                }
            });
            addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                    if (checkStraightLine.isSelected()) {
                        gc.beginPath(); gc.moveTo(tempLine.getStartX(), tempLine.getStartY()); gc.stroke();
                        gc.lineTo(event.getX(), event.getY()); gc.stroke();
                        tempLine.setStartX(0);
                        tempLine.setStartY(0);
                        tempLine.setEndX(0);
                        tempLine.setEndY(0);
                    } else if (checkEraser.isSelected()) {
                        gc.setStroke(colors.get(comboColor.getValue()));
                    }
                }
            });
        }
    }
            
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Images", "jpg", "png", "gif", "bmp"
        ));
        
        groupHeader = new Group();
        {
            Rectangle border = new Rectangle(1, 1, GROUP_HEADER_WIDTH - 2, GROUP_HEADER_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);
            
            comboLessions = new ComboBox<>();
            comboLessions.setTranslateX(DEFAULT_SPACING);
            comboLessions.setTranslateY(2 * DEFAULT_SPACING);
            comboLessions.setMinWidth(200);
            comboLessions.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            comboLessions.valueProperty().addListener(new ChangeListener<String>() {
                @Override 
                public void changed(ObservableValue ov, String t, String t1) { 
                    if (t1 != null) { saveBukvar(t1); }
                }
            });
            btnSave = new Button();
            btnSave.setText("Сачувај");
            btnSave.setMaxWidth(120);
            btnSave.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnSave.setTranslateX(230);
            btnSave.setTranslateY(2 * DEFAULT_SPACING);
            btnSave.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) { saveBukvar(null); }
            });
            btnDelete = new Button();
            btnDelete.setText("Обриши");
            btnDelete.setMaxWidth(120);
            btnDelete.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDelete.setTranslateX(330);
            btnDelete.setTranslateY(2 * DEFAULT_SPACING);
            btnDelete.setOnAction((ActionEvent event) -> {
                if (bukvar.deleteLession(selectedLession.name)) {
                    refreshLessions();
                    selectLession(bukvar.defaultLessionName);
                }
            });
            btnDefault = new Button();
            btnDefault.setText("Подразумевана");
            btnDefault.setMaxWidth(150);
            btnDefault.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDefault.setTranslateX(430);
            btnDefault.setTranslateY(2 * DEFAULT_SPACING);
            btnDefault.setOnAction((ActionEvent event) -> { bukvar.defaultLessionName = selectedLession.name; });
            fieldNewName = new TextField("");
            fieldNewName.fontProperty().set(Font.font(STYLESHEET_CASPIAN, 20));
            fieldNewName.setTranslateX(680);
            fieldNewName.setTranslateY(2 * DEFAULT_SPACING);
            fieldNewName.setMaxWidth(150);
            btnNewLession = new Button();
            btnNewLession.setText("Нова лекција");
            btnNewLession.setMaxWidth(150);
            btnNewLession.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnNewLession.setTranslateX(850);
            btnNewLession.setTranslateY(2 * DEFAULT_SPACING);
            btnNewLession.setOnAction((ActionEvent event) -> {
                String name = fieldNewName.getText();
                if (name.length() > 0 && !bukvar.lessions.containsKey(name)) {
                    bukvar.lessions.put(name, new Lession(name, blankTable()));
                    refreshLessions();
                    selectLession(name);
                    fieldNewName.setText("");
                }
            });
            
            groupHeader.getChildren().addAll(border, comboLessions, btnSave, btnDelete, btnDefault, fieldNewName, btnNewLession);
        }
        
        groupTable = new Group();
        {
            groupTable.setTranslateY(GROUP_HEADER_HEIGHT);
            
            Rectangle border = new Rectangle(1, 1, GROUP_TABLE_WIDTH - 2, GROUP_TABLE_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);
            Text text1 = new Text(DEFAULT_SPACING, 3.5 * DEFAULT_SPACING, "Боја: ");
            ObservableList<String> obsColors = FXCollections.observableArrayList(colors.keySet());
            comboColor = new ComboBox<>(obsColors);
            comboColor.setValue(colors.keySet().iterator().next());
            comboColor.setTranslateX(60);
            comboColor.setTranslateY(DEFAULT_SPACING);
            comboColor.setMinWidth(170);
            comboColor.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            comboColor.valueProperty().addListener((ov, t, t1) -> {
                if (canvas != null) { canvas.getGraphicsContext2D().setStroke(colors.get(t1)); }
            });
            Text text2 = new Text(260, 3.5 * DEFAULT_SPACING, "Дебљина линије: ");
            spinTick = new Spinner(1, 5, 3);
            spinTick.setTranslateX(400);
            spinTick.setTranslateY(DEFAULT_SPACING);
            spinTick.setMaxWidth(120);
            spinTick.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            spinTick.valueProperty().addListener((ov, t, t1) -> {
                if (canvas != null) { canvas.getGraphicsContext2D().setLineWidth(t1); }
            });
            checkStraightLine = new CheckBox("Права линија");
            checkStraightLine.setTranslateX(600);
            checkStraightLine.setTranslateY(2 * DEFAULT_SPACING);
            checkStraightLine.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    if (checkStraightLine.isSelected()) { checkEraser.setSelected(false); }
                }
            });
            checkEraser = new CheckBox("Гумица");
            checkEraser.setTranslateX(750);
            checkEraser.setTranslateY(2 * DEFAULT_SPACING);
            checkEraser.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    if (checkEraser.isSelected()) { checkStraightLine.setSelected(false); }
                }
            });
            groupCanvas = new Group();
            groupCanvas.setTranslateX(DEFAULT_SPACING);
            groupCanvas.setTranslateY(2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT);
            canvas = new DrawableCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
            groupCanvas.getChildren().add(canvas);
            tempLine = new Line(0, 0, 0, 0);
            tempLine.setTranslateX(DEFAULT_SPACING);
            tempLine.setTranslateY(2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT);
            Rectangle borderCanv = new Rectangle(DEFAULT_SPACING - 1, 2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT - 1, CANVAS_WIDTH + 2, CANVAS_HEIGHT + 2);
            borderCanv.setFill(Color.TRANSPARENT);
            borderCanv.setStroke(Color.BLACK);
            
            groupTable.getChildren().addAll(border, text1, comboColor, text2, spinTick, checkStraightLine, checkEraser, borderCanv, groupCanvas, tempLine);
        }
        
        groupImages = new Group();
        {
            groupImages.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT);
            
            btnNewImage = new Button();
            {
                btnNewImage.setText("Нова слика");
                btnNewImage.setMinWidth(BTN_NEW_IMAGE_WIDTH);
                btnNewImage.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
                btnNewImage.setTranslateX((GROUP_IMAGES_WIDTH - BTN_NEW_IMAGE_WIDTH) / 2);
                btnNewImage.setTranslateY(DEFAULT_SPACING);
                btnNewImage.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            String fPath = file.toURI().toString();
                            Image image = loadImage(fPath);
                            String imageType = fPath.substring(fPath.length() - 3);
                            selectedLession.images.add(new ImgContainer(image, imageType, "А"));
                            addImage(selectedLession.images.size() - 1);
                            spImageList.setVvalue(1.0);
                        }
                    }
                });
            }

            Rectangle borderGroupImages = new Rectangle(1, 1, GROUP_IMAGES_WIDTH - 2, GROUP_IMAGES_HEIGHT - 2);
            borderGroupImages.setFill(Color.TRANSPARENT);
            borderGroupImages.setStroke(Color.BLACK);
            
            groupImageList = new Group();
            
            spImageList = new ScrollPane(groupImageList);
            spImageList.setTranslateX(DEFAULT_SPACING);
            spImageList.setTranslateY(2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT);
            spImageList.setMaxHeight(GROUP_IMAGE_LIST_HEIGHT);
            spImageList.setMinWidth(GROUP_IMAGE_LIST_WIDTH);
            spImageList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            groupImages.getChildren().addAll(btnNewImage, borderGroupImages, spImageList);
        }
        
        groupParams = new Group();
        {
            groupParams.setTranslateX(GROUP_IMAGES_WIDTH);
            groupParams.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT);
            
            Rectangle borderGroupParams = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2, GROUP_PARAMETERS_HEIGHT - 2);
            borderGroupParams.setFill(Color.TRANSPARENT);
            borderGroupParams.setStroke(Color.BLACK);
            
            Group groupParam1 = new Group();
            {
                groupParam1.setTranslateX(DEFAULT_SPACING);
                groupParam1.setTranslateY(DEFAULT_SPACING);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2 * DEFAULT_SPACING - 2, 80);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Време за одабир слика у секундама: ");
                checkTimeIndef = new CheckBox("Неограничено");
                checkTimeIndef.setTranslateX(DEFAULT_SPACING);
                checkTimeIndef.setTranslateY(4 * DEFAULT_SPACING);
                checkTimeIndef.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        spinTime.setDisable(checkTimeIndef.isSelected());
                        selectedLession.timeToPickImgsIndef = checkTimeIndef.isSelected();
                    }
                });
                spinTime = new Spinner<>(5, 20, 5);
                spinTime.setTranslateX(200);
                spinTime.setTranslateY(4 * DEFAULT_SPACING);
                spinTime.valueProperty().addListener((ov, t, t1) -> { selectedLession.timeToPickImgsSecs = t1; });
                
                groupParam1.getChildren().addAll(border, text, checkTimeIndef, spinTime);
            }
            
            Group groupParam2 = new Group();
            {
                groupParam2.setTranslateX(DEFAULT_SPACING);
                groupParam2.setTranslateY(2 * DEFAULT_SPACING + GROUP_PARAM_1_HEIGHT);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2 * DEFAULT_SPACING - 2, GROUP_PARAM_1_HEIGHT);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Број понуђених слика: ");
                spinImgsToShow = new Spinner<>(4, 10, 1);
                spinImgsToShow.setTranslateX(DEFAULT_SPACING);
                spinImgsToShow.setTranslateY(4 * DEFAULT_SPACING);
                spinImgsToShow.valueProperty().addListener((ov, t, t1) -> {
                    IntegerSpinnerValueFactory isvf = (IntegerSpinnerValueFactory)spinMinMatchingImgs.getValueFactory();
                    isvf.setMax(t1 - 1);
                    selectedLession.imgsToPresent = t1;
                });
                groupParam2.getChildren().addAll(border, text, spinImgsToShow);
            }
            
            Group groupParam3 = new Group();
            {
                groupParam3.setTranslateX(DEFAULT_SPACING);
                groupParam3.setTranslateY(3 * DEFAULT_SPACING + 2 * GROUP_PARAM_1_HEIGHT);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2 * DEFAULT_SPACING - 2, GROUP_PARAM_1_HEIGHT);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Минимални број коректних слика: ");
                spinMinMatchingImgs = new Spinner<>(0, 3, 1);
                spinMinMatchingImgs.setTranslateX(DEFAULT_SPACING);
                spinMinMatchingImgs.setTranslateY(4 * DEFAULT_SPACING);
                spinMinMatchingImgs.valueProperty().addListener((ov, t, t1) -> { selectedLession.minMatchingImgs = t1; });
                groupParam3.getChildren().addAll(border, text, spinMinMatchingImgs);
            }
            
            groupParams.getChildren().addAll(borderGroupParams, groupParam1, groupParam2, groupParam3);
        }
        
        root = new Group();
        root.getChildren().addAll(groupHeader, groupTable, groupImages, groupParams);
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        bukvar = Bukvar.getBukvar(blankTable());
        selectLession(bukvar.defaultLessionName);
        refreshLessions();
        
        primaryStage.setTitle("Буквар - Едитор");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(false);
        primaryStage.show();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() { saveBukvar(null); }
        });
        btnNewImage.toFront();
    }
    
    private void refreshLessions() {
        comboLessions.getItems().clear();
        comboLessions.getItems().addAll(bukvar.lessions.keySet());
        comboLessions.setValue(bukvar.defaultLessionName);
    }
    
    private void selectLession(String lessionName) {
        saveBukvar(null);
        groupImageList.getChildren().clear();
        
        selectedLession = bukvar.lessions.get(lessionName);
        comboLessions.setValue(lessionName);
        
        canvas.getGraphicsContext2D().drawImage(selectedLession.table, 0, 0);
        
        for (int i = 0; i < selectedLession.images.size(); i++) {
            addImage(i);
        }
        
        checkTimeIndef.setSelected(selectedLession.timeToPickImgsIndef);
        spinTime.getValueFactory().setValue(selectedLession.timeToPickImgsSecs);
        spinTime.setDisable(selectedLession.timeToPickImgsIndef);
        spinImgsToShow.getValueFactory().setValue(selectedLession.imgsToPresent);
        spinMinMatchingImgs.getValueFactory().setValue(selectedLession.minMatchingImgs);
        
        spImageList.setVvalue(rememberedScrollPane);
        rememberedScrollPane = 0.0;
    }
    
    private Image loadImage(String path) {
        Image image = new Image(path);
        if (image.isError()) { return null; }
        
        double scale = 1.0;
        double w = image.getWidth();
        double h = image.getHeight();
        if (w > h) {
            scale = IMAGE_WIDTH / w;
        } else {
            scale = IMAGE_HEIGHT / h;
        }
        image = new Image(path, w * scale, h * scale, true, false);
        return image;
    }
    
    private void addImage(int imageId) {
        Group groupImage = new Group();
        
        Line topLine = new Line(0, 0, GROUP_IMAGE_LIST_WIDTH, 0);
        Image image = selectedLession.images.get(imageId).image;
        Rectangle rectImage = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        rectImage.setTranslateX(DEFAULT_SPACING + (IMAGE_WIDTH - image.getWidth()) / 2);
        rectImage.setTranslateY(DEFAULT_SPACING + (IMAGE_HEIGHT - image.getHeight()) / 2);
        rectImage.setFill(new ImagePattern(image));
        rectImage.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    String fPath = file.toURI().toString();
                    Image image = loadImage(fPath);
                    String imageType = fPath.substring(fPath.length() - 3);
                    selectedLession.images.get(imageId).image = image;
                    selectedLession.images.get(imageId).imgType = imageType;
                    rectImage.setWidth(image.getWidth());
                    rectImage.setHeight(image.getHeight());
                    rectImage.setTranslateX(DEFAULT_SPACING + (IMAGE_WIDTH - image.getWidth()) / 2);
                    rectImage.setTranslateY(DEFAULT_SPACING + (IMAGE_HEIGHT - image.getHeight()) / 2);
                    rectImage.setFill(new ImagePattern(image));
                }
            }
        });
        Button btnRemove = new Button("Уклони слику");
        btnRemove.setTranslateX(2 * DEFAULT_SPACING + IMAGE_WIDTH);
        btnRemove.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
        btnRemove.setTranslateY(2 * DEFAULT_SPACING);
        btnRemove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectedLession.images.remove(imageId);
                rememberedScrollPane = spImageList.getVvalue();
                selectLession(selectedLession.name);
            }
        });
        TextField letter = new TextField(selectedLession.images.get(imageId).letter);
        letter.setTranslateX(2 * DEFAULT_SPACING + IMAGE_WIDTH);
        letter.setTranslateY(3 * DEFAULT_SPACING + 2 * BTN_NEW_IMAGE_HEIGHT);
        letter.setMaxWidth(IMAGE_WIDTH / 2);
        letter.fontProperty().set(Font.font(STYLESHEET_CASPIAN, BTN_NEW_IMAGE_HEIGHT));
        letter.textProperty().addListener(new LetterChangeListener(letter, imageId));
        Line bottomLine = new Line(0, 2 * DEFAULT_SPACING + IMAGE_HEIGHT, GROUP_IMAGE_LIST_WIDTH, 2 * DEFAULT_SPACING + IMAGE_HEIGHT);
        groupImage.getChildren().addAll(topLine, rectImage, btnRemove, letter, bottomLine);
        groupImage.setTranslateY(imageId * (2 * DEFAULT_SPACING + IMAGE_HEIGHT));
        
        groupImageList.getChildren().add(groupImage);
    }
    
    private Image blankTable() {
        WritableImage wim = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
        DrawableCanvas canvas = new DrawableCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.snapshot(null, wim);
        return wim;
    }
    
    private void saveBukvar(String nextLession) {
        if (selectedLession == null) {
            return;
        }
        WritableImage wim = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvas.snapshot(null, wim);
                selectedLession.table = wim;
                bukvar.save();
                if (nextLession != null) {
                    selectLession(nextLession);
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
