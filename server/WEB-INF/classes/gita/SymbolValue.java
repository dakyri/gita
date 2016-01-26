/*
 * SymbolValue.java
 *
 * Created on 21 April 2005, 15:33
 *  abstract base class for the value of an interpreted symbol, must be extended
 *
 *   extended by
 *      CompoundSymbolValue
 *      DigitSymbolValue
 *      LetterSymbolValue
 */

package gita;
 
/**
 * Base abstract representation of a gita Symbol. This is superclassed and extended variously for
 * different possible symbol types. For all intents and purposes, a <i>Symbol</i> is a 
 *
 * @author David Karla
 */
public abstract class SymbolValue
{
    public abstract String stringValue();
}
