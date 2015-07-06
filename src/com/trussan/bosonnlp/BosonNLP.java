package com.trussan.bosonnlp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Jarod Yv @7/5/15 10:43 AM
 */
public class BosonNLP {
    private static final String DEFAULT_BOSONNLP_URL = "http://api.bosonnlp.com";
    private static final String ACCEPT = "application/json";
    private static final String Content_Type = "application/json";
    private static final String GZIP = "gzip";
    private static final int DEFAULT_TIMEOUT = 30 * 60;
    private static final String VERSION = "0.5.0";

    private String token;
    private String bosonnlp_url;
    private boolean compress;

    private String UA;

    public BosonNLP(String token) {
        this(token, DEFAULT_BOSONNLP_URL, true);
    }

    public BosonNLP(String token, String bosonnlp_url) {
        this(token, bosonnlp_url, true);
    }

    public BosonNLP(String token, String bosonnlp_url, boolean compress) {
        this.token = token;
        this.bosonnlp_url = bosonnlp_url;
        this.compress = compress;

        Properties props = System.getProperties();
        String osName = props.getProperty("os.name"); //操作系统名称
        String osArch = props.getProperty("os.arch"); //操作系统构架
        String osVersion = props.getProperty("os.version"); //操作系统版本
        String javaVendor = props.getProperty("java.vendor"); //Java 运行时环境供应商
        String javaVersion = props.getProperty("java.version"); //Java 运行时环境版本
        StringBuilder stringBuilder = new StringBuilder("bosonnlp.java/");
        stringBuilder.append(VERSION)
                .append(' ')
                .append(osName).append('/').append(osVersion)
                .append(" (").append(osArch).append(") ")
                .append(javaVendor).append('/').append(javaVersion);
        this.UA = stringBuilder.toString();
        System.out.println(UA);
    }

    public Object sentiment(String content) throws Exception {
        return this.sentiment(content, null);
    }

    public Object sentiment(String content, SentimentModel model) throws Exception {
        return this.sentiment(new String[]{content}, model);
    }

    public Object sentiment(String[] contents) throws Exception {
        return this.sentiment(contents, null);
    }

    public String sentiment(String[] contents, SentimentModel model) throws Exception {
        if (model == null)
            model = SentimentModel.GENERAL;
        String result = apiRequest("POST", "/sentiment/analysis?" + model.getValue(), contents);
        System.out.println(result);
        return result;
    }

    public String ner(String content) throws Exception {
        return this.ner(content, 3);
    }

    public String ner(String content, int sensitivity) throws Exception {
        return this.ner(new String[]{content}, sensitivity);
    }

    public String ner(String[] contents) throws Exception {
        return ner(contents, 3);
    }

    public String ner(String[] contents, int sensitivity) throws Exception {
        if (sensitivity < 1 || sensitivity > 5)
            sensitivity = 3;
        String result = apiRequest("POST", "/ner/analysis?sensitivity=" + sensitivity, contents);
        System.out.println(result);
        return result;
    }

    public String depparser(String content) throws Exception {
        return this.depparser(new String[]{content});
    }

    public String depparser(String[] contents) throws Exception {
        String result = apiRequest("POST", "/depparser/analysis", contents);
        System.out.println(result);
        return result;
    }

    public String keywords(String content, int top_k, boolean segmented) throws Exception {
        String result = apiRequest("POST", "/keywords/analysis?top_k=" + top_k + (segmented ? "&segmented=1" : ""), content);
        System.out.println(result);
        return result;
    }


    public String classify(String content) throws Exception {
        return this.classify(new String[]{content});
    }

    public String classify(String[] contents) throws Exception {
        String result = apiRequest("POST", "/classify/analysis", contents);
        System.out.println(result);
        return result;
    }

    public String suggest(String content) throws Exception {
        return this.suggest(content, 10);
    }

    public String suggest(String content, int top_k) throws Exception {
        if (top_k > 100)
            top_k = 100;
        else if (top_k < 1)
            top_k = 10;
        String result = apiRequest("POST", "/suggest/analysis?top_k=" + top_k, content);
        System.out.println(result);
        return result;
    }

    public String tag(String content) throws Exception {
        return this.tag(new String[]{content});
    }

    public String tag(String[] contents) throws Exception {
        String result = apiRequest("POST", "/tag/analysis", contents);
        System.out.println(result);
        return result;
    }

    public String convertTime(String pattern) throws Exception {
        String result = apiRequest("POST", "/time/analysis?pattern=" + pattern, null);
        System.out.println(result);
        return result;
    }

    public String convertTime(String pattern, long timestamp) throws Exception {
        String result = apiRequest("POST", "/time/analysis?pattern=" + pattern + "&basetime=" + (timestamp / 1000), null);
        System.out.println(result);
        return result;
    }

    private String apiRequest(String method, String path, Object data) throws Exception {

        URL url = new URL(bosonnlp_url + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("X-Token", this.token);
        conn.setRequestProperty("Accept", ACCEPT);
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Content-Type", Content_Type);

        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        if (data != null) {
            String jsonStr = JSON.toJSONString(data);
            byte[] bytes = jsonStr.getBytes("UTF-8");
            if (bytes.length > 1024 * 10 && compress) {
                try {
                    bytes = gzipCompress(bytes);
                    conn.setRequestProperty("Content-Encoding", GZIP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            conn.setDoInput(true);
            conn.setDoOutput(true);
            DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            output.write(bytes);
            output.close();
        }

        String result = null;

        InputStream is = conn.getInputStream();
        byte[] buffer = new byte[512];
        int i = -1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((i = is.read(buffer)) != -1) {
            baos.write(buffer, 0, i);
        }
        baos.flush();
        result = new String(baos.toByteArray());
        is.close();
        baos.close();

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return result;
        } else if (conn.getResponseCode() >= 400 && conn.getResponseCode() < 600) {
            String message = conn.getResponseMessage();
            try {
                JSONObject jsonObject = JSON.parseObject(result);
                message = jsonObject.getString("message");
            } catch (Exception e) {
            }
            throw new IllegalAccessException("HTTPError:" + conn.getResponseCode() + " " + message);
        } else {
            throw new IllegalAccessException("HTTPError:" + conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    private static byte[] gzipCompress(byte[] raw) throws IOException {
        if (raw == null || raw.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(raw);
        byte[] compressed = out.toByteArray();
        gzip.close();
        out.close();
        return compressed;
    }

    public enum SentimentModel {
        GENERAL("general"),
        AUTO("auto"),
        KITCHEN("kitchen"),
        FOOD("food"),
        NEWS("news"),
        WEIBO("weibo");

        private final String value;

        SentimentModel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
