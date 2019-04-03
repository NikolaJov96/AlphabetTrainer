/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.editor;

import bukvar.structs.Bukvar;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author jovan
 */
public class BukvarEditor extends Application {
    
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;
    private static final int DEFAULT_SPACING = 10;
    
    private static final int GROUP_HEADER_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_HEADER_HEIGHT = 80;
    
    private static final int GROUP_IMAGES_WIDTH = 400;
    private static final int GROUP_IMAGES_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT;
    private static final int BTN_NEW_IMAGE_WIDTH = 150;
    private static final int BTN_NEW_IMAGE_HEIGHT = 40;
    private static final int GROUP_IMAGE_LIST_WIDTH = GROUP_IMAGES_WIDTH - 2 * DEFAULT_SPACING;
    private static final int GROUP_IMAGE_LIST_HEIGHT = GROUP_IMAGES_HEIGHT - 3 * DEFAULT_SPACING - BTN_NEW_IMAGE_HEIGHT;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 200;
    
    private static final int GROUP_PARAMETERS_WIDTH = 600;
    private static final int GROUP_PARAMETERS_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT;
    private static final int GROUP_PARAM_1_HEIGHT = 80;
    
    private static final Set<String> alphabet = new HashSet<>(Arrays.asList(
            "А", "Б", "В", "Г", "Д", "Ђ", "Е"," Ж", "З", "И", "Ј", "К", "Л", "Љ", "М", 
            "Н", "Њ", "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ", "Ш"
    ));
    
    Scene scene;
    Group root;
    
    Group groupHeader;
    ComboBox<String> comboLessions;
    Button btnSave;
    Button btnDelete;
    Button btnDefault;
    TextField fieldNewName;
    Button btnNewLession;
    
    Group groupImages;
    Button btnNewImage;
    Group groupImageList;
    ScrollPane spImageList;
    
    Group groupParams;
    CheckBox checkTimeIndef;
    Spinner<Integer> spinTime;
    Spinner<Integer> spinImgsToShow;
    Spinner<Integer> spinMinMatchingImgs;
    
    Bukvar bukvar;
            
    @Override
    public void start(Stage primaryStage) {
        bukvar = new Bukvar();
        
        groupHeader = new Group();
        {
            Rectangle border = new Rectangle(1, 1, GROUP_HEADER_WIDTH - 2, GROUP_HEADER_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);
            
            comboLessions = new ComboBox<>();
            comboLessions.getItems().addAll("1", "л2", "л3");
            comboLessions.setTranslateX(DEFAULT_SPACING);
            comboLessions.setTranslateY(2 * DEFAULT_SPACING);
            comboLessions.setMinWidth(200);
            comboLessions.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnSave = new Button();
            btnSave.setText("Сачувај");
            btnSave.setMaxWidth(120);
            btnSave.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnSave.setTranslateX(230);
            btnSave.setTranslateY(2 * DEFAULT_SPACING);
            btnDelete = new Button();
            btnDelete.setText("Обриши");
            btnDelete.setMaxWidth(120);
            btnDelete.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDelete.setTranslateX(330);
            btnDelete.setTranslateY(2 * DEFAULT_SPACING);
            btnDefault = new Button();
            btnDefault.setText("Подразумевана");
            btnDefault.setMaxWidth(150);
            btnDefault.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
            btnDefault.setTranslateX(430);
            btnDefault.setTranslateY(2 * DEFAULT_SPACING);
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
            
            groupHeader.getChildren().addAll(border, comboLessions, btnSave, btnDelete, btnDefault, fieldNewName, btnNewLession);
        }
        
        groupImages = new Group();
        {
            groupImages.setTranslateY(GROUP_HEADER_HEIGHT);
            
            btnNewImage = new Button();
            {
                btnNewImage.setText("Нова слика");
                btnNewImage.setMinWidth(BTN_NEW_IMAGE_WIDTH);
                btnNewImage.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
                btnNewImage.setTranslateX((GROUP_IMAGES_WIDTH - BTN_NEW_IMAGE_WIDTH) / 2);
                btnNewImage.setTranslateY(DEFAULT_SPACING);
                /*btnNewImage.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                    }
                });*/
            }

            Rectangle borderGroupImages = new Rectangle(1, 1, GROUP_IMAGES_WIDTH - 2, GROUP_IMAGES_HEIGHT - 2);
            borderGroupImages.setFill(Color.TRANSPARENT);
            borderGroupImages.setStroke(Color.BLACK);
            
            groupImageList = new Group();
            {
                for (int i = 0; i < 3; i++) {
                    Group groupImage = new Group();
                    {
                        Line topLine = new Line(0, 0, GROUP_IMAGE_LIST_WIDTH, 0);
                        Rectangle rectImage = new Rectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                        rectImage.setTranslateX(DEFAULT_SPACING);
                        rectImage.setTranslateY(DEFAULT_SPACING);
                        Button btnRemove = new Button("Уклони слику");
                        btnRemove.setTranslateX(2 * DEFAULT_SPACING + IMAGE_WIDTH);
                        btnRemove.setMinHeight(BTN_NEW_IMAGE_HEIGHT);
                        btnRemove.setTranslateY(2 * DEFAULT_SPACING);
                        TextField letter = new TextField("А");
                        letter.setTranslateX(2 * DEFAULT_SPACING + IMAGE_WIDTH);
                        letter.setTranslateY(3 * DEFAULT_SPACING + 2 * BTN_NEW_IMAGE_HEIGHT);
                        letter.setMaxWidth(IMAGE_WIDTH / 2);
                        letter.fontProperty().set(Font.font(STYLESHEET_CASPIAN, BTN_NEW_IMAGE_HEIGHT));
                        letter.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                if (letter.getText().length() > 0) {
                                    String l = letter.getText().substring(0, 1).toUpperCase();
                                    if (alphabet.contains(l)) {
                                        letter.setText(l);
                                    } else {
                                        letter.setText("");
                                    }
                                    
                                }
                            }
                        });
                        Line bottomLine = new Line(0, 2 * DEFAULT_SPACING + IMAGE_HEIGHT, GROUP_IMAGE_LIST_WIDTH, 2 * DEFAULT_SPACING + IMAGE_HEIGHT);
                        groupImage.getChildren().addAll(topLine, rectImage, btnRemove, letter, bottomLine);
                        groupImage.setTranslateY(i * (2 * DEFAULT_SPACING + IMAGE_HEIGHT));
                    }
                    groupImageList.getChildren().add(groupImage);
                }
            }
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
            groupParams.setTranslateY(GROUP_HEADER_HEIGHT);
            
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
                spinTime = new Spinner<>(1, 20, 1);
                spinTime.setTranslateX(200);
                spinTime.setTranslateY(4 * DEFAULT_SPACING);
                checkTimeIndef.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        spinTime.setDisable(checkTimeIndef.isSelected());
                    }
                });
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
                spinMinMatchingImgs = new Spinner<>(0, 4, 1);
                spinMinMatchingImgs.setTranslateX(DEFAULT_SPACING);
                spinMinMatchingImgs.setTranslateY(4 * DEFAULT_SPACING);
                groupParam3.getChildren().addAll(border, text, spinMinMatchingImgs);
            }
            
            groupParams.getChildren().addAll(borderGroupParams, groupParam1, groupParam2, groupParam3);
        }
        
        root = new Group();
        root.getChildren().addAll(groupHeader, groupImages, groupParams);
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        primaryStage.setTitle("Буквар - Едитор");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(false);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
