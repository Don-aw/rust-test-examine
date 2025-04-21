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

    public HashMap<String, ArrayList<ArrayList<String>>> parseFilter(File f) throws IOException {

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

//                if (line.contains("apple:")) {
//                    System.out.println(line);
//                    System.out.println(f);
//                }

                // skip file altogether if is auxiliary
                if (line.contains("test") &&
                        (line.contains("aux")
                        || line.toLowerCase().contains("not a test")
                        || line.contains("helper")))
                    return fileFilters;

                else if (line.contains("revisions:")) {
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

        return fileFilters;

        //System.out.println(filters);

    }

    public void findAllFilters() throws IOException {

        filters = new HashMap<>();
        ArrayList<File> dirs = new ArrayList<>();

        listOfTestFiles(rootFolder, dirs);

        for (File test : dirs) {
            filters.put(test, parseFilter(test));
        }

    }

    public Categories analyseFolders(ArrayList<String> suiteNames) throws IOException {

        Categories info = new Categories();
        ArrayList<File> dirs = new ArrayList<>();

        for (String suite : suiteNames) {
            File currFolder = new File(Paths.get(new File("")
                    .getAbsolutePath()).getParent().toString().concat("/rust/tests/"+suite));

            listOfTestFiles(currFolder, dirs);
            for (File test : dirs) {
                HashMap<String, ArrayList<ArrayList<String>>> currFilters = parseFilter(test);
                for (String rev : currFilters.keySet()) {
                    int c = 0;
                    for (ArrayList<String> option : currFilters.get(rev)) {
                        for (String line : option) {
                            info.loadDir(line, c);
                        }
                        c++;
                    }
                }
            }
        }

        return info;


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

    public void givenEnv(ArrayList<String> selectedEnv) {

        ArrayList<File> pass = new ArrayList<>();
        ArrayList<File> fail = new ArrayList<>();

        for (File i : filters.keySet()) {   // for every test file

            for (String j : filters.get(i).keySet()) {  // for every revision { only, ignore, need }

                boolean isRan = true;

                for (String env : selectedEnv) {    // for every env selected

                    // only - if not found, isRan = false
                    if (!filters.get(i).get(j).get(0).isEmpty()) {
                        boolean found = false;
                        for (String dir : filters.get(i).get(j).get(0)) {

                            if (dir.contains(env)) found = true;

                        }
                        if (!found) {
                            isRan = false;
                            break;
                        }
                    }

                    if (!isRan) break;
                }




            }

        }

    }

    public ArrayList<String> givenDirs(ArrayList<ArrayList<String>> selectedFilter) {

        ArrayList<String> matches = new ArrayList<>();

        for (File i : filters.keySet()) {   // for every test file

            for (String j : filters.get(i).keySet()) {  // for every revision { only, ignore, need }

                boolean match = true;

                // only

                boolean found = false;


                for (int k = 0; k < 3; k++) {

                    if (!selectedFilter.get(k).isEmpty())
                        for (String keyword : selectedFilter.get(k)) {   // for every filter directive
                            found = false;
                            for (String dir : filters.get(i).get(j).get(k)) {   // for every directive in only, ignore, need
                                if (dir.contains(keyword)) {

                                    found = true;
                                }

                            }
                            if (!found) match = false;
                        }
                    
                }

                if (match) matches.add(i.getAbsolutePath().concat("#" + j));

            }

        }

        return matches;
    }

    public void addRevision(String revName, HashMap h) {
        ArrayList<ArrayList<String>> temp = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            temp.add(new ArrayList<>());
        }
        h.put(revName, temp);
    }

    public void listOfTestFiles(File folder, ArrayList<File> dirs) {

        //special parsing rules for the run-make suite
        if (folder.getName().equals("run-make")) {
            for (File subFolder : folder.listFiles()) {

                if (subFolder.getName().contains(".")) continue;

                for (File file : subFolder.listFiles()) {
                    if (file.getName().equals("rmake.rs")) {
                        dirs.add(new File(subFolder.getAbsolutePath().concat("/rmake.rs")));
                    }
                }

            }
            return;
        }

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

                for (int key = 0; key < 3; key++) {
                    for (String dir : filters.get(i).get(j).get(key)) {
                        categories.loadDir(dir, key);
                    }
                }
            }

        }

        // categories.display();

    }
}
