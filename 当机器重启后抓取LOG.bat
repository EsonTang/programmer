@echo Writed by dhy
adb devices
@echo "ץ��sdcard/mtklog"
@echo off
adb pull /sdcard/mtklog mtklog/
 
@echo ��ץ��sdcard2/mtklog��
@echo off
adb pull /sdcard2/mtklog mtklog/sdcard2
 
@echo "ץ��trace"
@echo off
adb pull /data/anr mtklog/anr
 
@echo "ץ��data aee db"
@echo off
adb pull /data/aee_exp mtklog/data_aee_exp
 
@echo "ץ��data mobilelog"
@echo off
adb pull /data/mobilelog mtklog/data_mobilelog
 
@echo "ץ��NE core"
@echo off
adb pull /data/core mtklog/data_core
 
@echo "ץ��tombstones"
@echo off
adb pull /data/tombstones mtklog/tombstones
 
@echo "ץsf rtt"
@echo off
adb pull /data/rtt_dump* mtklog/sf_rtt
adb pull /data/anr/sf_rtt mtklog/sf_rtt_1
 
@echo "���"
pause