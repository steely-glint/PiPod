/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.westhawk.pipod;

import com.phono.srtplight.Log;
import java.io.InputStream;
import java.net.URL;
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
            new PiPod(args[0],args[1]);
        } else {
        new PiPod("radio4", "intouch");
        }
    }

    PiPod(String channel, String show) {
        try {
            // http://downloads.bbc.co.uk/podcasts/radio4/comedy/rss.xml
            InputStream in = new URL("http://downloads.bbc.co.uk/podcasts/" + channel + "/" + show + "/rss.xml").openStream();
            String xpath = "/rss/channel/item/link";
            InputSource ips = new InputSource(in);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String res = xPath.evaluate(xpath, ips);
            Log.debug("res is " + res);
            MP3Play ep = new MP3Play(res);
            ep.start();

        } catch (Exception ex) {
            Log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
