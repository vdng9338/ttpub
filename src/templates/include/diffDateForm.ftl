[#assign nowDate = Parameters.NOW?string("MM-dd-yyyy")]
<p>
  <SCRIPT LANGUAGE="JavaScript" ID="js5">
  var diffDate = new CalendarPopup();
  diffDate.setDisplayType("month");
  diffDate.setReturnMonthFunction("monthReturn");
  diffDate.showYearNavigation();
  function monthReturn(y,m) { document.forms[0].diffDate.value=m+"-1-"+y; }
  </SCRIPT>
  2nd Service Date (to compare): <INPUT TYPE="text" NAME="diffDate" VALUE="${diffDate?default(nowDate)}" SIZE="9"/> <A HREF="#" onClick="diffDate.showCalendar('diffDateCal'); return false;" TITLE="diffDate.showCalendar('diffDateCal'); return false;" NAME="diffDateCal" ID="diffDateCal">select</A>
</p>
