package bukvar.trenazer;

import bukvar.structs.Bukvar;
import bukvar.structs.ImgContainer;
import bukvar.structs.Lession;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.*;
import java.util.stream.IntStream;

public class BukvarTrenazer extends Application {

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 900;
    private static final int DEFAULT_SPACING = 10;

    private static final int GROUP_HEADER_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_HEADER_HEIGHT = 80;

    private static final int GROUP_TABLE_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_TABLE_HEIGHT = 150;
    private static final int CANVAS_WIDTH = GROUP_TABLE_WIDTH - 2 * DEFAULT_SPACING;
    private static final int CANVAS_HEIGHT = 120;

    private static final int GROUP_PARAMETERS_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_PARAMETERS_HEIGHT = 100;
    private static final String REMAINING_TIME = "Преостало време: ";
    private static final String INFINITY_SYMBOL = Character.toString('\u221e');
    private static final String SCORE = "Ваш резултат је: ";
    private static final String SCORE_SEPARATOR = " / ";

    private static final int GROUP_IMAGES_WIDTH = WINDOW_WIDTH;
    private static final int GROUP_IMAGES_HEIGHT = WINDOW_HEIGHT - GROUP_HEADER_HEIGHT - GROUP_TABLE_HEIGHT - GROUP_PARAMETERS_HEIGHT;
    private static final int BTN_WIDTH = 150;
    private static final int BTN_HEIGHT = 40;
    private static final int GROUP_IMAGE_LIST_WIDTH = GROUP_IMAGES_WIDTH - 2 * DEFAULT_SPACING;
    private static final int GROUP_IMAGE_LIST_HEIGHT = GROUP_IMAGES_HEIGHT - 4 * DEFAULT_SPACING - BTN_HEIGHT;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = IMAGE_WIDTH;

    private static final Paint SELECT_COLOR = Color.rgb(255, 155, 0, 0.7);
    private static final Paint UNSELECT_COLOR = Color.rgb(255, 255, 255, 0.5);

    private static final ArrayList<String> alphabet = new ArrayList<>(Arrays.asList(
            "А", "Б", "В", "Г", "Д", "Ђ", "Е", "Ж", "З", "И", "Ј", "К", "Л", "Љ", "М",
            "Н", "Њ", "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ", "Ш"
    ));

    private Stage stage;
    private Scene scene;
    private Group root;

    private Group groupHeader;
    private Button btnNewTraining;

    private Group groupTable;
    private ArrayList<Rectangle> letters;
    private Rectangle letterToGuess;

    private Group groupParams;
    private Text timeText;

    private Group groupImages;
    private Button btnNextQuestion;
    private Button btnEnd;
    private GridPane gridPane;

    private Bukvar bukvar;
    private Lession selectedLession;
    private Set<String> lettersToGuess;

    private int numberOfPicturesToGuess;
    private int numberOfGuessedPictures;
    private int timeRemaining;
    private boolean theEnd;
    private boolean nextQuestion;

    private Map<Image, Rectangle> allPictures;
    private Map<Image, Rectangle> correctPictures;
    private Map<Image, Rectangle> selectedPictures;
    private Set<Rectangle> drawnPictures;


    private void btnNextQuestionClicked() {
        synchronized (this) {
            nextQuestion = true;
            notify();
        }
    }

