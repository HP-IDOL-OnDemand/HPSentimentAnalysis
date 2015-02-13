-- In Vertica, varchar size is given as byte length, not character length.
-- We consider 4 bytes per character (worst case in UTF-8).
-- Therefore 140*4 = 560
create table tweet
(	id                  integer         not null primary key,
	message             varchar(560)    not null,
    lang                char(2)         not null,
    created_at          timestamp       not null,
    aggregate_sentiment varchar(10),
    aggregate_score     float
);

create table sentiment
(	id        auto_increment primary key,
    tweet_id  integer not null,
    sentiment varchar(560),
    topic     varchar(560),
    score     float
);

alter table sentiment 
        add constraint fk_sentiment_tweet foreign key (tweet_id)
            references tweet (id);
