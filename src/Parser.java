import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Parser {

    public ArrayList<File> file_structure(File root) {
        ArrayList<File> files = new ArrayList<>();
        File folder = new File(Paths.get(root.getAbsolutePath()).getParent().toString().concat("/rust/tests"));

        for (File file : folder.listFiles()) {
            String filename = file.getName();
            if (filename.contains(".") || filename.contains("rustdoc")) continue;
            files.add(file);
        }

        System.out.println(files.toString());

        return files;

    }

    public void given_env() {

    }

}
