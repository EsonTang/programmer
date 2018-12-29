@echo "Pull important information from hang phone, written by dhy"
adb devices

@echo "attach sdcard/mtklog"
@echo off
adb pull /sdcard/mtklog mtklog/
 
@echo ¡°attach sdcard2/mtklog¡±
@echo off
adb pull /sdcard2/mtklog mtklog/sdcard2
 
@echo "attach trace"
@echo off
adb pull /data/anr mtklog/anr
 
@echo "attach rtt dump for surfaceflinger"
@echo off
adb pull /data/rtt_dump*  mtklog/sf_dump
adb pull /data/anr/sf_rtt mtklog/sf_rtt_1
 
@echo "attach data aee db"
@echo off
adb pull /data/aee_exp mtklog/data_aee_exp
 
@echo "attach data mobilelog"
@echo off
adb pull /data/mobilelog mtklog/data_mobilelog
 
@echo "attach NE core"
@echo off
adb pull /data/core mtklog/data_core
 
@echo "attach tombstones"
@echo off
adb pull /data/tombstones mtklog/tombstones
 
@echo ¡°attach phone state¡±
@echo off
adb shell ps -t> mtklog/ps.txt
adb shell top -t -m 5 -n 3 > mtklog/top.txt
adb shell service list  >  mtklog/serviceList.txt
adb shell cat /proc/meminfo > mtklog/meminfo
adb shell cat /proc/buddyinfo > mtklog/buddyinfo
adb shell procrank > mtklog/procrank.txt
adb shell cat proc/sched_debug > mtklog/sched_debug.txt
adb shell cat proc/interrupts > mtklog/interrupts.txt
adb shell dumpstate > mtklog/dumpstate.txt

@echo "Íê³É."
pause