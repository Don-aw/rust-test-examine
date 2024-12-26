import app.Parser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        Parser p = new Parser();    // put this project folder in same folder as rust compiler

        Timer t = new Timer();

        t.start();

        p.displayStats();

        t.end();

    }
}