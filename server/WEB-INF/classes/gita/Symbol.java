/*
 * Symbol.java
 *
 * Created on 9 September 2004, 02:01
 */

package gita;
 
import java.util.LinkedList;
import zone.DoubleLinkNode;

/**
 *   the basic class for an interpreted object deduced from a stream of stroke data.
 *<p>
 *   this is probably a provisional implementation.
 *   for the moment, (april 2005) the goal is a demo with not much room for allowing the possibility of ambiguous
 *   streams to come out of a recognizer.
 *   however, the bigger picture of gita allows for such fuzziness, (and at times
 *   demands it!). Fuzziness implies that structures will get quite convoluted, and the java LinkedList/Collection
 *   classes would be stretched to express it ... hence the more discrete implementation.
 *<p>
 * Note: Throughout this class, Stroke objects are referered to. These are references, and not copies.
 * The original strokes are still maintained in their original structure, likely a LinkedList<Stroke>
 * held within a PageData, or a descendant thereof.
 *
 * @see Stroke
 * @see PageData
 * @see SymbolList
 * @since you asked
 */
public class Symbol extends DoubleLinkNode
{

/**
 * basic symbol types for the visual language system
 */
    public enum Type {
/** a single numeral character */           Digit,
/** a single alphabetic */                  Letter,

////////////////////////////
// basic punctuation marks  
////////////////////////////
/** the '/' character */                    Slash,
/** the '\\' character */                   Backslash,
/** the '.' character */                    Fullstop,
/** the ',' character */                    Comma,
/** the ';' character */                    Semicolon,
/** the ':' character */                    Colon,
/** the '*' character */                    Asterisk,
/** the '@' character */                    At,
/** the '$' character */                    Dollar,
/** the '[' character */                    Leftsquare,
/** the ']' character */                    Rightsquare,
/** the '(' character */                    Leftparen,
/** the ')' character */                    Rightparen,
/** the '{' character */                    Leftbrace,
/** the '}' character */                    Rightbrace,
/** the '<' character */                    Leftangle,
/** the '>' character */                    Rightangle,
/** the '"' character */                    Leftquote,
/** the '"' character */                    Rightquote,
/** the '!' character */                    Shriek,
/** the '#' character */                    Hash,

//////////////////////////////
// basic drawn graphic marks
//////////////////////////////
/** a single line, straight or otherwise */  Line,
/** a 3 sided closed figure */              Triangle,
/** a drawn arrow */                        Arrow,
/** a rectangle */                          Box,
/** a closed circle */                      Circle,
/** a dashed line */                        Brokenline,
/** a dotted line */                        Dottedline,
/** a 'V' shape */                          Vhead,
/** a diamond shape/angled rectangle */     Diamondhead,
/** a rectangle with rounded corners */     Roundedbox,
/** a many sided closed figure */           Polygon,

///////////////////////////////////////////
// collections of letters and digits ...
///////////////////////////////////////////
/** a group of adjacent numerals */           Number,
/** a group of adjacent letters */            Word,

////////////////////////////////////////////////////////////////////////
// a compound/composite structure ... a rule in the graphicakl language
////////////////////////////////////////////////////////////////////////
/** a compound symbol/grammar rule */        Compound,

/////////////////////////////////////////////////////////////////////////////////////
// a branch in possible interpretations of a page of stroke data
// this doesn't correspond to any strokes ... it is a branch in the stream, for which the value structure
// provides a list of alternates.
///////////////////////////////////////////////////////////////////////////////////////////
/** a branch in the symbol list */            Fuzzy,

////////////////////////////////////////////////////////////////////////////////////////////////////
// room for user defined conglomerates... don't have an infrastructure to deal with them yet, though!
////////////////////////////////////////////////////////////////////////////////////////////////////
/** room for someone to define their own */    Userdefined,

////////////////////
// anything else!
/////////////////////
/** anything but plain, but very common. A stroke with no other known meaning */   Vanilla
    }
    
