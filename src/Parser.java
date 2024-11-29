import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

public class Parser {

    public ArrayList<File> findRustTestSuites(File root) {
        ArrayList<File> files = new ArrayList<>();
        File folder = new File(Paths.get(root.getAbsolutePath()).getParent().toString().concat("/rust/tests"));

        for (File file : folder.listFiles()) {
            String filename = file.getName();
            // excludes rustdoc and .DS_Store
            if (filename.contains(".") || filename.contains("rustdoc")) continue;
            files.add(file);
        }

        //System.out.println(files.toString());

        return files;

    }

    public void parse_filter(File f) throws IOException {

        BufferedReader r = new BufferedReader(new FileReader(f));
        ArrayList<String> directives_all = new ArrayList<>();
        ArrayList<ArrayList<String>> directives_rev = new ArrayList<>();
        String line = r.readLine();
        ArrayList<String> revisions = new ArrayList<>();

        while (line != null) {
            if (line.contains("//@ revisions:")) {
                //splice the "//@ revisions: " out
                StringTokenizer st = new StringTokenizer(line.substring(14));

                while (st.hasMoreTokens()) {
                    revisions.add(st.nextToken());
                    directives_rev.add(new ArrayList<>());
                }

            }
            else if (!revisions.isEmpty()) {
                for (int i = 0; i < revisions.size(); i++) {
                    if (line.contains("//@ [" + revisions.get(i))) {
                        directives_rev.get(i).add(line.substring(7 + revisions.get(i).length()));
                    }
                }
            }
            else if (line.contains("//@ ")) directives_all.add(line.substring(4));

            line = r.readLine();
        }

        System.out.println("revisions: " + revisions);
        //System.out.println(directives_rev);
        //System.out.println(directives_all);

        ArrayList<String> only_all = new ArrayList<>();
        ArrayList<String> ignore_all = new ArrayList<>();
        ArrayList<String> needs_all = new ArrayList<>();
        ArrayList<String> other_all = new ArrayList<>();

        while (!directives_all.isEmpty()) {
            String d = directives_all.getFirst();
            if (d.contains("only")) only_all.add(d.substring(5));
            else if (d.contains("ignore")) ignore_all.add(d.substring(7));
            else if (d.contains("needs")) needs_all.add(d.substring(6));
            else other_all.add(d);

            directives_all.removeFirst();
        }

        System.out.println("(all) only:   " + only_all);
        System.out.println("(all) ignore: " + ignore_all);
        System.out.println("(all) needs:  " + needs_all);
        System.out.println("(all) other:  " + other_all);

        ArrayList<ArrayList<String>> only_rev = new ArrayList<>();
        ArrayList<ArrayList<String>> ignore_rev = new ArrayList<>();
        ArrayList<ArrayList<String>> needs_rev = new ArrayList<>();
        ArrayList<ArrayList<String>> other_rev = new ArrayList<>();

        for (ArrayList<String> dr : directives_rev) {
            only_rev.add(new ArrayList<>());
            ignore_rev.add(new ArrayList<>());
            needs_rev.add(new ArrayList<>());
            other_rev.add(new ArrayList<>());

            while (!dr.isEmpty()) {
                String d = dr.getFirst();
                if (d.contains("only")) only_rev.getLast().add(d.substring(5));
                else if (d.contains("ignore")) ignore_rev.getLast().add(d.substring(7));
                else if (d.contains("needs")) needs_rev.getLast().add(d.substring(6));
                else other_all.add(d);

                dr.removeFirst();
            }

        }


        for (int i = 0; i < directives_rev.size(); i++) {
            System.out.println("(" + revisions.get(i) + ") only:   " + only_rev.get(i));
            System.out.println("(" + revisions.get(i) + ") ignore:   " + ignore_rev.get(i));
            System.out.println("(" + revisions.get(i) + ") needs:   " +  needs_rev.get(i));
            System.out.println("(" + revisions.get(i) + ") only:   " + other_rev.get(i));
        }




    }

    public void given_suites(ArrayList<File> suites) {

        for (File suite : suites) {
            
        }

    }
    public void given_env() {

    }

}
