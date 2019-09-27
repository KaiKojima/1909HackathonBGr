
create user bgr@'%' identified by 'alterbo159753';

grant all on hiscore.* to bgr@'%';

select * from record ;

select
  *
FROM
  record
ORDER BY
  score desc
  , date DESC
  LIMIT 1
;



INSERT INTO 
 RECORD 
VALUES(
'test'
,10
  ,SYSDATE() 
);


select sysdate();