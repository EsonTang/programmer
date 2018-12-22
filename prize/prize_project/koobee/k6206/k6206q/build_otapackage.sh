#!/bin/bash

git log --pretty=oneline > git_log.txt
git_ver=`awk 'END{print NR}' git_log.txt`
echo "Git Version: $git_ver"

sed -i 's/^BUILD_DISPLAY_ID_CUSTOM =.*$/BUILD_DISPLAY_ID_CUSTOM = F2 Plus.V1.00/' prize_project/koobee/k6206/k6206q/device/prize/pri6763_66l_kb_n1/ProjectConfig.mk
cp -rf prize_project/koobee/k6206/k6206q/* .

source build/envsetup.sh
lunch full_pri6763_66l_kb_n1-user
make -j72 2>&1 | tee build_log.txt
make -j72 otapackage 2>&1 | tee build_otapackage_log.txt
./vendor/mediatek/proprietary/scripts/sign-image/sign_image.sh
./packimages pri6763_66l_kb_n1
