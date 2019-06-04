import com.restfb.*;
import com.restfb.types.FacebookType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class FbPoster {

    private String[] messages = {"Wow, X, you sure do have a lot of friends!",
            "Take a look at X’s friends.",
            "X doesn’t seem to have very many friends.",
            "X has barely any friends, what a loser.",
            "I wish I had as many friends as X does!",
            "Hey X, here’s what your friend list looks like.",
            "I made a picture out of X’s friends!",
            "Looks like X could really use some more friends right now.",
            "I wish I could be friends with X.",
            "This is what X's friends list looks like.",
            "X's friend list is pretty cool looking.",
            "Check it out, I made art out of X's friend list!"};

    private String accessToken = "";
    private String pageId = "";

    public void postPhoto(String name) {

        System.out.println("Uploading photo...");

        Random rand = new Random();
        String message = messages[rand.nextInt(messages.length)].replace("X", name);
        try {
            File file = new File("/Users/EBT/Desktop/FriendlyBotFB/recent.png");
            byte[] imageAsBytes = Files.readAllBytes(file.toPath());
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken,Version.LATEST);
            FacebookType publishPhotoResponse = facebookClient.publish(pageId + "/photos", FacebookType.class,
                    BinaryAttachment.with("recent.png", imageAsBytes, "image/png"),
                    Parameter.with("message", message));
            System.out.println("Published: " + publishPhotoResponse.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}