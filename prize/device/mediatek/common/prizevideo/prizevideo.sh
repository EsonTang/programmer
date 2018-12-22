#!/bin/sh
echo "persist.prize.video = $(/system/bin/getprop persist.prize.video)"
if [ $(/system/bin/getprop persist.prize.video) == "1" ]; then
if test -d "/sdcard/Movies"; then
echo "/sdcard/Movies path exist"
(cp /system/media/koobeevideo/* /sdcard/Movies/)
/system/bin/setprop persist.prize.video 2
else
echo "/sdcard/Movies path not exist"
fi
else
echo "persist.prize.video is not 1"
fi

