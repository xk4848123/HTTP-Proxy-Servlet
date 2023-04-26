package com.zary.sniffer.agent.core.transfer.http;

import java.util.Random;

public class WebUserAgent {
    /**
     * 内置列表
     */
    private static final String[] agentPool  = new String[]{
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080219 Firefox/2.0.0.12 Navigator/9.0.0.6",
        "Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13",
        "Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13",
        "Mozilla/5.0 (iPhone; U; CPU like Mac OS X) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/4A93 Safari/419.3",
        "Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0",
        "Opera/8.0 (Macintosh; PPC Mac OS X; U; en) ",
        "Opera/9.27 (Windows NT 5.2; U; zh-cn) ",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070803 Firefox/1.5.0.12",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070309 Firefox/2.0.0.3",
        "Mozilla/5.0 (Windows; U; Windows NT 5.2) Gecko/2008070208 Firefox/3.0.1",
        "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT) ",
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) ",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2) ",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)",
        "Mozilla/5.0 (Windows NT 6.1; rv:27.0) Gecko/20100101 Firefox/27.0",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)  Chrome/43.0.2357.81 Safari/537.36",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.2)",
        "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; InfoPath.2; rv:11.0) like Gecko"
    };

    /**
     * 随机返回Agent
     * @return
     */
    public static String getRandomUserAgent(){
        Random random = new Random();
        Integer idx = random.nextInt(agentPool.length);
        return agentPool[idx];
    }
}
