RouteNames.csv
===============
  Purpose: Place to update route names (route, service key, destination) from defaults.

  ex: 
       1. Route 53 has a key name of 'Morning Loop' and 'Evening Loop'
       2. Route 31 has a different Destination for each Service Key (eg: on Sundays it doesn't go to Estacada)
       3. ComboRoutes need a name -  example MAX Blue & Red

  Gotchas: 
       If you define a new Combo Route (or change the Combo Route's number) in ComboRoutes.csv, you
       also better update this file, or you won't see your combo route.


ComboRoutes.csv
===============
  Purpose: Place where Time Tables which are composed of two or more routes are defined
  Limits:  Can only combine tables of the same direction.  Eg, you can't say to make a table from 
           Route X-Inbound and Route Y-Outbound.

  ex: 
       1. MAX Red & Blue  
       2. 80/81 Kane Road - http://trimet.org/schedules/w/t1280_0.htm
       3. 54/56 Beav/Schl - http://trimet.org/schedules/w/t1254_0.htm

  Gotchas: 
       When you define a new Combo Route, you need to also define the RouteName and TimePoints 
       for anything to show up.  Unlike normal routes, you don't have the luxery of pulling this
       data from the data prior.




RouteNotes.csv:
===============
 - CSV file that John Kellerman generated, and Frank Purcell edited (eg: I cut some stuff from John's
   original file, and I also put the multiple values per footnote).  
 - NOTE: You'll see the same footnote text repeated multiple times.  That's by design, as I wanted to 
   keep each route with it's own note, in case just that route's note changes (if a bunch of routes
   shared one note, it could be kind of disasterous...).  The MAX note is the only one shared across
   routes, since that's such a generic note.

