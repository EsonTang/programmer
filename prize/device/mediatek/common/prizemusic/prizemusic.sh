#!/bin/sh
echo "persist.prize.music = $(getprop persist.prize.music)"
if [ $(getprop persist.prize.music) == "1" ]; then
if test -d "/sdcard/Music"; then
echo "/sdcard/Music path exist"
(cp /system/media/audio/music/* /sdcard/Music/)
setprop persist.prize.music 2
else
echo "/sdcard/Music path not exist"
fi
else
echo "persist.prize.music is not 1"
fi
