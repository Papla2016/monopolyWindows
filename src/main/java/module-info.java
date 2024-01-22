module ru.vsu.cs.monopolywindows {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens ru.vsu.cs.monopolywindows to javafx.fxml;
    exports ru.vsu.cs.monopolywindows;
}