[#ftl]
[#include "../include/constants.ftl"]

[#-- IMPORTANT!!!: For Testing URLs & Style Sheets.  Set devPath & prodPath to "" when producing production TT --]
[#assign prodPath = timesTable.getAgencyURL()?default("http://www.capmetro.org")]
[#assign devPath  = "http://www.trimet.org"]

[#-- NOTE: THESE CONSTANTS SHOULD BE UNCOMMENTED AT SOME POINT - YOU WANT RELATIVE PATHS TO YOUR SITE
[#assign devPath  = ""]
[#assign prodPath = ""]
--]


[#-- CapMetro Customizations --]
[#assign homeURL        =  "${prodPath}/index.asp"]
[#assign homeIMG        =  "${devPath}/images/logo2.jpg"]
[#assign busURL         =  "${prodPath}/riding/schedulesandmaps.asp"]
[#assign lightRailURL   =  "http://allsystemsgo.capmetro.org"]
[#assign railURL        =  "http://allsystemsgo.capmetro.org/regional-commuter-rail.shtml"]
[#assign faresURL       =  "${prodPath}/riding/fares.asp"]
[#assign mapURL         =  "${prodPath}/riding/schedulesandmaps.asp"]
[#assign tripPlannerURL =  "${prodPath}/riding/tripplanner.asp"]
[#assign alertsURL      =  "${prodPath}/riding/current_detours.asp"]
[#assign alertsPHONE    =  "(512) 474-1200"]
[#assign stopsURL       =  "${prodPath}/gisdata/gisdata.asp"]
[#assign stopDetailURL  =  "${prodPath}/riding/trip_info.asp"]
[#assign howToRideURL   =  "${prodPath}/riding/howtoride.asp"]
[#assign helpURL        =  "${howToRideURL}"]
[#assign storeURL       =  "${prodPath}/riding/buy.asp"]
[#assign spacerLINE             =  "<img src='http://www.capmetro.org/images/footer_line.gif' width='379' height='14'/>"]
[#assign agencyNAME             = "Capitol Metro"]
[#assign agencyFullNAME         = "Capital Metropolitan Transportation Authority"]
[#assign agencyAddressLineONE   = "2910 East 5th Street | Austin, Texas 78702 | (512) 389-7400"]
[#assign agencyAddressLineTWO   = "Specific Route Information | (512) 474-1200 | (800) 474-1201"]
[#assign agencyAddressLineTHREE = ""]


[#macro head]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<!-- InstanceBegin template="/Templates/2col_ride.dwt" codeOutsideHTMLIsLocked="false" -->
<head>
[#if preview?exists && preview == true]
<meta http-equiv="Expires" content="${Parameters.NEXT_MONTH?string('EEE, dd MMM yyyy hh:mm:ss')} GMT">
[#else]
<meta http-equiv="Expires" content="${Parameters.NEXT_WEEK?string('EEE, dd MMM yyyy hh:mm:ss')} GMT">
[/#if]
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta content="${agencyNAME}"/>
<meta content="schedule"/>
<meta content="timetable"/>
<meta content="built on: ${Parameters.NOW?datetime}"/>
<meta name="robots" content="all"/>

<!-- InstanceBeginEditable name="doctitle" -->
<title>TimeTable Publisher: ${name} ${key} ${dest}</title>
<meta name="keywords" content="schedule timetable route arrival" />

<!-- InstanceEndEditable -->
<!-- InstanceBeginEditable name="head" --><!-- InstanceEndEditable -->
<link href="${devPath}/css/schedules.css" rel="stylesheet" type="text/css" />
<link href="${devPath}/css/print.css" rel="stylesheet" type="text/css" media="print"/>
<script type="text/javascript" src="${devPath}/js/TriMetBasic.js"></script>
<script type="text/javascript" src="${devPath}/js/qTip.js"></script>
<style>
div#qTip {
  padding: 3px;
  border: 1px solid #666;
  border-right-width: 2px;
  border-bottom-width: 2px;
  display: none;
  background: #3B5A95;
  color: #FFF;
  font: bold 9px Verdana, Arial, Helvetica, sans-serif;
  text-align: left;
  position: absolute;
  z-index: 1000;
}
</style>
[/#macro]


[#macro header]
    <!-- Begin top-->
    <div id="top_liq">
        <!-- logo -->
        <div id="logo"><a href="${homeURL}" alt="home"><img src="${homeIMG}" alt="home img" border="0"/></a></div>
        <div id="transit"> 
          <div id="bus"><a href="${busURL}">Bus</a></div>
          <div id="rail"><a href="${lightRailURL}">Light Rail</a></div>
          <div id="streetcar"><a href="${railURL}">Commuter Rail</a></div>          
        </div> <!-- id="transit" -->
    </div>
    <!--End top--> 
    <div>
      <!--Begin top Navigation-->
      <div id="mainnav_liq">
        <ul>
          <li id="fares" title="Learn about tickets &amp; passes"><a href="${faresURL}">Fares</a></li>
          <li id="maps"  title="View route maps &amp; schedules, or browse an interactive system map"><a href="${mapURL}">Maps &amp; Schedules</a></li>
          <li id="stops" title="Get details on bus stops and rail stations near you"><a href="${stopsURL}">Stops &amp; Stations</a></li>
          <li id="how"   title="New to ${agencyNAME}? Learn the basics here"><a href="${howToRideURL}">How to Ride</a></li>
          <li id="store" title="Order tickets, passes and other merchandise for delivery by mail"><a href="${storeURL}">${agencyNAME} Store</a></li>
        </ul>
      </div> 
      <!--End top Navigation-->
    </div>
[/#macro]

[#macro footer]
  <div id="footer_liq">
   ${spacerLINE?default("<br/>")}
   <br/>${agencyFullNAME}
   <br/>${agencyAddressLineONE?default("")}
   <br/>${agencyAddressLineTWO?default("")}
   <br/>${agencyAddressLineTHREE?default("")}
  </div> <!-- id="footer_liq" -->
  <div id="expand"></div>
[/#macro]

[#macro columnFour rn="001"]
      <div id="columnFour">
        <ul>
          <li class="planner" title="Get step-by-step travel directions"><a href="${tripPlannerURL}">Trip Planner</a></li>
          <li class="status" title="Detours, delays &amp; rider notices"><a href="${alertsURL}">Rider Alerts</a></li>
        </ul>
      </div> <!--id="columnFour"-->
[/#macro]

[#macro warning]
      <p class="footnotes">
        <B>Please note:</B> Schedules may change without notice by up to three minutes to relieve overcrowding or adjust to traffic conditions.
        Service can also be affected by construction, accidents and weather conditions.
        You can check for any current detours or service disruptions at <a href="${alertsURL}">Rider Alerts</a> or call ${alertsPHONE} for route information.
      </p>
[/#macro]


[#-- 
  
  NOTE: showAllStops is set by the template as a true / false boolean
  @see Constanst.java variable allStops
 --]
[#macro links fromAllStopsPage=false fromVerticalPage=false]
      <p class="side">
  [#-- eg: don't show the route landing page linke if we're in 'PREVIEW' mode --]
  [#if preview?exists && preview == true]
        <!-- PREVIEW SCHEDULE MODE, NO RETURN to ROUTE LANDING PAGE -->
  [#else]
        <a href="${routeUrl}">${name} info</a> 
        |
  [/#if]
        <a href="${helpURL}">Help & tips</a> 
        |
        <a href="${pdfUrl}">Printable PDF</a> 
  [#if showVertical?exists && showVertical == true]      
      [#if fromVerticalPage == true]
        |
        <a href="${htmUrl}">Normal Layout</a>
[#-- 
VERT COMMENTED OUT FOR NOW -- not PUBLIC
      [#else]
        |
        <a href="${vertUrl}">Vertical Layout</a>
--]        
      [/#if]
  [/#if]
  [#if showAllStops?exists && showAllStops == true]      
        |
      [#if fromAllStopsPage == true]      
        <a href="${htmUrl}">Show schedule stops only</a>
      [#else]
        <a href="${allUrl}">Show all stops</a>
      [/#if]
  [/#if]      
      </p>
[/#macro]

[#macro basicTimesTable isMap=false tableID="timeTable" ]
      <div id="route">
      <table id="${tableID}"  border="0" cellpadding="0" cellspacing="0" width="100%"
       summary="This table shows schedules for a selection of key stops on the route for ${name} ${key} ${dest}. Stops and their schedule times are listed in the columns.">
      [#assign width = 10 ]
      [#if columns?has_content]
        [#assign width = 100 / columns?size ]
        <COLGROUP span="columns?size" width="${width}%"/>
      [/#if]
        <thead>
          <tr class="headers">
          [#assign i=0]
          [#list columns as c]
            [#if isMap == true]
            ${funct.getTh(i, "yellowdata", "bluedata", "")} scope="col" valign="top" width="${width}%" onClick="myPanTo('${c.getStopId()}')" onDblClick="myDblClck('${c.getStopId()}')">${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}</th>
            [#else]
            ${funct.getTh(i, "yellowdata", "bluedata", "")} scope="col" valign="top" width="${width}%">${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}</th>
            [/#if]
            [#assign i=i + 1]
          [/#list]
          </tr>
        </thead>
        <tbody>
        <tr>
          [#assign i=0]
          [#list columns as c]
            ${funct.getTdToolTip(i, "yellowdata", "bluedata", getColName(c))}
              <table width="100%">[#list rows as rw]<tr><td>${getHtmlTime(rw, i, "pm", "am")}</td></tr>[/#list]</table>
            </td>
            [#assign i=i + 1]
          [/#list]
        </tr>
        <tr class="headers">
          [#assign i=0]
          [#list columns as c]
            ${funct.getTh(i, "yellowdata", "bluedata", "")} valign="top">${getColNameWithMarkup(c)}</th>
            [#assign i=i + 1]
          [/#list]
        </tr>
        </tbody>
      </table>
      <a name="footnotes"></a>
      [@macros.footnotes noteList=timesTable.getFootnotes()/]
      [@warning/]
      [@effectiveDate/]          
    </div> <!-- id="route" -->
[/#macro]



[#macro verticalTimesTable isMap=false tableID="timeTable" ]
    <div id="route">
      <table id="${tableID}"  border="0" cellpadding="0" cellspacing="0" width="100%"
       summary="This table shows schedules for a selection of key stops on the route for ${name} ${key} ${dest}. Stops and their schedule times are listed in the columns.">
      [#if columns?has_content]
      <tbody>
      [#-- step 1: loop through all the stops ()  --]
      [#assign rowNum = 1]
      [#assign prevRow="#logo"]
      [#assign nextRow="#row" + (rowNum + 1)]
      [#list columns as c]
        <tr>
        [#-- step 2: the first column of the vertical table is the stop desc/id  --]
        <th scope="col" align="left" valign="top" width="30%"><a name="row${rowNum}"></a>${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}<a href="${prevRow}">&uarr;</a><a href="${nextRow}">&darr;</a></th>

        [#-- step 3: loop through the arrival times for a the stop (columns)  --]
        [#assign stTimes = timesTable.getStopTimes(c.getStopId()) ]
        [#if stTimes?exists]
          [#assign i = 1]
          [#list stTimes as cell]${funct.getTd(i, "yellowdata", "bluedata")}[#if cell?exists]${funct.getHtmlTimeFromCell(cell, "pm", "am")}[#else]&mdash;[/#if]</td>[#assign i = i+1][/#list]

[#--
aCC -- layout that saves a lot of disk space ... but doesn't layout correctly vertitaclly -- need vertical line to seperate the cells

          ${funct.getTdToolTip(rowNum, "yellowdata", "bluedata", getColName(c))}
          <table><tr>[#list stTimes as cell][#if cell?exists]${funct.getHtmlTimeFromCell(cell, "pm", "am")}[#else]&mdash;[/#if]</td>[/#list]</tr></table>
 --]          
        </td>
        [/#if]
        </tr>
        [#assign prevRow="#row" + rowNum]
        [#assign rowNum = 1 + rowNum]
        [#assign nextRow="#row" + (rowNum + 1)]
      [/#list]
      </tbody><a name="${nextRow}"></a>
      [/#if]
      </table>
      [@macros.footnotes noteList=timesTable.getFootnotes()/]
      [@warning/]
      [@effectiveDate/]          
    </div> <!-- id="route" -->
[/#macro]


[#macro verticalChunked tableID="timeTable"  chunkSize=6]
  [#assign rowNum = 1]
  [#assign M=0]
  [#assign N=chunkSize - 1]
  [#assign MAX=rows?size - 1]
  [#assign headerStr = "stops"]

    <div id="route">
      <table id="${tableID}"  border="0" cellpadding="0" cellspacing="0" width="100%"
       summary="This table shows schedules for a selection of key stops on the route for ${name} ${key} ${dest}. Stops and their schedule times are listed in the columns.">
      [#if columns?has_content]
      <tbody>

      [#-- step 1: want to 'chunk' the times table, so that we limit the number of columns of trips 
               we'll keep track of the chunks via indexs M and N (which M + chunkSize)
      --]
      [#list rows?chunk(chunkSize) as ch]
        [#if N > MAX][#assign N=MAX][/#if]

        [#-- step 2: print out column headings (trip numbers) ... again, the number of columns is chunked, 
                   so print between indicies M to N 
        --]
        <tr>
          <th width="200">${headerStr}</th>[#list rows[M..N] as rw]<th>${rw.getFootnoteSymbol()}</th>[/#list]
        </tr>

        [#-- step 3: print out a set of rows, each representing a stop and stop times for the route ... again, chunk it M to N --]
        [#list columns as c]
        ${funct.getTr(rowNum, "odd", "even")}
          [#-- step 3a: the stop id in the first column  --]
          <th width="25%">${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}</th>
          [#assign stTimes = timesTable.getStopTimes(c.getStopId())]
          [#-- step 3b: and the stop times in the subsequent columns --]
          [#assign i=0]
          [#list stTimes[M..N] as cell][#if cell?exists]${funct.getTd(i, "yellowdata", "bluedata")}${funct.getHtmlTimeFromCell( cell )}</td>[#else]${funct.getTd(i, "yellowdata", "bluedata")}&mdash;</td>[/#if][#assign i=i + 1][/#list]
        </tr>
        [#assign rowNum = 1 + rowNum]
        [/#list]

        [#-- step 4: increment our chunk indicies by the chunkSize --]
        [#assign M=M + chunkSize]
        [#assign N=N + chunkSize]

        [#-- step 5: insert two blank rows to seperate the chunks of trips --]
        [#assign headerStr = ""]
      [/#list]
      <tr><th colspan="${chunkSize + 1}"/></tr>
      <tr><th colspan="${chunkSize + 1}"/></tr>
      </table>
[/#if]
      [@macros.footnotes noteList=timesTable.getFootnotes()/]
      [@warning/]
      [@effectiveDate/]          
    </div> <!-- id="route" -->
[/#macro]


[#--
  tmOldStyle was a start into building a template that replicated JK's old schedules
--]
[#macro oldStyleTable tableID="timeTable" chunkSize=6]
  [#assign rowNum = 1]
  [#assign M=0]
  [#assign N=chunkSize - 1]
  [#assign MAX=rows?size - 1]
  [#assign headerStr = "stops"]

    <div id="route">
      <A title="This table shows schedules for a selection of key stops on the route for ${name} ${key} ${dest}. Stops and their schedule times are listed in the columns."></A>
      [#if columns?has_content]
      [#-- step 1: want to 'chunk' the times table, so that we limit the number of columns of trips 
               we'll keep track of the chunks via indexs M and N (which M + chunkSize)
      --]
      [#list rows?chunk(chunkSize) as ch]
        [#if N > MAX][#assign N=MAX][/#if]
        [#-- step 2: print out column headings (trip numbers) ... again, the number of columns is chunked, 
                   so print between indicies M to N 
        --]	

[#--
http://dev/schedules/x/t1100_0a.html
[#if hasNotes]
  [#list rows[M..N] as rw][#assign hasNotes = true]  rw.getFootnoteSymbol()?has_content[/#if][/#list]
          <A HREF="#footer">Notes-&gt;</A>
[/#if]
[/#macro]

        
        [${rw.getFootnoteSymbol()}

<PRE>
                              <A HREF="#middle">later-&gt;</A>
                              [@notesRow M=M N=N rows=rows]


        </tr>
 --]
 
 
        [#-- step 3: print out a set of rows, each representing a stop and stop times for the route ... again, chunk it M to N --]
        [#list columns as c]
        ${funct.getTr(rowNum, "odd", "even")}
          [#-- step 3a: the stop id in the first column  --]
          <th width="25%">${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}</th>
          [#assign stTimes = timesTable.getStopTimes(c.getStopId())]
          [#-- step 3b: and the stop times in the subsequent columns --]
          [#assign i=0]
          [#list stTimes[M..N] as cell][#if cell?exists]${funct.getTd(i, "yellowdata", "bluedata")}${funct.getHtmlTimeFromCell( cell )}</td>[#else]${funct.getTd(i, "yellowdata", "bluedata")}&mdash;</td>[/#if][#assign i=i + 1][/#list]
        </tr>
        [#assign rowNum = 1 + rowNum]
        [/#list]

        [#-- step 4: increment our chunk indicies by the chunkSize --]
        [#assign M=M + chunkSize]
        [#assign N=N + chunkSize]

        [#-- step 5: insert two blank rows to seperate the chunks of trips --]
        [#assign headerStr = ""]
      [/#list]
      <tr><th colspan="${chunkSize + 1}"/></tr>
      <tr><th colspan="${chunkSize + 1}"/></tr>
      </table>
[/#if]
      [@macros.footnotes noteList=timesTable.getFootnotes()/]
      [@warning/]
      [@effectiveDate/]          
    </div> <!-- id="route" -->
[/#macro]


[#macro effectiveDate]
      <p class="effective">
        This schedule is effective ${prettyDate}.
      </p>
[/#macro]

[#macro startContent]
    <!-- Begin middle-->
    <div id="content_liq">
[/#macro]

[#macro endContent] 
    </div> <!-- id="content_liq" -->   
[/#macro]


[#macro analytics account="GA-YOUR_DEFAULT_NUMBER-HERE"]
    <!-- Google Analytics -->
    <script src="http://www.google-analytics.com/urchin.js" type="text/javascript"></script>
    <script type="text/javascript">
        _uacct = "${account}";
        urchinTracker();
    </script>
[/#macro]



[#function getDetails c date]
  [#if c.getUrl()?has_content]
    [#return "<div class=\"h8\"><a title=\"Get more information about this stop.\" href=\"${c.getUrl()}\" \">Details</a></div>"]
  [#else]  
    [#return "<div class=\"h8\"><a title=\"Get more information about this stop.\" href=\"${stopDetailURL}?action=entry&Loc=" + c.getStopId() + "&date=" + date + "\">Details</a></div>"]
  [/#if]
[/#function]

[#function getColNameWithMarkup c sidOpen="<div class=\"h7\">" sidClose="</div>"]
  [#return getColName(c, "", sidOpen, sidClose, "<BR/>")]
[/#function]

[#function getColName c seperator=", " sidOpen="" sidClose="" seperatorTwo=""]
  [#assign retVal = ""]
  [#if c?exists]  
    [#assign retVal = c.getDescription() + seperatorTwo]
    [#if showStopIDs && !c.hideStopId()]
       [#assign retVal = retVal + seperator + sidOpen + "Stop ID " + c.getStopId() + sidClose]
    [/#if]
  [/#if]
  [#return retVal]
[/#function]

[#function getHtmlTimeFromCell cell pm="" am=""]
  [#if cell.getTimeAsStr()?exists]
    [#assign symbol = cell.getFootnoteSymbol()?default("") ]
    [#if symbol?has_content]
       [#assign symbol = "<a href=\"#footnotes\">" + symbol + "</a>"]    [/#if]
    [#assign time = cell.getTime() % 86400 ]
    [#if (time >= 43200)]
      [#assign retVal = "<b>" + cell.getTimeAsStr() + pm + symbol +  "</b>"]
      [#return retVal]
    [#else]
     [#assign retVal = cell.getTimeAsStr() + am + symbol]
     [#return retVal]
    [/#if]
  [/#if]

  [#return "&mdash;"]
[/#function]

[#function getHtmlTime row i pm="" am=""]
  [#if row.getCell(i)?exists]
    [#return getHtmlTimeFromCell(row.getCell(i), pm, am)]
  [/#if]

  [#return "&mdash;"]
[/#function]