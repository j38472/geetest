package BiLi;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.SourceType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * bilibili极验验证码
 *
 * @author cyh
 * 参考项目： https://blog.csdn.net/qq_28379809/article/details/81210761
 * selenium模拟验证过程
 * 1.加载页面
 * 2.获取图片
 * 3.拼凑出图片
 * 4.计算需要平移的距离
 * 5.平移过去（尽可能模拟人的先快后慢的轨迹习惯）--------!----!--!-!-(将平移时间平均分配到这五段上)可以再加上随机数
 */
public class GeettestCrawler {
    private static String basePath = "src/main/resources";
    private static String FULL_IMAGE_NAME = "full-image";//完整的图像名称
    private static String BG_IMAGE_NAME = "bg-image";   //背景图像名称
    private static String INDEX_URL = "https://passport.bilibili.com/login";//验证码URL
    private static int[][] moveArray;// = new int [52][2];
    private static boolean moveArrayInit = false;
    private static int pieceNummber = 0; //小图片数量
    private static int TestTimes = 1000;//测试次数
    private static int successTimes = 0;//成功次数
    private static WebDriver driver;//selenium 实例声明

    /**
     * 静态代码块
     * selenium 连接chrom浏览器 并创建selenium的Chrome实例
     */
    static {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver\\chromedriver_win32\\chromedriver.exe");
        driver = new ChromeDriver();
    }

