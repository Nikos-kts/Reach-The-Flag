package net.etravel.com.resources;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton function being responsible for creation/deletion of manually generated Resources such as Images, Colors, fonts
 * This is the proper way to handle these type of resources within an SWT application
 */
public class ResourceManager {

    private static Map<RGB, Color> m_colorMap = new HashMap<RGB, Color>();

    public static Color getColor(int systemColorID) {
        return getDisplay().getSystemColor(systemColorID);
    }

    public static Color getColor(int r, int g, int b) {
        return getColor(new RGB(r, g, b));
    }

    public static Color getColor(RGB rgb) {
        Color color = m_colorMap.get(rgb);
        if (color == null) {
            color = new Color(getDisplay(), rgb);
            m_colorMap.put(rgb, color);
        }
        return color;
    }

    public static void disposeColors() {
        for (Color color : m_colorMap.values()) {
            color.dispose();
        }
        m_colorMap.clear();
    }

    private static Map<String, Image> m_imageMap = new HashMap<String, Image>();

    protected static Image getImage(InputStream stream) throws IOException {
        try {
            ImageData data = new ImageData(stream);
            if (data.transparentPixel > 0) {
                return new Image(getDisplay(), data, data.getTransparencyMask());
            }
            return new Image(getDisplay(), data);
        } finally {
            stream.close();
        }
    }

    public static Image getImage(String path) {
        Image image = m_imageMap.get(path);
        if (image == null) {
            try {
                image = getImage(ResourceManager.class.getResourceAsStream(path));
                m_imageMap.put(path, image);
            } catch (Exception e) {
                image = getMissingImage();
                m_imageMap.put(path, image);
            }
        }
        return image;
    }

    public static Image getImage(Class<?> clazz, String path) {
        String key = clazz.getName() + '|' + path;
        Image image = m_imageMap.get(key);
        if (image == null) {
            try {
                image = getImage(clazz.getResourceAsStream(path));
                m_imageMap.put(key, image);
            } catch (Exception e) {
                image = getMissingImage();
                m_imageMap.put(key, image);
            }
        }
        return image;
    }

    private static final int MISSING_IMAGE_SIZE = 10;

    private static Image getMissingImage() {
        Image image = new Image(getDisplay(), MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
        GC gc = new GC(image);
        gc.setBackground(getColor(SWT.COLOR_RED));
        gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
        gc.dispose();
        return image;
    }



    public static void disposeImages() {
        // dispose loaded images
        {
            for (Image image : m_imageMap.values()) {
                image.dispose();
            }
            m_imageMap.clear();
        }
    }

    private static Display getDisplay() {
        return Display.getDefault();
    }


    /**
     * Dispose of cached objects and their underlying OS resources. This should only be called when the cached
     * objects are no longer needed (e.g. on application shutdown).
     */
    public static void dispose() {
        disposeColors();
        disposeImages();
    }
}
