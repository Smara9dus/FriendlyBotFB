import java.util.Random;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        GephiVizualizer gv = new GephiVizualizer();
        FbPoster fb = new FbPoster();
        Random r = new Random();
        int i;

        while(true) {
            String name = gv.vizualize();
            fb.postPhoto(name);
            i = r.nextInt(1800000) + 1800000;
            System.out.println("Sleeping for " + i);

            Thread.sleep(i);
        }
    }
}
