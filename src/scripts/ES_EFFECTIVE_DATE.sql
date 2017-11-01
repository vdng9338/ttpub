-- 
-- Frank Purcell
-- September 1, 2006
-- 

set define off;
drop table trans.ES_EFFECTIVE_DATE cascade constraints;

create table trans.ES_EFFECTIVE_DATE (
  start_date  DATE  DEFAULT SYSDATE not null primary key,
  end_date    DATE  DEFAULT TO_DATE('01-APR-9998','DD-MON-YYYY') not null,
  CONSTRAINT start_date CHECK(start_date <  end_date)
);

--
-- trigger updates the EFFECTIVE_DATE's END_DATE...such that the END_DATE 
-- of a previous row is set to the newly inserted date - 1 
--
-- ASSUMES: that newly inserted rows have a START_DATE that is LATER than any 
--          existing row's START_DATE
--
create or replace 
TRIGGER ES_EFFECTIVE_DATE_BI1
BEFORE INSERT ON ES_EFFECTIVE_DATE
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
BEGIN
   update ES_EFFECTIVE_DATE ed
   set    ed.END_DATE = :new.START_DATE - 1
   where  :new.START_DATE between ed.START_DATE and ed.END_DATE
   and    ed.START_DATE <> :new.START_DATE;
END;
/

grant SELECT, DELETE, REFERENCES, ALTER, INSERT, UPDATE on ES_EFFECTIVE_DATE to PUBLIC;

INSERT INTO ES_EFFECTIVE_DATE VALUES (TO_DATE('01-03-2006','MM-DD-YYYY'), TO_DATE('09-02-2006','MM-DD-YYYY'));
INSERT INTO ES_EFFECTIVE_DATE VALUES (TO_DATE('09-03-2006','MM-DD-YYYY'), TO_DATE('01-06-2007','MM-DD-YYYY'));
INSERT INTO ES_EFFECTIVE_DATE VALUES (TO_DATE('01-07-2007','MM-DD-YYYY'), TO_DATE('01-06-2009','MM-DD-YYYY'));


commit;
select * from ES_EFFECTIVE_DATE order by START_DATE;
select * from ES_EFFECTIVE_DATE where sysdate between start_date and end_date;
select * from ES_EFFECTIVE_DATE where TO_DATE('01-07-2007','MM-DD-YYYY') between start_date and end_date;

-- NOTE: VERY DANGEROUS CODE BELOW...EXTREME CAUTION
-- USED TO TEST the EFFECTIVE_DATE END_DATE UPDATE TRIGGER
-- delete from ES_EFFECTIVE_DATE where START_DATE > '1-15-2007';
-- update ES_EFFECTIVE_DATE SET END_DATE='1-1-2222' where START_DATE = '1-14-2007';
-- INSERT INTO ES_EFFECTIVE_DATE VALUES(TO_DATE('03-04-2007','MM-DD-YYYY'), TO_DATE('01-01-2111','MM-DD-YYYY'));
-- commit;
select * from ES_EFFECTIVE_DATE order by START_DATE;
