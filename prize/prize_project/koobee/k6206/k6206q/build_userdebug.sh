#!/bin/bash

git log --pretty=oneline > git_log.txt
git_ver=`awk 'END{print NR}' git_log.txt`
echo "Git Version: $git_ver"

cp -rf prize_project/koobee/k6206/k6206q/* .

source build/envsetup.sh
lunch full_pri6763_66l_kb_n1-userdebug
make -j72 2>&1 | tee build_log.txt
./vendor/mediatek/proprietary/scripts/sign-image/sign_image.sh