    /**
     * gives the symbol type associated with "something" that would represent a given ascii character
     * 
     * @param c a character to check
     * @return a Symbol Type value
     */
    public static Type charSymbolType(char c)
    {
        switch (c) {
            case '/': return Type.Slash;
            case '\\': return Type.Backslash;
            case '.': return Type.Fullstop;
            case ',': return Type.Comma;
            case ';': return Type.Semicolon;
            case ':': return Type.Colon;
            case '*': return Type.Asterisk;
            case '@': return Type.At;
            case '$': return Type.Dollar;
            case '[': return Type.Leftsquare;
            case ']': return Type.Rightsquare;
            case '(': return Type.Leftparen;
            case ')': return Type.Rightparen;
            case '{': return Type.Leftbrace;
            case '}': return Type.Rightbrace;
            case '<': return Type.Leftangle;
            case '>': return Type.Rightangle;
            case '`': return Type.Leftquote;
            case '\'': return Type.Rightquote;
            case '!': return Type.Shriek;
            case '#': return Type.Hash;
        }
        return Type.Vanilla;
    }
    
    /**
     * constructs a symbol of the given type, certainty, and constituting strokes
     *
     * @param t type of symbol (Symbol.Type)
     * @param c certainty value for new symbol
     * @param si strokes that constitute the symbol
     * @see Symbol.Type
     */
    public Symbol(Type t, float c, Stroke ... si)
     {
        type = t;
        certainty = c;
        strokes = new LinkedList();
        for (Stroke s: si) {
            strokes.add(s);
        }
        value = null;
     }

    /**
     * constructs a symbol of the given type, certainty, and constituting strokes
     *
     * @param t type of symbol (Symbol.Type)
     * @param c certainty value for new symbol
     * @param v the value structure for the new symbol
     * @param si strokes that constitute the symbol
     * @see Symbol.Type
     */
     public Symbol(Type t, float c, SymbolValue v, Stroke ... si)
     {
        type = t;
        certainty = c;
        strokes = new LinkedList();
        for (Stroke s: si) {
            strokes.add(s);
        }
        value = v;
     }
     
    /**
     * wrapper arround the next bit
     * @return cast of next
     */
    public Symbol n() { return (Symbol) next; }
    
    /**
     * wrapper arround the next bit
     * @param cnt number of times to iterate
     * @return cast of next
     */
    public Symbol n(int cnt)
    {
        if (cnt == 0) {
            return this;
        }
        Symbol  p = (Symbol) next;
        while (next != null && cnt > 0) {
            cnt--;
            p = (Symbol)p.next;
        }
        return p;
    }

    /**
     * wrapper arround the next bit
     * @return cast of next
     */
    public Symbol p() { return (Symbol) prev; }
    
    /**
     * wrapper arround the prev bit
     * @param cnt number of times to iterate
     * @return cast of next
     */
    public Symbol p(int cnt)
    {
        if (cnt == 0) {
            return this;
        }
        Symbol  p = (Symbol) prev;
        while (next != null && cnt > 0) {
            cnt--;
            p = (Symbol)p.prev;
        }
        return p;
    }


    /** the <i>Type</i> of this symbol */
    public Type                type=Type.Vanilla;
    /** the certainty we have in this particular assignment... a float value from [0,1] giving the odds that we'return right with this interpretation */
    public float               certainty=1; 
    /** the strokes that compose this Symbol */
    public LinkedList<Stroke>  strokes=null;
    /** the value of the Symbol. This structure might contain actual values, subtypes, composing strokes and symbols */
    public SymbolValue         value=null;

/////////////////////////////////////////////////////////////////////////////////////////
// Some convenience classes
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Convenience class for a time Symbol and SymbolValue constructor
     *
     * @param h the hour
     * @param m the minute
     * @param si component symbols if any
     * @return a properly setup Symbol for a time
     */
     static public Symbol Time(int h, int m, Symbol ... si)
     {
         return new Symbol(Type.Compound, 1, new TimeSymbolValue(h, m, si));
     }
     
    /**
     * Convenience class for a date Symbol and SymbolValue constructor
     *
     * @param d the day
     * @param m the month
     * @param y the year
     * @param si component symbols if any
     * @return a properly setup Symbol for a time
     * @see DateSymbolValue
     */
     static public Symbol Date(int d, int m, int y, Symbol ... si)
     {
         return new Symbol(Type.Compound, 1, new DateSymbolValue(d, m, y, si));
     }
     
    /**
     * Convenience class for a date Symbol and SymbolValue constructor
     *
     * @param name the name of the compound symbol
     * @param si component symbols if any
     * @return a properly setup Symbol for a time
     * @see CompoundSymbolValue
     */
     static public Symbol Compound(String name, Symbol ... si)
     {
         return new Symbol(Type.Compound, 1, new CompoundSymbolValue(name, si));
     }

}
