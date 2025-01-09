import app.Parser;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        Parser p = new Parser();    // put this project folder in same folder as rust compiler

        Timer t = new Timer();

        t.start();

        //p.displayStats();
        ArrayList<ArrayList<String>> selectedFilter = new ArrayList<>();
        for (int i = 0; i < 3; i++) selectedFilter.add(new ArrayList<>());
        // need
        // selectedFilter.get(0).add("nightly");

        // ignore
        selectedFilter.get(1).add("none");

        // need
        // selectedFilter.get(2).add("git-hash");

        System.out.println(p.givenDirs(selectedFilter));

        t.end();

    }
}