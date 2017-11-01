<map version="0.8.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1157646903281" ID="Freemind_Link_646143295" MODIFIED="1165434808812" TEXT="TimesTablePublisher How To">
<node CREATED="1167954941258" ID="Freemind_Link_319844269" MODIFIED="1168028093642" POSITION="right" TEXT="TimeTablePublisher Instructions">
<node CREATED="1165435129891" FOLDED="true" ID="Freemind_Link_68088701" MODIFIED="1167961312930" TEXT="1. NEW DATA from TERRY">
<node CREATED="1165435166844" MODIFIED="1165435171953" TEXT="0. Purpose">
<node CREATED="1165435172812" MODIFIED="1165435191609" TEXT="We&apos;re currently lacking a DB Triiger on the ES_EFFECTIVE_DATE table in Maui"/>
<node CREATED="1165435249406" MODIFIED="1165435279766" TEXT="So, we have to run a couple of SQL update statements BY HAND (ugly, I know...but)"/>
<node CREATED="1165435192344" MODIFIED="1165435242000" TEXT="This table is used in my db queries.  And it needs to be updated when a new signup is added to the ES_TRIP and ES_TRIP_TIMES tables"/>
</node>
<node CREATED="1165435282203" MODIFIED="1165435293141" TEXT="1. Open a SQL editor"/>
<node CREATED="1165435363437" MODIFIED="1165437244937" TEXT="2. Insert a new EFFECTIVE date row">
<node CREATED="1165435941172" MODIFIED="1165437059453" TEXT="INSERT INTO ES_EFFECTIVE_DATE VALUES (&apos;01-01-2007&apos;, &apos;01-01-9999&apos;); &#xa;commit;"/>
</node>
<node CREATED="1165435293781" MODIFIED="1165437249234" TEXT="3. Change the END DATE on the current &apos;effective&apos; date row">
<node CREATED="1165435319281" MODIFIED="1165435343422" TEXT="There should be one row in the DB with an end date of the Year 3000"/>
<node CREATED="1165435346297" MODIFIED="1165435360859" TEXT="Set this date to the new signup date, -1"/>
<node CREATED="1165437070312" MODIFIED="1165437233422" TEXT="update ES_EFFECTIVE_DATE&#xa;set END_DATE = &apos;09-03-9999&apos;&#xa;where START_DATE = &apos;12-31-2006&apos;;&#xa;commit;&#xa;"/>
</node>
</node>
<node CREATED="1167954990836" FOLDED="true" ID="Freemind_Link_828034019" MODIFIED="1167961834508" TEXT="2. BUILD APPS for PAN">
<node CREATED="1167955001899" MODIFIED="1167955094930" TEXT="All web &amp; batch applications run on PAN"/>
<node CREATED="1167955031805" MODIFIED="1167955045555" TEXT="BUILD">
<node CREATED="1167955096477" MODIFIED="1167955108492" TEXT="ant:  clean-most-war"/>
<node CREATED="1167955150399" MODIFIED="1167955160180" TEXT="ant:   cmdline-TRANS-trimet"/>
<node CREATED="1167955109649" MODIFIED="1167955141445" TEXT="ant:   war-pan-es"/>
<node CREATED="1167955142430" MODIFIED="1167955149711" TEXT="ant:   war-pan-web"/>
<node CREATED="1167958657211" MODIFIED="1167958666289" TEXT="this will generate the following">
<node CREATED="1167958666727" MODIFIED="1167958720274" TEXT="ttpub-cmdline.zip"/>
<node CREATED="1167958700805" MODIFIED="1167958706586" TEXT="ttpub.war"/>
<node CREATED="1167958707102" MODIFIED="1167958709664" TEXT="ttweb.war"/>
</node>
</node>
<node CREATED="1167955082336" MODIFIED="1167955084195" TEXT="DEPLOY">
<node CREATED="1167955187492" MODIFIED="1167955197524" TEXT="log into PAN with the ttpub account"/>
<node CREATED="1167955200070" MODIFIED="1167958599461" TEXT="cd ~/time_table_configuration"/>
<node CREATED="1167960645164" MODIFIED="1167960674180" TEXT="mkdir zipBkup"/>
<node CREATED="1167960675633" MODIFIED="1167960789961" TEXT="cp zips/* zipBkup"/>
<node CREATED="1167958600633" MODIFIED="1167958618664" TEXT="rm -rf ttpublisher webapps/tt*"/>
<node CREATED="1167958637992" MODIFIED="1167961033899" TEXT="unzip ttpub-cmdline.zip into current directory -- will generate ttpublish directory"/>
<node CREATED="1167958774242" MODIFIED="1167958791352" TEXT="mv ttpub.war and ttweb.war into webapps directory"/>
<node CREATED="1167958792524" MODIFIED="1167958798899" TEXT="(restart tomcat if needed)">
<node CREATED="1167958805242" MODIFIED="1167958844352" TEXT="type in TOM at cmd prompt (will CD you to tomcat bin directory)"/>
<node CREATED="1167958823383" MODIFIED="1167958825899" TEXT="shutdown.sh"/>
<node CREATED="1167958827836" MODIFIED="1167958878867" TEXT="rm -rf ../logs/* ../work ../temp"/>
<node CREATED="1167958879695" MODIFIED="1167958882867" TEXT="startup.sh"/>
</node>
<node CREATED="1167960691836" MODIFIED="1167960776055" TEXT="mv zipBkup ../webapps/ttpub/zips"/>
</node>
</node>
<node CREATED="1167959056867" FOLDED="true" ID="Freemind_Link_798197087" MODIFIED="1167963261477" TEXT="3. CONFIGURE">
<node CREATED="1167959122852" FOLDED="true" MODIFIED="1167959169336" TEXT="The are two diretorys on pan that hold the Configure csv files">
<node CREATED="1167959080570" LINK="\\pan\time_table_configuration\configure" MODIFIED="1167959419399" TEXT="\\pan\time_table_configuration\configure"/>
<node CREATED="1167959080570" LINK="\\pan\time_table_configuration\webconfig" MODIFIED="1167959414820" TEXT="\\pan\time_table_configuration\webconfig"/>
</node>
<node CREATED="1167959170180" LINK="\\pan\time_table_configuration\configure" MODIFIED="1167959419399" TEXT="configure">
<node CREATED="1167959233039" MODIFIED="1167959484227" TEXT="The &apos;BETA&apos; configuration for the next signup."/>
<node CREATED="1167959252227" MODIFIED="1167959519680" TEXT="This is the configuration for the NEXT SIGNUP, which IDP will be activly editing."/>
<node CREATED="1167959537602" MODIFIED="1167959555336" TEXT="You&apos;d only use this config to create a &apos;PREVIEW&apos; of the schedule."/>
<node CREATED="1167959287680" LINK="http://pan:1111/ttpub" MODIFIED="1167959530180" TEXT="http://pan:1111/ttpub is the application that would allow you to make edits to this config"/>
</node>
<node CREATED="1167959170180" LINK="\\pan\time_table_configuration\webconfig" MODIFIED="1167959414820" TEXT="webconfig">
<node CREATED="1167959233039" MODIFIED="1167959251586" TEXT="The &apos;PRODUCTION&apos; configuration for the current signup"/>
<node CREATED="1167959252227" MODIFIED="1167959286649" TEXT="This is the config that the weekly (each crunch) batch process uses."/>
<node CREATED="1167959287680" LINK="http://pan:1111/ttweb/" MODIFIED="1167959331711" TEXT="http://pan:1111/ttweb is the application that would allow you to make edits to this config"/>
</node>
</node>
<node CREATED="1167961301836" FOLDED="true" ID="Freemind_Link_1037151652" MODIFIED="1167963261477" TEXT="4. Monday BEFORE SIGNUP">
<node CREATED="1167962376742" MODIFIED="1167962633742" TEXT="CONFIGURE NEEDS UPDATING"/>
<node CREATED="1167962498524" MODIFIED="1167962529820" TEXT="configure should now point to next signup configure files"/>
<node CREATED="1167962530258" MODIFIED="1167962948336" TEXT="webconfig should now point to the configure that IDP was working on"/>
<node CREATED="1167962635586" MODIFIED="1167962676695" TEXT="log into PAN"/>
<node CREATED="1167962679320" MODIFIED="1167962682883" TEXT="cd ~/time_table_configuration"/>
<node CREATED="1167962684555" FOLDED="true" ID="Freemind_Link_1432490369" MODIFIED="1167962714883" TEXT="ls -l data-store configure webconfig">
<node CREATED="1167962717820" MODIFIED="1167962735242" TEXT="NOTE that webconfig and configure are soft links"/>
</node>
<node CREATED="1167962896867" FOLDED="true" ID="Freemind_Link_50815328" MODIFIED="1167962921305" TEXT="Update configure to next signup date">
<node CREATED="1167962737227" MODIFIED="1167962745195" TEXT="cd data-store"/>
<node CREATED="1167962748008" MODIFIED="1167962784289" TEXT="cp -r &lt;the directory that configure is pointing to&gt; &lt;next singup date&gt;">
<node CREATED="1167962875742" MODIFIED="1167962890383" TEXT="cp -r  1-14-2007 3-4-2007"/>
</node>
<node CREATED="1167962894055" MODIFIED="1167962895977" TEXT="cd .."/>
<node CREATED="1167962963758" MODIFIED="1167962970820" TEXT="rm configure"/>
<node CREATED="1167962971430" MODIFIED="1167962997602" TEXT="ln -s data-store/&lt;next singup date directory&gt; configure"/>
<node CREATED="1167962989274" MODIFIED="1167963086789" TEXT="eg: ln -s data-store/3-4-2007 signup"/>
</node>
<node CREATED="1167963013164" FOLDED="true" MODIFIED="1167963021399" TEXT="Update webconfig">
<node CREATED="1167963022039" MODIFIED="1167963026945" TEXT="rm webconfig"/>
<node CREATED="1167963027992" MODIFIED="1167963058430" TEXT="ln -s data-store/&lt;last finished config from IDP&gt;"/>
<node CREATED="1167962989274" MODIFIED="1167963093305" TEXT="eg: ln -s data-store/1-14-2007 webconfig"/>
</node>
<node CREATED="1167963100555" MODIFIED="1167964004055" TEXT="That&apos;s it...the system&apos;s updated.  Weekly CRON should take care to publish newest schedule on desired date."/>
<node CREATED="1167963149695" MODIFIED="1167964023524" TEXT="NOTE: if you have to produce timetable by hand...look at PREVIEW TIMETABLE instructions below."/>
</node>
<node CREATED="1167954948383" FOLDED="true" ID="Freemind_Link_1314941952" MODIFIED="1167962618695" TEXT="5. PREVIEW TIMETABLE">
<node CREATED="1167957825977" MODIFIED="1167959600617" TEXT="Preview timetables are something that CS puts up on the website prior to a schedule going into effect. The zip of these timetables needs to be generated manually (no cron exists), and unzipped into the &apos;new&apos; directory on web1 by hand."/>
<node CREATED="1167960479789" MODIFIED="1167960494258" TEXT="Create Tables">
<node CREATED="1167958990602" MODIFIED="1167960223602" TEXT="cd ttpublisher"/>
<node CREATED="1167960224133" MODIFIED="1167960247789" TEXT="edit hibernate.cfg.xml -- point at Maui (from Hawaii)"/>
<node CREATED="1167960249227" MODIFIED="1167961796867" TEXT="run script to generate PREVIEW timetables">
<node CREATED="1167960249227" MODIFIED="1167961773008" TEXT="trimetPreviewBatch.sh -preview -date &apos;12-1-2007&apos;"/>
<node CREATED="1167960249227" MODIFIED="1167961743305" TEXT="(or whatever the effective date you want is)"/>
</node>
<node CREATED="1167960448977" MODIFIED="1167960842242" TEXT="VERY IMPORTANT: un-edit hibernate.cfg.xml -- point back at Hawaii"/>
</node>
<node CREATED="1167960501242" MODIFIED="1167960976367" TEXT="Unzip preview schedule on Web1">
<node CREATED="1167960512977" MODIFIED="1167960535774" TEXT="The trimetPreviewBatch.sh script should have moved the ZIP file over to web1"/>
<node CREATED="1167960536367" MODIFIED="1167960607258" TEXT="log into web1">
<node CREATED="1167960608930" MODIFIED="1167960859883" TEXT="NOTE: you&apos;re now logged into trimet.org...this is a live webserver, so be careful"/>
</node>
<node CREATED="1167960548836" MODIFIED="1167960600664" TEXT="cd ~/public_html/schedules/new"/>
<node CREATED="1167960882914" MODIFIED="1167960949133" TEXT="make sure that preview files exist">
<node CREATED="1167960931211" MODIFIED="1167960931211" TEXT="unzip -l PREVIEW-SCH.ZIP"/>
</node>
<node CREATED="1167960601883" MODIFIED="1167960867883" TEXT="rm -rf w s u"/>
<node CREATED="1167960954180" MODIFIED="1167960961617" TEXT="unzip PREVIEW-SCH.ZIP"/>
</node>
</node>
<node CREATED="1167954957555" ID="Freemind_Link_806370254" MODIFIED="1167961859633" TEXT="6. WEEKLY CRUNCH">
<node CREATED="1168027265970" MODIFIED="1168027285142" TEXT="on PAN, there is a cron job owned by the ttpub account">
<node CREATED="1168027285908" MODIFIED="1168027295204" TEXT="log into ttpub on pan"/>
<node CREATED="1168027299767" MODIFIED="1168027304095" TEXT="crontab -l"/>
</node>
<node CREATED="1168027306439" MODIFIED="1168027328564" TEXT="This cron job runs Weekly on Saturdays, at ~7pm"/>
<node CREATED="1168027329673" MODIFIED="1168027351845" TEXT="It ultimately executes the trimetBatch.sh script"/>
<node CREATED="1168027352548" MODIFIED="1168027380892" TEXT="trimetBatch.sh will create a zip of all the timetables, and scp them over to web1"/>
<node CREATED="1168027382189" ID="Freemind_Link_994737918" MODIFIED="1168027525845" TEXT="the folder on web1 is called:">
<node CREATED="1168027526454" LINK="/u01/trimet/public_html/schedules/z" MODIFIED="1168027530767" TEXT="/u01/trimet/public_html/schedules/zip"/>
<node CREATED="1168027541454" MODIFIED="1168027567986" TEXT="there are existing cron jobs on web1, which will unzip the NEW-SCH.ZIP file"/>
<node CREATED="1168028156486" ID="Freemind_Link_1991382699" MODIFIED="1168028207251" TEXT="see ~/new-wk, ~/new-sat, ~/new-sun"/>
</node>
</node>
<node CREATED="1167963271586" FOLDED="true" MODIFIED="1167963275195" TEXT="7. TESTING">
<node CREATED="1167963276055" MODIFIED="1167963711430" TEXT="a. testTTInstall.rb">
<node CREATED="1167963714149" MODIFIED="1167963746539" TEXT="will look at the route landing pages that are live on a server, and compare whether the same files exist"/>
<node CREATED="1167963747383" MODIFIED="1167963772914" TEXT="this ruby script currently only works on windows (and maybe just cygwin)"/>
<node CREATED="1167963774867" MODIFIED="1167963779836" TEXT="run the script">
<node CREATED="1167963780680" MODIFIED="1167963791242" TEXT="c:&gt; testTTInstall.rb"/>
</node>
<node CREATED="1167963792820" MODIFIED="1167963891024" TEXT="It will go compare the directory (currently ares / dev path), looking at CS&apos;s route landing pages, and seeing if there are matching timetable that are specified in the links within the route landing pages."/>
</node>
<node CREATED="1167963894367" MODIFIED="1167963929102" TEXT="b. Selenium">
<node CREATED="1167963929508" MODIFIED="1167963947211" TEXT="Can be used to at least &apos;eyeball&apos; that things look good."/>
</node>
</node>
</node>
<node CREATED="1166558362658" FOLDED="true" ID="Freemind_Link_1598500810" MODIFIED="1166558379330" POSITION="right" TEXT="Dynamically Set LOG LEVEL on running web app">
<node CREATED="1166558380236" ID="Freemind_Link_465900713" MODIFIED="1166558403361" TEXT="http://HOST-SERVER/ttpub/timetable.web?LOG_NAME=org.trimet&amp;LOG_LEVEL=FINEST"/>
<node CREATED="1166558380236" ID="Freemind_Link_1761386954" MODIFIED="1166562522669" TEXT="http://HOST-SERVER/ttpub/timetable.web?LOG_NAME=org.timetablepublisher&amp;LOG_LEVEL=ALL"/>
</node>
<node CREATED="1160824157359" FOLDED="true" ID="_" MODIFIED="1165434873516" POSITION="right" TEXT="Prep Code for Outside Trimet">
<node CREATED="1160824469328" ID="Freemind_Link_1788164245" MODIFIED="1165434946203" TEXT="0. Purpose">
<node CREATED="1165434947625" ID="Freemind_Link_1614309925" MODIFIED="1165434958187" TEXT="generate a zip of the code that compiles &amp; runs externally to TriMet"/>
<node CREATED="1165434959562" ID="Freemind_Link_1093751366" MODIFIED="1165434984797" TEXT="why?  TriMet code has links &amp; code dependencies on Maui / Hawaii in the build"/>
<node CREATED="1165434985516" ID="Freemind_Link_949569787" MODIFIED="1165434993656" TEXT="this process removes those depencdencies"/>
</node>
<node CREATED="1165435599672" ID="Freemind_Link_340577105" MODIFIED="1165450794656" TEXT="1. ant clean"/>
<node CREATED="1165450795109" ID="Freemind_Link_1212387960" MODIFIED="1165450804891" TEXT="2. ant "/>
</node>
<node CREATED="1167955051352" FOLDED="true" ID="Freemind_Link_218250676" MODIFIED="1167955053227" POSITION="right" TEXT="old">
<node CREATED="1165435463516" FOLDED="true" ID="Freemind_Link_1690796460" MODIFIED="1165435486719" TEXT="TimeTables on trimet.org">
<node CREATED="1165450487156" ID="Freemind_Link_1552752594" MODIFIED="1165450625969" TEXT="A few DAYS prior to a new schedule takes effect">
<node CREATED="1165450679469" ID="Freemind_Link_117565130" MODIFIED="1165450706547" TEXT="Have to move the new configuration that IDP has been working on into place."/>
<node CREATED="1165450518812" ID="Freemind_Link_1210209847" MODIFIED="1165450563906" TEXT="\\pan\time_table_configuration\webconfig\"/>
<node CREATED="1165450565922" ID="Freemind_Link_312949149" MODIFIED="1167961064961" TEXT="New schedules should be generated weekly via the crunch, using the batch tool">
<arrowlink DESTINATION="Freemind_Link_1158725147" ENDARROW="Default" ENDINCLINATION="420;0;" ID="Freemind_Arrow_Link_1553366174" STARTARROW="None" STARTINCLINATION="413;0;"/>
</node>
</node>
<node CREATED="1165434998828" FOLDED="true" ID="Freemind_Link_1158725147" MODIFIED="1167961064961" TEXT="Build &amp; Run Batch TimesTable for Generating Website">
<node CREATED="1165435047969" ID="Freemind_Link_1496182987" MODIFIED="1165435052437" TEXT="0. Purpose">
<node CREATED="1165435052922" ID="Freemind_Link_1922863734" MODIFIED="1165435084531" TEXT="Part of TTPUB is a batch runner that will generate the active schedule."/>
<node CREATED="1165435086359" ID="Freemind_Link_744251620" MODIFIED="1165438282859" TEXT="It&apos;s best to export a zip of this application out of the build tree and run that &#xa;(although you can run in the code tree too)."/>
</node>
<node CREATED="1165438284328" ID="Freemind_Link_1937601481" MODIFIED="1165438294219" TEXT="1. Create the Application">
<node CREATED="1165438409672" ID="Freemind_Link_1898734772" MODIFIED="1165438479578" TEXT="ant most cmdline-trimet"/>
</node>
<node CREATED="1165438294922" ID="Freemind_Link_643164292" MODIFIED="1165438310094" TEXT="2. (Install) Unzip the Application">
<node CREATED="1165438436984" ID="Freemind_Link_692235792" MODIFIED="1165438455781" TEXT="unzip ttpub-cmdline.zip"/>
</node>
<node CREATED="1165438310703" ID="Freemind_Link_1901214559" MODIFIED="1165438322344" TEXT="3. Run the Application">
<node CREATED="1165438484422" ID="Freemind_Link_1786250153" MODIFIED="1165438502172" TEXT="cd ttpublisher"/>
<node CREATED="1165438507359" ID="Freemind_Link_22999189" MODIFIED="1165438508219" TEXT="ls"/>
<node CREATED="1165438528906" ID="Freemind_Link_364577188" MODIFIED="1165438529953" TEXT="chmod 777 trimetBatch.bat"/>
<node CREATED="1165438540781" ID="Freemind_Link_366333606" MODIFIED="1165438541500" TEXT="trimetBatch.bat"/>
<node CREATED="1165439095156" ID="Freemind_Link_473507696" MODIFIED="1165439242766" TEXT="NOTE">
<node CREATED="1165439012547" ID="Freemind_Link_84413648" MODIFIED="1165450446609" TEXT="you might have to edit this .bat file ">
<node CREATED="1165450430734" ID="Freemind_Link_1106282315" MODIFIED="1165450469203" TEXT="probably want to change the EFFECTIVE DATE"/>
<node CREATED="1165450432969" ID="Freemind_Link_456384505" MODIFIED="1165450473578" TEXT="also the CONFIG_DIR"/>
<node CREATED="1165450450828" ID="Freemind_Link_551051367" MODIFIED="1165450476828" TEXT="(and maybe other settings)"/>
</node>
<node CREATED="1165439037859" ID="Freemind_Link_1445493959" MODIFIED="1165439070375" TEXT="BTW, the .sh script is incomplete, and currently not working...you help is needed!"/>
</node>
</node>
<node CREATED="1165439157344" ID="Freemind_Link_1588818110" MODIFIED="1165442509672" TEXT="4. ZIP">
<node CREATED="1165442510062" ID="Freemind_Link_1798504707" MODIFIED="1165442511656" TEXT="Running the application will create a ZIP file of TimeTables ready for the web"/>
<node CREATED="1165442519937" ID="Freemind_Link_1572620964" MODIFIED="1165442535656" TEXT="cd zips"/>
<node CREATED="1165442539437" ID="Freemind_Link_119487231" MODIFIED="1165442551375" TEXT="unzip NEW-SCH.ZIP"/>
</node>
<node CREATED="1165439191656" ID="Freemind_Link_1294988200" MODIFIED="1165442562312" TEXT="5. to the web">
<node CREATED="1165442564266" ID="Freemind_Link_1597521520" MODIFIED="1165442565719" TEXT="I often extract these to, and they are then available via http://dev/schedules"/>
<node CREATED="1165442668312" ID="Freemind_Link_266339685" MODIFIED="1165446468312" TEXT="cp zips/NEW-SCH.ZIP //ares/u01/wwwdev/schedules"/>
<node CREATED="1165442567125" ID="Freemind_Link_429042231" MODIFIED="1165442650859" TEXT="cd //ares/u01/wwwdev/schedules"/>
<node CREATED="1165442651953" ID="Freemind_Link_1102357894" MODIFIED="1165442659859" TEXT="rm -rf w s u"/>
<node CREATED="1165442687672" ID="Freemind_Link_1912952426" MODIFIED="1165442696750" TEXT="unzip NEW-SCH.ZIP"/>
</node>
<node CREATED="1165442984781" ID="Freemind_Link_760749540" MODIFIED="1165442990078" TEXT="6. my own dir on ares">
<node CREATED="1165442991656" ID="Freemind_Link_340521865" MODIFIED="1165442993078" TEXT="//ares/u01/home/purcellf/apache-tomcat-5.5.17/webapps/ROOT/schedules"/>
<node CREATED="1165446994328" ID="Freemind_Link_1209287351" MODIFIED="1165446995969" TEXT="\\ares\u01\home\purcellf\apache-tomcat-5.5.17\webapps\ROOT\schedules"/>
</node>
</node>
<node CREATED="1165987402851" FOLDED="true" ID="Freemind_Link_1667760531" MODIFIED="1165987418664" TEXT="trimet.org timetables">
<node CREATED="1165987440320" ID="Freemind_Link_1039111091" MODIFIED="1165987644773" TEXT="Purpose: add TimeTable to trimet.org website by hand"/>
<node CREATED="1165987467226" ID="Freemind_Link_1424008" MODIFIED="1165987592757" TEXT="step 1: ftp NEW-SCHED.ZIP to web1 (probably /u01/trimet/public_html/schedules/new/ directory)"/>
<node CREATED="1165987512726" ID="Freemind_Link_829727663" MODIFIED="1165987614273" TEXT="step 2: ssh into web1, and cd to that directory"/>
<node CREATED="1165987595679" ID="Freemind_Link_613925341" MODIFIED="1165987623445" TEXT="step 3: unzip"/>
</node>
<node CREATED="1165435388687" FOLDED="true" ID="Freemind_Link_80156019" MODIFIED="1165450767297" TEXT="Testing trimet.org for all the required TimesTables">
<node CREATED="1165435772531" ID="Freemind_Link_1013013721" MODIFIED="1165435790531" TEXT="There is a test to confirm a complete TT install. "/>
<node CREATED="1165435792344" ID="Freemind_Link_1992362622" MODIFIED="1165435799875" TEXT="It requries Ruby.  "/>
<node CREATED="1165435801000" ID="Freemind_Link_1456792334" MODIFIED="1165435816156" TEXT="It will read a bunch of the static landing pages for links to the generated time tables "/>
<node CREATED="1165435817359" ID="Freemind_Link_915646777" MODIFIED="1165435837312" TEXT="To run the test program">
<node CREATED="1165435837859" ID="Freemind_Link_691271443" MODIFIED="1165987993398" TEXT="cd to ttpublisher"/>
<node CREATED="1165435858750" ID="Freemind_Link_1164756568" MODIFIED="1165435860109" TEXT="testTTInstall.rb"/>
<node CREATED="1165988001023" ID="Freemind_Link_299397871" MODIFIED="1165988009617" TEXT="you should get some output like this">
<node CREATED="1165988046757" ID="Freemind_Link_1413988600" MODIFIED="1165988050445" TEXT="//ares/webdev/schedules/maxredline.htm&#xa;//ares/webdev/schedules/maxyellowline.htm&#xa;//ares/webdev/schedules/maxlines.htm&#xa;//ares/webdev/schedules/maxblueline.htm&#xa;//ares/webdev/schedules/r104.htm&#xa;//ares/webdev/schedules/r034.htm"/>
</node>
<node CREATED="1165988064773" ID="Freemind_Link_806026576" MODIFIED="1165988091273" TEXT="missing timetables">
<node CREATED="1165988018570" ID="Freemind_Link_675550383" MODIFIED="1165988038195" TEXT="//ares/webdev/schedules/r001.htm&#xa;WARN: Missing TimeTable: //ares/webdev/schedules/new/w/t1001_0.htm&#xa;WARN: Missing TimeTable: //ares/webdev/schedules/new/w/t1001_1.htm"/>
<node CREATED="1165988097273" ID="Freemind_Link_856143997" MODIFIED="1165988127617" TEXT="this may be OK, since a route may no longer active (and the route page is wrong)"/>
</node>
<node CREATED="1165988141617" ID="Freemind_Link_215387967" MODIFIED="1165988149070" TEXT="bogus timetables">
<node CREATED="1165988166570" ID="Freemind_Link_375711147" MODIFIED="1165988244945" TEXT="//ares/webdev/schedules/r004.htm&#xa;ERROR: I don&apos;t see the test string &apos;Portland, Oregon&apos; in the test file //ares.../w/t1004_0.htm"/>
<node CREATED="1165988254820" ID="Freemind_Link_1918291359" MODIFIED="1165988261460" TEXT="if you  see this message, that&apos;s not good"/>
<node CREATED="1165988262960" ID="Freemind_Link_16324631" MODIFIED="1165988289414" TEXT="it means that a TimeTable was generated, but the expected string &apos;Portland, Oregon&apos; is missing"/>
<node CREATED="1165988291273" ID="Freemind_Link_1599648160" MODIFIED="1165988301132" TEXT="take a look at the html and see what went wrong"/>
</node>
</node>
</node>
</node>
</node>
</node>
</map>
