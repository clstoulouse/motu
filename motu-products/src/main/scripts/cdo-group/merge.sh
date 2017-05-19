#!/bin/sh
#
# @Param
# $1 Output absolute file path to generate. It is the result of the merge operation (Example: /opt/cmems-cis-validation/motu/data/public/download/HR_MOD.nc)
# $2 First longitude of the output file (Example: 110.0)
# $3 Distance between the first longitude and the last longitude of the output file (Example: 110)
# $4 First absolute file path to merge
# $5 Second absolute file path to merge
# $6 Optional: Third absolute file path to merge if needed

cd "$(dirname "$0")"
CUR_DIR=$(pwd)


OUTPUT_FILE=$1
TARGET_GRID_FILE=$OUTPUT_FILE"_targetGrid"
START_POINT=$2
LENGTH=$3
FILE1=$4
INPUT_GRID_FILE1=$FILE1"_Griddes.txt"
FILE2=$5
INPUT_GRID_FILE2=$FILE2"_Griddes.txt"

checkCommand(){
   if [ $1 -ne 0 ]; then
       exit $2
   fi
}

if [ $# -eq 6 ]; then
	FILE3=$6
fi

#Generate the squeletum grid
${CUR_DIR}/cdo.sh griddes $FILE1 > $INPUT_GRID_FILE1
checkCommand $? $?

#Read the squeletum grid file
while read line
do 
	echo "Line : "$line
	field=$(echo $line | cut -d '=' -f1 | cut -d ' ' -f1)
	if [ $field = "xinc" ]; then
		xinc=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "yinc" ]; then
		yinc=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "ysize" ]; then
		ysize=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "yfirst" ]; then
		yfirst=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "xname" ]; then
		xname=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "xlongname" ]; then
		xlongname=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "xunits" ]; then
		xunits=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "yname" ]; then
		yname=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "ylongname" ]; then
		ylongname=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
	if [ $field = "yunits" ]; then
		yunits=$(echo $line | cut -d '=' -f2 | cut -d ' ' -f2)
	fi
done < $INPUT_GRID_FILE1

#Compute the data to expense the grid and create the super grid
echo "The xinc value : "$xinc
echo "The ysize value : "$ysize
xsize_tmp=$(awk -v dividend="$LENGTH" -v divisor="$xinc" 'BEGIN {printf "%.2f", dividend/divisor; exit(0)}')
checkCommand $? $?
xsize=$(echo $xsize_tmp | cut -d "." -f1)
echo "The xsize value : "$xsize
echo "The grid size value : "$(($xsize*$ysize))

#Generate the super grid used to merge the files.
echo "" > $TARGET_GRID_FILE
echo "gridtype  = lonlat"             >> $TARGET_GRID_FILE
echo "gridsize  = $(($xsize*$ysize))" >> $TARGET_GRID_FILE
echo "xname     = $xname"             >> $TARGET_GRID_FILE
echo "xlongname = $xlongname"         >> $TARGET_GRID_FILE
echo "xunits    = $xunits"            >> $TARGET_GRID_FILE
echo "yname     = $yname"             >> $TARGET_GRID_FILE
echo "ylongname = $ylongname"         >> $TARGET_GRID_FILE
echo "yunits    = $yunits"            >> $TARGET_GRID_FILE
echo "xsize     = $xsize"             >> $TARGET_GRID_FILE
echo "ysize     = $ysize"             >> $TARGET_GRID_FILE
echo "xfirst    = $START_POINT"       >> $TARGET_GRID_FILE
echo "xinc      = $xinc"              >> $TARGET_GRID_FILE
echo "yfirst    = $yfirst"            >> $TARGET_GRID_FILE
echo "yinc      = $yinc"              >> $TARGET_GRID_FILE

#remap the files with the new grid ($TARGET_GRID_FILE)
echo $FILE1
${CUR_DIR}/cdo.sh remapbil,$TARGET_GRID_FILE -setgrid,$INPUT_GRID_FILE1 $FILE1 $FILE1"_Global"
checkCommand $? $?

echo $FILE2
${CUR_DIR}/cdo.sh griddes $FILE2 > $INPUT_GRID_FILE2
checkCommand $? $?
${CUR_DIR}/cdo.sh remapbil,$TARGET_GRID_FILE -setgrid,$INPUT_GRID_FILE2 $FILE2 $FILE2"_Global"
checkCommand $? $?

#Merge the 2 first files
${CUR_DIR}/cdo.sh mergegrid $FILE1"_Global" $FILE2"_Global" $OUTPUT_FILE
checkCommand $? $?
rm -f $FILE1"_Global"
rm -f $FILE2"_Global" 

#If a third file is provided remap and merge it
if [ $# -eq 6 ]; then
	echo $FILE3
	mv $OUTPUT_FILE $OUTPUT_FILE"_aux"
	${CUR_DIR}/cdo.sh remapbil,$TARGET_GRID_FILE $FILE3 $FILE3"_Global"
	checkCommand $? $?
	${CUR_DIR}/cdo.sh mergegrid $FILE3"_Global" $OUTPUT_FILE"_aux" $OUTPUT_FILE
	checkCommand $? $?
	rm -f $FILE3"_Global"
	rm -f $OUTPUT_FILE"_aux"
fi

rm $INPUT_GRID_FILE1
rm $INPUT_GRID_FILE2
rm $TARGET_GRID_FILE