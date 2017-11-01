/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule;

/**
 * The purpose of Trip is to TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TtTripImpl implements TtTrip 
{
    private final String m_trip;
    private final String m_block;
    private final int    m_startTime;
    private final int    m_endTime;
    
    /**
     * @param trip
     * @param block
     * @param startTime
     * @param endTime
     */
    public TtTripImpl(String trip, String block, int startTime, int endTime)
    {
        m_trip      = trip;
        m_block     = block;
        m_startTime = startTime;
        m_endTime   = endTime;
    }
    
    public TtTripImpl(Short trip, Short block, int startTime, int endTime)
    {
        this(trip.toString(), block.toString(), startTime, endTime);
    }

    public String getBlock()
    {
        return m_block;
    }

    public int getEndTime()
    {
        return m_endTime;
    }

    public int getStartTime()
    {
        return m_startTime;
    }

    public String getTrip()
    {
        return m_trip;
    }
}
