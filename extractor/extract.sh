#!/bin/bash
IFS='
'
find ./wav -name *.wav | while read fname; do
#	echo $fname
	gender=${fname:6:1}
#	echo $gender
	if [ "$gender" = "m" ]; then
              gender="male"
        else
              gender="female"
        fi
	Rscript main.R "$fname" "$gender" 2> /dev/null >> out.csv
	echo ''>>out.csv
done 
