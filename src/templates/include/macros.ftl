[#--
    FREEMARKER MACROS

    Frank Purcell
    Version 1.0  
    Created:     July 10, 2006
    Update:      July 10, 2006    - added vertical time tables
    Update:      August 28, 2006  - added nav-bar macro; handful of simple updates
    Update:      Sept 20, 2006    - added showLocation 
    Last Update: Nov 5, 2006      - route select & agency, show / hide stop ids
  --]


[#--
    LOCATION DETAILS in AN HTML TABLE
    
    - this will print out a list of Locations
  --]
[#macro showMessage]	
[#if errorMessage?exists ]
<B>${errorMessage}</B><br/>
[/#if]
[#if errorMessagePopup?exists ]
<script type="text/javascript">
  alert("${errorMessagePopup}")
</script>
[/#if]
[/#macro]


[#--
    LOCATION DETAILS in AN HTML TABLE
    
    - this will print out a list of Locations
  --]
[#macro showLocations locations tableStyle="its"]
[#if locations?exists]
<table class="${tableStyle}">
<thead>
  <tr>
    <th>Stop ID</th> <th>Description</th> <th>Street Direction</th> <th>Relative Position</th> <th>Preferred Transfer</th> <th>Place IDs</th>
  </tr>
</thead>
<tbody>
  [#assign N=locations?size - 1]
  [#assign rowNum = 1]
  [#list locations as loc]
    ${funct.getTr(rowNum, "odd", "even")}
      <td>${loc.getLocationId()}</td>  
      <td>${loc.getPublicLocationDescription()}</td> 
      <td>${loc.getStreetDirection()}</td> 
      <td>${loc.getRelativePosition()}</td> 
      <td>${loc.getPreferredTransfer() }</td>
      <td> 
      [#if loc.getHastusPlaceLocations()?exists ]
        [#list loc.getHastusPlaceLocations() as hpl ]${hpl.getId().getPlaceId()}[#if hpl_has_next],[/#if] [/#list]
      [/#if]
      </td>
    </tr>
    [#assign rowNum = 1 + rowNum]
  [/#list]
</tbody>
</table>
[/#if]
[/#macro]

 
[#--
    SIMPLE TIMESTABLE
    
    - this will print out a time table with stop ids on the left side of the table
    - the columns are the trips, and each row is a time for a given stop
  --]
[#macro simpleTimesTable columns showStopIDs=true rows="" tableStyle="its"]
<table class="${tableStyle}">
[#if columns?has_content]
<thead>
  <tr>
    [#list columns as cl]
      <th>${cl.getDescription()?default("")}</th>
    [/#list]
  </tr>
  [#if showStopIDs]
  <tr>
    [#list columns as c]
        ${funct.getStopIdAsHTML(c, "th")}
    [/#list]
  </tr>
  [/#if]
</thead>
[/#if]
[#if rows?has_content]
<tbody>
  [#assign N=columns?size - 1]
  [#assign rowNum = 1]
  [#list rows as rw]
    ${funct.getTr(rowNum, "odd", "even")}
      [#list 0..N as i]
        <td>${funct.getHtmlTime(rw, i)}</td>
      [/#list]
    </tr>
    [#assign rowNum = 1 + rowNum]
  [/#list]
</tbody>
[/#if]
</table>
[/#macro]


[#--
    VERTICAL TIMESTABLE
    
    - this will print out a time table with stop ids on the left side of the table
    - the columns are the trips, and each row is a time for a given stop
  --]
[#macro verticalTimesTable tt stops times="" tableStyle="its"]
<table class="${tableStyle}">
[#if times?has_content]
<thead>
  <tr>
    <th width="200">stops below / trips numbers right:</th>
    [#list times as rw]
      <th width="20">${rw.getTrip()}</th>
    [/#list]
  </tr>
</thead>
[/#if]
[#if stops?has_content]
<tbody>
  [#-- step 1: loop through all the stops ()  --]
  [#assign rowNum = 1]
  [#list stops as cl]
    ${funct.getTr(rowNum, "odd", "even")}

      [#-- step 2: the first column of the vertical table is the stop desc/id  --]
      <th width="25%">
         <div align="left">${cl.getDescription()?default("")} [#if tt.showStopIDs() && !cl.hideStopId()]<BR/> ${cl.getStopId()}[/#if]</div>
      </th>

      [#-- step 3: loop through the arrival times for a the stop (columns)  --]
      [#assign stTimes = tt.getStopTimes(cl.getStopId()) ]
      [#if stTimes?exists]
        [#list stTimes as cell]
          [#if cell?exists]
          <td width="30">${funct.getHtmlTimeFromCell( cell )}</td> 
          [#else]
          <td width="30">&mdash;</td> 
          [/#if]
        [/#list]
      [/#if]
    </tr>
    [#assign rowNum = 1 + rowNum]
  [/#list]
</tbody>
[/#if]
</table>
[/#macro]



[#--
    VERTICAL CHUNKED TIMESTABLE
    
    - this will print out a time table with stop ids on the left side of the table
    - the columns are the trips, and each row is a time for a given stop
    - the number of columns (eg: number of trips) is specified by rowLen.  when the number of
      trips exceeds rowLen, the next trips are added to another set of rows below
    - there's a two row 'blank' seperator between sets of stop time rows

  --]
[#macro verticalChunkedTimesTable tt stops times="" tableStyle="its" chunkSize=11 ]
[#if times?has_content]
<table class="${tableStyle}">
  [#assign rowNum = 1]
  [#assign M=0]
  [#assign N=chunkSize - 1]
  [#assign MAX=times?size - 1]
  [#assign headerStr = "stops below / trips numbers right:"]

  [#-- step 1: want to 'chunk' the times table, so that we limit the number of columns of trips 
               we'll keep track of the chunks via indexs M and N (which M + chunkSize)
   --]
  [#list times?chunk(chunkSize) as ch]
      [#if N > MAX]
        [#assign N=MAX]
      [/#if]

      [#-- step 2: print out column headings (trip numbers) ... again, the number of columns is chunked, 
                   so print between indicies M to N 
      --]
      <tr>
          <th width="200">${headerStr}</th>
          [#list times[M..N] as rw]
              <th width="20">${rw.getTrip()}</th>
          [/#list]
      </tr>

      [#-- step 3: print out a set of rows, each representing a stop and stop times for the route ... again, chunk it M to N --]
      [#list stops as cl]
        ${funct.getTr(rowNum, "odd", "even")}
          [#-- step 3a: the stop id in the first column  --]
          <th width="25%"><div align="left">${cl.getDescription()?default("")} [#if tt.showStopIDs() && !cl.hideStopId()]<br/>Stop ID ${cl.getStopId()}[/#if]</div></th>
          [#assign stTimes = tt.getStopTimes(cl.getStopId()) ]

          [#-- step 3b: and the stop times in the subsequent columns --]
          [#list stTimes[M..N] as cell] 
          [#if cell?exists]
             <td width="30">${funct.getHtmlTimeFromCell( cell )}</div></td> 
          [#else]
             <td width="30"><div valign="center">&mdash;</td> 
          [/#if]
          [/#list]
        </tr>
        [#assign rowNum = 1 + rowNum]
      [/#list]

      [#-- step 4: increment our chunk indicies by the chunkSize --]
      [#assign M=M + chunkSize]
      [#assign N=N + chunkSize]


      [#-- step 5: insert two blank rows to seperate the chunks of trips --]
      [#assign headerStr = ""]
      <tr><th colspan="${chunkSize + 1}"/></tr>
      <tr><th colspan="${chunkSize + 1}"/></tr>
  [/#list]
</table>
[/#if]
[/#macro]


[#--
    SIMPLE TIMESTABLE (Not sure why this is here)
 --]
[#macro table tableStyle columns showStopIDs=true rows=""]
<table>
<thead>
  <tr>
    [#list columns as cl]
      <th>${cl.getDescription()?default("")}</th>
    [/#list]
  </tr>
  [#if showStopIDs]
  <tr>
    [#list columns as c]
      ${funct.getStopIdAsHTML(c)}
    [/#list]
  </tr>
  [/#if]
</thead>
<tbody>
  [#assign N=columns?size - 1]
  [#assign rowNum = 1]
  [#list rows as rw]
    <tr>
      [#list 0..N as i]
        <td>${funct.getHtmlTime(rw, i)}</td>
      [/#list]
    </tr>
    [#assign rowNum = 1 + rowNum]
  [/#list]
</tbody>
</table>
[/#macro]


[#--
    FOOTNOTES
 --]
[#macro footnotes noteList caption=""]
  [#if noteList?has_content ]
      <a name="footnotes"></a>
      [#if caption?has_content ]
      <p class="caption">${caption}</p>
      [/#if]
      <p class="footnotes">
      [#list noteList as fn]
        [#if fn.getSymbol()?has_content]<B>${fn.getSymbol()}:</B>[/#if] ${fn.getFormattedNote()}<BR/>
      [/#list]
      </p>
  [/#if]
[/#macro]



[#--
    Standard Web Page Header
 --]
[#macro head title="tool" path="." terminate=true]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
  <title>TimeTable Publisher: ${title}</title>
  <link rel="shortcut icon" href="/favicon.ico"/>
  <link rel="icon" href="/animated_favicon1.gif" type="image/gif"/>
  <SCRIPT LANGUAGE="JavaScript" SRC="${path}/js/CalendarPopup.js"></SCRIPT>
  <SCRIPT LANGUAGE="JavaScript" SRC="${path}/js/editor.js"></SCRIPT>
  <meta content="-1" http-equiv="Expires"/>
  <meta content="no-cache" http-equiv="Pragma"/>
  <meta content="no-cache" http-equiv="Cache-Control"/>
  <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
  <style media="all" type="text/css">
      @import url("${path}/css/ttpub.css");
  </style>
[#if terminate]
</head>
[/#if]
[/#macro]



[#--
    Route List Drop Down
    Used on pages like Configure and View
 --]
[#macro routeDropDown routeList route="" submit=true]
<p>
  <select class="routeDropDown" name="${Parameters.ROUTE}">
    <option disabled selected>Please select a route</option>
    [#list routeList as rt]
      [@routeOption rtParam=rt selectValue=route agency=agency/]
    [/#list]
  </select>
  [#if submit == true]
    <input type="submit" value="submit"/>
  [/#if]
</p>
[/#macro]
 
[#macro routeOption rtParam selectValue agency=""]
  [#assign num    = "${rtParam.getRouteID()}"]
  [#assign ageNum = "${agency}${Parameters.AGENCY_ROUTE_SEP}${selectValue}"]
  [#if rtParam.getRouteName()?exists ]
    [#if num == selectValue]
      <OPTION VALUE="${num}" selected>${rtParam.getRouteName()?default("")}</option>
    [#elseif num == ageNum]
      <OPTION VALUE="${num}" selected>${rtParam.getRouteName()?default("")}</option>
    [#else]
      <OPTION VALUE="${num}">${rtParam.getRouteName()?default("")}</option>
  [/#if]
  [/#if]
[/#macro]


[#--
    Route Selection List
    used on pages like Print
 --]
[#macro routesSelectList message routeList rightForm] 
<TABLE BORDER="0" >
  <TR>
     <TD>${message}</TD>
     <TD></TD>
     <TD>Selected Routes</TD>
  </TR>
  <TR>
    <TD>
      <SELECT NAME="routeList" MULTIPLE SIZE="5" onDblClick="opt.transferRight()">
      [#if routeList?exists]
      [#list routeList as rt]
          <OPTION VALUE="${rt.getRouteID()}">${rt.getRouteName()}</OPTION>
      [/#list]      
      [/#if]
      </SELECT>
    </TD>
    <TD VALIGN=MIDDLE ALIGN=CENTER>
      <INPUT TYPE="button" NAME="right" VALUE="&gt;&gt;"     ONCLICK="opt.transferRight()"><BR>
      <INPUT TYPE="button" NAME="right" VALUE="All &gt;&gt;" ONCLICK="opt.transferAllRight()"><BR>
      <INPUT TYPE="button" NAME="left" VALUE="&lt;&lt;"      ONCLICK="opt.transferLeft()"><BR>
      <INPUT TYPE="button" NAME="left" VALUE="All &lt;&lt;"  ONCLICK="opt.transferAllLeft()">
    </TD>
    <TD>
      <SELECT ID="${rightForm}" NAME="${rightForm}" SIZE="5" multiple onDblClick="opt.transferLeft()"></SELECT>
    </TD>
  </TR>
</TABLE>
[/#macro]


[#macro stopSelectList destination stopsList showAll rightForm]
<TABLE BORDER="0" >
  <TR>
     <TD>Stops heading toward: ${destination}</TD>
     <TD></TD>
     <TD>Stops you want to add to the schedule</TD>
  </TR>
  <TR>
    <TD>
      <SELECT NAME="stopsList" MULTIPLE SIZE="5" onDblClick="opt.transferRight()">
      [#if stopsList?exists]
      [#list stopsList as st]
        [#if st.isPublic()]
          <OPTION VALUE="${st.getStopId()}">${st.getDescription()?default("")}  (${st.getStopId()})</OPTION>
        [#elseif showAll]
          <OPTION VALUE="${st.getStopId()}"><i>${st.getDescription()?default("")} (${st.getStopId()})</i></OPTION>
        [/#if]
      [/#list]      
      [/#if]
      </SELECT>
    </TD>
    <TD VALIGN=MIDDLE ALIGN=CENTER>
      <INPUT TYPE="button" NAME="right" VALUE="&gt;&gt;"     ONCLICK="opt.transferRight()"><BR>
      <INPUT TYPE="button" NAME="right" VALUE="All &gt;&gt;" ONCLICK="opt.transferAllRight()"><BR>
      <INPUT TYPE="button" NAME="left" VALUE="&lt;&lt;"      ONCLICK="opt.transferLeft()"><BR>
      <INPUT TYPE="button" NAME="left" VALUE="All &lt;&lt;"  ONCLICK="opt.transferAllLeft()">
    </TD>
    <TD>
      <SELECT ID="${rightForm}" NAME="${rightForm}" SIZE="5" multiple onDblClick="opt.transferLeft()"></SELECT>
    </TD>
  </TR>
  <TR>
   <!--
    <TD colspan="3">
        [#assign tpStr = timepoints?default("all") ]
        [@input name="timepoints" type="radio" value="cust"  selectValue=tpStr string="Custom" /]
        [@input name="timepoints" type="radio" value="sched" selectValue=tpStr string="TP's defined by Scheduling" /]
        [@input name="timepoints" type="radio" value="shelt" selectValue=tpStr string="Stops with Shelters" /]
        [@input name="timepoints" type="radio" value="all"   selectValue=tpStr string="All Stops in this Direction" /]
    </TD>
   -->
  </TR>
</TABLE>
[/#macro]

[#macro input name type value selectValue string]
  [#if value == selectValue ]
    <input name="${name}" id="${value}" value="${value}" type="${type}" checked>${string}</input>
  [#else]
    <input name="${name}" id="${value}" value="${value}" type="${type}">${string}</input>
  [/#if]
[/#macro]

[#-------- NAV LINKS / BUTTONS  ROUTINES ----------]
[#macro navRouteForm path="." meth="get" page="" selectedPage="" selectedData="" terminate=true]
[#if page?has_content == false]
  [#assign page = selectedPage]
[/#if]
<FORM action="${page}" method="${meth}">
  [@navBar path="${path}" selectedPage="${selectedPage}" selectedData=selectedData/]
  [#if RequestParameters?exists && RequestParameters.method?exists ]
    <input type="hidden" name="${Parameters.METHOD}"  value="${RequestParameters.method}"/>
  [/#if]
  [@routeDropDown routeList=routeList route=route/] 
  [#include "dateform.ftl"]
  [@directionButtons dir1=timesTable.getDir() val1=timesTable.getDir() dir2=timesTable.getDir().getOpposite() val2=timesTable.getDir().getOpposite() selected=timesTable.getDir() /]
  [@tmKeyButtons selected=key/]
[#if terminate]
</FORM>
[/#if]
[/#macro]

[#macro navBar path="." selectedPage="" selectedData=""]
  [#assign qs = queryString?default("new=true")]
  [#assign url = thisURL?default("timetable.web")]
  [#assign url = url?replace(".*/", "", "rf")]
  &bull;
  [  
    <a href="${path}">Home</a> 
    |
    [@navPageButton name="Configure" page="timetable.config"  path=path qs=qs selected=selectedPage/]
    |
    [@navPageButton name="Compare"   page="timetable.compare" path=path qs=qs selected=selectedPage/]
    |
    [@navPageButton name="Print"     page="timetable.print"   path=path qs=qs selected=selectedPage/]
    |
    [@navPageButton name="Test"      page="testRunner.suite"  path=path qs=qs selected=selectedPage/]
    |
    [@navPageButton name="View"      page="timetable.web"     path=path qs=qs selected=selectedPage/]
    |
    [@navPageButton name="Vertical View" page="vtt.web"       path=path qs=qs selected=selectedPage/]
  ] 
  &asymp; 
  [ 
    [@navDataButton name="Stop Level Data (TRANS)"  url=url qs=qs method="TRANS" selected=selectedData/]
    |
    [@navDataButton name="Mock Data"                url=url qs=qs method="MOCK"  selected=selectedData/]
    | 
    [@navDataButton name="Google Feed Spec Data"    url=url qs=qs method="GTFS"  selected=selectedData/]    
  ]
  &bull;
  <br/><br/>
[/#macro]

[#macro navPageButton name page path qs selected]
    [#assign nm = name]
    [#if page == selected]
       [#assign nm = "<b><i>" + name + "</i></b>"]
    [/#if]
    <a href="${path + "/" + page + "?" + qs}">${nm}</a> 
[/#macro]

[#macro navDataButton name url qs method selected]
    [#assign nm = name]
    [#if method == selected]
      [#assign nm = "<b><i>" + name + "</i></b>"]
    [/#if]
    <a href="${url + "?method=" + method + "&" + qs}">${nm}</a>
[/#macro]



[#macro configRadioButtons config=false]
[#assign stag = ""]
[#assign etag = ""]
[#assign dirty = false]
[#assign configu = timesTable.getConfiguration()?default("")]
[#if configu?has_content && configu.isDirty()]
  [#assign stag = "<i>"]
  [#assign etag = "</i>"]  
  [#assign dirty = true]
[/#if]
<p>
   <input type="radio"  title="Reloads last saved Configuration back into memory (possibly deleting any un-saved Configurations)" 
          [#if dirty == true]onclick="alert('WARNING: there are pending edits in this Configuration.\n\nIf you reload now (without performing a *${Parameters.PERSIST}* first), you will lose those edits forever.')"[/#if]
          name="${Parameters.SUBMIT}" value="${Parameters.RELOAD}">${Parameters.RELOAD}</input>
   <input type="radio"  title="Will remove from memory the TimePoints configuration for the specified route/dir/key." 
          name="${Parameters.SUBMIT}" value="${Parameters.REVERT}">${Parameters.REVERT}</input>
   <input type="radio"  title="Shows the RAW scheduling data -- very useful to see how Configuration effects the timetable"
          name="${Parameters.SUBMIT}" value="${Parameters.BYPASS}">${Parameters.BYPASS}</input>
   <input type="radio"  title="Saves the current Configuration edits back to the .CSV files (or whatever the persistance mechanism is)" 
          name="${Parameters.SUBMIT}" value="${Parameters.PERSIST}">${stag}${Parameters.PERSIST}${etag}</input>
   <input type="radio"  title="Updates the table based on the current in-memory Configuration" 
          name="${Parameters.SUBMIT}" value="${Parameters.UPDATE}" checked>${Parameters.UPDATE}</input>
   <input type="submit" value="submit"/>
</p>
[/#macro]


[#macro limiterButtonsOnEditTimepoint]
  <div align="left">
      limit timepoint changes to the selected:<br/>
      <input type="checkbox" name="${Parameters.USE_ROUTE}" value="true" checked> route </input><br/>
      <input type="checkbox" name="${Parameters.USE_KEY}" value="true" > service key </input><br/>
      <input type="checkbox" name="${Parameters.USE_DIR}" value="true" checked> direction </input>
  </div>
[/#macro]

[#macro directionButtons type="radio" dir1="Outbound" val1="Outbound" dir2="Inbound" val2="Inbound" selected="Inbound"]
<p>
  [@input name="dir" type=type value=val1 selectValue=selected string=dir1 /]
  [@input name="dir" type=type value=val2 selectValue=selected string=dir2 /]  
</p>
[/#macro]


[#macro tmKeyButtons type="radio" selected="Weekday"]
  [@serviceKeyButtons type=type key1="Weekday" val1="Weekday" key2="Saturday" val2="Saturday" key3="Sunday" val3="Sunday" selected=selected /]
[/#macro]

[#macro serviceKeyButtons type="radio" key1=""  val1="Weeekday" selected="Weekday"
                                       key2=""  val2="S" 
                                       key3=""  val3="U" 
                                       key4=""  val4="H" 
                                       key5=""  val5="S" ]
<p>
[#if key1?length > 0 ]
  [@input name="key" type=type value=val1 selectValue=selected string=key1 /]
[/#if]
[#if key2?length > 0 ]
  [@input name="key" type=type value=val2 selectValue=selected string=key2 /]
[/#if]
[#if key3?length > 0 ]
  [@input name="key" type=type value=val3 selectValue=selected string=key3 /]
[/#if]
[#if key4?length > 0 ]
  [@input name="key" type=type value=val4 selectValue=selected string=key4 /]
[/#if]
[#if key5?length > 0 ]
  [@input name="key" type=type value=val5 selectValue=selected string=key5 /]
[/#if]
</p>
[/#macro]


[#macro previewLinks queryString="new=true" path="." htmlLink=true]
[#if htmlLink == true]
  <a href="${path}/timetable.web?${queryString?default("new=true")}" target="_blank">html preview</a> |
  <a href="${path}/timetable.web?${queryString?default("new=true") + "&" + Parameters.SUBMIT + "=" + Parameters.BYPASS}" target="_blank">bypass config</a> |  
[/#if]
  <a href="${path}/pdf/timetable.web?${queryString}" target="_blank">pdf  preview</a> |
  <a href="${path}/webPageTemplates/mapntable.web?${queryString}" target="_blank">map  preview</a>      
[/#macro]


[#--  used to create links within the GTimesTable ... --]
[#macro GTimesTableLinks thisFile="." dir="." ]
  <a href="../${dir.getOpposite()}">${dir.getOpposite()}</a> <a href="${htmFile?default("")}">html</a> <a href="${mapFile?default("")}">map</a> <a href="${pdfFile?default("")}">pdf</a>
[/#macro]