#!/bin/bash
TWEET_FILE=$1 # e.g. id|tweet|es|created_at
IDOL_FILE=$2 # e.g. either 'id|neutral|score', blank line or 'id|sentiment|topic|score'

AGGREGATE_REGEX="(^[0-9]+\|[^|]+\|[^|]+$)|^\s*$" # e.g. id|neutral|score or blank line

grep -vE $AGGREGATE_REGEX $IDOL_FILE > sentiment.tbl # id|sentiment|topic|score

TMP_FILE=`mktemp topcoder.XXXXXX`
grep -E $AGGREGATE_REGEX $IDOL_FILE | cut -d'|' -f2,3 > $TMP_FILE
paste -d'|' $TWEET_FILE $TMP_FILE | sed 's/\|$//' > tweet.tbl
rm $TMP_FILE

