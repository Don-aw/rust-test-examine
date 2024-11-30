import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.fxml.FXMLLoader;

public class GUI extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        Group root = new Group();
        Scene scene = new Scene(root, 1000, 500, Color.AQUAMARINE);

        stage.setScene(scene);
        stage.show();
    }

}
