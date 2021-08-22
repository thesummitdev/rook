INSERT INTO USERS (username) VALUES ('flink_system');
INSERT INTO USERS (username) VALUES ('tyler');

INSERT INTO LINKS (url, tags, unread, userId) 
  VALUES ('https://www.google.com', 
          'google', 
          true, 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (url, tags, unread, userId) 
  VALUES ('https://www.google.com/maps', 
          'google maps', 
          true, 
          (SELECT id from USERS where username='tyler')
         );

INSERT INTO LINKS (url, tags, unread, userId) 
  VALUES ('https://www.arstechnica.com', 
          'tech', 
          false, 
          (SELECT id from USERS where username='tyler')
         );
