/*
 * TimeSymbolValue.java
 *
 * Created on 18 May 2005, 19:15
 *
 */

package gita;

/**
 * A SymbolValue extension for all the bits of handwritten times recognised by the system
 * Time values are stored for a 24 hour handwritten clock.
 *<p>
 * Not sure if we should worry about seconds. There are a few theoretical examples in the
 * original patent that use precisely defined timelines.... accurate to frames that are
 * fractions of seconds.
 *
 * @see CompoundSymbolValue 
 * @author dak
 * @since you asked
 */
public class TimeSymbolValue extends CompoundSymbolValue
{
    
    /**
     * Creates a new instance of TimeSymbolValue
     */
    public TimeSymbolValue(Symbol ... symi)
    {
        super("time");
        set(23, 59);
        addSymbols(symi);
    }
    
    /**
     * Creates a new instance of TimeSymbolValue
     *
     * @param h the hour
     * @param m the minute
     */
    public TimeSymbolValue(int h, int m, Symbol ... symi)
    {
        super("time");
        set(h, m);
        addSymbols(symi);
    }
    
    /**
     * Sets the value of this TimeSymbolValue structure
     *
     * @param h the hour
     * @param m the minute
     */
    public void set(int h, int m) 
    {
        hour = h;
        minute = m;
    }
    
    /** the hour of the day */
    public int     hour;
    /** the minute of the hour */
    public int     minute;
    
}
