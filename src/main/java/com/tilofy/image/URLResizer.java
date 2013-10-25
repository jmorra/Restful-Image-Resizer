package com.tilofy.image;

import com.google.common.io.Files;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import com.tilofy.manager.Manager;

/**
 * A class that resizes URLs.  This class will perform
 * the operation on the same machine as the web server.
 */
public class URLResizer extends Resizer {
    protected URL url;
    protected Manager manager;

    public URLResizer(URL url, int targetWidth, int targetHeight, Manager manager) {
        super(targetWidth, targetHeight);
        this.url = url;
        this.manager = manager;
    }

    /**
     * It then
     * uses ImageIO to read the URL, Scalr to resize it, and ImageIO to write it back out.  It will
     * use the extension supplied in the URL, and if none is present, it will use jpg.
     */
    @Override
    public void run() {
        try {
            BufferedImage image = ImageIO.read(url);
            // This can be null if the URL does not resolve to an image.
            if (image == null) {
                manager.setError(jobID, "URL does not represent an image");
            }
            else {
                // Let's have a default format of jpg and otherwise try and
                // use the format provided by the user
                String format = Files.getFileExtension(url.getPath());
                if (format == null || format.isEmpty())
                    format = "jpg";
                File output = new File(manager.getOutputDirectory() + File.separator + jobID + "." + format);
                BufferedImage resizedImage = Scalr.resize(image, targetWidth, targetHeight);
                ImageIO.write(resizedImage, format, output);
                manager.setOutputFile(jobID, output);
            }
        }
        catch (IOException e) {
            // There are a few reasons we might get here, such as the URL is not valid or
            // the resizing fails.  In any case, we need to log those errors.
            manager.setError(jobID, e.getMessage());
        }
    }
}
