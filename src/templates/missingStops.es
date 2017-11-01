[#ftl]
[#setting number_format="0.##########"/]
[#import "include/functions.ftl" as funct]
[#import "include/macros.ftl"    as macros]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
  <title>TimeTable Publisher: ES Missing StopID Tool</title>
  <link rel="shortcut icon" href="/favicon.ico" >
  <link rel="icon" href="/animated_favicon1.gif" type="image/gif" >  
  <SCRIPT LANGUAGE="JavaScript" SRC="js/CalendarPopup.js"></SCRIPT>
  <meta content="-1" http-equiv="Expires"/>
  <meta content="no-cache" http-equiv="Pragma"/>
  <meta content="no-cache" http-equiv="Cache-Control"/>
  <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
  <style media="all" type="text/css">
	@import url("css/site.css");
	@import url("css/screen.css");
  </style>
</head>
<body class="composite">

<FORM action="missingStops.es" method="get">
[@macros.navBar /]

[#include "include/dateform.ftl"]  [#include "include/diffDateForm.ftl"]
<INPUT TYPE=CHECKBOX NAME="showAll" [#if RequestParameters.showAll?exists] CHECKED > Hide [#else] > Show [/#if] Route Info <input type="submit" value="update"/>
<br/>
<br/>
[#if PLACE_TO_STOP?exists ]
<br/>
[#assign csvUrl = "toCsv.es?" + queryString?default("new=true")] 
Missing Places: <a href="${csvUrl}">to CSV</a>
  [#if PLACE_TO_STOP.isShowRoute() ]
    [@showStopInfoWithRoute PLACE_TO_STOP.getNewStops() /]
  [#else]
    [@showStopInfo PLACE_TO_STOP.getNewStops() /]
  [/#if]
[/#if]
<br/>
<br/>
Location Search: <INPUT TYPE="text" NAME="text" VALUE="" SIZE="30"/> <input type="submit" value="submit"/>
<br/>
[#if LOCATIONS?exists ]
  [@macros.showLocations LOCATIONS /]
[/#if]
</FORM>
</body>
</html>


[#--
    LOCATION DETAILS in AN HTML TABLE
    
    - this will print out a list of Locations
  --]
[#macro showStopInfo tpList tableStyle="report"]
[#if tpList?exists ]
<table class="${tableStyle}">
<thead>
  <tr>
    <th>Description</th> <th>Place IDs</th> <th>Stop ID(s)</th> 
  </tr>
</thead>
<tbody>
  [#assign N=tpList?size - 1]
  [#assign rowNum = 1]
  [#list tpList as tp]
    [#if ! tp.isProcessed()] 
    ${funct.getTr(rowNum, "odd", "even")}
      [@printPlaceInfo tp, tableStyle /]
      [#assign rowNum = 1 + rowNum]
    </tr>
    [/#if]
  [/#list]
</tbody>
</table>
[/#if]
[/#macro]

[#--
    LOCATION DETAILS in AN HTML TABLE with ROUTE and STOP
    
    - this will print out a list of Locations
  --]
[#macro showStopInfoWithRoute tpList tableStyle="report"]
[#if tpList?exists ]
<table class="${tableStyle}">
<thead>
  <tr>
    <th>Route</th> <th>Dir</th> <th>Description</th> <th>Place IDs</th> <th>Stop ID(s)</th> 
  </tr>
</thead>
<tbody>
  [#assign N=tpList?size - 1]
  [#assign rowNum = 1]
  [#list tpList as tp]
    ${funct.getTr(rowNum, "odd", "even")}
      <td>${tp.getRoute()}</td>
      <td>${tp.getDir()}</td>
      [@printPlaceInfo tp, tableStyle /]
      [#assign rowNum = 1 + rowNum]
    </tr>
  [/#list]
</tbody>
</table>
[/#if]
[/#macro]

[#macro printPlaceInfo tp tableStyle="report"]
[#if tp?exists ]
      <td>${tp.getDescription()}</td>  
      <td>${tp.getPlaceId()}</td>  
      <td> 
      [#if tp.getStopList()?exists ]
        [#list tp.getStopList() as st ]${st}[#if st_has_next],[/#if] [/#list]
      [/#if]
      </td>
[/#if]
[/#macro]
