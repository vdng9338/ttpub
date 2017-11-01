[#ftl strip_whitespace="true" attributes={"content_type" : "application/vnd.ms-excel. csv"}]
[#setting number_format="0.##########"/]
[#import "include/functions.ftl" as funct]
[#import "include/macros.ftl"    as macros]
[#if PLACE_TO_STOP?exists ]
[#if PLACE_TO_STOP.isShowRoute() ]
    [@showStopInfoWithRoute PLACE_TO_STOP.getNewStops() /]
[#else]
    [@showStopInfo PLACE_TO_STOP.getNewStops() /]
[/#if]
[/#if]

[#--
    LOCATION DETAILS in AN CSV TABLE
    
    - this will print out a list of Locations
  --]
[#macro showStopInfo tpList ]
[#if tpList?exists ]
Description, Place IDs, Stop ID(s)
[#list tpList as tp] [#if ! tp.isProcessed()] [@printPlaceInfo tp /] [/#if] [/#list]
[/#if]
[/#macro]

[#--
    LOCATION DETAILS in AN HTML TABLE with ROUTE and STOP
    
    - this will print out a list of Locations
  --]
[#macro showStopInfoWithRoute tpList ]
[#if tpList?exists ]
Route, Direction, Description, Place IDs, Stop ID(s)
[#list tpList as tp] ${tp.getRoute()}, ${tp.getDir()}, [@printPlaceInfo tp /] [/#list]
[/#if]
[/#macro]

[#macro printPlaceInfo tp ]
[#if tp?exists ] ${tp.getDescription()?trim}, ${tp.getPlaceId()}, [#if tp.getStopList()?exists ][#list tp.getStopList() as st ]${st}, [/#list] [/#if] [/#if]
[/#macro]
