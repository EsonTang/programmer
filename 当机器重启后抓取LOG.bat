@echo Writed by dhy
adb devices
@echo "抓出sdcard/mtklog"
@echo off
adb pull /sdcard/mtklog mtklog/
 
@echo “抓出sdcard2/mtklog”
@echo off
adb pull /sdcard2/mtklog mtklog/sdcard2
 
@echo "抓出trace"
@echo off
adb pull /data/anr mtklog/anr
 
@echo "抓出data aee db"
@echo off
adb pull /data/aee_exp mtklog/data_aee_exp
 
@echo "抓出data mobilelog"
@echo off
adb pull /data/mobilelog mtklog/data_mobilelog
 
@echo "抓出NE core"
@echo off
adb pull /data/core mtklog/data_core
 
@echo "抓出tombstones"
@echo off
adb pull /data/tombstones mtklog/tombstones
 
@echo "抓sf rtt"
@echo off
adb pull /data/rtt_dump* mtklog/sf_rtt
adb pull /data/anr/sf_rtt mtklog/sf_rtt_1
 
@echo "完成"
pause