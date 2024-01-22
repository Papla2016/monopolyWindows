package ru.vsu.cs.monopolywindows.view;

import ru.vsu.cs.monopolywindows.controller.GameController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class MenuView {
    private final Scene scene;
    private final Pane pane;
    private final GameController controller;

    public MenuView(GameController controller) {
        this.controller = controller;

        pane = new VBox();
        scene = new Scene(pane);
    }


    /**
     * Initializes the MenuView.
     * Sets the scene style, pane size, and adds UI elements for starting a new game, loading a game, and exiting the program.
     */
    public void init() {

        scene.getStylesheets().add("/stylesheets/MenuViewStyles.css");
        pane.setPrefSize(700, 700);

        Label newGameLabel = new Label("Начать новую игру");
        newGameLabel.setId("heading1");

        Label otherLabel = new Label("Продолжить");
        otherLabel.setId("heading2");

        Button loadGameButton = new Button("Загрузить игру");
        loadGameButton.setOnAction(actionEvent -> controller.loadGameButtonPressed(scene));

        Button exitButton = new Button("Выход");
        exitButton.setOnAction(actionEvent -> {
            Stage stage = (Stage) scene.getWindow();
            stage.close();
        });

        pane.getChildren().addAll(
            createBackgroundImage(),
            newGameLabel,
            createNewGameControls(2, 2),
            createNewGameControls(3, 4),
            otherLabel,
            loadGameButton,
            exitButton
        );
    }

    private HBox createNewGameControls(int numberOfButtons, int startingNumberOfPlayers) {

        HBox newGameButtons = new HBox();
        for (int i = startingNumberOfPlayers; i < (startingNumberOfPlayers + numberOfButtons); i++) {
            Button newGameButton = createNewGameButton(i);
            newGameButtons.getChildren().add(newGameButton);
        }
        return newGameButtons;
    }

    private ImageView createBackgroundImage() {
        ImageView backgroundImage = new ImageView();
        backgroundImage.setImage(new Image("/images/monopoly_logo.png"));
        backgroundImage.setFitWidth(350);
        backgroundImage.setPreserveRatio(true);
        return backgroundImage;
    }

    private Button createNewGameButton(int i) {
        String playerRu;
        if (i < 5){
            playerRu = " игрока";
        }else{
            playerRu = " игроков";
        }
        Button button = new Button(i+playerRu);
        button.setOnAction(actionEvent -> controller.startNewGameButtonPressed(scene, i));
        return button;
    }

    public Scene getScene() {
        return scene;
    }
}
