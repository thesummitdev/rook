INSERT INTO USERS (username) VALUES ('flink_system');
INSERT INTO USERS (username, userencryptedpassword, usersalt) 
  VALUES ('tyler', 'GDDhPB/h//hXtcCbme45iS6/KgA=','OLnkz/DcTKM=');

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('google',
          'https://www.google.com', 
          'google', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('google maps',
          'https://www.google.com/maps', 
          'google maps', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('arstechnica',
          'https://www.arstechnica.com', 
          'tech', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('tweetdeck',
          'https://tweetdeck.twitter.com', 
          'social tech', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('home.bld',
          'http://home.bld', 
          'homelab bld', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('news',
          'https://news.google.com', 
          'news feeds tech google', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('github',
          'https://www.github.com', 
          'tech dev code git', 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('rapha',
          'https://www.rapha.cc', 
          'cycling', 
          (SELECT id from USERS where username='tyler')
         );
