package ru.vsu.cs.monopolywindows.controller;

import com.github.cliftonlabs.json_simple.JsonException;
import src.logic.GameModel;
import src.logic.board.squares.*;
import src.logic.decks.cards.Card;
import src.logic.decks.cards.MoveToCard;
import src.logic.decks.cards.NearestSquareCard;
import src.logic.player.Player;
import ru.vsu.cs.monopolywindows.view.GameView;
import ru.vsu.cs.monopolywindows.view.MenuView;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;

public class GameController {

    private MenuView menuView;
    private GameView gameView;
    private GameModel gameModel;


    public void initialize() {

        menuView = new MenuView(this);
        menuView.init();

        Stage stage = new Stage();
        stage.setTitle("Монополия");
        stage.setResizable(false);
        stage.setScene(menuView.getScene());
        stage.show();

    }


    public void startNewGameButtonPressed(Scene oldScene, int numberOfPlayers) {

        gameModel = new GameModel();
        gameModel.startNewGame(numberOfPlayers);

        gameView = new GameView(gameModel, this);
        gameView.init();

        changeScenes(oldScene, gameView.getScene());
    }

    private void changeScenes(Scene oldScene, Scene newScene) {

        Stage stage = (Stage) oldScene.getWindow();
        stage.setScene(newScene);
        stage.show();

    }


    public void rollButtonPressed() {

        int steps = gameModel.getDie().roll();

        // player in jail logic
        if (gameModel.getActivePlayer().isInJail().getValue()) {
            if (!gameModel.getDie().isDoubles()) {
                gameView.getRollButton().setDisable(true);
                return;
            }

            // set player free from jail
            gameModel.getActivePlayer().setInJail(false);
            new Alert(Alert.AlertType.INFORMATION, "Поздравляем. Вы вышли из тюрьмы!.", ButtonType.OK).show();
        }

        // advance player and play sprite animation
        movePlayer(steps);
    }

    private void movePlayer(int steps) {

        // advance player and play sprite animation
        SequentialTransition spriteMovementAnimation = advancePlayerPositionBy(gameModel.getActivePlayer(), steps);
        spriteMovementAnimation.setOnFinished(actionEvent -> {
            if (gameModel.getDie().isDoubles()) {
                // remove a binding to be able to use rollButton.set() method
                gameView.getRollButton().disableProperty().unbind();
            } else {
                // disable rollButton after the animation is finished
                gameView.getRollButton().disableProperty().unbind();
                gameView.getRollButton().setDisable(true);
            }

            // run action based on what square has player landed on
            int activePlayerNewBoardPosition = gameModel.getActivePlayer().getBoardPosition().get();
            Square square = gameModel.getBoard().getBoardSquares()[activePlayerNewBoardPosition];
            stepOnSquare(gameModel.getActivePlayer(), square);
        });

    }



