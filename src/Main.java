public class Main {

    public static void main(String[] args) throws InterruptedException {

        GephiVisualizer gv = new GephiVisualizer();
        FbPoster fb = new FbPoster();

        String name = gv.visualize();
        fb.postPhoto(name);
    }
}
