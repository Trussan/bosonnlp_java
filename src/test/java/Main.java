import com.alibaba.fastjson.JSON;
import com.trussan.bosonnlp.BosonNLP;

public class Main {

    public static void main(String[] args) {
        String[] strs = {"他是个傻逼", "美好的世界"};
        String jsonStr = JSON.toJSONString(strs);
        System.out.println(jsonStr);
        jsonStr = JSON.toJSONString("明天会更好");
        System.out.println(jsonStr);

        BosonNLP bosonNLP = new BosonNLP("jOSzIYzf.3123.iOxqIqOxAuRK");
        try {
//            Object result = bosonNLP.sentiment("明天会更好", BosonNLP.SentimentModel.GENERAL);
            bosonNLP.convertTime("2013年二月二十八日下午四点三十分二十九秒", System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
