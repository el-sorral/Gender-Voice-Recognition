#!/bin/bash
cd mp3
find . -mindepth 1 -maxdepth 2 -type f -name '*.mp3' | while read fname; do
	echo $fname
echo 	"mpg123 -w wav/""$fname.wav"" ""$fname"""
	mpg123 -w ../wav/"$fname.wav" "$fname"
done
 
