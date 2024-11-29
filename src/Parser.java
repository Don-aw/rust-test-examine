import java.io.*;
import java.nio.file.Paths;
import java.util.*;

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

    public HashMap<String, ArrayList<ArrayList<String>>> parse_filter(File f) throws IOException {

        BufferedReader r = new BufferedReader(new FileReader(f));

        HashMap<String, ArrayList<ArrayList<String>>> filters = new HashMap<>();
        addRevision("main", filters);

        /*
            {
                "*revision name*":
                    [*only*],
                    [*ignore*],
                    [*needs*],
                    [*others*]
            }
         */

        String line = r.readLine();
        while (line != null) {
            if (line.contains("//@ ")) {

                line = line.substring(4);
                //splice the "//@ " out

                if (line.contains("revisions:")) {
                    //splice the "revisions: " out
                    StringTokenizer st = new StringTokenizer(line.substring(11));

                    while (st.hasMoreTokens()) {
                        addRevision(st.nextToken(), filters);
                    }

                } else if (line.contains("[")) {

                    for (String i : filters.keySet()) {
                        if (line.contains(i)) {

                            line = line.substring(3 + i.length());

                            if (line.contains("only")) filters.get(i).get(0).add(line.substring(5));
                            else if (line.contains("ignore")) filters.get(i).get(1).add(line.substring(7));
                            else if (line.contains("needs")) filters.get(i).get(2).add(line.substring(6));
                            else filters.get(i).get(3).add(line);

                        }
                    }


                } else {

                    if (line.contains("only")) filters.get("main").get(0).add(line.substring(5));
                    else if (line.contains("ignore")) filters.get("main").get(1).add(line.substring(7));
                    else if (line.contains("needs")) filters.get("main").get(2).add(line.substring(6));
                    else filters.get("main").get(3).add(line);
                }
            }

            line = r.readLine();
        }

        System.out.println(filters);

        return filters;

    }

    public void given_suites(ArrayList<File> suites) {

        for (File suite : suites) {
            
        }

    }
    public void given_env() {

    }

    public void addRevision(String revName, HashMap h) {
        ArrayList<ArrayList<String>> temp = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            temp.add(new ArrayList<>());
        }
        h.put(revName, temp);
    }
}