    public void endTurnButtonPressed(Scene oldScene) {

        if (gameModel.hasGameEnded()) {
            changeScenes(oldScene, menuView.getScene());

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Победитель - "+gameModel.getWinner().getName()+", поздравляем!\nИгра завершена, но вы всегда сможете начать новую.",
                    ButtonType.OK
            ).showAndWait();
        } else {
            gameModel.setNextPlayerAsActive();
            gameView.getRollButton().setDisable(false);
        }
    }

    public void purchasePropertyButtonPressed() {

        Player activePlayer = gameModel.getActivePlayer();
        int position = activePlayer.getBoardPosition().get();
        Square square = gameModel.getBoard().getBoardSquares()[position];

        if (!gameModel.getActivePlayer().purchaseSquare(square)) {
           new Alert(Alert.AlertType.INFORMATION, "Эта клетка уже куплена или её нельзя покупать или у вас недостаточно денег.").showAndWait();
        }
    }


    public void saveGameButtonPressed(Scene oldScene) {

        gameModel.save();
        changeScenes(oldScene, menuView.getScene());
    }


    public void loadGameButtonPressed(Scene oldScene) {

        try {
            gameModel = GameModel.load();

            gameView = new GameView(gameModel, this);
            gameView.init();

            changeScenes(oldScene, gameView.getScene());
        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.INFORMATION, "Файл сохранения не найден.").showAndWait();
            throw new RuntimeException(e);
        } catch (JsonException e) {
            new Alert(Alert.AlertType.INFORMATION, "Ошибка чтени файла сохранения. Похоже майкрософт победила в этой монополии :)").showAndWait();
            throw new RuntimeException(e);
        }
    }

    private SequentialTransition advancePlayerPositionBy(Player player, int steps) {

        SequentialTransition sequentialTransition = new SequentialTransition();

        // create sprite movement animation
        int currentPosition = player.getBoardPosition().get();
        for (int i = 1; i <= steps; i++) {
            int[] previousPosition = calculateSpritePositionOnBoard((currentPosition + i - 1) % 40);
            int[] nextPosition = calculateSpritePositionOnBoard((currentPosition+i) % 40);

            TranslateTransition t = new TranslateTransition(Duration.seconds(0.3), gameView.getSprites().get(player.getName()));
            t.setByX(nextPosition[0] - previousPosition[0]);
            t.setByY(nextPosition[1] - previousPosition[1]);

            sequentialTransition.getChildren().add(t);
            sequentialTransition.getChildren().add(new PauseTransition(Duration.seconds(0.08)));

        }

        // disable rollButton and endTurnButton while a sprite animation is running
        gameView.getRollButton().disableProperty().bind(sequentialTransition.statusProperty().isEqualTo(Animation.Status.RUNNING));
        gameView.getEndTurnButton().disableProperty().bind(sequentialTransition.statusProperty().isEqualTo(Animation.Status.RUNNING));

        player.advancePositionBy(steps);

        sequentialTransition.play();

        return sequentialTransition;
    }

    /**
     * Calculates the x and y coordinates of the sprite on the board for the given board position.
     *
     * @param boardPosition the position of the sprite on the board
     * @return an integer array containing the x and y coordinates of the sprite on the board
     * @throws RuntimeException if boardPosition is less than 0 or greater than 39
     */
    public int[] calculateSpritePositionOnBoard(int boardPosition) {
        int x;
        int y;

        if (boardPosition < 0 || boardPosition > 39) {
            throw new RuntimeException("Board position out of valid range 0-39. Board position: " + boardPosition);
        }

        if (boardPosition <= 9) { // squares 0-9
            y = 785;
            if (boardPosition == 0) {
                x = 785;
            } else {
                int xCoordinateOfBoardPosition1 = 677;
                x = xCoordinateOfBoardPosition1 - ((boardPosition - 1) * 68);
            }
        } else if (boardPosition <= 19) { // squares 10-19
            x = 25;
            if (boardPosition == 10) {
                y = 785;
            } else {
                int yCoordinateOfBoardPosition11 = 677;
                y = yCoordinateOfBoardPosition11 - ((boardPosition-11) * 68);
            }
        } else if (boardPosition <= 29) { // squares 20-29
            y = 25;
            if (boardPosition == 20) {
                x = 25;
            } else {
                int xCoordinateOfBoardPosition21 = 133;
                x = xCoordinateOfBoardPosition21 + ((boardPosition-21) * 68);
            }
        } else { // squares 30-39
            x = 785;
            if (boardPosition == 30) {
                y = 25;
            } else {
                int yCoordinateOfBoardPosition11 = 133;
                y = yCoordinateOfBoardPosition11 + ((boardPosition-31) * 68);
            }
        }

        return new int[]{x,y};
    }


    public void sellPropertyButtonPressed() {

        ComboBox<Ownable> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(gameModel.getActivePlayer().getOwnedSquares());

        Button sellButton = new Button("Продать");
        sellButton.setOnAction(actionEvent -> {
            Ownable selectedItem = comboBox.getValue();
            if (selectedItem != null)
            gameModel.getActivePlayer().sellOwnedSquare((Square) selectedItem);
            ((Stage) sellButton.getScene().getWindow()).close();
        });

        gameView.showSellPropertyDialog(comboBox, sellButton);
    }

    private void stepOnSquare(Player player, Square square) {
        if (square instanceof Ownable ownable) {
            steppedOnOwnable(player, ownable);
        } else if (square instanceof Cards cards) {
            steppedOnCards(player, cards);
        } else if (square instanceof GoToJail goToJail) {
            steppedOnGoToJail(player, goToJail);
        } else if (square instanceof Tax tax) {
            steppedOnTax(player, tax);
        }
    }

    private void steppedOnTax(Player player, Tax tax) {

        player.stepOnTax(tax);

        new Alert(
                Alert.AlertType.INFORMATION,
                "Вы задолжали за "+tax.getName()+" и должны заплатить налог в размере $"+tax.getTax(),
                ButtonType.OK
        ).show();
    }

    private void steppedOnGoToJail(Player player, GoToJail goToJail) {
        player.stepOnGoToJail();

        // animate sprite movement to jail square
        SequentialTransition spriteMovementAnimation = advancePlayerPositionBy(player, (player.getBoardPosition().getValue() - goToJail.getJailPosition()));
        spriteMovementAnimation.setOnFinished(actionEvent -> {
            // disable roll button
            gameView.getRollButton().disableProperty().unbind();
            gameView.getRollButton().setDisable(true);
        });

        new Alert(
                Alert.AlertType.INFORMATION,
                "Ты попался в тюрьму за отмывание денег\nЧтобы выйти тебе надо выкинуть дубль на кубиках.",
                ButtonType.OK
        ).show();
    }

    private void steppedOnCards(Player player, Cards cards) {

        Card card = gameModel.steppedOnCards(cards, player);

        new Alert(
                Alert.AlertType.INFORMATION,
                card.toString(),
                ButtonType.OK
        ).show();

        if (card instanceof MoveToCard moveToCard) {
            int steps = moveToCard.getSteps(player, gameModel.getBoard());
            movePlayer(steps);
        } else if (card instanceof NearestSquareCard nearestSquareCard) {
            int steps = nearestSquareCard.getSteps(player, gameModel.getBoard());
            movePlayer(steps);
        }
    }

    private void steppedOnOwnable(Player player, Ownable ownable) {

        if (gameModel.steppedOnOwnable(ownable, player)) return;

        int rent;
        if (ownable instanceof Utility utility) {
            rent = utility.getRent(gameModel.getDie());
        } else rent = ownable.getRent();

        new Alert(
                Alert.AlertType.INFORMATION,
                "Ты ступил на "+((Square) ownable).getName()+", которой уже владеет "+ownable.getOwner().getName()+".\nЗаплати за ренту $"+rent+".",
                ButtonType.OK
        ).show();
    }
}