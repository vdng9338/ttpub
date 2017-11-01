[#function getProperElementName el=""]
  [#return el?js_string?replace('&', '_')?replace(':', '_')?replace('-', '_')]
[/#function]


[#function getInDesignString str ]
  [#if str?exists]
    [#return str?replace("<BR/>", " ")?xml ]
  [/#if]

  [#return "" ]
[/#function]

[#function stripLineBreak str ]
  [#if str?exists]
    [#return str?replace("<BR/>", " ")?html ]
  [/#if]

  [#return "" ]
[/#function]

[#function getStopIdAsHTML stop element="th" prefix="Stop ID " suffix=""]
  [#assign retVal = ""]
  
  [#if stop?has_content]
    [#if stop.hideStopId()]
      [#assign retVal = "<${element}></${element}>"]
    [#else]
      [#assign retVal = "<${element}>" + prefix + stop.getStopId() + suffix + "</${element}>"]
    [/#if]
  [/#if]
  
  [#return retVal]
[/#function]

[#function getStopIdAsString stop prefix="" suffix=""]
  [#assign retVal = ""]
  
  [#if stop?has_content && !stop.hideStopId()]
      [#assign retVal = prefix + stop.getStopId() + suffix]
  [/#if]
  
  [#return retVal]
[/#function]


[#function getTr rowNum odd="odd" even="even" close=">"] 
  [#return getElement(rowNum, "tr", odd, even, close)]
[/#function]

[#function getTh colNum odd="odd" even="even" close=">"] 
  [#return getElement(colNum, "th", odd, even, close)]
[/#function]

[#function getTd colNum odd="odd" even="even" close=">"] 
  [#return getElement(colNum, "td", odd, even, close)]
[/#function]

[#function getTdToolTip colNum odd="odd" even="even" tip="" close=">"]
  [#return getElementWithToolTip(colNum, "td", odd, even, tip, close)]
[/#function]

[#function getTrToolTip colNum odd="odd" even="even" tip="" close=">"]
  [#return getElementWithToolTip(colNum, "tr", odd, even, tip, close)]
[/#function]

[#function getElement num element odd="odd" even="even" close=""] 
  [#assign retVal = "<" + element + " class=\"" ]
  [#if num % 2 == 0]
    [#assign retVal = retVal + even + "\"" + close]
  [#else]
    [#assign retVal = retVal + odd  + "\"" + close]  
  [/#if]
      
  [#return retVal]
[/#function]

[#function getElementWithToolTip num element odd="odd" even="even" tip="" close=""] 
  [#return getElement(num, element, odd, even, "") + " title=\"" + tip + "\"" + close]
[/#function]


[#function getFootnoteSymbol row i]
  [#if row.getFootnoteSymbol(i)?exists]
    [#return row.getFootnoteSymbol(i)]
  [/#if]
  [#return ""]
[/#function]

[#function getHtmlTimeFromCell cell pm="" am=""]
  [#if cell.getTimeAsStr()?exists]
    [#assign symbol = cell.getFootnoteSymbol()?default("") ]
    [#assign time = cell.getTime() % 86400 ]
    [#if (time >= 43200)]
      [#assign retVal = "<b>" + symbol + cell.getTimeAsStr() + pm + "</b>"]
      [#return retVal]
    [#else]
     [#assign retVal = symbol + cell.getTimeAsStr() + am]
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


[#function getXmlTime row i]
  [#if row.getTimeAsStr(i)?exists]
    [#assign comment = getXmlFootnoteSymbol(row, i) ]
    [#assign time = row.getTime(i) % 86400 ]
    [#if (time >= 43200)]
      [#assign retVal = "&#x0009;" + "<pm>" + comment + row.getTimeAsStr(i) + "</pm>"]
    [#else]
      [#assign retVal = "&#x0009;" + comment + row.getTimeAsStr(i) ]
    [/#if]
  [#else]
    [#assign retVal = "&#x2014;"]
  [/#if]

  [#return retVal]
[/#function]


[#function getXmlFootnoteSymbol row i]
  [#if row.getFootnoteSymbol(i)?exists && row.getFootnoteSymbol(i)?length > 0 ]
    [#return "<fn>" + row.getFootnoteSymbol(i) + "</fn> "]
  [/#if]
  [#return ""]
[/#function]
