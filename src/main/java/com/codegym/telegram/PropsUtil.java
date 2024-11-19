package com.codegym.telegram;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class PropsUtil {


    private static final Logger logger = Logger.getLogger(PropsUtil.class.getName());

    public static String getProperty(String key, String defaultMsg){
        try(FileInputStream propsFile = new FileInputStream("src/main/resources/app.properties")) {
            Properties props = new Properties();
            props.load(propsFile);
            return props.getProperty(key,defaultMsg);

        } catch (IOException e) {
            logger.severe("fail to load properties file");
        }
        return defaultMsg;
    }
}
