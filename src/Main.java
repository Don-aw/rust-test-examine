import java.io.File;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Parser p = new Parser();

        //file path of rust-test-examine tool
        File root = new File("");

        ArrayList<File> suites = p.file_structure(root);
        p.given_env();
    }
}