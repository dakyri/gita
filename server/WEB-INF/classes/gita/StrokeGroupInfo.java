/*
 * StrokeGroupInfo.java
 *
 * Created on 19 April 2005, 11:47
 */
package gita;
 
import zone.HTMLwriter;
import gita.*;
/**
 * Base abstract class for the extra information that is calculated, carried about and displayed
 * for each stroke group. The type of info structure actually used will be an extension of this class
 * amd will be dictated by the <i>type</i> member of the StrokeGroup
 *
 * @see StrokeGroup
 * @author dak
 * @since you asked
 */
public abstract class StrokeGroupInfo
{
    /**
     * construct a new bit of stroke group info
     * @param g the StrokeGroup for which this structure provides additional info
     */
    public StrokeGroupInfo(StrokeGroup g)
    {
        group = g;
    }
    
    /**
     * Calculate features relevant to this stroke group
     * @param http a HTMLwriter to put diagnostic output to
     * @throws Cow if something goes wrong
     */
    public abstract void CalculateFeatures(HTMLwriter http) throws Cow;
    /**
     * Displays calculated features relevant to this stroke group
     * @param http a HTMLwriter to put diagnostic output to
     */
    public abstract void DisplayFeatures(HTMLwriter http);
    
    /** the group that this structure provides info for */
    public StrokeGroup group=null;
}
