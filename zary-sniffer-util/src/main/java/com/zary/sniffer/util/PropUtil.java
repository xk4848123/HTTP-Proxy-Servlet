package com.zary.sniffer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropUtil {

    public static Properties readProperties() throws IOException {
        InputStream in = PropUtil.class.getResourceAsStream("/admx.properties");
        Properties props = new Properties();
        InputStreamReader inputStreamReader = new InputStreamReader(in, "UTF-8");
        props.load(inputStreamReader);
        return props;
    }
}
