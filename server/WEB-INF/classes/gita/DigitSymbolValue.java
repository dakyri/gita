/*
 * DigitSymbolValue.java
 *
 * Created on 21 April 2005, 15:58
 */

package gita;
 
/**
 * Value of a stroke group that corresponds to a digit in the range 0 .. 9
 *
 * @author dak
 * @since you asked
 */
public class DigitSymbolValue extends SymbolValue
{
    /**
     * Creates a new instance of DigitSymbolValue
     *
     * @param v the value of the digit
     */
    public DigitSymbolValue(int v)
    {
        value = v;
    }
    
    /**
     * Returns the display value of the digie
     *
     * @return the display value of the digie
     */
    public String stringValue()
    {
        return Integer.toString(value);
    }
    
    /** the value of the digit */
    public int     value=0;
}
