package TWWall;

/**
 * 破解腾讯防水墙可疑用户的极验
 * @author  cyh
 * 参考项目：https://www.cnblogs.com/w-y-c-m/p/7359455.html
 * selenium模拟验证过程
 * 1.加载防水墙页面
 * 2.获取两张图片(如：imgTest 中的 一张下层含缺口图片 一张下层补缺口图片)以下皆称为下层图片 与 上层图片
 * 3.计算需要平移的距离
 * 4.平移过去（尽可能模拟人的先快后慢的轨迹习惯）
 */
public class GeettestCrawler {
    //验证码所在
    public String INDEX_URL = "https://007.qq.com/online.html?ADTAG=capt.slide";

    /**
     * 模拟鼠标移动轨迹
     */
    public void move(){

    }

    /**
     * 等待模块加载 10S
     */
    public void waitForLoad(){

    }

    /**
     * 计算需要平移的距离
     *  首先根据上层图片纯白边界固定其形状在从下层图片中找到匹配的位置
     * @return 平移距离
     */
    public int getMoveDistance(){

        return 1;
    }


    public static void main(String[] args) {

    }

}
