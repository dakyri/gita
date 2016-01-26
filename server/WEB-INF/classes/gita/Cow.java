/*
 * Cow.java
 *
 * Created on 15 February 2005, 14:14
 *
 *  generic internal error .... throw a cow
 */
package gita;
 
import java.lang.Exception;


/**
 * Generic exception class for errors within gita, which throws a Cow if something goes wrong
 *
 * @author dak
 */
public class Cow extends Exception
{
    
    /**
     * Creates a new instance of a Cow
     *<p>
     * Mooooo!
     *
     * @param s string error message
     */
    public Cow(String s)
    {
        super(s);
    }
}
