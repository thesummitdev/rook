INSERT INTO USERS (username, userencryptedpassword, usersalt) 
  VALUES ('tyler', 'GDDhPB/h//hXtcCbme45iS6/KgA=','OLnkz/DcTKM=');
INSERT INTO USERS (username, userencryptedpassword, usersalt, isAdmin) 
  VALUES ('rook', 'L2otWDcLTevzep8cCB8KnEJL+gw=','It5j6Xv5sDk=', true);

INSERT INTO APIKEYS (key, agent, userid) VALUES (
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJkZXYudGhlc3VtbWl0LnJvb2siLCJpYXQiOjE2NzI1Njc4MjksInVzZXJuYW1lIjoidHlsZXIifQ.Kt8Iw9NsBsw7qy_CfOQfxiLo1alnT8IHheZ-Cb2DEFAV0wMzU95JDLn0UazqzUjFytCkuNH6rmFB6YhX5k1V8A',
    'testdata', 
    (SELECT id from USERS where username='tyler')
);

INSERT INTO APIKEYS (key, agent, userid) VALUES (
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJkZXYudGhlc3VtbWl0LnJvb2siLCJpYXQiOjE2NzI1Njc4MTcsInVzZXJuYW1lIjoicm9vayJ9.oX86c8EuxNHKtVe12bOCK2sLg_2lasBtaM-JozLIYQyC26pXxGt7131ULFmIHb0fv4aHcC-usXHqcFli_QtLZw',
    'testdata', 
    (SELECT id from USERS where username='rook')
);

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

INSERT INTO LINKS (title, url, tags, userId) 
  VALUES ('google', 
          'https://www.google.com', 
          'google', 
          (SELECT id from USERS where username='rook')
        );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('google maps',
          'https://www.google.com/maps',
          'google maps',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('arstechnica',
          'https://www.arstechnica.com',
          'tech',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('tweetdeck',
          'https://tweetdeck.twitter.com',
          'social tech',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('home.bld',
          'http://home.bld',
          'homelab bld',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('news',
          'https://news.google.com',
          'news feeds tech google',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('github',
          'https://www.github.com',
          'tech dev code git',
          (SELECT id from USERS where username='rook')
         );

INSERT INTO LINKS (title, url, tags, userId)
  VALUES ('rapha',
          'https://www.rapha.cc',
          'cycling',
          (SELECT id from USERS where username='rook')
         );
