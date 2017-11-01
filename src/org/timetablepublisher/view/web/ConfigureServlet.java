/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.HttpTTPubUtils;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.utils.Params;


/**
 * The purpose of ConfigureServlet is to provide the Controller for editing the timepoints on a TimeTable.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 20, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class ConfigureServlet extends FreemarkerBaseServlet
{    
    private static final long serialVersionUID = -4575316288522971953L;
    
    protected enum UpdateType {STOP_NAME, STOP_SEQUENCE, DELETE_STOP}; 
    
    protected boolean preTemplateProcess(
                          HttpServletRequest  req, 
                          HttpServletResponse resp,
                          Template            template,
                          TemplateModel       data,
                          Params              params,
                          Params              oldParams                          
        )
        throws ServletException, IOException 
    {        
        TimesTable tt = TimeTableFactory.create(params);
        boolean update = false;
        
        if(params.isReloadConfig())
        {
            tt.reloadConfig();
            update = true;
        }
        else if(params.isRevertTimePointConfig())
        {
            // find timespoints based on route/key/dir, then remove them from the config
            TimePoints findMe = TimePoints.getTimePoint(params);
            
            List<TimePoints> tpList = findMe.find(tt);
            if(tpList != null && tt.getConfiguration() != null)
                tt.getConfiguration().removeData(tpList);
            
            update = true;
        }
        else
        {
            // step 1: check whether we've got timespoints for this route in our CSV file
            TimePoints findMe = TimePoints.getFinderTimePoint(params);
            List<TimePoints> tpList = findMe.find(tt);
            List added = null;
            boolean cleanupDirtyFlag = false;
            if(tt.getConfiguration() != null && (tpList == null || tpList.size() < 1))
            {
                // step 2a: a bit of house-keeping...if TimePoints is currently not dirty, then capture this state for a later undo -- see below
                if(!tt.getConfiguration().isDirty(TimePoints.class)) {
                    cleanupDirtyFlag = true;  
                }
                
                // step 2b: no timepoints in our file matching our route, so let's go get them and put them into CSV
                added = TimePoints.makeTimePoints(tt, params);
                tt.getConfiguration().addData(added);
            }
            else if(!params.isUseKey() || !params.isUseDir())
            {
                // step 2: we have some timepoints in our list -- but do these match this route / dir / service key ???
                TimePoints thisRt = TimePoints.getTimePoint(params);
                List<TimePoints> thisRtTPs = thisRt.find(tt);
                if(tt.getConfiguration() != null && (thisRtTPs == null || thisRtTPs.size() < 1)) 
                {
                    added = TimePoints.makeTimePoints(tt);
                    tt.getConfiguration().addData(added);
                }
            }

            // step 3: now that our CSV data is all loaded for this route, let's configure
            update = configure(tt, req, params);
            
            // step 4: if we added stuff to the config (in step 2 above), but didn't change or update anything in the config, 
            //         then remove what we added so as not to add unnecessary things 
            if(tt.getConfiguration() != null && (added != null && update == false))
            {
                tt.getConfiguration().removeData(added);
                
                // since we added stops to TimePoints above, which we just deleted (since we didn't configure them afterall)
                // we have to clean up the dirty flag for TimePoints...so that's done here.
                if(cleanupDirtyFlag) {
                    tt.getConfiguration().unDirty(TimePoints.class);
                }
            }
            
            // step 5: save -- if given the save command
            try
            {
                if(tt.getConfiguration() != null && params.isSaveConfig())
                {
                    tt.getConfiguration().persist();
                }               
            }
            catch(IOException io)
            {
                req.setAttribute(ERROR_MESSAGE_POPUP, "Problem Saving/Updating the Configuration: " + io.getLocalizedMessage());       
            }
        }
        
        // with our newly updated configuration put into the Configure Memory, go get another (updated) TimeTable
        if(update) 
        {
            tt = TimeTableFactory.create(params);
        }        
        
        // NOTE: the following seems to take up a lot of processing
        //List<String> amenityHelpList = FreemarkerUtils.getAmenityHelpList(TransQueryUtils.getAmenityDescriptions(false), "BCID");
        //req.setAttribute(AMENITY_DESCRIPTIONS, amenityHelpList);            
        setParams(req, tt);
        
        // set some session variables -- to prevent dupilcate entries of data
        HttpTTPubUtils.addAttributeToSession(ADD_TP_FORM, req);
        HttpTTPubUtils.addAttributeToSession(ADD_TP_FORM, req);        
        
        return true;
    }

    
    private boolean configure(TimesTable tt, HttpServletRequest req, Params params) 
    {
        boolean update = false;
        update |= updateTimePoints(tt, req, params);        
        update |= addStops(tt, req, params);        
        TimePoints.sort(tt);
        
        // make sure we make things dirty, so TimePoints changes are able to be saved
        if(update)
        {
            tt.getConfiguration().setDirty(TimePoints.class);
        }

        return update;
    }

    /**
     * Get all new stops that the user desires to be added to the table.
     * Put these stops into our change list.
     *  
     *  @return boolean to indicate whether an update took place
     */   
    protected boolean updateTimePoints(TimesTable tt, HttpServletRequest req, Params params)
    {
        boolean retVal = false;
        if(params.isUpdate()) return false;  // don't want to edit when user requests changing to another timetable 
        
        for(Object o : req.getParameterMap().keySet())
        {
            String stopParam = (String)o;
            if(stopParam != null && stopParam.startsWith(STOP_NAME_))
            {               
                String stopId = stopParam.substring(STOP_NAME_.length()).trim();
                String value  = req.getParameter(stopParam);                
                retVal |= edit(tt, stopId, params, value, UpdateType.STOP_NAME);
            }
            else if(stopParam != null && stopParam.startsWith(STOP_SEQ_))
            {
                String stopId = stopParam.substring(STOP_SEQ_.length()).trim();
                String value  = req.getParameter(stopParam);
                if(value.equalsIgnoreCase("X"))
                {
                    retVal |= edit(tt, stopId, params, value, UpdateType.DELETE_STOP);
                }
                else
                {
                    retVal |= edit(tt, stopId, params, value, UpdateType.STOP_SEQUENCE);
                }
            }
        }
        
        return retVal;
    }
    
    
    /**
     * addStops
     *
     * @param m_tt 
     * @param req
     * @param params
     * @param m_tt 
     * @return boolean to indicate whether an update took place
     */
    protected boolean addStops(TimesTable tt, HttpServletRequest req, Params params)
    {
        // don't want to edit when user requests changing to another timetable
        if(params.isUpdate()) return false;  
        if(HttpTTPubUtils.isAttributeInSession(ADD_TP_FORM, req)) return false;
        
        boolean retVal = false;        
                
        // step 1: get the list of ADDED timepoints from HTML params 
        String selected[] = req.getParameterValues(ADD_TP_FORM);
        if(selected != null && selected.length > 0)
        {
            // step 2: make a time point list from this list of stop ID strings
            List<TimePoints> tpList = TimePoints.makeTimePoints(selected, tt, params);
            if(tt.getConfiguration() != null && (tpList != null && tpList.size() > 0))
            {
                // step 3: when that list of TimePoints comes back with data, 
                //         we add it to the in-memory configuration 
                tt.getConfiguration().addData(tpList);
                retVal = true;
            }            
        }

        return retVal;
    }


    /**
     * Goal here is to update something inside of the TimePoints configuration.  NOTE: there are usually MULTIPLE edits to the TimePoints list. 
     * The most likely update to multiple entries in TimePoint store is to cover a change to all three enteries for each service key... 
     * EG: there are entries for each key (Weekday / Sat / Sun), and since the TimePoints are usually the same for each key, we're editing three 
     * TimePoints entries for a single and direction (eg: we make and edit to the same stop for all service keys -- makes sense, since most routes 
     * use the same stops for all service keys)
     * 
     * @param m_tt 
     * @param  stopId
     * @param  params
     * @param  value
     * @param  type
     * 
     * @return boolean to indicate whether an update took place
     */
    protected boolean edit(TimesTable tt, String stopId, Params params, String value, UpdateType type)
    {
        boolean retVal = false;
                
        // STEP 1: we're going to find any existing TimePoints configuration data for this route/direction/key
        TimePoints findMe = TimePoints.getFinderTimePoint(params);  
        findMe.setStopID(stopId);
        List<TimePoints> foundList = findMe.findUniqueStopIDs(tt);

        // STEP 2: if there's existing TimePoints config data to edit, we'll go here
        if(foundList != null)
        {
            // STEP 2b: edit 1..N TimePoints records (again, this is probably W/S/U edits) based on the instructions given by way of Update Type
            for(TimePoints f : foundList)
            {
                switch(type)
                {
                    case STOP_NAME:
                        f.setName(value);
                        
                        retVal = true;
                    break;

                    case STOP_SEQUENCE:
                        Integer i = IntUtils.getIntegerFromString(value);
                        if(i != null) 
                        {
                            f.setSequence(i);
                            retVal = true;
                        }
                    break;                        

                    case DELETE_STOP:
                    if(tt.getConfiguration() != null)
                    {
                        tt.getConfiguration().deleteConfig(f);
                        retVal = true;
                    }
                    break;
                }
            }
        }
        
        return retVal;
    }
}
