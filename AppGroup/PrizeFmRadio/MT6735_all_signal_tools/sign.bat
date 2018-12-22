@echo "start sign apk..."
java -jar signapk.jar platform.x509.pem platform.pk8 %1 %1_sign.apk
@echo "Signed OK, start copy to system..."
::adb remount
::adb push Prize_LockScreen.apk /system/app/Prize_LockScreen/Prize_LockScreen.apk
::adb reboot
::pause