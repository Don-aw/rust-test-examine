import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        Parser p = new Parser();    // put this project folder in same folder as rust compiler

        long start = System.nanoTime();

        p.displayStats(p.findAllFilters());

        long time = System.nanoTime() - start;
        System.out.println("time taken: " + time / 1000000000 + "." + time % 1000000000 + " s");

    }
}