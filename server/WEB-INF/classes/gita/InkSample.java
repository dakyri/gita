/*
 * InkSample.java
 *
 * Created on 19 February 2004, 02:06
 */

package gita;
 
import zone.Point;

/**
 * Basic chunk of ink trace data. In truth really just a point.
 * In our dreams and schemes of things, we would be tracking pressure, and giving every
 * point a timestamp. There's space for that
 *
 * @author  dak
 * @since you asked
 */
public class InkSample extends Point {
    
    /**
     * Creates a new instance of InkSample set to (0,0)
     */
    public InkSample()
    {
        x = y = 0;
        timestamp = 0;
        pressure = 0;
    } 
 
    /**
     * Creates a new instance of InkSample set to (xi,yi)
     * @param xi x coordinate
     * @param yi y coordinate
     */
    public InkSample(float xi, float yi)
    {
        x = xi;
        y = yi;
        timestamp = 0;
        pressure = 0;
    }
     
    /**
     * Creates a new instance of InkSample set to (xi,yi)
     * @param p point its at
     */
    public InkSample(Point p)
    {
        x = p.x;
        y = p.y;
        timestamp = 0;
        pressure = 0;
    }
    
    /**
     * Creates a new instance of InkSample set to (xi,yi)
     * @param p point its at
     */
    public InkSample(InkSample p)
    {
        x = p.x;
        y = p.y;
        timestamp = p.timestamp;
        pressure = p.pressure;
    }
    
    /** the time recorded along with the position, ... would be nice */
    public long    timestamp;
    /** the pressure applied along with the position, ... would also be nice */
    public long    pressure;
// would also be nice to have orientation, wouldn't it?
//    orientation
}
