import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public class Parser {

    //file path of rust-test-examine tool
    File root = new File("");

    public void file_structure() {
        File folder = new File(Paths.get(root.getAbsolutePath()).getParent().toString().concat("/rust/tests"));

        for (File file : folder.listFiles()) {
            System.out.println(file.getName());
        }

    }

    public void given_env() {

    }

}
