package com.zary.sniffer.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigCache {

    private static Config config;

    public static List<Pattern2TargetUrl> getPattern2TargetUrlList() {
        Map<String, String> pattern2TargetUrlList = config.getPattern2TargetUrlList();
        List<Pattern2TargetUrl> pattern2TargetUrls = new ArrayList<>();
        pattern2TargetUrlList.forEach((pattern, targetUrl) -> pattern2TargetUrls.add(new Pattern2TargetUrl(pattern, targetUrl)));
        return pattern2TargetUrls;
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        ConfigCache.config = config;
    }


    public static class Pattern2TargetUrl {

        private String pattern;
        private String targetUrl;

        public Pattern2TargetUrl(String pattern, String targetUrl) {
            this.pattern = pattern;
            this.targetUrl = targetUrl;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        @Override
        public String toString() {
            return "Pattern2TargetUrl{" +
                    "pattern='" + pattern + '\'' +
                    ", targetUrl='" + targetUrl + '\'' +
                    '}';
        }
    }


}
