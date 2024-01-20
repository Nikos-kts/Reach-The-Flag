package net.etravel.com.properties;


import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyReader {

    private static String propertiesLocation = "configuration.properties";
    private Properties appProperties;
    private static PropertyReader instance;
    private static boolean customlocation = false;

    protected PropertyReader() {
        appProperties = loadProperties();
    }

    protected PropertyReader(String location) {

        this();

    }

    @SuppressWarnings("rawtypes")
    public Enumeration getKeys() {
        return appProperties.keys();
    }

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }

    public static PropertyReader getInstance(String location) {
        if (instance == null) {
            customlocation = true;
            propertiesLocation = location;
            instance = new PropertyReader(location);
        }
        return instance;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            if (customlocation) {
                properties.load(new FileReader(propertiesLocation));

            } else {
                properties.load(this.getClass().getClassLoader().getResourceAsStream(propertiesLocation));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    public String getProperty(String key) {
        return getProperty(key, key);
    }

    public String getProperty(String key, String defaultValue) {
        String result = appProperties.getProperty(key);
        if (result != null)
            return result;

        return defaultValue;
    }

    public int getInteger(String key, int defaultValue) {
        String result = getProperty(key, null);
        if (result == null)
            return defaultValue;

        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
        }

        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        String result = getProperty(key, null);
        if (result == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(result);
        } catch (NumberFormatException e) {
        }

        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String result = getProperty(key, null);
        if (result == null) {
            return defaultValue;
        }

        try {
            return Boolean.parseBoolean(result);
        } catch (NumberFormatException e) {
        }

        return defaultValue;
    }

}