package cz.cvut.fel.pvj.nejedly.monopoly.view;

import cz.cvut.fel.pvj.nejedly.monopoly.controller.GameController;
import cz.cvut.fel.pvj.nejedly.monopoly.model.GameModel;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.*;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class GameView {
    private final static Logger LOGGER = Logger.getLogger(GameView.class.getName());
    private final GameModel gameModel;
    private final GameController controller;
    private final Scene scene;
    private final BorderPane pane;
    private final ImageView boardImageView;
    private final Button rollButton;
    private final Button endTurnButton;
    private final Button purchaseButton;
    private final Button sellButton;
    private final Button saveGameButton;
    private final HashMap<String, ImageView> sprites;

    public GameView(GameModel gameModel, GameController gameController) {
        this.gameModel = gameModel;
        controller = gameController;

        pane = new BorderPane();
        scene = new Scene(pane);
        boardImageView = createImage("/images/board.jpg", 850);
        rollButton = new Button("Кинуть кубики");
        rollButton.setOnAction(actionEvent -> controller.rollButtonPressed());
        endTurnButton = new Button("Закончить ход");
        endTurnButton.setOnAction(actionEvent -> controller.endTurnButtonPressed(scene));
        purchaseButton = new Button("Купить собственность");
        purchaseButton.setOnAction(actionEvent -> controller.purchasePropertyButtonPressed());
        sellButton = new Button("Продать собственность");
        sellButton.setOnAction(actionEvent -> controller.sellPropertyButtonPressed());
        saveGameButton = new Button("Сохранить игру и выйти");
        saveGameButton.setOnAction(actionEvent -> controller.saveGameButtonPressed(scene));
        sprites = createSpriteImageViews();
    }
    /**
     * Initializes the GameView by setting up the scene, adding stylesheets,
     * and initializing the BorderPanes for the left, center, and right sections of the view.
     */
    public void init() {


        scene.getStylesheets().add("/stylesheets/GameViewStyles.css");
        pane.setPrefSize(1450, 850);
        initBorderPaneLeft();
        initBorderPaneCenter();
        initBorderPaneRight();
    }

    private HashMap<String, ImageView> createSpriteImageViews() {
        HashMap<String, ImageView> sprites = new HashMap<>();

        for (Player player : gameModel.getPlayers()) {

            ImageView sprite = new ImageView(new Image("sprites/"+player.getSpriteImage()));
            sprite.setPreserveRatio(true);
            sprite.prefHeight(35);

            int[] spriteBoardPosition = controller.calculateSpritePositionOnBoard(player.getBoardPosition().get());
            sprite.setX(spriteBoardPosition[0]);
            sprite.setY(spriteBoardPosition[1]);

            sprites.put(player.getName(), sprite);
        }

        return sprites;
    }

    private void initBorderPaneCenter() {

        Pane center = new Pane();
        center.setMaxSize(850, 850);
        center.getChildren().add(boardImageView);
        for (ImageView spriteImageView : sprites.values()) {
            center.getChildren().add(spriteImageView);
        }
        pane.setCenter(center);
        BorderPane.setAlignment(center, Pos.CENTER_LEFT);
    }

    private VBox createRollInformationBox() {

        Label label = new Label("Результаты броска кубиков");
        label.setId("heading3");
        Label description = new Label();
        description.textProperty().bind(Bindings.createStringBinding(() -> gameModel.getDie().toString(), gameModel.getDie().getDieOneRoll(), gameModel.getDie().getDieTwoRoll()));

        VBox vBox = new VBox(label, description);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private VBox createSquareInformationBox() {

        Label label = new Label("Текущая клетка");
        label.setId("heading3");
        Label description = new Label();

        List<Observable> observables = new ArrayList<>();
        observables.add(gameModel.getActivePlayerProperty());
        for (Player player : gameModel.getPlayers()) {
            observables.add(player.getBoardPosition());
            observables.add(player.getOwnedSquares());
        }

        description.textProperty().bind(Bindings.createStringBinding(() -> {
            int squarePosition = gameModel.getActivePlayer().getBoardPosition().getValue();
            return gameModel.getBoard().getBoardSquares()[squarePosition].toString();
        }, observables.toArray(new Observable[0])));

        VBox vBox = new VBox(label, description);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPrefHeight(250);
        return vBox;
    }

    private HBox createCurrentPlayerLabel() {

        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setPreserveRatio(true);
        avatar.imageProperty().bind(Bindings.createObjectBinding(() ->
            new Image("/sprites/" + gameModel.getActivePlayer().getSpriteImage()),
            gameModel.getActivePlayerProperty()
        ));

        Label playerName = new Label();
        playerName.setId("playerName");
        playerName.textProperty().bind(Bindings.createStringBinding(
            () -> " " + gameModel.getActivePlayer().getName() + " ход",
            gameModel.getActivePlayerProperty()
        ));

        HBox hBox = new HBox(avatar, playerName);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    private void initBorderPaneRight() {

        HBox player = createCurrentPlayerLabel();
        VBox square = createSquareInformationBox();
        VBox roll = createRollInformationBox();

        VBox saveButton = new VBox(saveGameButton);
        saveButton.setPadding(new Insets(60, 0, 0, 0));
        saveButton.setAlignment(Pos.CENTER);

        VBox right = new VBox(player, roll, rollButton, endTurnButton, square, purchaseButton, sellButton, saveButton);
        right.setAlignment(Pos.CENTER);
        right.setSpacing(30);
        right.setMaxWidth(300);
        right.setPrefWidth(300);

        pane.setRight(right);
    }

    private VBox createPlayerInfoBox(Player player) {

        ImageView avatar = createImage("/sprites/"+player.getSpriteImage(), 30);
        Label label = new Label();
        label.textProperty().bind(Bindings.createStringBinding(() -> " "+player.getNameWithStatus()+" - ", player.isBankrupt(), player.isInJail()));

        Label money = new Label();
        money.textProperty().bind(Bindings.concat("$", player.getMoney().asString()));

        HBox playerInfo = new HBox(avatar, label, money);
        playerInfo.setPadding(new Insets(5, 0, 5, 0));
        playerInfo.setAlignment(Pos.CENTER);

        TableView<Ownable> table = createEmptyTableView();
        table.setMaxWidth(290);
        table.setSelectionModel(null);
        table.setItems(player.getOwnedSquares());

        VBox playerInfoBox = new VBox(playerInfo, table);
        playerInfoBox.setAlignment(Pos.CENTER);

        return playerInfoBox;
    }

    private TableView<Ownable> createEmptyTableView() {
        TableView<Ownable> table = new TableView<>();

        TableColumn<Ownable, String> nameColumn = new TableColumn<>("Собственность");
        TableColumn<Ownable, Number> priceColumn = new TableColumn<>("Цена");
        TableColumn<Ownable, String> rentColumn = new TableColumn<>("Рента");
        TableColumn<Ownable, String> colorColumn = new TableColumn<>("Цвет");

        nameColumn.setSortable(false);
        priceColumn.setSortable(false);
        rentColumn.setSortable(false);
        colorColumn.setSortable(true);

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(((Square) data.getValue()).getName()));

        priceColumn.setCellValueFactory(data -> {
            Ownable ownable = data.getValue();
            return new SimpleIntegerProperty(ownable.getPurchasePrice());
        });

        rentColumn.setCellValueFactory(data -> {
            Ownable ownable = data.getValue();
            if (ownable instanceof Utility) {
                return new SimpleStringProperty("4 * (dice)\n10 * (dice)");
            }
            return new SimpleStringProperty(Integer.toString(ownable.getRent()));
        });

        colorColumn.setCellValueFactory(data -> {
            Ownable ownable = data.getValue();
            if (ownable instanceof Property property) {
                return new SimpleStringProperty(property.getGroup().toString());
            }
            return new SimpleStringProperty("----");
        });

        table.getColumns().add(nameColumn);
        table.getColumns().add(priceColumn);
        table.getColumns().add(rentColumn);
        table.getColumns().add(colorColumn);

        return table;
    }

    private void initBorderPaneLeft() {

        VBox left = new VBox();
        left.setPrefSize(300, 845);
        left.setMaxSize(300, 845);
        for (Player player : gameModel.getPlayers()) {
            VBox playerInfoBox = createPlayerInfoBox(player);
            left.getChildren().add(playerInfoBox);
        }
        pane.setLeft(left);
        BorderPane.setAlignment(left, Pos.TOP_CENTER);
    }

    private ImageView createImage(String uri, double width) {
        ImageView imageView = new ImageView();
        imageView.setImage(new Image(uri));
        imageView.setFitWidth(width);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public Scene getScene() {
        return scene;
    }

    public Button getRollButton() {
        return rollButton;
    }

    public HashMap<String, ImageView> getSprites() {
        return sprites;
    }

    public Button getEndTurnButton() {
        return endTurnButton;
    }

    /**
     * Displays a dialog box for selling a property
     *
     * <p>Displays a dialog box with a combo box listing the player's owned squares
     * and a "Sell property" button to initiate the sale. The dialog also includes a "Cancel" button
     * to close the dialog without taking any action.</p>
     *
     * @param comboBox   a ComboBox object containing the list of owned squares to be displayed in the dialog
     * @param sellButton a Button object that initiates the sale of the selected property when pressed
     */
    public void showSellPropertyDialog(ComboBox<Ownable> comboBox, Button sellButton) {

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        Button closeButton = new Button("Отмена");
        closeButton.setOnAction(actionEvent -> ((Stage) closeButton.getScene().getWindow()).close());

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(40);
        buttons.getChildren().addAll(sellButton, closeButton);

        vbox.getChildren().addAll(comboBox, buttons);

        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().setPrefSize(350, 150);
        dialog.getDialogPane().setContent(vbox);
        dialog.showAndWait();
    }
}