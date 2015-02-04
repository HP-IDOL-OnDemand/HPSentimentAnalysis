\set t_pwd `pwd`

\set input_file '''':t_pwd'/tweet.tbl'''
COPY tweet
FROM :input_file DELIMITER AS '|' NULL AS 'null'
DIRECT;

\set input_file '''':t_pwd'/sentiment.tbl'''
COPY sentiment (tweet_id, sentiment, topic, score) 
FROM :input_file DELIMITER AS '|' NULL AS 'null'
DIRECT;