    private void startTraining() {
        int minMatchingImgs = selectedLession.minMatchingImgs;
        int imgsToPresent = selectedLession.imgsToPresent;
        if (selectedLession.timeToPickImgsIndef) {
            timeText.setText(REMAINING_TIME + INFINITY_SYMBOL);
        }
        else {
            timeRemaining = selectedLession.timeToPickImgsSecs;
            timeText.setText(REMAINING_TIME + timeRemaining);
        }
        numberOfGuessedPictures = 0;
        numberOfPicturesToGuess = 0;
        theEnd = false;
        nextQuestion = false;
        countNumberOfLetters(selectedLession);
        int numberOfLettersToGuess = lettersToGuess.size();
        if (numberOfLettersToGuess == 0) {
            btnNewTraining.disableProperty().setValue(false);
            btnNextQuestion.disableProperty().setValue(true);
            btnEnd.disableProperty().setValue(true);
            theEnd = true;
            timeText.setText("Нема одговарајућих тренинга!");
        }
        else {
            Object[] lettersInOrder = lettersToGuess.toArray();
            while (!theEnd && numberOfLettersToGuess > 0) {
                if (letterToGuess != null) {
                    letterToGuess.setFill(UNSELECT_COLOR);
                }
                letterToGuess = letters.get(alphabet.indexOf(lettersInOrder[numberOfLettersToGuess - 1]));
                letterToGuess.setFill(SELECT_COLOR);
                drawLetters(minMatchingImgs, imgsToPresent, (String) lettersInOrder[numberOfLettersToGuess - 1]);

                if (selectedLession.timeToPickImgsIndef) {
                    while (!nextQuestion && !theEnd) {
                        synchronized (this) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    timeRemaining = selectedLession.timeToPickImgsSecs;
                    while (!nextQuestion && timeRemaining > 0) {
                        if (theEnd) {
                            break;
                        }
                        timeText.setText(REMAINING_TIME + timeRemaining);
                        synchronized (this) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        timeRemaining--;
                    }
                    if (!theEnd) {
                        timeText.setText(REMAINING_TIME + timeRemaining);
                        checkAnswer();
                        synchronized (this) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                nextQuestion = false;
                numberOfLettersToGuess--;
            }
            btnNewTraining.disableProperty().setValue(false);
            btnNextQuestion.disableProperty().setValue(true);
            btnEnd.disableProperty().setValue(true);
            theEnd = true;
            showResults();
        }
    }

    private void checkAnswer() {
        numberOfPicturesToGuess++;
        boolean mistake = false;
        for (Image image : allPictures.keySet()) {

            if (selectedPictures.containsKey(image) && correctPictures.containsKey(image)) {
                correctPictures.get(image).setStroke(Color.GREEN);
            }
            else if (selectedPictures.containsKey(image)) {
                selectedPictures.get(image).setStroke(Color.RED);
                mistake = true;
            }
            else if (correctPictures.containsKey(image)) {
                correctPictures.get(image).setStrokeWidth(10);
                correctPictures.get(image).setStroke(Color.YELLOW);
                mistake = true;
            }
        }
        if (!mistake)
            numberOfGuessedPictures++;
    }

    private void showResults() {
        timeText.setText(SCORE + numberOfGuessedPictures + SCORE_SEPARATOR + numberOfPicturesToGuess);
    }

    private void drawLetters(int minMatchingImgs, int imgsToPresent, String letter) {
        allPictures.clear();
        selectedPictures.clear();
        correctPictures.clear();
        int countMatching = 0;
        int count = 0;
        while (count < imgsToPresent || countMatching < minMatchingImgs) {
            int rand = new Random().nextInt(selectedLession.images.size());
            Image image = selectedLession.images.get(rand).image;
            if (!allPictures.containsKey(image)) {
                Rectangle rectangle = new Rectangle();
                ImagePattern imagePattern = new ImagePattern(image, 0, 0, 1, 1, true);
                rectangle.setFill(imagePattern);
                rectangle.setStroke(Color.BLUE);
                rectangle.setStrokeWidth(0);
                rectangle.setStrokeType(StrokeType.INSIDE);
                allPictures.put(image, rectangle);
                if (selectedLession.images.get(rand).letter.equals(letter)) {
                    correctPictures.put(image, rectangle);
                    countMatching++;
                }
                rectangle.setOnMouseClicked(e -> {
                    if (selectedPictures.containsKey(image)) {
                        selectedPictures.get(image).setStrokeWidth(0);
                        selectedPictures.remove(image);
                    }
                    else {
                        selectedPictures.put(image, rectangle);
                        selectedPictures.get(image).setStrokeWidth(10);
                    }
                });
                count++;
            }
        }
        int colsNumber = (int)Math.ceil(Math.sqrt(count));
        int rowsNumber = (int)Math.ceil((double)count / colsNumber);
        int GROUP_IMAGE_HEIGHT = GROUP_IMAGE_LIST_HEIGHT / rowsNumber - 2 * DEFAULT_SPACING;
        int GROUP_IMAGE_WIDTH = GROUP_IMAGE_LIST_WIDTH / colsNumber - 2 * DEFAULT_SPACING;
        int i = 0, j = 0;
        for (Rectangle rectangle : drawnPictures) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    groupImages.getChildren().remove(rectangle);
                }
            });
        }
        for (Image image : allPictures.keySet()) {
            double scale = 1.0;
            double width = image.getWidth();
            double height = image.getHeight();
            if (height / IMAGE_WIDTH * GROUP_IMAGE_WIDTH > GROUP_IMAGE_HEIGHT) {
                scale = GROUP_IMAGE_HEIGHT / height;
            } else {
                scale = GROUP_IMAGE_WIDTH / width;
            }
            Rectangle rectangle = allPictures.get(image);
            rectangle.setHeight(height * scale);
            rectangle.setWidth(width * scale);

            double ver_shift = (GROUP_IMAGE_HEIGHT - height * scale) / 2;
            double hor_shift = (GROUP_IMAGE_WIDTH - width * scale) / 2;
            rectangle.setTranslateX(j * (GROUP_IMAGE_WIDTH + 2 * DEFAULT_SPACING) + hor_shift + DEFAULT_SPACING);
            rectangle.setTranslateY(i * (GROUP_IMAGE_HEIGHT + 2 * DEFAULT_SPACING) + ver_shift + DEFAULT_SPACING);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    groupImages.getChildren().add(rectangle);
                }
            });
            drawnPictures.add(rectangle);
            j++;
            if (j == colsNumber) {
                j = 0;
                i++;
            }
        }
    }

    private void countNumberOfLetters (Lession lession) {
        lettersToGuess = new HashSet<>();
        if (lession.images.size() < lession.imgsToPresent)
            return;
        for (int i = 0; i < lession.images.size(); i++) {
            if (!lettersToGuess.contains(lession.images.get(i).letter)) {
                lettersToGuess.add(lession.images.get(i).letter);
            }
        }
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < lettersToGuess.size(); i++) {
            String l = (String) lettersToGuess.toArray()[i];
            int count = 0;
            for (int j = 0; j < lession.images.size(); j++) {
                if (l.equals(lession.images.get(j).letter))
                    count++;
            }
            if (count < lession.minMatchingImgs)
                arr.add(l);
        }
        IntStream.range(0, arr.size()).forEach(i -> lettersToGuess.remove(arr.get(i)));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        bukvar = Bukvar.getBukvar(null);
        selectedLession = bukvar.lessions.get(bukvar.defaultLessionName);
        countNumberOfLetters(selectedLession);
        allPictures = new HashMap<>();
        selectedPictures = new HashMap<>();
        correctPictures = new HashMap<>();
        drawnPictures = new HashSet<>();

        groupHeader = new Group();
        {
            Rectangle border = new Rectangle(1, 1, GROUP_HEADER_WIDTH - 2, GROUP_HEADER_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);

            btnNewTraining = new Button();
            {
                btnNewTraining.setText("Започни тренинг");
                btnNewTraining.setMaxWidth(BTN_WIDTH);
                btnNewTraining.setMinHeight(BTN_HEIGHT);
                btnNewTraining.setTranslateX(GROUP_HEADER_WIDTH / 2 - BTN_WIDTH / 2);
                btnNewTraining.setTranslateY(2 * DEFAULT_SPACING);
                btnNewTraining.setOnAction(new EventHandler<ActionEvent>() {
                    private void run() {
                        startTraining();
                    }

                    @Override
                    public void handle(ActionEvent event) {
                        btnNewTraining.disableProperty().setValue(true);
                        btnNextQuestion.disableProperty().setValue(false);
                        btnEnd.disableProperty().setValue(false);
                        Thread t = new Thread(this::run);
                        t.start();
                    }
                });
            }

            groupHeader.getChildren().addAll(border, btnNewTraining);
        }

        groupTable = new Group();
        {
            groupTable.setTranslateY(GROUP_HEADER_HEIGHT);

            Rectangle border = new Rectangle(1, 1, GROUP_TABLE_WIDTH - 2, GROUP_TABLE_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);

            Rectangle rectangle = new Rectangle(0, (GROUP_TABLE_HEIGHT - CANVAS_HEIGHT) / 2, CANVAS_WIDTH, CANVAS_HEIGHT);
            Image image = selectedLession.table;
            if (image != null) {
                ImagePattern imagePattern = new ImagePattern(image, 0, 0, 1, 1, true);
                rectangle.setFill(imagePattern);
            }
            else {
                rectangle.setFill(Color.WHITE);
            }
            groupTable.getChildren().addAll(border, rectangle);

            letters = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                int x = (i < 15 ? i : i - 15) * (GROUP_TABLE_WIDTH / 15) + GROUP_TABLE_WIDTH / 60;
                int y = (i < 15 ? 1 : 5) * GROUP_TABLE_HEIGHT / 9;
                rectangle = new Rectangle(x, y,GROUP_HEADER_WIDTH / 20, GROUP_TABLE_HEIGHT / 3);
                //rectangle.setFill(Color.rgb(255, 255, 153, 0.5));
                rectangle.setFill(UNSELECT_COLOR);
                rectangle.setStroke(Color.BLACK);
                letters.add(rectangle);
                Text text = new Text(x + 1, y + GROUP_TABLE_HEIGHT / 3 - 6, alphabet.get(i));
                text.setFont(new Font(GROUP_TABLE_HEIGHT / 3));
                text.setWrappingWidth(GROUP_HEADER_WIDTH / 20 - 2);
                text.setTextAlignment(TextAlignment.CENTER);
                groupTable.getChildren().addAll(rectangle, text);
            }
            letterToGuess = null;
        }

        groupParams = new Group();
        {
            groupParams.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT);

            Rectangle border = new Rectangle(1, 1, GROUP_PARAMETERS_WIDTH - 2, GROUP_PARAMETERS_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);

            timeText = new Text(1, GROUP_PARAMETERS_HEIGHT - 36, REMAINING_TIME);
            {
                timeText.setFont(new Font(GROUP_PARAMETERS_HEIGHT - 58));
                timeText.setWrappingWidth(GROUP_PARAMETERS_WIDTH - 2);
                timeText.setTextAlignment(TextAlignment.CENTER);
            }

            groupParams.getChildren().addAll(border, timeText);
        }

        groupImages = new Group();
        {
            groupImages.setTranslateY(GROUP_HEADER_HEIGHT + GROUP_TABLE_HEIGHT + GROUP_PARAMETERS_HEIGHT);

            Rectangle border = new Rectangle(1, 1, GROUP_IMAGES_WIDTH - 2, GROUP_IMAGES_HEIGHT - 2);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.BLACK);

            btnNextQuestion = new Button();
            {
                btnNextQuestion.setText("Даље");
                btnNextQuestion.setMinWidth(BTN_WIDTH);
                btnNextQuestion.setMinHeight(BTN_HEIGHT);
                btnNextQuestion.setTranslateX((GROUP_IMAGES_WIDTH + BTN_WIDTH) / 2);
                btnNextQuestion.setTranslateY(GROUP_IMAGES_HEIGHT - BTN_HEIGHT - 2 * DEFAULT_SPACING);
                btnNextQuestion.setOnAction(new EventHandler<ActionEvent>() {
                    private void run() {
                        btnNextQuestionClicked();
                    }

                    @Override public void handle(ActionEvent event) {
                        btnNewTraining.disableProperty().setValue(true);
                        btnNextQuestion.disableProperty().setValue(false);
                        btnEnd.disableProperty().setValue(false);
                        Thread t = new Thread(this::run);
                        t.start();
                    }
                });
            }

            btnEnd = new Button();
            {
                btnEnd.setText("Крај");
                btnEnd.setMinWidth(BTN_WIDTH);
                btnEnd.setMinHeight(BTN_HEIGHT);
                btnEnd.setTranslateX((GROUP_IMAGES_WIDTH) / 2 + 2 * BTN_WIDTH);
                btnEnd.setTranslateY(GROUP_IMAGES_HEIGHT - BTN_HEIGHT - 2 * DEFAULT_SPACING);
                btnNextQuestion.disableProperty().setValue(true);
                btnEnd.disableProperty().setValue(true);
                btnEnd.setOnAction(new EventHandler<ActionEvent>() {
                    private void run() {
                        setEnd();
                    }

                    @Override public void handle(ActionEvent event) {
                        btnNewTraining.disableProperty().setValue(false);
                        btnNextQuestion.disableProperty().setValue(true);
                        btnEnd.disableProperty().setValue(true);
                        Thread t = new Thread(this::run);
                        t.start();
                    }
                });
            }

            groupImages.getChildren().addAll(border, btnNextQuestion, btnEnd);
        }

        root = new Group();
        root.getChildren().addAll(groupHeader, groupTable, groupImages, groupParams);
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("Буквар - Тренажер");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(false);
        primaryStage.show();
    }

    private void setEnd() {
        synchronized (this) {
            theEnd = true;
            notify();
        }
    }

    public void stop() {
        setEnd();
    }
}
