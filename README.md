PiPod
=====

A side project to turn the Rasp Pi into a single use consumer device for the blind or elderly that plays the editions of a specified BBC podcasts 

It requires a working Java8 - As comes in NOOBS and Jessie 

On the hardware front, the Pi needs a nice loud audio device - we used the Google cardboard one from May2017 MagPi (Thanks Sam !)

We also use the Yellow button as a control to allow the skipping of podcasts.

To set up:

1) install Raspberian on your pi with suitable drivers for your audio device and set up wifi credentials.
2) put the 3 jar files in the pi user's home directory
3) edit the pipod.sh script to change the default list of BBC podcasts
currently 
"b01s6xyk", "b007qlvb", "p02pc9pj", "p02pc9x6"
Tweet of the day, Womans hour, and 2 commedy podcasts.

Simply add the new list of program ids to the end of the command in pipod.sh

4) If you aren't using the MagPi kit, you will need to change push.sh to reflect
how your pushbutton is connected.
5) test by running pipod.sh by hand check that the push button skips to the next show
6) use raspi-config to configure the boot options on the Pi - set it to auto boot without a graphical prompt and to wait for network on boot
7) add a line to /etc/rc.local
su -c 'nohup ~/pipod.sh &' -l pi
This makes PiPod start on powerup

8) you are all set.... enjoy!





