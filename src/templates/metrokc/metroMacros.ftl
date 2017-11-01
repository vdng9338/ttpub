[#ftl]
[#import "../include/functions.ftl" as funct]
[#import "../include/macros.ftl"    as macros]
[#import "mapMacros.ftl" as mapMacros]

[#macro mainHead]
<title>Metro Route ${timesTable.getRouteName()?default("")} Timetable, ${timesTable.getKeyName()?default("")}</title>
<! -- Meta tag below will prevent browser from using chached file after expiration date -->
<META HTTP-EQUIV="Expires" CONTENT="9 Feb 2007 24:00:00 PST">
<!-- Required meta tags begin here -->
<META CONTENT="timetable, schedule, route" NAME="keywords">
<META CONTENT="Metro Route 1 Timetable, Weekday" NAME="description">
<META CONTENT="mocomments@metrokc.gov, King County Metro Transit" NAME="author">
<LINK REL="Stylesheet" HREF="/metro.css" TYPE="text/css">
<META CONTENT="http://transit.metrokc.gov/tops/bus/schedules/s001_0_.html" NAME="URL">

[@mapMacros.mapStyle/]
  
<STYLE>
.kingco a { color: #006666; }
.kingco a:visited { color: #993366; }
.kingco { font-family: arial, verdana, san-serif; font-size: 90%; }
.bg { background-image: url(/logos/background.gif); background-repeat: repeat-x; }
strong { font-family: verdana, arial, san-serif; font-size: 105%; color: #000066; font-weight: bold; }
h1 { font-family: verdana, arial, san-serif; font-size: 100%; color: #009999; font-style: normal; font-weight: bold; }
.trail { font-family: verdana, arial, san-serif; font-size: 70%; margin-bottom: 2px; font-weight: normal; }
b { font-family: verdana, arial, san-serif; font-size: 90%; font-weight: bold; }
p { font-family: arial, helvetica, san-serif; font-size: 90%; }
.nav3 { font-family: verdana, arial, san-serif; font-size: 63%; color: #009999; font-weight: bold; }
.nav3 a { color: #000066; text-decoration: none; font-weight: normal; }
.notice { font-family: arial, helvetica, san-serif; font-size: 80%; }
.feature { font-family: arial, helvetica, san-serif; font-size: 80%; }
.footer { font-family: arial, helvetica, san-serif; font-size: 74%; margin-top: 4px; margin-bottom: 4px;  }
.timetable { font-family: verdana, arial, san-serif; font-size: 215%; color: #009999; font-style: normal; font-weight: bold; }
.updated { font-family: arial, helvetica, san-serif; font-size: 80%; }
.photoby { font-family: verdana, arial, san-serif; font-size: 63%; color: #333333; }
.fineprint { font-family: verdana, arial, san-serif; font-size: 63%; }
.bigthree { font-family: arial, san-serif; font-size: 11px; line-height: 14px; } 
h2 { font-family: verdana, arial, san-serif; font-size: 15px; color: #009999; font-style: normal; font-weight: bold; margin-bottom: 5px; }
ul { font-family: arial, helvetica, san-serif; font-size: 90%; }
ul.none {list-style-position: outside; list-style-type: none; font-family: arial; helvetica, san-serif; font-size: 100%; padding-left: 16px;}
ol { font-family: arial, helvetica, san-serif; font-size: 90%; }
.orderlist { font-family: arial, helvetica, san-serif; font-size: 100%; }
dl { font-family: arial, helvetica, san-serif; font-size: 90%; }

.bullet { list-style-image: url(/logos/bullet.gif); }
.bullet-indent { list-style-image: url(/logos/bullet.gif); font-size: 100%; }
.arrow { list-style-image: url(/logos/doublearrow.gif); font-size: 100%; }
.arrow-alone { list-style-image: url(/logos/doublearrow.gif); }
.baseball { list-style-image: url(/logos-metro/icon-baseball.gif); font-size: 100%; }
.baseball-alone { list-style-image: url(/logos-metro/icon-baseball.gif); }
.music-black { list-style-image: url(/logos-metro/icon-musicnoteblack.jpeg); font-size: 100%; }
.music-black-alone { list-style-image: url(/logos-metro/icon-musicnoteblack.jpeg); }
.music-blue { list-style-image: url(/logos-metro/icon-musicnoteblue.jpeg); font-size: 100%; }
.music-blue-alone { list-style-image: url(/logos-metro/icon-musicnoteblue.jpeg); }
.snowflake { list-style-image: url(/logos-metro/icon-snowflake.gif); font-size: 100%; }
.snowflake-alone { list-style-image: url(/logos-metro/icon-snowflake.gif); }
.speaker { list-style-image: url(/logos-metro/icon-speaker.gif); font-size: 100%; }
.speaker-alone { list-style-image: url(/logos-metro/icon-speaker.gif); }
.train { list-style-image: url(/logos-metro/icon-steamtrain.gif); font-size: 100%; }
.train-alone { list-style-image: url(/logos-metro/icon-steamtrain.gif); }
.video { list-style-image: url(/logos/video-icon.gif); font-size: 100%; }
.video-alone { list-style-image: url(/logos/video-icon.gif); }
.airplane { list-style-image: url(/logos-metro/icon-airplane.gif); font-size: 100%; }
.airplane-alone { list-style-image: url(/logos-metro/icon-airplane.gif); }
</STYLE>
[/#macro]

[#macro mainBodyTOP]
<TABLE WIDTH="697" BORDER="0" CELLPADDING="0" CELLSPACING="0">
<TR>
	<TD WIDTH="697">
	<IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="30" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="10" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""><a href="http://transit.metrokc.gov/mappings/wwwnav.map"><IMG HEIGHT="41" WIDTH="586" ALT="King County Navigation Bar (text navigation at bottom)" BORDER="0" HSPACE="0" VSPACE="0" ALIGN="top" ISMAP SRC="http://transit.metrokc.gov//images/wwwnav.gif"></A>
	<BR CLEAR="all">
	<IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="3" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""><BR CLEAR="all">
	<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
		<TR>
			<TD VALIGN="top" WIDTH="207"><img src="http://transit.metrokc.gov/logos/shim.gif" alt="" width="18" height="1" hspace="0" vspace="0" border="0" align="top"><A HREF="http://transit.metrokc.gov/"><IMG SRC="http://transit.metrokc.gov//logos/mo_logo.gif" WIDTH="189" HEIGHT="74" BORDER="0" HSPACE="0" VSPACE="0" ALT="Metro Online Home" ALIGN="top"></A></TD>
			<TD VALIGN="top" WIDTH="207">
				<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="207">
				<TR><TD VALIGN="top"><IMG SRC="http://transit.metrokc.gov//logos/tagline.gif" WIDTH="207" HEIGHT="55" HSPACE="0" VSPACE="0" BORDER="0" ALT="We'll Get You There"></TD></TR>
				<TR><TD VALIGN="bottom"><IMG SRC="http://transit.metrokc.gov//logos/logo_timetables.gif" ALT="" WIDTH="116" HEIGHT="60" HSPACE="0" VSPACE="0" BORDER="0"></TD></TR>
				</TABLE>
			</TD>
            <TD VALIGN="top" >
              <!-- Note that logos/links appear with sitemap in schedules -->
			  <a href="/sitemap-alpha.html"><IMG SRC="http://transit.metrokc.gov//logos/sitemap.gif" ALT="Site Map" WIDTH="151" HEIGHT="27" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></a><br clear="all">
			  <a href="http://tripplanner.metrokc.gov/"><img src="http://transit.metrokc.gov/logos/tripplan.gif" width="81" heigh="63" hspace="0" vspace="0" border="0" align="top" alt="Trip Planner"></a><a href="http://buypass.metrokc.gov/"><img src="http://transit.metrokc.gov/logos/pass_sales.gif" width="81" height="63" hspace="0" vspace="0" border="0" align="top" alt="Pass Sales"></a><img src="http://transit.metrokc.gov/logos/timetables_off.gif" width="82" height="63" hspace="0" vspace="0" border="0" align="top" alt="Timetables (disabled because you are in this section)"><img src="http://transit.metrokc.gov/logos/shim.gif" width="7" height="1" hspace="0" vspace="0" border="0" align="top" alt="">
			</TD>
		</TR>
	</TABLE>
	</TD>
</TR>
</TABLE>

<TABLE WIDTH="100%" BORDER="0" CELLPADDING="0" CELLSPACING="0">
<TR>
	<TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" WIDTH="697" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
	<TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
	<TD VALIGN="top" BGCOLOR="#009999">
	<TABLE WIDTH="697" BORDER="0" CELLPADDING="0" CELLSPACING="0">
	<TR>
		<TD WIDTH="40" BGCOLOR="#009999"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="40" HEIGHT="21" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
		<TD WIDTH="141" BGCOLOR="#FFCC00" VALIGN="top" ALIGN="left"><A HREF="http://transit.metrokc.gov/"><IMG SRC="http://transit.metrokc.gov//logos/nav_moh.gif" ALT="Metro Online Home page" WIDTH="141" HEIGHT="21" HSPACE="0" VSPACE="0" BORDER="0"></A></TD>
		<TD WIDTH="26" BGCOLOR="#009999"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
		<TD WIDTH="490" BGCOLOR="#009999"><IMG SRC="http://transit.metrokc.gov//logos/title_timetables.gif" ALT="Timetables" WIDTH="145" HEIGHT="21" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></TD>
	</TR>
	</TABLE>
	</TD>
	<TD BGCOLOR="#009999"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="21" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
	<TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" WIDTH="630" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
	<TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
	<TD VALIGN="top">
	<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0">
	<TR>
		<TD WIDTH="40"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="40" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
		<TD WIDTH="141" VALIGN="top" BGCOLOR="#FFE6A8" ALIGN="left">
<!-- menu table -->
			<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
			<TR>
			    <TD VALIGN="top" BGCOLOR="#FFF6E8"> <A HREF="http://transit.metrokc.gov/tops/travops.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_to.gif" ALT="Travel Options" WIDTH="141" HEIGHT="21" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
                  <A HREF="http://transit.metrokc.gov/oltools/oltools.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_ot_on.gif" ALT="Online Tools Home" WIDTH="141" HEIGHT="23" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
<!-- subnav starts here -->
			<A HREF="http://transit.metrokc.gov/oltools/timetables.html"><IMG SRC="http://transit.metrokc.gov//logos/subnav_OT/timetables_on.gif" ALT="Timetables" WIDTH="141" HEIGHT="10" HSPACE="0" VSPACE="3" BORDER="0"></A><BR CLEAR="all"> 
<!-- third level nav. need extra table cell or BR at end.  table cell is small Br is big -->
			<TABLE CELLPADDING="0" CELLSPACING="3" BORDER="0" WIDTH="141">
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/bus/fare/fare-info.html">Fare information</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/bus/calculator.html">Calculate your commute</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/bus/area_maps/regional.html">Regional & area maps</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/bus/neighborhoods/region_text.html">Neighborhood routes</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/bus/psystem_map.html">Metro system map</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/tops/parknride/parknride.html">Park &amp; Ride</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/cs/faq/faq_answers_cs.html#holiday_schedule">Holiday information</A></TD>
			</TR>
			<TR>
				<TD WIDTH="22" ALIGN="right" VALIGN="top" CLASS="nav3">-</TD>
				<TD WIDTH="116" CLASS="nav3" VALIGN="top"><A HREF="http://transit.metrokc.gov/oltools/help/help_questions_ptt.html">Custom Print Help</A></TD>

			</TR>
			</TABLE>
<!-- end third level nav -->
			<A HREF="http://tripplanner.metrokc.gov/"><IMG SRC="http://transit.metrokc.gov//logos/subnav_OT/trip.gif" ALT="Trip Planner" WIDTH="141" HEIGHT="10" HSPACE="0" VSPACE="3" BORDER="0"></A><BR CLEAR="all">
			<A HREF="http://transit.metrokc.gov/oltools/tracker.html"><IMG SRC="http://transit.metrokc.gov//logos/subnav_ot/tracker.gif" ALT="Tracker" WIDTH="141" HEIGHT="10" HSPACE="0" VSPACE="3" BORDER="0"></A><BR CLEAR="all">
			<A HREF="http://transit.metrokc.gov/tops/van-car/ridematch.html"><IMG SRC="http://transit.metrokc.gov//logos/subnav_OT/rideshare.gif" ALT="RideShare" WIDTH="141" HEIGHT="10" HSPACE="0" VSPACE="3" BORDER="0"></A><BR CLEAR="all">
			<A HREF="http://transit.metrokc.gov/oltools/trafficupdate.html"><IMG SRC="http://transit.metrokc.gov//logos/subnav_OT/traffic.gif" ALT="Traffic and roads" WIDTH="141" HEIGHT="10" HSPACE="0" VSPACE="3" BORDER="0"></A><BR CLEAR="all">
                  <!-- subnav ends here --> <A HREF="http://transit.metrokc.gov/up/updates.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_u.gif" ALT="Updates" WIDTH="141" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
                  <A HREF="http://transit.metrokc.gov/prog/programs.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_p.gif" ALT="Programs" WIDTH="141" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
                  <A HREF="http://transit.metrokc.gov/cs/services.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_cs.gif" ALT="Customer Services" WIDTH="141" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
                  <A HREF="http://transit.metrokc.gov/am/metro.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_am.gif" ALT="About Metro" WIDTH="141" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
		  <A HREF="http://transit.metrokc.gov/sitemap-alpha.html"><IMG SRC="http://transit.metrokc.gov//logos/nav_sm.gif" ALT="Site Map" WIDTH="141" HEIGHT="23" HSPACE="0" VSPACE="0" BORDER="0"></A><BR CLEAR="all">
			</TD>
			</TR>
			</TABLE>
<!-- end menu table -->
		</TD>
		<TD WIDTH="26"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="26" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
		<TD  VALIGN="top">
<!-- content area -->
<!-- top Timetable nav -->
        <FORM NAME="showsched" METHOD="GET" ACTION="http://transit.metrokc.gov/cftemplates/show_schedule.cfm">
        <TABLE WIDTH="451" CELLPADDING="0" CELLSPACING="0" BORDER="0">
        <TR>
            <TD WIDTH="354" VALIGN="top"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" ALT="" WIDTH="258" HEIGHT="17" HSPACE="0" VSPACE="0" BORDER="0"><a href="http://tripplanner.metrokc.gov/cgi-bin/headway.pl?action=entry&amp;operator=MT&amp;bus_route=001&amp;type=0"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_print.gif" ALT="Create a custom timetable, with less information and easier to print." WIDTH="96" HEIGHT="17" HSPACE="0" VSPACE="0" BORDER="0"></a></TD>
            <TD WIDTH="97" VALIGN="top" ROWSPAN="2">
                <TABLE WIDTH="97" CELLPADDING="0" CELLSPACING="0" BORDER="0">
                <TR>
                    <TD WIDTH="1" BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" ALT="" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></TD>
                    <TD WIDTH="95" BGCOLOR="#C2E5E5" VALIGN="top" ALIGN="center" COLSPAN="2"><IMG SRC="http://transit.metrokc.gov//logos/tt_findroute.gif" ALT="Find route number" WIDTH="95" HEIGHT="17" HSPACE="0" VSPACE="0" BORDER="0"></TD>
                    <TD WIDTH="1" BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" ALT="" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></TD>
                </TR>
                <TR>
                    <TD WIDTH="1" BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" ALT="" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></TD>
                    <TD BGCOLOR="#C2E5E5" VALIGN="top" ALIGN="right"><INPUT NAME="BUS_ROUTE" TYPE="TEXT" STYLE="height: 15px; font-size: 10px;" SIZE="5" MAXLENGTH="3">&nbsp;</TD>
                    <TD VALIGN="top" BGCOLOR="#C2E5E5"><INPUT TYPE="IMAGE" SRC="http://transit.metrokc.gov//logos/route_go.gif" WIDTH="23" HEIGHT="17" BORDER="0" NAME="Submit" ALT="Go"></TD>
                    <TD WIDTH="1" BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" ALT="" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top"></TD>
                </TR>
                <TR>
                    <TD COLSPAN="4"><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="97" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0"></TD>
                </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD>
<!-- route number and effective dates -->
			<H2><SPAN CLASS="timetable">${timesTable.getRouteShortName()?default("")}</SPAN><BR>${timesTable.getKeyName()?default("")}: Sept. 23, 2006 thru Feb. 9, 2007</H2>
<!-- extra info - delete from <SPAN> to </SPAN> (inclusive) if not needed -->
            <SPAN CLASS="fineprint">&#8226; Be sure to read the <A HREF=#spclsvc>Special Service Info</A> for this route.</SPAN>
            </TD>
        </TR>
        </TABLE></FORM>
<!-- end top Timetable nav -->
<!-- timetable bar nav, first row contains nav buttons, to turn button ON, replace both button img and blue shim img with _on image, & remove blue shim from previous cell -->
<!-- for rider alert, add <BR> and rider alert button in same cell -->
        <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
        <TR>
            [@keyTab route=timesTable.getRouteShortName() dir=timesTable.getDirName() key=timesTable.getKeyName() /]
			<TD VALIGN="top"><A HREF="http://transit.metrokc.gov/cftemplates/show_map.cfm?BUS_ROUTE=001&amp;DAY_NAV=WSU"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_map.gif" ALT="Route Map" WIDTH="84" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
        </TR>
        </TABLE>
<!-- end timetable bar nav -->

<H4> ${timesTable.getDestination()?default("")} </H4>

[/#macro]



[#macro mainBodyBOTTOM]
<!--
<BR/>
<H4>Night shuttle service: To DOWNTOWN (Weekday):</H4>
COLS
<BR/>
ROWS
<BR/>

<H4>To KINNEAR (Weekday):</H4>
COLS
<BR/>
ROWS
<BR/>

<H4>Night shuttle service: To KINNEAR (Weekday):</H4>
COLS
<BR/>
ROWS
<BR/>

<HR width=586 align=left>
<TABLE width=586><TR><TD>
<P>
<B>Timetable Symbols</B><BR>
<DL>
  <DT><B>x</B>footnote
</DL>
</TD></TR>
</TABLE>
<TABLE WIDTH="586"><TR><td>
<P>
<A NAME=spclsvc>
<B>Special Service Info</B><BR>
<UL>
  <LI>To downtown evenings and Sunday mornings, Route 1 will shuttle between Kinnear and Queen Anne Ave N & Republican St, where connections are made with Route 2, 13, 15 or 18. Returning to Kinnear, board Route 2 or 13 downtown and transfer to Route 1 at 1st Ave N & Republican St. </LI>
</UL>
</td>
</tr>
</table>
-->
<br>
<TABLE WIDTH="586"><TR><td>
<P>
<FORM NAME="showsched" METHOD="GET" ACTION="http://transit.metrokc.gov/cftemplates/show_schedule.cfm"> <B>Select Another Route Number:</B>
<INPUT NAME="BUS_ROUTE" SIZE=3 MAXLENGTH=3>
<INPUT TYPE="submit" VALUE="Show Schedule">
</FORM>
</td></tr></table>
<!-- end content area -->
        <BR CLEAR="all">
        </TD>
    </TR>
    </TABLE>
    </TD>
    <TD></TD>
</TR>
<TR>
    <TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" WIDTH="697" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
    <TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
    <TD>
    <TABLE WIDTH="100%" BORDER="0" CELLPADDING="0" CELLSPACING="0">
    <TR>
        <TD WIDTH="697" BGCOLOR="#FFCC00"><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="465" HEIGHT="19" HSPACE="0" VSPACE="0" BORDER="0"><IMG SRC="http://transit.metrokc.gov//logos/bot_bar_mid.gif" ALT="" WIDTH="20" HEIGHT="19" HSPACE="0" VSPACE="0" BORDER="0"><IMG SRC="http://transit.metrokc.gov//logos/c_ffcc00.gif" ALT="" WIDTH="182" HEIGHT="19" HSPACE="0" VSPACE="0" BORDER="0"></TD>
        <TD BGCOLOR="#FFCC00"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
    </TR>
    </TABLE>
    </TD>
</TR>
<TR>
    <TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
    <TD><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="9" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
    <TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
<TR>
    <TD VALIGN="top" BGCOLOR="#FFF6E8">
    <TABLE WIDTH="697" BORDER="0" CELLPADDING="0" CELLSPACING="0">
    <TR>
    <TD WIDTH="40"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="40" HEIGHT="1" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
    <TD WIDTH="657" ALIGN="center">
    <P CLASS="footer"><A HREF="http://transit.metrokc.gov/">Home</A> | <A HREF="http://transit.metrokc.gov/tops/travops.html">Travel Options</A> | <A HREF="http://transit.metrokc.gov/oltools/oltools.html">Online Tools</A> | <A HREF="http://transit.metrokc.gov/up/updates.html">Updates</A> | <A HREF="http://transit.metrokc.gov/prog/programs.html">Programs</A> | <A HREF="http://transit.metrokc.gov/cs/services.html">Customer Services</A> | <A HREF="http://transit.metrokc.gov/am/metro.html">About Metro</A> | <A HREF="http://transit.metrokc.gov/sitemap-alpha.html">Site Map</A></P>
        <BR><P CLASS="footer">&copy; 1994-2006, Metro Transit.<BR>
        Metro Transit is a division of the <A HREF="http://www.metrokc.gov/kcdot/">King County Department of Transportation</A></P></TD>
    </TR>
    </TABLE>
    </TD>
</TR>
<TR>
    <TD BGCOLOR="#000066"><IMG SRC="http://transit.metrokc.gov//logos/shim.gif" WIDTH="1" HEIGHT="3" HSPACE="0" VSPACE="0" BORDER="0" ALIGN="top" ALT=""></TD>
</TR>
</TABLE>

<table cellpadding="0" cellspacing="0" border="0" width="697">
<tr>
  <td height="10" colspan="5"></td>  
</tr>
<tr>
  <td width="5"></td>
  <td valign="top" width="175"></td>
  <td valign="top" width="365" align="center">
  <!-- Required text navigational footer starts here. -->
  <p><A HREF="http://www.metrokc.gov/">King County</A> | <A HREF="http://www.metrokc.gov/news.htm">News</A> | <A HREF="http://www.metrokc.gov/services.htm">Services</A> | <A HREF="http://transit.metrokc.gov/cs/feedback_choose.html">Comments</A> | <A HREF="http://find.metrokc.gov/">Search</A></p>
  <p class="kinco"><FONT FACE="arial" SIZE="1">Links to external sites do not constitute endorsements by King County. By visiting this and other King County web pages,  you expressly agree to be bound by terms and conditions of the site.<br />
  <a href="http://www.metrokc.gov/terms.htm">Terms of Use</a> | <a href="http://www.metrokc.gov/privacy.aspx">Privacy Policy</a></FONT></p>
  <!-- Required footer ends here here --></td>
</tr>
</table>
[/#macro]

[#macro keyTab route dir="." key="Weekday"]
   [#assign routeNum = route?left_pad(3, "0") ]

   [#if key == "Weekday"]
      <TD VALIGN="top"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_weekday_on.gif" ALT="Weekday" WIDTH="85" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><A HREF="s${routeNum}_1_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_saturday.gif" ALT="Saturday" WIDTH="86" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><A HREF="s${routeNum}_2_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_sunday.gif" ALT="Sunday" WIDTH="84" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
   [/#if]
   [#if key == "Saturday"]
      <TD VALIGN="top"><A HREF="s${routeNum}_0_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_weekday.gif" ALT="Saturday" WIDTH="86" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_saturday_on.gif" ALT="Saturday" WIDTH="85" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><A HREF="s${routeNum}_2_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_sunday.gif" ALT="Sunday" WIDTH="84" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
   [/#if]
   [#if key == "Sunday"]
      <TD VALIGN="top"><A HREF="s${routeNum}_0_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_weekday.gif" ALT="Saturday" WIDTH="86" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><A HREF="s${routeNum}_1_.htm"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_saturday.gif" ALT="Saturday" WIDTH="86" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></A><IMG SRC="http://transit.metrokc.gov//logos/c_000066.gif" ALT="" WIDTH="1" HEIGHT="18" HSPACE="0" VSPACE="0" BORDER="0"></TD>
      <TD VALIGN="top"><IMG SRC="http://transit.metrokc.gov//logos/tt/tt_sunday_on.gif" ALT="Sunday" WIDTH="85" HEIGHT="22" HSPACE="0" VSPACE="0" BORDER="0"></TD>
   [/#if]
[/#macro]
