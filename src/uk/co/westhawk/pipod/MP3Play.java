/* based on : */

/*
 * Copyright 2011 Voxeo Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.co.westhawk.pipod;

import java.io.InputStream;
import java.net.URL;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;
import com.phono.srtplight.Log;

/**
 *
 * @author tim
 */
public class MP3Play {




    /*
    uri, {we trust the mime type if it is http ? thp} start(),
    stop(), volume(value) -> value
     *
     */

    final public String uri; // public property
    Player _play;
    Thread _playThread;
    private float _gain;
    private boolean loop = true;

    public MP3Play(String suburi) {

        uri = suburi;
        _gain = (float) 0.5;


        Runnable arun = new Runnable() {

            public void run() {
                playRun();
            }
        };
        _playThread = new Thread(arun);


    }
    public void setLoop(boolean v){
        loop = v;
    }
    AudioDevice makeAudioDevice() throws JavaLayerException {
        final AudioDevice rdev = FactoryRegistry.systemRegistry().createAudioDevice();

        AudioDevice vad = new AudioDevice() {

            public void open(Decoder decoder) throws JavaLayerException {
                rdev.open(decoder);
            }

            public boolean isOpen() {
                return rdev.isOpen();
            }

            public void write(short[] samples, int offs, int len) throws JavaLayerException {
                scale(samples, offs, len);
                rdev.write(samples, offs, len);
            }

            public void close() {
                rdev.close();
            }

            public void flush() {
                rdev.flush();
            }

            public int getPosition() {
                return rdev.getPosition();
            }
        };
        return vad;
    }

    public int volume(int value) {
        _gain = (float) (value / 100.0);
        return value; // for now
    }

    public int volume() {
        return (int) _gain * 100;
    }

    private void scale(short[] samples, int offs, int len) {
        for (int i = offs; i < offs + len; i++) {
            samples[i] = (short) (samples[i] * _gain);
        }
    }

    public void playRun() {
        try {
            InputStream in;
            AudioDevice dev = null;
            while (_playThread != null){
                if (_play != null) {
                    _play.close();
                    _play = null;
                }
                if (dev != null) {
                    dev.close();
                    dev = null;
                }
                // this looks wasteful - but I'm assuming _every_ other layer is 
                // caching or buffering this - so there is really no point in doing
                // it again.
                Log.verb("playing " + uri);
                in = new URL(uri).openStream();
                dev = makeAudioDevice();

                _play = new Player(in, dev);
		if (_playThread != null) {
                 	_play.play();
		}
                if (!loop) { break;}
            }
        } catch (Exception ex) {
            Log.error(ex.toString());
        }
    }

    public void start() {
        try {
            _playThread.start();
        } catch (Throwable t) {
            Log.error("play.start() error" + t.getMessage());
        }

    }

    public void stop() {
        try {
            _playThread = null;
            _play.close();
        } catch (Throwable t) {
            Log.error("play.stop() error" + t.getMessage());
        }
    }
    
    
    public void awaitEnd() throws InterruptedException{
        if (_playThread != null){
            _playThread.join();
            _playThread = null;
        }
    }

    public static void main(String[] argv) {
        // test harness.
        Log.setLevel(Log.ALL);
        MP3Play testPlay = new MP3Play("http://downloads.bbc.co.uk/podcasts/radio4/intouch/intouch_20121218-2135a.mp3");
        testPlay.start();
        for (int s = 0; s < 60; s++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ;
            }
            System.out.print("Playing " + s + "\r");
        }
        System.out.println("\nDone.");
        testPlay.stop();
    }
}

