package ru.vsu.cs.monopolywindows;

import ru.vsu.cs.monopolywindows.controller.GameController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    /**
     * The main entry point for the application.
     * <p>This method initializes the logger and launches the application. After the
     * application has finished running, the logger is closed.</p>
     *
     * @param args the command-line arguments passed to the application
     * @throws IOException if there is an error configuring the logger
     */
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    /**
     * {@inheritDoc}
     * <p>This method creates a new {@link GameController} instance and initializes
     * the game.</p>
     *
     * @param primaryStage the primary stage for the application
     */
    @Override
    public void start(Stage primaryStage) {
        try {

            GameController gameController = new GameController();
            gameController.initialize();
        } catch (Exception ex){
            System.out.println(ex.toString());
        }
    }



}
