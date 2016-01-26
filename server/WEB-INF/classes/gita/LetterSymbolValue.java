/*
 * LetterSymbolValue.java
 *
 * Created on 21 April 2005, 19:23
 */

package gita;
 
/**
 *
 * Value of a stroke group that corresponds to an alphabetic character
 *
 * @author dak
 */
public class LetterSymbolValue
{
    /**
     * Creates a new instance of LetterSymbolValue
     */
    public LetterSymbolValue(char c)
    {
        value = c;
    }
    
    /**
     * The default display value of the letter
     * @return returns the ascii character converted to a string
     */
    public String stringValue()
    {
        return Character.toString(value);
    }
    
    /** the ascii character corresponding to the letter */
    public char    value=0;
}
