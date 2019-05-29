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
import javafx.event.EventHandler;
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
    private static final int WINDOW_HEIGHT = 730;
    private static final int DEFAULT_SPACING = 10;
    
    private static final int GROUP_HEADER_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_HEADER_HEIGHT = 80;
    
    private static final int GROUP_TABLE_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_TABLE_HEIGHT = 200;
    private static final int CANVAS_WIDTH = GROUP_TABLE_WIDTH - 2 * DEFAULT_SPACING;
    private static final int CANVAS_HEIGHT = 120;
    private static final int ERASER = 5;
    
    private static final int GROUP_PARAMETERS_WIDTH = WINDOW_WIDTH;
    private static final int PARAM_SPIN_WIDTH = 100;
    private static final int GROUP_PARAM_1_HEIGHT = 80;
    private static final int GROUP_PARAM_1_WIDTH = (GROUP_PARAMETERS_WIDTH - 4 * DEFAULT_SPACING) / 3;
    private static final int GROUP_PARAMETERS_HEIGHT = 2 * DEFAULT_SPACING + GROUP_PARAM_1_HEIGHT;
    
    private static final int GROUP_IMAGES_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_IMAGES_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT - GROUP_TABLE_HEIGHT - GROUP_PARAMETERS_HEIGHT + DEFAULT_SPACING;
    private static final int BTN_NEW_IMAGE_WIDTH = 150;
    private static final int BTN_NEW_IMAGE_HEIGHT = 40;
    private static final int GROUP_IMAGE_LIST_WIDTH = GROUP_IMAGES_WIDTH - 2 * DEFAULT_SPACING;
    private static final int GROUP_IMAGE_LIST_HEIGHT = GROUP_IMAGES_HEIGHT - 3 * DEFAULT_SPACING - BTN_NEW_IMAGE_HEIGHT;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = IMAGE_WIDTH;
    
    private static final Set<String> alphabet = new HashSet<>(Arrays.asList(
            "А", "Б", "В", "Г", "Д", "Ђ", "Е","Ж", "З", "И", "Ј", "К", "Л", "Љ", "М",
            "Н", "Њ", "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ", "Ш"
    ));
    
    private static final Map<String, Color> colors = new HashMap<String, Color>(){{
        put("Red", Color.RED);
        put("Green", Color.GREEN);
        put("Blue", Color.BLUE);
    }};
    
    private Stage stage;

    private ComboBox<String> comboLessions;
    private TextField fieldNewName;

    private ComboBox<String> comboColor;
    private Spinner<Integer> spinTick;
    private CheckBox checkStraightLine;
    private CheckBox checkEraser;
    private DrawableCanvas canvas;
    private Line tempLine;

    private CheckBox checkTimeIndef;
    private Spinner<Integer> spinTime;
    private Spinner<Integer> spinImgsToShow;
    private Spinner<Integer> spinMinMatchingImgs;

    private Group groupImageList;
    private ScrollPane spImageList;
    
    private FileChooser fileChooser;
    
    private Bukvar bukvar;
    private Lession selectedLession;
    private double rememberedScrollPane = 0.0;

    class LetterChangeListener implements ChangeListener<String> {
        
        private TextField fieldLetter;
        int imageId;
        
        LetterChangeListener(TextField fieldLetter, int imageId) {
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
        DrawableCanvas(double width, double height) {
            super(width, height);
            final GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.setStroke(colors.get(comboColor.getValue()));
            gc.setLineWidth(spinTick.getValue());
            addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                if (checkStraightLine.isSelected()) {
                    gc.setStroke(colors.get(comboColor.getValue()));
                    tempLine.setStartX(event.getX());
                    tempLine.setStartY(event.getY());
                    tempLine.setEndX(event.getX());
                    tempLine.setEndY(event.getY());
                    tempLine.setStroke(colors.get(comboColor.getValue()));
                } else if (checkEraser.isSelected()) {
                    gc.setStroke(Color.WHITE);
                    int width12 = ERASER * spinTick.getValue();
                    gc.fillRect(event.getX() - (width12 >> 1), event.getY() - (width12 >> 1), width12, width12); gc.stroke();
                } else {
                    gc.setStroke(colors.get(comboColor.getValue()));
                    gc.beginPath(); gc.moveTo(event.getX(), event.getY()); gc.stroke();
                }
            });
            addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                if (checkStraightLine.isSelected()) {
                    tempLine.setEndX(event.getX());
                    tempLine.setEndY(event.getY());
                } else if (checkEraser.isSelected()) {
                    int width1 = ERASER * spinTick.getValue();
                    gc.rect(event.getX() - (width1 >> 1), event.getY() - (width1 >> 1), width1, width1); gc.stroke();
                } else {
                    gc.lineTo(event.getX(), event.getY()); gc.stroke();
                }
            });
            addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
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

        Group groupHeader = new Group();
        {
            Rectangle border = new Rectangle(1, 1, GROUP_HEADER_WIDTH - 2, GROUP_HEADER_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);
            
            comboLessions = new ComboBox<>();
            comboLessions.setTranslateX(DEFAULT_SPACING);
            comboLessions.setTranslateY(2 * DEFAULT_SPACING);
            comboLessions.setMinWidth(200);
            comboLessions.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            comboLessions.valueProperty().addListener((ov, t, t1) -> { if (t1 != null) { saveBukvar(t1); } });
            Button btnSave = new Button();
            btnSave.setText("Сачувај");
            btnSave.setMaxWidth(120);
            btnSave.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnSave.setTranslateX(230);
            btnSave.setTranslateY(2 * DEFAULT_SPACING);
            btnSave.setOnAction(event -> saveBukvar(null));
            Button btnDelete = new Button();
            btnDelete.setText("Обриши");
            btnDelete.setMaxWidth(120);
            btnDelete.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDelete.setTranslateX(330);
            btnDelete.setTranslateY(2 * DEFAULT_SPACING);
            btnDelete.setOnAction((ActionEvent event) -> {
                if (bukvar.deleteLession(selectedLession.name)) {
                    refreshLessions();
                    comboLessions.setValue(bukvar.defaultLessionName);
                }
            });
            Button btnDefault = new Button();
            btnDefault.setText("Подразумевана");
            btnDefault.setMaxWidth(150);
            btnDefault.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDefault.setTranslateX(430);
            btnDefault.setTranslateY(2 * DEFAULT_SPACING);
            btnDefault.setOnAction((ActionEvent event) -> bukvar.defaultLessionName = selectedLession.name);
            fieldNewName = new TextField("");
            fieldNewName.fontProperty().set(Font.font(STYLESHEET_CASPIAN, 20));
            fieldNewName.setTranslateX(680);
            fieldNewName.setTranslateY(2 * DEFAULT_SPACING);
            fieldNewName.setMaxWidth(150);
            Button btnNewLesson = new Button();
            btnNewLesson.setText("Нова лекција");
            btnNewLesson.setMaxWidth(150);
            btnNewLesson.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnNewLesson.setTranslateX(850);
            btnNewLesson.setTranslateY(2 * DEFAULT_SPACING);
            btnNewLesson.setOnAction((ActionEvent event) -> {
                String name = fieldNewName.getText();
                if (name.length() > 0 && !bukvar.lessions.containsKey(name)) {
                    bukvar.lessions.put(name, new Lession(name, blankTable()));
                    refreshLessions();
                    comboLessions.setValue(name);
                    fieldNewName.setText("");
                }
            });
            
            groupHeader.getChildren().addAll(border, comboLessions, btnSave, btnDelete, btnDefault, fieldNewName, btnNewLesson);
        }

        Group groupTable = new Group();
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
            checkStraightLine.setOnAction(event -> {
                if (checkStraightLine.isSelected()) { checkEraser.setSelected(false); }
            });
            checkEraser = new CheckBox("Гумица");
            checkEraser.setTranslateX(750);
            checkEraser.setTranslateY(2 * DEFAULT_SPACING);
            checkEraser.setOnAction(event -> {
                if (checkEraser.isSelected()) { checkStraightLine.setSelected(false); }
            });
            Group groupCanvas = new Group();
            groupCanvas.setTranslateX(DEFAULT_SPACING);
            groupCanvas.setTranslateY(2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT);
            canvas = new DrawableCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
            groupCanvas.getChildren().add(canvas);
            tempLine = new Line(0, 0, 0, 0);
            tempLine.setTranslateX(DEFAULT_SPACING);
            tempLine.setTranslateY(2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT);
            Rectangle borderCanvas = new Rectangle(DEFAULT_SPACING - 1, 2 * DEFAULT_SPACING + BTN_NEW_IMAGE_HEIGHT - 1, CANVAS_WIDTH + 2, CANVAS_HEIGHT + 2);
            borderCanvas.setFill(Color.TRANSPARENT);
            borderCanvas.setStroke(Color.BLACK);
            
            groupTable.getChildren().addAll(border, text1, comboColor, text2, spinTick, checkStraightLine, checkEraser, borderCanvas, groupCanvas, tempLine);
        }

        Group groupParams = new Group();
        {
            groupParams.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT);
            
            Rectangle borderGroupParams = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2, GROUP_PARAMETERS_HEIGHT - 2);
            borderGroupParams.setFill(Color.TRANSPARENT);
            borderGroupParams.setStroke(Color.BLACK);
            
            Group groupParam1 = new Group();
            {
                groupParam1.setTranslateX(DEFAULT_SPACING);
                groupParam1.setTranslateY(DEFAULT_SPACING);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAM_1_WIDTH - 2, GROUP_PARAM_1_HEIGHT - 2);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Време за одабир слика у секундама: ");
                checkTimeIndef = new CheckBox("Неограничено");
                checkTimeIndef.setTranslateX(DEFAULT_SPACING);
                checkTimeIndef.setTranslateY(4 * DEFAULT_SPACING);
                checkTimeIndef.setOnAction(event -> {
                    spinTime.setDisable(checkTimeIndef.isSelected());
                    selectedLession.timeToPickImgsIndef = checkTimeIndef.isSelected();
                });
                spinTime = new Spinner<>(5, 20, 5);
                spinTime.setTranslateX(170);
                spinTime.setTranslateY(4 * DEFAULT_SPACING);
                spinTime.setMaxWidth(PARAM_SPIN_WIDTH);
                spinTime.valueProperty().addListener((ov, t, t1) -> selectedLession.timeToPickImgsSecs = t1);
                
                groupParam1.getChildren().addAll(border, text, checkTimeIndef, spinTime);
            }
            
            Group groupParam2 = new Group();
            {
                groupParam2.setTranslateX(2 * DEFAULT_SPACING + GROUP_PARAM_1_WIDTH);
                groupParam2.setTranslateY(DEFAULT_SPACING);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAM_1_WIDTH - 2, GROUP_PARAM_1_HEIGHT - 2);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Број понуђених слика: ");
                spinImgsToShow = new Spinner<>(4, 10, 1);
                spinImgsToShow.setTranslateX(DEFAULT_SPACING);
                spinImgsToShow.setTranslateY(4 * DEFAULT_SPACING);
                spinImgsToShow.setMaxWidth(PARAM_SPIN_WIDTH);
                spinImgsToShow.valueProperty().addListener((ov, t, t1) -> {
                    IntegerSpinnerValueFactory isvf = (IntegerSpinnerValueFactory)spinMinMatchingImgs.getValueFactory();
                    isvf.setMax(t1 - 1);
                    selectedLession.imgsToPresent = t1;
                });
                groupParam2.getChildren().addAll(border, text, spinImgsToShow);
            }
            
            Group groupParam3 = new Group();
            {
                groupParam3.setTranslateX(3 * DEFAULT_SPACING + 2 * GROUP_PARAM_1_WIDTH);
                groupParam3.setTranslateY(DEFAULT_SPACING);
                Rectangle border = new Rectangle(1, 1, GROUP_PARAM_1_WIDTH - 2, GROUP_PARAM_1_HEIGHT - 2);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.BLACK);
                Text text = new Text(DEFAULT_SPACING, 2 * DEFAULT_SPACING, "Минимални број коректних слика: ");
                spinMinMatchingImgs = new Spinner<>(0, 3, 1);
                spinMinMatchingImgs.setTranslateX(DEFAULT_SPACING);
                spinMinMatchingImgs.setTranslateY(4 * DEFAULT_SPACING);
                spinMinMatchingImgs.setMaxWidth(PARAM_SPIN_WIDTH);
                spinMinMatchingImgs.valueProperty().addListener((ov, t, t1) -> selectedLession.minMatchingImgs = t1);
                groupParam3.getChildren().addAll(border, text, spinMinMatchingImgs);
            }
            
            groupParams.getChildren().addAll(borderGroupParams, groupParam1, groupParam2, groupParam3);
        }

        Group groupImages = new Group();
        Button btnNewImage;
        {
            groupImages.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT + GROUP_PARAMETERS_HEIGHT);
            
            btnNewImage = new Button();
            {
                btnNewImage.setText("Нова слика");
                btnNewImage.setMinWidth(BTN_NEW_IMAGE_WIDTH);
                btnNewImage.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
                btnNewImage.setTranslateX((GROUP_IMAGES_WIDTH - BTN_NEW_IMAGE_WIDTH) >> 1);
                btnNewImage.setTranslateY(DEFAULT_SPACING);
                btnNewImage.setOnAction(event -> {
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        String fPath = file.toURI().toString();
                        Image image = loadImage(fPath);
                        String imageType = fPath.substring(fPath.length() - 3);
                        selectedLession.images.add(new ImgContainer(image, imageType, "А"));
                        addImage(selectedLession.images.size() - 1);
                        spImageList.setHvalue(1.0);
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
            spImageList.setMinHeight(GROUP_IMAGE_LIST_HEIGHT);
            spImageList.setMaxWidth(GROUP_IMAGE_LIST_WIDTH);

            groupImages.getChildren().addAll(btnNewImage, borderGroupImages, spImageList);
        }

        Group root = new Group();
        root.getChildren().addAll(groupHeader, groupTable, groupImages, groupParams);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        bukvar = Bukvar.getBukvar(blankTable());
        refreshLessions();
        comboLessions.setValue(bukvar.defaultLessionName);
        
        primaryStage.setTitle("Буквар - Едитор");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(false);
        primaryStage.show();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveBukvar(null)));
        btnNewImage.toFront();
    }
    
    private void refreshLessions() {
        comboLessions.getItems().clear();
        comboLessions.getItems().addAll(bukvar.lessions.keySet());
    }
    
    private void selectLession(String lessionName) {
        saveBukvar(null);
        groupImageList.getChildren().clear();
        
        selectedLession = bukvar.lessions.get(lessionName);
        
        canvas.getGraphicsContext2D().drawImage(selectedLession.table, 0, 0);
        
        for (int i = 0; i < selectedLession.images.size(); i++) {
            addImage(i);
        }
        
        checkTimeIndef.setSelected(selectedLession.timeToPickImgsIndef);
        spinTime.getValueFactory().setValue(selectedLession.timeToPickImgsSecs);
        spinTime.setDisable(selectedLession.timeToPickImgsIndef);
        spinImgsToShow.getValueFactory().setValue(selectedLession.imgsToPresent);
        spinMinMatchingImgs.getValueFactory().setValue(selectedLession.minMatchingImgs);
        
        spImageList.setHvalue(rememberedScrollPane);
        rememberedScrollPane = 0.0;
    }
    
    private Image loadImage(String path) {
        Image image = new Image(path);
        if (image.isError()) { return null; }
        
        double scale;
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
        
        Line leftLine = new Line(0, 0, 0, 3 * DEFAULT_SPACING + IMAGE_HEIGHT + BTN_NEW_IMAGE_HEIGHT);
        Image image = selectedLession.images.get(imageId).image;
        Rectangle rectImage = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        rectImage.setTranslateX(DEFAULT_SPACING + (IMAGE_WIDTH - image.getWidth()) / 2);
        rectImage.setTranslateY(DEFAULT_SPACING + (IMAGE_HEIGHT - image.getHeight()) / 2);
        rectImage.setFill(new ImagePattern(image));
        rectImage.setOnMouseClicked(t -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                String fPath = file.toURI().toString();
                Image image1 = loadImage(fPath);
                String imageType = fPath.substring(fPath.length() - 3);
                selectedLession.images.get(imageId).image = image1;
                selectedLession.images.get(imageId).imgType = imageType;
                rectImage.setWidth(image1.getWidth());
                rectImage.setHeight(image1.getHeight());
                rectImage.setTranslateX(DEFAULT_SPACING + (IMAGE_WIDTH - image1.getWidth()) / 2);
                rectImage.setTranslateY(DEFAULT_SPACING + (IMAGE_HEIGHT - image1.getHeight()) / 2);
                rectImage.setFill(new ImagePattern(image1));
            }
        });
        Button btnRemove = new Button("Уклони слику");
        btnRemove.setTranslateX(DEFAULT_SPACING);
        btnRemove.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
        btnRemove.setTranslateY(2 * DEFAULT_SPACING + IMAGE_HEIGHT);
        btnRemove.setOnAction(event -> {
            selectedLession.images.remove(imageId);
            rememberedScrollPane = spImageList.getHvalue();
            comboLessions.setValue(selectedLession.name);
        });
        TextField letter = new TextField(selectedLession.images.get(imageId).letter);
        letter.setTranslateX(140);
        letter.setTranslateY(2 * DEFAULT_SPACING + IMAGE_WIDTH);
        letter.setMaxWidth(IMAGE_WIDTH / 3);
        letter.fontProperty().set(Font.font(STYLESHEET_CASPIAN, BTN_NEW_IMAGE_HEIGHT / 2));
        letter.textProperty().addListener(new LetterChangeListener(letter, imageId));
        Line rightLine = new Line(
                2 * DEFAULT_SPACING + IMAGE_WIDTH, 0, 
                2 * DEFAULT_SPACING + IMAGE_WIDTH, 3 * DEFAULT_SPACING + IMAGE_HEIGHT + BTN_NEW_IMAGE_HEIGHT);
        groupImage.getChildren().addAll(leftLine, rectImage, btnRemove, letter, rightLine);
        groupImage.setTranslateX(imageId * (2 * DEFAULT_SPACING + IMAGE_HEIGHT));
        
        groupImageList.getChildren().add(groupImage);
    }
    
    private Image blankTable() {
        WritableImage wim = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
        DrawableCanvas canvas = new DrawableCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.snapshot(null, wim);
        return wim;
    }
    
    private void saveBukvar(String nextLesson) {
        if (selectedLession == null) {
            if (nextLesson != null) { selectLession(nextLesson); }
            return;
        }
        WritableImage wim = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
        Platform.runLater(() -> {
            canvas.snapshot(null, wim);
            selectedLession.table = wim;
            bukvar.save();
            if (nextLesson != null) { selectLession(nextLesson); }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
