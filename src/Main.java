import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        Parser p = new Parser();

        //file path of rust-test-examine tool
        File root = new File("");

        ArrayList<File> suites = p.findRustTestSuites(root);

        File f = new File("");
        // random file to test on
        p.parse_filter(f);

        File g = new File("");
        p.parse_filter(g);
    }
}