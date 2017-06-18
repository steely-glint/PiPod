/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.westhawk.pipod;

import com.phono.srtplight.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author tim
 */
public class PiPod implements Runnable {

    /**
     * http://www.bbc.co.uk/podcasts.opml prog codes from here.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Log.setLevel(Log.DEBUG);
        if (args.length == 2) {
            new PiPod(args);
        } else {
            String[] names = {"b01s6xyk", "b007qlvb", "p02pc9pj", "p02pc9x6"};
            new PiPod(names);
        }
    }
    MP3Play ep = null;
    Path playPath = Paths.get("played");
    List<String> played = new ArrayList();

    public PiPod(String[] names) {
        // grab what we have already heard
        try {
            readPlayed();
        } catch (IOException ex) {
            Log.debug(ex.getMessage());
        }
        // pull in the current list of available programs
        HashMap<String, Long> progs = new HashMap();
        for (String n : names) {
            HashMap ps = getPrograms(n);
            progs.putAll(ps);
        }
        // remove anything in the played list that isn't available
        cleanPlayed(progs.keySet());

        Stream<Map.Entry<String, Long>> sorted
                = progs.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Stream<Map.Entry<String, Long>> unplayed = sorted.filter(x -> {
            return !played.contains(x.getKey());
        });

        Thread tin = new Thread(this);
        tin.setName("tin");
        tin.start();

        // and play them each in turn, allowing the user to skip
        unplayed.forEach(x -> {
            try {
                playProg(x.getKey());
                played.add(x.getKey());
                writePlayed(); // store the list of what we've heard.
            } catch (Exception ex) {
                Log.error(ex.getMessage());
            }
        });
    }

    public void cleanPlayed(Set<String> avail) {
        ArrayList<String> nplay = new ArrayList();
        played.stream().filter(p -> {
            return avail.contains(p);
        }).forEach(o -> nplay.add(o));
        played = nplay;
    }

    public void writePlayed() throws IOException {
        Files.write(playPath, played);
    }

    public void readPlayed() throws IOException {
        played = Files.readAllLines(playPath);
    }

    public void playProg(String p) throws InterruptedException {
        Log.debug("url is " + p);
        ep = new MP3Play(p);
        ep.setLoop(false);
        ep.start();
        ep.awaitEnd();

    }

    HashMap<String, Long> getPrograms(String programcode) {
        HashMap<String, Long> ret = new HashMap();
        try {
            // http://downloads.bbc.co.uk/podcasts/radio4/comedy/rss.xml
            // feed://podcasts.files.bbci.co.uk/p02pc9pj.rss
            String hurl = "https://podcasts.files.bbci.co.uk/" + programcode + ".rss";
            Log.debug("opening" + hurl);
            // "http://downloads.bbc.co.uk/podcasts/" + channel + "/" + show + "/rss.xml"
            HttpURLConnection icon = (HttpURLConnection) (new URL(hurl).openConnection());
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
                Log.debug("Redirect to URL : " + newUrl);
            }
            int lin = icon.getContentLength();
            byte[] buff = new byte[lin];
            Log.debug("read " + lin);
            DataInputStream is = new DataInputStream(icon.getInputStream());
            is.readFully(buff);
            ByteArrayInputStream bbi = new ByteArrayInputStream(buff);
            InputSource ips = new InputSource(bbi);
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression exp = xPath.compile("/rss/channel/item");
            XPathExpression uexp = xPath.compile("enclosure/@url");
            XPathExpression dexp = xPath.compile("pubDate");

            NodeList items = (NodeList) exp.evaluate(ips, XPathConstants.NODESET);
            int nitmes = items.getLength();
            for (int i = 0; i < nitmes; i++) {
                Node item = items.item(i);
                String url = uexp.evaluate(item);
                String date = dexp.evaluate(item);
                Long d = Date.parse(date);
                Log.verb("adding " + url);
                Log.verb("date " + date + " " + d);
                ret.put(url, d);
            }

        } catch (Exception ex) {
            Log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return ret;
    }

    @Override
    public void run() {
        DataInputStream din = new DataInputStream(System.in);
        while (true) {
            try {
                String l = din.readLine();
                if (ep != null) {
                    ep.stop();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
