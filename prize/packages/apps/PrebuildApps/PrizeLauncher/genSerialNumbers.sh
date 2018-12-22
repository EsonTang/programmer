#!/bin/bash

# author: lyt-linkh 2013.11.21
# Desc: Gen the serial numbers.

total_wp=$1

[ -z "${total_wp}" ] && total_wp=0

# total_wp <= 0
[ ${total_wp} -le 0 ] && exit
# total_wp > 99
[ ${total_wp} -gt 99 ] && exit

for i in $(seq ${total_wp});
do
	tmp=$[$i-1]
	if [ $tmp -le 9 ]; then
		echo 0$tmp
	else
		echo $tmp
	fi
done

