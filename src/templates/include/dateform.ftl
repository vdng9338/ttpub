[#assign nowDate = Parameters.NOW?string("MM-dd-yyyy")]
[#if effectiveDate?has_content && !date?has_content]
  [#assign date=effectiveDate?string("M-d-yyyy")]
[/#if]
<p>
  <SCRIPT LANGUAGE="JavaScript" ID="js5">
  var cal5 = new CalendarPopup();
  cal5.setDisplayType("month");
  cal5.setReturnMonthFunction("dateMonthReturn");
  cal5.showYearNavigation();
  function dateMonthReturn(y,m) { document.forms[0].date.value=m+"-1-"+y; }
  </SCRIPT>
  Service Date (on / after): <INPUT TYPE="text" NAME="date" VALUE="${date?default(nowDate)}" SIZE="9"/> <A HREF="#" onClick="cal5.showCalendar('anchor5'); return false;" TITLE="cal5.showCalendar('anchor5'); return false;" NAME="anchor5" ID="anchor5">select</A>
</p>
