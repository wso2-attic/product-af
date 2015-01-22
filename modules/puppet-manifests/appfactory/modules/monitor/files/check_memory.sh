#!/bin/bash

USAGE="`basename $0` [-w|--warning]<percent free> [-c|--critical]<percent free>"
THRESHOLD_USAGE="WARNING threshold must be greater than CRITICAL: `basename $0` $*"
calc=/tmp/memcalc
percent_free=/tmp/mempercent
critical=""
warning=""
STATE_OK=0
STATE_WARNING=1
STATE_CRITICAL=2
STATE_UNKNOWN=3
# print usage
if [[ $# -lt 4 ]]
then
	echo ""
	echo "Wrong Syntax: `basename $0` $*"
	echo ""
	echo "Usage: $USAGE"
	echo ""
	exit 0
fi
# read input
while [[ $# -gt 0 ]]
  do
        case "$1" in
               -w|--warning)
               shift
               warning=$1
        ;;
               -c|--critical)
               shift
               critical=$1
        ;;
        esac
        shift
  done
# verify input
if [[ $warning -eq $critical || $warning -lt $critical ]]
then
	echo ""
	echo "$THRESHOLD_USAGE"
	echo ""
        echo "Usage: $USAGE"
	echo ""
        exit 0
fi
# Total memory available
total=`free -m | head -2 |tail -1 |awk '{print $2}'`
# Total memory used
used=`free -m | head -3 |tail -1 |awk '{print $3}'`
# Calc total minus used
#free=`free -m | head -2 |tail -1 |awk '{print $2-$3}'`
free=`free -m | head -3 |tail -1 |awk '{print $4}'`
# normal values
#echo "$total"MB total
#echo "$used"MB used
#echo "$free"MB free
# make it into % percent free = ((free mem / total mem) * 100)
echo "5" > $calc # decimal accuracy
echo "k" >> $calc # commit
echo "100" >> $calc # multiply
echo "$free" >> $calc # division integer
echo "$total" >> $calc # division integer
echo "/" >> $calc # division sign
echo "*" >> $calc # multiplication sign
echo "p" >> $calc # print
percent=`/usr/bin/dc $calc|/bin/sed 's/^\./0./'|/usr/bin/tr "." " "|/usr/bin/awk {'print $1'}`
#percent1=`/usr/bin/dc $calc`
#echo "$percent1"
if [[ "$percent" -le  $critical ]]
	then
		echo "CRITICAL - $free MB ($percent%) Free Memory"
		exit 2
fi
if [[ "$percent" -le  $warning ]]
        then
                echo "WARNING - $free MB ($percent%) Free Memory"
                exit 1
fi
if [[ "$percent" -gt  $warning ]]
        then
                echo "OK - $free MB ($percent%) Free Memory"
                exit 0
fi
