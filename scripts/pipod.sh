#!/bin/sh
./push.sh | java -Xmx128m -cp JLayer.jar:srtplight.jar:PiPod.jar  uk.co.westhawk.pipod.PiPod
