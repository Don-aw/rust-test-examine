import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Parser {

    ArrayList<String> ignoreList = new ArrayList<>();
    File rootFolder;

    // specify rust compiler's /tests folder location
    public Parser(File rootFolder) throws IOException {
        this.rootFolder = rootFolder;

        BufferedReader r = new BufferedReader(new FileReader("src/ignoredKeywords.txt"));
        String l = r.readLine();
        while (l != null) {
            ignoreList.add(l);
            l = r.readLine();
        }

    }

    // this root folder at same location as rust compiler folder
    public Parser() throws IOException {

        this(new File(Paths.get(new File("").getAbsolutePath()).getParent().toString().concat("/rust/tests")));

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

        //System.out.println(filters);

        return filters;

    }

    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> findAllFilters() throws IOException {

        ArrayList<File> dirs = new ArrayList<>();

        listOfTestFiles(rootFolder, dirs);

        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> testFilters = new HashMap<>();

        for (File test : dirs) {
            testFilters.put(test.getName(), parse_filter(test));
        }

        return testFilters;

    }

    public void displayStats(HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> filters) {

        ArrayList<HashMap<String, Integer>> stat = new ArrayList<>();
        // only, ignore, need
        for (int i = 0; i < 3; i++) stat.add(new HashMap<>());

        for (String i : filters.keySet()) {

            for (String j : filters.get(i).keySet()) {

                for (int k = 0; k < 3; k++) {

                    for (String condition : filters.get(i).get(j).get(k)) {

                        HashMap<String, Integer> counter = stat.get(k);

                        if (counter.keySet().contains(condition)) counter.put(condition, counter.get(condition) + 1);
                        else counter.put(condition, 1);
                    }

                }

            }

        }

        System.out.println("\n# of tests: " + filters.size() + "\n");
        System.out.println("only:\n{");
        for (String k : stat.get(0).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(0).get(k));
        }
        System.out.println("}\n");

        System.out.println("ignore:\n{");
        for (String k : stat.get(1).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(1).get(k));
        }
        System.out.println("}\n");

        System.out.println("need:\n{");
        for (String k : stat.get(2).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(2).get(k));
        }
        System.out.println("}\n");
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

    public void listOfTestFiles(File folder, ArrayList<File> dirs) {

        if (folder.listFiles() != null) {
            for (File file : folder.listFiles()) {
                String filename = file.getName();
                boolean ignore = false;

                for (String keyword : ignoreList) {
                    if (filename.contains(keyword)) ignore = true;
                }

                // add rust files if not ignored, else continue finding files under folders only
                if (ignore);
                else if (filename.contains(".rs")) dirs.add(file);
                else if (!filename.contains(".")) listOfTestFiles(file, dirs);


            }
        }

    }
}
