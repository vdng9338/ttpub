[#--
    MAP MACROS (for FreeMarker)

    Frank Purcell
    Version 1.0    
    Created:     October 1, 2006
    Update:      October 1, 2006  - Initial 
    Last Update: October 1, 2006  - Initial 
  --]
[#setting number_format="0.############"/]


[#macro gMapsKey ]
  <script>
  [#include "google-keys.js"] 
  </script>
 [#--  
  <script language="JavaScript" src="http://maps.google.com/maps?file=api&v=2&key=ABQIAAAA3xnAknagQvUXnjob62T29hTwM0brOpm-All5BF6PoaKBxRWWERTfugYWjQArs32FOZvk_pvjjBEVQA" type="text/javascript"></script>
  --] 
[/#macro]

[#macro mapStyle]
<style>
body,input,select,td,textarea {
    font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;
    font-size: 12px;
}
td{
}
h4,h5,h6,p,strong,div,form,acronym,label,table,td,th,span,a,hr,code,pre,hr
    {
    margin: 0;
    padding: 0;
    font-size: 1em;
    background-repeat: no-repeat;
    list-style-type: none;
}
table tr.odd {
    background-color: #eec;
    text-align: center;    
}

table tr.even {
    background-color: #def;
    text-align: center;
}

table thead tr {
    background-color: #9c9;
}
</style>
[/#macro]


[#macro gMapsCode ]
<script language="JavaScript" type="text/JavaScript">
  //<![CDATA[
  if (GBrowserIsCompatible()) 
  {
      //
      // map init code
      // 
      var MAP = new GMap2(document.getElementById("map"));
      MAP.addControl(new GLargeMapControl());
      MAP.addControl(new GMapTypeControl());
      MAP.setCenter(new GLatLng(45.522976677385074, -122.67119407653809), 9);

      var sidebar_html;
      var gmarkers = [];
      var gpoints = [];
      var htmls = [];
      var start_htmls = [];
      var to_htmls = [];
      var from_htmls = [];
      var i = 0;
      var bounds = new GLatLngBounds();  

      // This function picks up the click and opens the corresponding info window
      function myClick(stopId) 
      {
      }


      function hideMarker(stopId) 
      {
          MAP.removeOverlay(gmarkers[stopId]);
          gmarkers[stopId].show = false;
      }
      function showMarker(stopId) 
      {
          if( gmarkers[stopId].show != true ) 
          {
              MAP.addOverlay(gmarkers[stopId]);
              gmarkers[stopId].show = true;
          }

          myPanTo(stopId);
      }


      function myDblClick(stopId) 
      {
          gmarkers[stopId].openInfoWindowTabsHtml([
	                          new GInfoWindowTab('info', htmls[stopId]),
                              new GInfoWindowTab('directions', start_htmls[stopId]) ]);

      }

      function myPanTo(stopId) 
      {
          MAP.panTo(gpoints[stopId]);
      }


      // functions that open the directions forms
      function tohere(stopId) {
          gmarkers[stopId].openInfoWindowTabsHtml([
                              new GInfoWindowTab('directions', to_htmls[stopId]),
	                          new GInfoWindowTab('info', htmls[stopId])]);

      }
      function fromhere(stopId) {
          gmarkers[stopId].openInfoWindowTabsHtml([
                              new GInfoWindowTab('directions', from_htmls[stopId]),
	                          new GInfoWindowTab('info', htmls[stopId])]);

      }
      function getDirections(stopId, point, html) 
      {

        return start_htmls[stopId];
      }

      // A function to create the marker with a tooltip
      function createMarker(point, stopId, description, html) 
      {
          html = '<div style="white-space:nowrap;">' + html + '</div>';
          var dirHtml = '<b>' + description + '</b><br/>Directions: <a href="javascript:tohere(\''+stopId+'\')">To here</a> - <a href="javascript:fromhere(\''+stopId+'\')">From here</a>';

          // === marker with tooltip ===
          var marker = new GMarker(point, {title:description});
          GEvent.addListener(marker, "click", function() 
          {
             marker.openInfoWindowTabsHtml([new GInfoWindowTab('info', html), 
                                            new GInfoWindowTab('directions', dirHtml)]);
          });
          gpoints[stopId] = point;
          gmarkers[stopId] = marker;
          gmarkers[stopId].show = true;
          htmls[stopId] = html;

        // The info window version with the "to here" form open
        to_htmls[stopId] = '<b>' + description + '</b><br/>Directions: <b>To here</b> - <a href="javascript:fromhere(\'' + stopId + '\')">From here</a>' +
           '<br>Start address:<form action="http://maps.google.com/maps" method="get" target="_blank">' +
           '<input type="text" SIZE=40 MAXLENGTH=40 name="saddr" id="saddr" value="" /><br>' +
           '<INPUT value="Get Directions" TYPE="SUBMIT">' +
           '<input type="hidden" name="daddr" value="' + point.lat() + ',' + point.lng() + '"/>';
        // The info window version with the "to here" form open
        from_htmls[stopId] = '<b>' + description + '</b><br/>Directions: <a href="javascript:tohere(\'' + stopId + '\')">To here</a> - <b>From here</b>' +
           '<br>End address:<form action="http://maps.google.com/maps" method="get"" target="_blank">' +
           '<input type="text" SIZE=40 MAXLENGTH=40 name="daddr" id="daddr" value="" /><br>' +
           '<INPUT value="Get Directions" TYPE="SUBMIT">' +
           '<input type="hidden" name="saddr" value="' + point.lat() + ',' + point.lng() + '"/>';

        // The inactive version of the direction info
        start_htmls[stopId] = dirHtml;


          sidebar_html += '<a href="javascript:myclick(' + stopId + ')">' + name + '</a><br>';

          MAP.addOverlay(marker);
      }


      function getPopupHtml(lat, lng, stopId, description)
      {
        var html = "<table width='250' border='0'>";
            html += "<tr><td><div align='left'><b>" + description + "</b></div></td></tr>";
            html += "<tr><td><div align='left'>Stop ID: " + stopId + "</div></td></tr>";
            html += "<tr><td><div align='left'>Latitude: " + lat + "</div></td></tr>";
            html += "<tr><td><div align='left'>Longitude: " + lng + "</div></td></tr>";
            html += "<tr><td><div align='left'>" + (new Date()).toLocaleString() + "</div></td></tr>"
            html += "<tr><td><div align='left'><form method='post' onSubmit='sendText(this.message.value); return false;'>Stop Lookup: <input name='message' type='text' size='5' value='" + stopId + "' maxlength='10' /> <input type='submit' value='go' /></form></div></td></tr></table>";

        return html;
      }


      function addMarkerToMap(stopId, description, lg, lt)
      {
          var lat = parseFloat(lt);
          var lng = parseFloat(lg);
          var point = new GLatLng(lat, lng);
          var html = getPopupHtml(lat, lng, stopId, description);
          createMarker(point, stopId, description, html);

          bounds.extend(point);
	      return point;
      }

      /**
       * zoom to extent
       * @see Mike Williams' code at http://www.econym.demon.co.uk/googlemaps/basic14.htm
       * 
       */
      function zoomToExtents(map, bounds, numPoints, first)
      {  
        // ===== determine the zoom level from the bounds =====
        var zoomer = 0;
        if(numPoints > 10) {
            zoomer = 3;
        } else if(numPoints > 5) {
            zoomer = 2;
        } else if(numPoints > 2) {
            zoomer = 1;
        }
        map.setZoom(map.getBoundsZoomLevel(bounds) + zoomer);

        // ===== determine the centre from the bounds ======
        //var clat = (bounds.getNorthEast().lat() + bounds.getSouthWest().lat()) /2;
        //var clng = (bounds.getNorthEast().lng() + bounds.getSouthWest().lng()) /2;
        //map.setCenter(new GLatLng(clat,clng));
        map.setCenter(first);
      }

      [#list timesTable.getTimePoints() as cl]
      [#assign lng = "-122.5"]
      [#assign lat = "45.5"]
      [#if cl.getLongitude()?has_content]
        [#assign lng = "${cl.getLongitude()?string.number?replace(',', '.')}"]
      [/#if]
      [#if cl.getLatitude()?has_content]
        [#assign lat = "${cl.getLatitude()?string.number?replace(',', '.')}"]
      [/#if]      
      addMarkerToMap("${cl.getStopId()?default("unknown")}", "${cl.getDescription()?default("unknown")}", "${lng}" , "${lat}");
      [/#list]

      [#assign first=timesTable.getTimePoints()?first ]
      zoomToExtents( MAP, bounds, ${timesTable.getTimePoints()?size}, gpoints['${first.getStopId()?default("unknown")}']);
   }
  //]]></script>
[/#macro]


[#macro mapTimesTable columns rows showStopIDs=true tableStyle="its"]
<table class="${tableStyle}">
<thead>
  <tr>
    [#list columns as cl]
      <th onClick="myPanTo('${cl.getStopId()}')" onDblClick="myDblClick('${cl.getStopId()}')">${cl.getDescription()}</th>
    [/#list]
  </tr>
  [#if showStopIDs]
  <tr>
    [#list columns as cl]
      <th onMouseOver="myPanTo('${cl.getStopId()}')" onDblClick="myDblClick('${cl.getStopId()}')">${funct.getStopIdAsString(cl, "Stop ID ")}</th>
    [/#list]
  </tr>
  [/#if]
</thead>
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
</table>
[/#macro]
