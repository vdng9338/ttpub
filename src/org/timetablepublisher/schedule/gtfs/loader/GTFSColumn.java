/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs.loader;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;

/**
 * The purpose of CsvColumn is in support of Java 1.5 Annotations.  If you notice,
 * the Configure POJO classes (eg: CullTrips, LoopFillIn, TripNotes, etc...) all have
 * @ CsvColumn annotations next to their 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     org.timetablepublisher.view.web.CSVEditorServlet
 * @see     org.timetablepublisher.configure.Configure
 */
@Retention(RUNTIME)
public @interface GTFSColumn
{
    String name();
    String description();
    boolean useSetter() default false;
}
