import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class demo {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            int moveX = new Random().nextInt(10) - 5;
//            BufferedImage fullBI = ImageIO.read(new File(basePath + "result/" + FULL_IMAGE_NAME + "result3.jpg"));
//            System.out.println((fullBI.getRGB(i, j) & 0xff0000));
        }

    }
}
