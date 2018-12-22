#init rc start
PRODUCT_COPY_FILES += frameworks/base/founderfontsframework/init.founderfonts.rc:root/init.founderfonts.rc
#init rc end

#sepolicy start
BOARD_SEPOLICY_DIRS += \
    frameworks/base/founderfontsframework/sepolicy

BOARD_SEPOLICY_UNION += \
    file.te \
    file_contexts \
    system_server.te \
    platform_app.te \
    priv_app.te \
    system_app.te \
    untrusted_app.te \
    domain.te \
    nfc.te \
    app.te \
    adbd.te \
    bluetooth.te \
    zygote.te 

#sepolicy end




