[#ftl]
[#include "../include/constants.ftl"]

[#--
   (TriMet DeFacto) File Naming Convention
   Online static pages at trimet have names like /w/t1006_1.htm and /u/t1006_0.htm, etc...
   The format is: 
       /w/, /s/ and /u/ directory == service key
       "t1xxx"   = no idea what the t1 means...it's just prepended to each file name
                   the xxx is the 3-digit route number (zeros are prepended if len of route num is < 3 : eg 006 == 6)
       _1 and _0 = route direction _1 = inbound, _0 = outbound.
       htm / pdf = file type. 
--]


[#-- IMPORTANT!!!: For Testing URLs & Style Sheets.  Set devPath & prodPath to "" when producing production TT --]
[#assign devPath  = "http://dev.trimet.org"]
[#assign prodPath = "http://www.trimet.org"]
[#assign devPath  = ""]
[#assign prodPath = ""]

[#macro head]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
[#if preview?exists && preview == true]
<meta http-equiv="Expires" content="${Parameters.NEXT_MONTH?string('EEE, dd MMM yyyy hh:mm:ss')} GMT">
[#else]
<meta http-equiv="Expires" content="${Parameters.NEXT_WEEK?string('EEE, dd MMM yyyy hh:mm:ss')} GMT">
[/#if]
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta content="TriMet"/>
<meta content="schedule"/>
<meta content="timetable"/>
<meta content="built on: ${Parameters.NOW?datetime}"/>
<meta name="robots" content="all"/>

<title>TriMet: ${name} ${key} ${dest}</title>
<meta name="keywords" content="schedule timetable route arrival" />

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
        <!--TriMet logo-->
        <div id="logo"><a href="${devPath}/index.shtml" title="Back to TriMet Home"><img src="${devPath}/images/logo2.gif" alt="TriMet logo" border="0"/></a></div>
        <div id="transit"> 
          <div id="bus"><a href="${devPath}/bus/index.htm">Bus<br /><br /></a></div>
          <div id="rail"><a href="${devPath}/max/index.htm">MAX<br/>Light Rail</a></div>
          <div id="wes"><a href="${devPath}/wes/index.htm">WES<br/>Commuter Rail</a></div>
          <div id="streetcar"><a href="${devPath}/streetcar/index.htm">Portland<br/>Streetcar</a></div>
        </div> <!-- id="transit" -->
    </div>
    <!--End top--> 
    <div>
      <!--Begin top Navigation-->
      <div id="mainnav_liq">
        <ul>
          <li id="fares" title="Learn about TriMet tickets &amp; passes"><a href="${devPath}/fares/index.htm">Fares</a></li>
          <li id="maps"  title="View route maps &amp; schedules, or browse an interactive system map"><a href="${devPath}/schedules/index.htm">Maps &amp; Schedules</a></li>
          <li id="stops" title="Get details on bus stops and MAX stations near you"><a href="${devPath}/stopsandstations/index.htm">Stops &amp; Stations</a></li>
          <li id="how"   title="New to TriMet? Learn the basics here"><a href="${devPath}/howtoride/index.htm">How to Ride</a></li>
          <li id="store" title="Order tickets, passes and other merchandise for delivery by mail"><a href="${devPath}/store/index.htm">TriMet Store</a></li>
        </ul>
      </div> 
      <!--End top Navigation-->
    </div>
[/#macro]

[#macro footer]
  <div id="footer_liq">
    <a href="${devPath}/emailupdates/index.htm">Email Updates</a> &bull;
    <a href="${devPath}/rss/index.htm">RSS News Feeds</a> &bull;
    <a href="${devPath}/contact/index.htm">Contact/Feedback</a><br/>
    <a href="${devPath}/legal/index.htm">Terms of Use</a> &bull; <a href="${devPath}/legal/privacy.htm">Privacy Policy</a> &bull;
    <a href="${devPath}/help/linking.htm">Linking to <em>trimet.org</em></a> &bull; <a href="${devPath}/help/siteindex.htm">Site Index</a><br/>
    &copy; ${Parameters.NOW?string("yyyy")} TriMet &bull; Tri-County Metropolitan Transportation District of Oregon &bull; Portland, Oregon<br/>
  </div> <!-- id="footer_liq" -->
  <div id="expand"></div>
[/#macro]


[#macro columnFour]
      <div id="columnFour">
        <ul>
          <li class="planner" title="Get step-by-step travel directions"><a href="${prodPath}/go/cgi-bin/plantrip.cgi">Trip Planner</a></li>
        [#-- if this route is a 'normal' route, not a combo schedule (eg: 281, 254, etc...), use a link to TT that points to the route --]
        [#if routeNum?starts_with("0") || routeNum?starts_with("1")] 
          <li class="tracker" title="Real-time arrivals for ${name}"><a href="${prodPath}/arrivals/routeStopsList.jsp?route=${routeID}">TransitTracker</a></li>
	    [#else]
          <li class="tracker" title="Real-time arrivals for ${name}"><a href="${prodPath}/arrivals/">TransitTracker</a></li>
	    [/#if]                      
          <li class="status" title="Detours, delays &amp; rider notices">${alertsLink}</li>
        </ul>
      </div> <!--id="columnFour"-->
[/#macro]


[#macro title]
      [#assign sep = "" ]
      [#if key?has_content && key != "&nbsp;" && dest?has_content && dest != "&nbsp;"]
          [#assign sep = "&middot;" ]
      [/#if]
      <h1>${name}</h1>
      <h2>${dest} ${sep} ${key}</h2>
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
      [@macros.footnotes noteList=timesTable.getFootnotes()/]
      [@warning/]
      [@effectiveDate/]          
    </div> <!-- id="route" -->
[/#macro]



[#macro horizontalTimesTable isMap=false tableID="timeTableHorizontal" ]
    <div id="route">
      <table id="${tableID}"  border="0" cellpadding="10" cellspacing="0" width="100%"
       summary="This table shows schedules for a selection of key stops on the route for ${name} ${key} ${dest}. Schedule times are listed in rows, starting with the stop name in the first cell of the row.">
      [#if columns?has_content]
      <tbody>
      [#-- step 1: loop through all the stops ()  --]
      [#assign rowNum = 0]
      [#assign prevRow="#logo"]
      [#assign nextRow="#row" + (rowNum + 1)]
      [#list columns as c]
        ${funct.getTrToolTip(rowNum, "yellowdata", "bluedata", getColName(c))}
        [#-- step 2: the first column of the vertical table is the stop desc/id  --]
[#-- 
        WITH ARROWS
        <th scope="col" align="left" valign="top" width="30%"><a name="row${rowNum}"></a>${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}<a href="${prevRow}">&uarr;</a><a href="${nextRow}">&darr;</a></th>
 --]
        <th scope="col" align="left" valign="top" width="30%"><a name="row${rowNum}"></a>${getColNameWithMarkup(c)}<br/>${getDetails(c, date)}</th>
        [#-- step 3: loop through the arrival times for a the stop (columns)  --]
        [#assign stTimes = timesTable.getStopTimesPropagateFootnotes(c.getStopId(), rowNum, "E,Y,W,F,ZF,XF")]
        [#if stTimes?exists]
          [#list stTimes as cell]<td>[#if cell?exists]${getHtmlTimeFromCell(cell, "pm", "am")}[#else]&mdash;[/#if]</td>[/#list]
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


[#macro warning]
      <p class="footnotes">
        <B>Please note:</B> Schedules may change without notice by up to three minutes to relieve overcrowding or adjust to traffic conditions.
        Service can also be affected by construction, accidents and weather conditions.
        You can check for any current detours or service disruptions at ${alertsUrl} or call 503-238-RIDE (7433) for real-time arrival information from TransitTracker<SMALL><SUP>TM</SUP></SMALL>.
        All buses, MAX trains and streetcars are accessible to people with disabilities.
      </p>
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


[#macro analytics account="UA-688646-3"]
    <!-- Google Analytics -->
    <script src="http://www.google-analytics.com/urchin.js" type="text/javascript"></script>
    <script type="text/javascript">
        _uacct = "${account}";
        urchinTracker();
    </script>
[/#macro]


[#-- 
  
  NOTE: showAllStops is set by the template as a true / false boolean
  @see Constanst.java variable allStops
 --]
[#macro links fromAllStopsPage=false fromHorizontalPage=false]
      <p class="side">
  [#-- eg: don't show the route landing page linke if we're in 'PREVIEW' mode --]
  [#if preview?exists && preview == true]
        <!-- PREVIEW SCHEDULE MODE, NO RETURN to ROUTE LANDING PAGE -->
  [#else]
        <a href="${routeUrl}">${name} info</a> 
        |
  [/#if]
        <a href="${devPath}/schedule/howto.htm">Help & tips</a> 
        |
        <a href="${pdfUrl}" onClick="javascript:urchinTracker('/pdf/${pdfUrl}');">Printable PDF</a> 
  [#if showVertical?exists && showVertical == true]      
      [#if fromHorizontalPage == true]
        |
        <a href="${htmUrl}">Vertical layout</a>
      [#else]
        |
        <a href="${horizUrl}">Horizontal layout</a>
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


[#function getDetails c date]
  [#return "<div class=\"h8\"><a title=\"Get more information about this stop.\" href=\"${prodPath}/go/cgi-bin/cstops.pl?action=entry&Loc=" + c.getStopId() + "&date=" + date + "\">Details</a></div>"]
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


[#--
  TRIMET FootNote CUSTOMIZATION
--]

[#function getHtmlTimeFromCell cell pm="" am="" defaultFootnoteSymbol=""]
  [#if cell.getTimeAsStr()?exists]
    [#assign symbol = cell.getFootnoteSymbol()?default("") + defaultFootnoteSymbol]
    [#if symbol?has_content]
       [#assign symbol = "<a href=\"#footnotes\">" + symbol + "</a>"] [/#if]
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