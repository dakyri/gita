/*
 * DateSymbolValue.java
 *
 * Created on 18 May 2005, 18:58
 *
 */

package gita;

/**
 * A SymbolValue extension for all the bits of handwritten dates recognised by the system
 *
 * @see CompoundSymbolValue 
 * @author dak
 * @since you asked
 */
public class DateSymbolValue extends CompoundSymbolValue
{
    /**
     * Creates a new instance of DateSymbolValue
     */
    public DateSymbolValue(Symbol ... symi)
    {
        super("date");
        set(31, 12, 1999);
        addSymbols(symi);
    }
    
    /**
     * Creates a new instance of DateSymbolValue
     * @param d the day
     * @param m the month
     * @param y the year
     */
    public DateSymbolValue(int d, int m, int y, Symbol ... symi)
    {
        super("date");
        set(d, m, y);
        addSymbols(symi);
    }
    
    /**
     * Sets the date part of the value of this DateSymbolValue
     *
     * @param d the day
     * @param m the month
     * @param y the year
     */
    public void set(int d, int m, int y)
    {
        day = d;
        month = m;
        year = y;
    }
    
    /** the day of the month */
    public int     day;
    /** the month of the year */
    public int     month;
    /** the year ... 4 digits please! */
    public int     year;
}
