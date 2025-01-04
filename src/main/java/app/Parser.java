package app;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Parser {

    ArrayList<String> ignoreList = new ArrayList<>();
    File rootFolder;
    ArrayList<String> suites = new ArrayList<>();
    HashMap<File, HashMap<String, ArrayList<ArrayList<String>>>> filters;
    Categories categories = new Categories();
    // category name: {  only:{}, ignore:{}, need:{}  }

    // specify rust compiler's /tests folder location
    public Parser(File rootFolder) throws IOException {
        this.rootFolder = rootFolder;

        BufferedReader r = new BufferedReader(new FileReader("src/main/java/ignoredKeywords.txt"));
        String l = r.readLine();
        while (l != null) {
            ignoreList.add(l);
            l = r.readLine();
        }

        // save all filters found for tests into "filter"
        findAllFilters();

        // load categories into variable
        loadCategories();
    }

    // this root folder at same location as rust compiler folder
    public Parser() throws IOException {

        this(new File(Paths.get(new File("").getAbsolutePath()).getParent().toString().concat("/rust/tests")));

    }

    public void parseFilter(File f) throws IOException {

        BufferedReader r = new BufferedReader(new FileReader(f));

        HashMap<String, ArrayList<ArrayList<String>>> fileFilters = new HashMap<>();
        addRevision("main", fileFilters);

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

                if (line.contains("apple:")) {
                    System.out.println(line);
                    System.out.println(f);
                }

                if (line.contains("revisions:")) {
                    //splice the "revisions: " out, and parse for all revisions
                    StringTokenizer st = new StringTokenizer(line.substring(11));

                    while (st.hasMoreTokens()) {
                        addRevision(st.nextToken(), fileFilters);
                    }

                } else if (line.contains("[")) {    // directive of a revision

                    for (String i : fileFilters.keySet()) {     //if contains name of revision
                        if (line.contains(i)) {

                            line = line.substring(3 + i.length());  //splice out [revision]

                            if (line.startsWith("only")) fileFilters.get(i).get(0).add(line.substring(5));
                            else if (line.startsWith("ignore")) fileFilters.get(i).get(1).add(line.substring(7));
                            else if (line.startsWith("needs")) fileFilters.get(i).get(2).add(line.substring(6));
                            else fileFilters.get(i).get(3).add(line);

                        }
                    }


                } else {

                    if (line.startsWith("only")) fileFilters.get("main").get(0).add(line.substring(5));
                    else if (line.startsWith("ignore")) fileFilters.get("main").get(1).add(line.substring(7));
                    else if (line.startsWith("needs")) fileFilters.get("main").get(2).add(line.substring(6));
                    else fileFilters.get("main").get(3).add(line);
                }
            }

            line = r.readLine();
        }

        filters.put(f, fileFilters);

        //System.out.println(filters);

    }

    public void findAllFilters() throws IOException {

        filters = new HashMap<>();
        ArrayList<File> dirs = new ArrayList<>();

        listOfTestFiles(rootFolder, dirs);

        for (File test : dirs) {
            parseFilter(test);
        }

    }

    public void displayStats() throws IOException {

        int[] amount = new int[3];

        ArrayList<TreeMap<String, Integer>> stat = new ArrayList<>();
        // only, ignore, need
        for (int i = 0; i < 3; i++) stat.add(new TreeMap<>());

        for (File i : filters.keySet()) {

            for (String j : filters.get(i).keySet()) {

                for (int k = 0; k < 3; k++) {

                    for (String condition : filters.get(i).get(j).get(k)) {

                        amount[k]++;

                        //condition = condition.split(" ")[0];

                        stat.get(k).merge(condition, 1, Integer::sum);;

                    }

                }

            }

        }

        System.out.println("\n# of tests: " + filters.size() + "\n");



        System.out.println("only: " + amount[0] + " cases\n{");
        for (String k : stat.get(0).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(0).get(k));
        }
        System.out.println("}\n");

        System.out.println("ignore: " + amount[1] + " cases\n{");
        for (String k : stat.get(1).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(1).get(k));
        }
        System.out.println("}\n");

        System.out.println("needs: " + amount[2] + " cases\n{");
        for (String k : stat.get(2).keySet()) {
            System.out.println("    \"" + k + "\" = " + stat.get(2).get(k));
        }
        System.out.println("}\n");
    }

    public void givenEnv(ArrayList<String> only, ArrayList<String> ignore, ArrayList<String> need) {

        ArrayList<File> pass = new ArrayList<>();
        ArrayList<File> fail = new ArrayList<>();

        for (File i : filters.keySet()) {   // for every test file

            for (String j : filters.get(i).keySet()) {  // for every revision { only, ignore, need }

                boolean isRan = true;

                // only - if not found, isRan = false
                if (!filters.get(i).get(j).get(0).isEmpty())
                    for (String dir : filters.get(i).get(j).get(0)) {
                        boolean found = false;


                        if (found) break;
                }


            }

        }

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

    public ArrayList<String> getSuiteNames() {

        ArrayList<String> names = new ArrayList<>();

        for (File f : rootFolder.listFiles()) {

            boolean ignore = false;
            for (String keyword : ignoreList)
                if (f.getName().contains(keyword)) ignore = true;
            if (!ignore && !f.getName().contains(".")) names.add(f.getName());
        }

        return names;

    }

    public void loadCategories() {


        for (File i : filters.keySet()) {   // for every file

            for (String j : filters.get(i).keySet()) {  // for every revision

                // for the "only" in revision

                for (String dir: filters.get(i).get(j).get(0)) {
                    categories.loadOnly(dir);
                }

                // for the "ignore"
                for (String dir: filters.get(i).get(j).get(1)) {
                    categories.loadIgnore(dir);
                }

                // for "need"
                for (String dir: filters.get(i).get(j).get(2)) {
                    categories.loadNeed(dir);
                }
            }

        }

        categories.display();

    }
}
