PiPod
=====

A side project to turn the Rasp Pi into a single use consumer device for the blind or elderly that plays the editions of a specified BBC podcasts 

It requires a working Java8 - As comes in NOOBS and Jessie  (but not Jessie-lite)

On the hardware front, the Pi needs a nice loud audio device - we used the Google cardboard one from May2017 MagPi (Thanks Sam !)

We also use the Yellow button as a control to allow the skipping of podcasts.

To set up:

1) install Raspberian on your pi with suitable drivers for your audio device and set up wifi credentials.
2) download the 3 jar files in the pi user's home directory
wget https://github.com/steely-glint/PiPod/raw/master/dist/PiPod.jar
wget https://github.com/steely-glint/PiPod/raw/master/dist/lib/JLayer.jar
wget https://github.com/steely-glint/PiPod/raw/master/dist/lib/srtplight.jar

and the 2 scripts:
wget https://raw.githubusercontent.com/steely-glint/PiPod/master/scripts/pipod.sh
wget https://raw.githubusercontent.com/steely-glint/PiPod/master/scripts/push.sh
wget https://raw.githubusercontent.com/steely-glint/PiPod/podcasts

Make the scripts executable:
chmod 755 pipod.sh
chmod 755 push.sh



3) copy podcasts to /boot/podcasts 
and edit it to contain a list of podcasts you want to hear.
Note - the advantage of the file being in /boot is that it can subsequently
be edited by removing the SD from the pi (when switched off of course), placing it in a Mac, linux laptop or even a windows machine, and editing the file in the podcast file on the boot disk which is FAT formated so can be opened and written to by other non-linux machines.
If you don't create a /boot/podcasts file PiPod will default to (my) selection of BBC podcasts :-)


4) If you aren't using the MagPi kit, you will need to change push.sh to reflect
how your pushbutton is connected.
5) test by running pipod.sh by hand check that the push button skips to the next show
6) use raspi-config to configure the boot options on the Pi - set it to auto boot without a graphical prompt and to wait for network on boot
7) add a line to /etc/rc.local
su -c 'nohup ~/pipod.sh &' -l pi
This makes PiPod start on powerup

8) you are all set.... enjoy!

Comments, bugreports etc welcome - especially if the come with Pull Requests!





