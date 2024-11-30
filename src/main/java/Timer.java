public class Timer {

    private long start;

    public Timer() {
    }

    public void start() {
        start = System.nanoTime();
    }

    public void end() {

        long time = System.nanoTime() - start;
        System.out.println("time elapsed: " + time / 1000000000 + "." + time % 1000000000 + " s");
    }

}
