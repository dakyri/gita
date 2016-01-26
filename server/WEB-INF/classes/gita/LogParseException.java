package gita;
 
import java.lang.Exception;

/**
 * Specific exception class for errors within the log parse componenets of gita.
 *
 * @author dak
 */
public class LogParseException extends Exception
{
    /**
     * Creates a new instance of a LogParseException
     *
     * @param s string error message
     */
    public LogParseException(String s)
    {
        super(s);
    }
}