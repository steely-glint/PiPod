/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.westhawk.pipod;

import com.phono.srtplight.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author tim
 */
public class PiPod {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Log.setLevel(Log.ALL);
        if (args.length == 2) {
            new PiPod(args[0], args[1]);
        } else {
            new PiPod("radio4", "comedy");
        }
    }

    PiPod(String channel, String show) {
        try {
            // http://downloads.bbc.co.uk/podcasts/radio4/comedy/rss.xml
            HttpURLConnection icon = (HttpURLConnection) (new URL("http://downloads.bbc.co.uk/podcasts/" + channel + "/" + show + "/rss.xml").openConnection());
            // normally, 3xx is redirect
            boolean redirect = false;
            int status = icon.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                }
            }
            if (redirect) {
                // get redirect url from "location" header field
                String newUrl = icon.getHeaderField("Location");
                // open the new connnection again
                icon = (HttpURLConnection) new URL(newUrl).openConnection();
                System.out.println("Redirect to URL : " + newUrl);
            }
            int lin = icon.getContentLength();
            byte[] buff = new byte[lin];
            DataInputStream is = new DataInputStream(icon.getInputStream());
            is.readFully(buff);
           
            String xpath = "count(/rss/channel/item/enclosure/@url)";
            ByteArrayInputStream bbi = new ByteArrayInputStream(buff);
            InputSource ips = new InputSource(bbi);
            XPath xPath = XPathFactory.newInstance().newXPath();

            String res = xPath.evaluate(xpath, ips);
            int items = Integer.parseInt(res);
            Log.debug("count is " + items);
            for (int i = 1; i <= items; i++) {
                bbi = new ByteArrayInputStream(buff);
                ips = new InputSource(bbi);
                xPath = XPathFactory.newInstance().newXPath();
                xpath = "/rss/channel/item["+i + "]/enclosure/@url";
                xPath = XPathFactory.newInstance().newXPath();
                res = xPath.evaluate(xpath, ips);
                Log.debug("url is " + res);
            }
            //MP3Play ep = new MP3Play(res);
            //ep.start();

        } catch (Exception ex) {
            Log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