    public static void main(String[] args) {
        for (int i = 0; i < TestTimes; i++) {//测试次数
            try {
                invoke();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("当前在测第" + i + 1 + "次,已成功" + successTimes + "次,成功率:" + ((double) successTimes / (double) i + 1) * 100 + "%");

        }
        System.out.println("Finally,测试" + TestTimes + "次,成功" + successTimes + "次,成功率:" + ((double) successTimes / (double) TestTimes) * 100 + "%");
        driver.quit();//关闭模拟浏览器
    }

    /**
     * 逻辑调用层
     */
    public static void invoke() throws IOException, InterruptedException {
        driver.get(INDEX_URL);

        By moveBet = By.cssSelector(".gt_slider_knob.gt_show");//滑动验证码滑动按钮
        waitForLoad(driver, moveBet);//等待十秒
        WebElement moveElemet = driver.findElement(moveBet);//获取验证码滑动按钮
        int i = 0;
        while (i++ < 15) {
            int distance = getMoveDistance(driver);
            move(driver,moveElemet,distance - 6);
            By gtTypeBy = By.cssSelector(".gt_info_type");
            By gtInfoBy = By.cssSelector(".gt_info_content");
            waitForLoad(driver,gtTypeBy);
            String gtType = driver.findElement(gtTypeBy).getText();
            waitForLoad(driver,gtInfoBy);
            String gtInfo = driver.findElement(gtInfoBy).getText();
            System.out.println(gtType +"---"+gtInfo);
            if (gtType.contains("验证通过")){
                successTimes++;
            }
            /**
             * 失败
             */
            if (!gtType.equals("再来一次:")&&!gtType.equals("验证失败:")){
                Thread.sleep(4000);
                System.out.println(driver);
                break;
            }
            Thread.sleep(4000);
        }
    }

    private static void printLocation(WebElement element) {
        Point point = element.getLocation();
        System.out.println("final:"+point.toString());//(632,360)
    }

    /**
     * 移动
     *
     * @param driver   模拟器
     * @param element  模块
     * @param distance 距离
     * @throws InterruptedException 睡眠异常捕获
     */
    public static void move(WebDriver driver, WebElement element, int distance) throws InterruptedException {
        int xDis = distance;
        int moveX = new Random().nextInt(10) - 5;
        int moveY = 1;
        Actions actions = new Actions(driver);
        new Actions(driver).clickAndHold(element).perform();
        //两次移动
        Thread.sleep(2000);
        actions.moveByOffset((xDis + moveX) / 2, moveY).perform();
        Thread.sleep((int) (Math.random() * 2000));
        actions.moveByOffset((xDis + moveX) / 2, moveY).perform();
        Thread.sleep(500);
        actions.release(element).perform();

    }

    /**
     * 计算需要平移的距离
     *
     * @param driver 需要计算的模块
     * @return 平移距离
     */
    public static int getMoveDistance(WebDriver driver) throws IOException {
        String pageSource = driver.getPageSource();//获取页面源码
        String fullImageUrl = getFullImageUrl(pageSource);//获取原始图片Url
        FileUtils.copyURLToFile(new URL(fullImageUrl), new File(basePath + FULL_IMAGE_NAME + ".jpg"));
        String getBgImageUrl = getBgimageUrl(pageSource);
        FileUtils.copyURLToFile(new URL(getBgImageUrl), new File(basePath + BG_IMAGE_NAME + ".jpg"));
        initMoveArray(driver);
        restoreImage(FULL_IMAGE_NAME);
        restoreImage(BG_IMAGE_NAME);

        BufferedImage fullBI = ImageIO.read(new File(basePath + "result/" + FULL_IMAGE_NAME + "result3.jpg"));
        BufferedImage bgBI = ImageIO.read(new File(basePath + "result/" + BG_IMAGE_NAME + "result3.jpg"));
        for (int i = 0; i < bgBI.getWidth(); i++) {
            for (int j = 0; j < bgBI.getHeight(); j++) {
                int[] fullRgb = new int[3];
                fullRgb[0] = (fullBI.getRGB(i, j) & 0xff0000) >> 16;
                fullRgb[1] = (fullBI.getRGB(i, j) & 0xff00) >> 8;
                fullRgb[2] = (fullBI.getRGB(i, j) & 0xff);

                int[] bgRgb = new int[3];
                bgRgb[0] = (bgBI.getRGB(i, j) & 0xff0000) >> 16;
                bgRgb[1] = (bgBI.getRGB(i, j) & 0xff00) >> 8;
                bgRgb[2] = (bgBI.getRGB(i, j) & 0xff);
                if (difference(fullRgb, bgRgb) > 255) {
                    return i;
                }
            }
        }
        throw new RuntimeException("未找到需要平移的位置");
    }

    /**
     * @param a 图a像素点
     * @param b 图b像素点
     * @return 灰度值的绝对值
     */
    private static int difference(int[] a, int[] b) {
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
    }

    /**
     * 还原图片
     *
     * @param type 类型
     */
    private static void restoreImage(String type) {
        //分割图片 2 * 26
        for (int i = 0; i < pieceNummber; i++) {
            cutPic(basePath + type + ".jpg", basePath + "result/" + type + ".jpg", -moveArray[i][0], -moveArray[i][1], 10, 58);
        }
        //拼接图片
        String[] b = new String[(int) pieceNummber / 2];
        for (int i = 0; i < (int) pieceNummber / 2; i++) {
            b[i] = String.format(basePath + "result/" + type + "%d.jpg", i);
        }
        mergeImage(b, 1, basePath + "result/" + type + "result1.jpg");
        String[] c = new String[(int) pieceNummber / 2];
        for (int i = 0; i < (int) pieceNummber; i++) {
            c[i] = String.format(basePath + "result/" + type + "%d.jpg", i + (int) pieceNummber / 2);
        }
        mergeImage(c, 1, basePath + "result/" + type + "result2.jpg");
        mergeImage(new String[]{basePath + "result/" + type + "result1.jpg",
                basePath + "result/" + type + "result2.jpg"}, 2, basePath + "result//" + type + "result3.jpg");
        //删除产生的中间图片
        for (int i = 0; i < pieceNummber; i++) {
            new File(basePath + "result/" + type + i + ".jpg").deleteOnExit();
        }
        new File(basePath + "result/" + type + "result1.jpg").deleteOnExit();
        new File(basePath + "result/" + type + "result2.jpg").deleteOnExit();

    }

    /**
     * 图片拼接
     *
     * @param files      需要拼接的文件列表
     * @param type       1横向拼接 2纵向拼接
     * @param targetFile 输出文件
     */
    private static void mergeImage(String[] files, int type, String targetFile) {
        int length = files.length;
        File[] src = new File[length];
        BufferedImage[] images = new BufferedImage[length];
        int[][] ImageArrays = new int[length][];
        for (int i = 0; i < length; i++) {
            try {
                src[i] = new File(files[i]);
                images[i] = ImageIO.read(src[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            ImageArrays[i] = new int[width * height];
            ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
        }
        int newHeigth = 0;
        int newWidth = 0;
        for (int i = 0; i < images.length; i++) {
            //横向
            if (type == 1) {
                newHeigth = newHeigth > images[i].getHeight() ? newHeigth : images[i].getHeight();
                newWidth += images[i].getWidth();
            } else if (type == 2) {
                //纵向
                newWidth = newWidth > images[i].getWidth() ? newWidth : images[i].getWidth();
                newHeigth += images[i].getHeight();
            }
        }
        if (type == 1 && newWidth < 1) {
            return;
        }
        if (type == 2 && newHeigth < 1) {
            return;
        }
        //生成新图片
        try {

            BufferedImage ImageNew = new BufferedImage(newWidth, newHeigth, BufferedImage.TYPE_INT_RGB);
            int height_i = 0;
            int width_i = 0;
            for (int i = 0; i < images.length; i++) {
                if (type == 1) {
                    ImageNew.setRGB(width_i, 0, images[i].getWidth(), newHeigth, ImageArrays[i], 0, newWidth);
                    height_i += images[i].getHeight();
                }
            }
            //输出新图片
            ImageIO.write(ImageNew, targetFile.split("\\.")[1], new File(targetFile));
        } catch (Exception e) {
            System.err.println("生成新图片失败");
            throw new RuntimeException(e);
        }


    }


    /**
     * 分割图片
     *
     * @param srcFile 图片路径\\名称.jpg
     * @param outFile 图片输出路径//名称.jpg
     * @param x       图片坐标x
     * @param y       图片坐标y
     * @param width   图宽
     * @param height  图高
     * @return 是否成功
     */
    public static boolean cutPic(String srcFile, String outFile, int x, int y, int width, int height) {
        FileInputStream fis = null;
        ImageInputStream iis = null;
        if (!new File(srcFile).exists()) {
            return false;
        }
        try {
            fis = new FileInputStream(srcFile);
            String ext = srcFile.substring(srcFile.lastIndexOf(".") + 1);
            Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(ext);
            ImageReader reader = it.next();
            iis = ImageIO.createImageInputStream(fis);
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x, y, width, height);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0, param);
            File tempOutFile = new File(outFile);
            if (!tempOutFile.exists()) {
                tempOutFile.mkdirs();
            }
            ImageIO.write(bi, ext, new File(outFile));
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (iis != null) {
                    iis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 获取move数组
     * 单个图片的坐标
     *
     * @param driver 图片模块
     */
    private static void initMoveArray(WebDriver driver) {
        if (moveArrayInit) {
            return;
        }
        Document document = Jsoup.parse(driver.getPageSource());
        Elements elements = document.select("[class=gt_cut_bg gt_show]").first().children();//获取底图错位图片元素们
        int i = 0;
        pieceNummber = elements.size();
        moveArray = new int[pieceNummber][2];
        for (Element element :
                elements) {
            Pattern pattern = Pattern.compile(".*background-position: (.*?)px (.*?)px.*");
            Matcher matcher = pattern.matcher(element.toString());
            if (matcher.find()) {
                String width = matcher.group(1);
                String height = matcher.group(2);
                moveArray[i][0] = Integer.parseInt(width);
                moveArray[i++][1] = Integer.parseInt(height);
            } else {
                throw new RuntimeException("解析异常");
            }
        }
        moveArrayInit = true;
    }

    /**
     * 获取原始图URL
     *
     * @param pageSource 源码
     * @return 原始图URL
     */
    private static String getFullImageUrl(String pageSource) {
        String url = null;
        Document document = Jsoup.parse(pageSource);
        // 元素中第一个元素(first()) 返回属性值(attr())　
        String style = document.select("[class=gt_cut_fullbg_slice]").first().attr("style");
        //获取碎片图片的URL的编译好的正则表达式
        Pattern pattern = Pattern.compile("url\\(\"(.*)\"\\)");
        //将编译好的正则表达式放入正则适配器中
        Matcher matcher = pattern.matcher(style);//
        if (matcher.find()) {
            url = matcher.group(1);
        }
        url = url.replace(".webp", ".jpg");
        System.out.println(url);
        return url;
    }

    /**
     * 获取带背景的URL
     *
     * @param pageSoutrce 源码
     * @return 背景图的URL
     */
    public static String getBgimageUrl(String pageSoutrce) {
        String url = null;
        Document document = Jsoup.parse(pageSoutrce);
        String style = document.select(".gt_cut_bg_slice").first().attr("style");
        Pattern pattern = Pattern.compile("url\\(\"(.*)\"\\)");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            url = matcher.group(1);
        }
        url = url.replace(".webp", ".jpg");
        System.out.println(url);
        return url;
    }


    /**
     * selenium的显示等待(硬性要求等待时间达到指定时间 显示等待不需要) 等待元素加载 10S
     *
     * @param driver 浏览器模拟器
     * @param by     需要等待加载的元素
     */
    public static void waitForLoad(final WebDriver driver, final By by) {
        new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                WebElement element = driver.findElement(by);
                if (element != null) {
                    return true;
                }
                return false;
            }
        });
    }


}
