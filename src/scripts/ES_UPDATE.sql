select * from ES_TRIP_TIME
where start_date > '1-1-2007'

UPDATE ES_TRIP_TIMEx
SET START_DATE = '1-7-2007'
WHERE START_DATE = '1-14-2007';
commit;

select * from ES_TRIP_TIME
where start_date > '1-1-2007'


select * from ES_TRIP_TIME
where place = 'BTC2'
and   route_number = 90
and   direction = 1
and   start_date = '1-7-2007'


update ES_TRIP_TIME
set place = 'BeavTC'
where place = 'BTC2'
and   route_number = 90
and   direction = 1
and   start_date = '1-7-2007';
commit;


select * from ES_TRIP_TIME
where place = 'BTC2'
and   route_number = 90
and   direction = 1
and   start_date = '1-7-2007';
