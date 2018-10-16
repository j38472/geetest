package BiLi;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

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
 * 5.平移过去（尽可能模拟人的先快后慢的轨迹习惯）
 */
public class GeettestCrawler {
    //验证码URL
    private static String INDEX_URL = "https://passport.bilibili.com/login";
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
            invoke();
            System.out.println("当前在测第" + i + 1 + "次,已成功" + successTimes + "次,成功率:" + ((double) successTimes / (double) i + 1) * 100 + "%");

        }
        System.out.println("Finally,测试" + TestTimes + "次,成功" + successTimes + "次,成功率:" + ((double) successTimes / (double) TestTimes) * 100 + "%");
        driver.quit();//关闭模拟浏览器
    }

    /**
     * 逻辑调用层
     */
    public static void invoke() {
        driver.get(INDEX_URL);

        By moveBet = By.cssSelector(".gt_slider_knob.gt_show");//滑动验证码滑动按钮
        waitForLoad(driver, moveBet);//等待十秒
        WebElement moveElemet = driver.findElement(moveBet);//获取验证码滑动按钮
        int i = 0 ;
        while (i++ <15){
            int distance = getMoveDistance(driver);
        }
    }

    /**
     * 模拟鼠标移动轨迹
     */
    public void move() {

    }

    /**
     * selenium的显示等待(硬性要求等待时间达到指定时间 显示等待不需要) 等待元素加载 10S
     * @param driver 浏览器模拟器
     * @param by 需要等待加载的元素
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

    /**
     * 计算需要平移的距离
     * @param driver 需要计算的模块
     * @return 平移距离
     */
    public static int getMoveDistance(WebDriver driver) {


        return 1;
    }


}
