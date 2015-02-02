#!/bin/bash
tr '|' ' ' |\
  awk '{ print "update tweet set aggregate_sentiment = '\''"$2"'\'', aggregate_score = "$3" where id="$1";"  }'

