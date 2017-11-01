[#ftl]
[#include "../include/constants.ftl"]

[#--
chrome://selenium-ide/content/selenium/TestRunner.html?test=http://localhost:8080/ttpub/testTemplates/trimetWebTest.web%3Fmethod%3DGTFS%26route%3DAmtrak%20Cascades---4%26dir%3DNorth
chrome://selenium-ide/content/selenium-ide.xul?test=http://localhost:8080/ttpub/testTemplates/trimetWebTest.web%3Fmethod%3DGTFS%26route%3DAmtrak%20Cascades---4%26dir%3DNorth
--]

[#assign AMP = "@"]
[#macro openTPPubPage method="ES" page="View" date="1-14-2007" route="9-Powell" dir="Inbound" key="Weekday" submit="submit"]
<tr>
	<td>open</td>
	<td>/ttpub/</td>
	<td></td>
</tr>
<tr>
	<td>clickAndWait</td>
	<td>link=${page}</td>
	<td></td>
</tr>
<tr>
	<td>type</td>
	<td>date</td>
	<td>${date}</td>
</tr>
[@changeRtDirKey route=route dir=dir key=key submit=submit/]
[/#macro]


[#macro openRelativeURL url="/schedules/w/t1004_1.htm"]
<tr>
	<td>open</td>
	<td>${url}</td>
	<td></td>
</tr>
[/#macro]


[#macro changeRtDirKey route="4-Fessenden" dir="Inbound" key="Weekday" submit="submit"]
<tr>
	<td>select</td>
	<td>route</td>
	<td>label=${route}</td>
</tr>
<tr>
	<td>click</td>
	<td>document.getElementById('${dir}')</td>
	<td></td>
</tr>
<tr>
	<td>click</td>
	<td>document.getElementById('${key}')</td>
	<td></td>
</tr>
<tr>
	<td>clickAndWait</td>
	<td>//input[${AMP}value='${submit}']</td>
	<td></td>
</tr>
[/#macro]


[#macro testForStopNamesAndIDs columns showStopIDs]
[#if columns?has_content]
  [#assign colNum = 1]
  [#list columns as cl]
    [#assign  desc = "${cl.getDescription()?default('')?replace(' & ', '*')?replace('&', '*')?replace(' / ', '*')?replace('/', '*')?replace(' at ','*')}"]
    [#-- ESSENTIALLY COMMENT OUT STOP NAME COMPARE FOR NOW, SINCE THERE'S SUCH A DIFF BETWEEN SCHED & SAM --]
    [#assign  desc = ""]
    [@verifyTableHeaderText text="*${desc?js_string}" x=colNum y=1/]
    [#if showStopIDs && !cl.hideStopId()]
      [@verifyTableHeaderText text="*${cl.getStopId()?js_string}" x=colNum y=2/]
    [/#if]
    [#assign colNum = colNum + 1]    
  [/#list]
[/#if]
[/#macro]


[#macro trimetOrgStopNamesAndIDs columns showStopIDs]
[#if columns?has_content]
  [#assign colNum = 1]
  [#list columns as cl]
    [#assign text = cl.getDescription()?default('')?js_string + "*"]    
    [#-- ESSENTIALLY COMMENT OUT STOP NAME COMPARE FOR NOW, SINCE THERE'S SUCH A DIFF BETWEEN SCHED & SAM --]
    [#assign text = "*"]
    [#if showStopIDs && !cl.hideStopId()]
      [#assign text = text + cl.getStopId()?js_string + "*"]
    [/#if]
    
    [@verifyTableHeaderText text=text x=colNum y=1/]
    [#assign colNum = colNum + 1]    
  [/#list]
[/#if]
[/#macro]


[#-- This test will check a table for stop times (hours) within table--]
[#macro testTableTimes rows everyNrows=3 tableFormat=""]
[#if rows?has_content]
  [#assign Y = 1]
  [#list rows as r]
    [#assign cellList = r.getRow()]
    [#if cellList?has_content && Y % everyNrows == 1]
      "<!-- testing times (hours:tenMins) for row #${Y} -->"
      [#assign X = 1]      
      [#list cellList as cell]
        [#if cell?has_content]
	      [@verifyTableTime x=X y=Y hour=cell.getHour()?default("1") tenMin=cell.getTensOfMinutes()?default("1")  tableFormat=tableFormat/]
        [#else]
          [@verifyNotTableText text="*:*" x=X y=Y tableFormat=tableFormat/]
        [/#if]
        [#assign X = X + 1]
      [/#list]
    [/#if]
    [#assign Y = Y + 1]
  [/#list]
[/#if]
[/#macro]


[#-- This test will check a table for Footnotes within the table--]
[#macro testTableWithFNs rows symbolFirst=true tableFormat=""]
[#if rows?has_content]
  [#assign Y = 1]
  [#list rows as r]
    [#assign cellList = r.getRow()]
    [#if cellList?has_content]
      [#assign X = 1]
      [#list cellList as cell]
        [#if cell?has_content && cell.getFootnoteSymbol()?has_content]
          [#assign fnTime = ""]
          [#if symbolFirst]
             [#assign fnTime = "" + cell.getFootnoteSymbol() + cell.getHour() + ":*"]
          [#else]             
             [#assign fnTime = "" + cell.getHour() + ":*" + cell.getFootnoteSymbol() + "*"]             
          [/#if]
	      [@verifyTableText text=fnTime x=X y=Y tableFormat=tableFormat/]
        [/#if]
        [#assign X = X + 1]
      [/#list]
    [/#if]
    [#assign Y = Y + 1]
  [/#list]
[/#if]
[/#macro]


[#-- this will check the footnote content --]
[#macro testFootnoteContent footnotes]
[#if footnotes?has_content ]
  [#list footnotes as fn]
    [#assign txt = fn.getFormattedNote()]
    [@verifyTextPresent text=txt?replace("<.*>", "*", "r")/]    
  [/#list]
[/#if]
[/#macro]


[#-- this will check the bounds of the table --]
[#macro testTableExists columns rows tableFormat=""]
[#if columns?has_content && rows?has_content]
  [#assign X = columns?size]
  [#assign Y = rows?size]
  [@verifyTableElementPresent x=X y=Y tableFormat=tableFormat/]
  [@verifyTableElementNotPresent x=X+1 y=Y tableFormat=tableFormat/]
  [@verifyTableElementNotPresent x=X y=Y+1 tableFormat=tableFormat/]  
[/#if]
[/#macro]

[#macro verifyTableElementPresent x y tableFormat]
  [#assign pos = getTablePos(x, y, tableFormat)]
  <tr>
	<td>verifyElementPresent</td>
	<td>${pos}</td>	
	<td></td>
  </tr>
[/#macro]


[#macro verifyTableElementNotPresent x y tableFormat]
  [#assign pos = getTablePos(x, y, tableFormat)]
  <tr>
	<td>verifyElementNotPresent</td>
    <td>${pos}</td>
	<td></td>
  </tr>
[/#macro]


[#macro verifyTableHeaderText text x y]
<tr>
	<td>verifyText</td>
	<td>//tr[${y}]/th[${x}]</td>
	<td>${text}</td>
</tr>
[/#macro]

[#macro verifyTableText text x y tableFormat]
  [#assign pos = getTablePos(x, y, tableFormat)]
  <tr>
	<td>verifyText</td>
	<td>${pos}</td>
	<td>${text}</td>
  </tr>
[/#macro]

[#macro verifyNotTableText text x y tableFormat]
  [#assign pos = getTablePos(x, y, tableFormat)]
  <tr>
	<td>verifyNotText</td>
    <td>${pos}</td>
	<td>${text}</td>
  </tr>
[/#macro]


[#macro verifyTableTime x y hour tableFormat tenMin="0"]
  [#assign pos = getTablePos(x, y, tableFormat)]
  <tr>
	<td>verifyText</td>
	<td>${pos}</td>
	<td>${getTimeRegEx(hour, tenMin)}</td>
  </tr>
[/#macro]



[#macro verifyText text target=""]
<tr>
	<td>verifyText</td>
	<td>${target}</td>
	<td>${text}</td>
</tr>
[/#macro]

[#macro verifyNotText text target=""]
<tr>
	<td>verifyNotText</td>
	<td>${target}</td>
	<td>${text}</td>
</tr>
[/#macro]

[#macro verifyTextPresent text]
<tr>
	<td>verifyTextPresent</td>
    <td>${text}</td>
    <td></td>    
</tr>
[/#macro]

[#function getTablePos x y tableFormat] 
  [#assign retVal = "//tr[${y}]/td[${x}]"]
  [#if tableFormat == "trimet.org"]
    [#assign retVal = "//td[${x}]/table/tbody/tr[${y}]/td"]
  [/#if]
  [#return retVal]
[/#function]

[#function getTimeRegEx hour tenMin] 
  [#assign iHour   = hour?number]
  [#assign iTenMin = tenMin?number]  
  [#assign retVal  = "regex:" + hour]

  [#-- do hour regex field --]
  [#if iTenMin == 0]
    [#assign tmp = iHour - 1]
    [#if tmp <= 0][#assign tmp = 12][/#if]
    [#assign retVal = "regex:[" + tmp?string("0") + hour + "]+"]
  [#elseif iTenMin == 5]
    [#assign tmp = iHour + 1]
    [#if tmp > 12][#assign tmp = 1][/#if]
    [#assign retVal = "regex:[" + tmp?string("0") + hour + "]+"]    
  [/#if]

  [#assign retVal = retVal + ":"]
      
  [#-- do tens of minute regex field --]
  [#assign low = iTenMin - 1][#if low < 0][#assign low = 5][/#if]
  [#assign hi  = iTenMin + 1][#if hi  > 5][#assign hi  = 0][/#if]
  [#assign retVal = retVal + "[" + low?string("0") + tenMin + hi?string("0") + "]"]

  [#return retVal]
[/#function]

