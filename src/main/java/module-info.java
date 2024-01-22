module ru.vsu.cs.monopolywindows {
    requires javafx.controls;
    requires json.simple;

    requires com.pomp.sv.monopoly;

    opens ru.vsu.cs.monopolywindows to javafx.fxml;
    opens images;
    opens sprites;
    opens stylesheets;
    exports ru.vsu.cs.monopolywindows;
}