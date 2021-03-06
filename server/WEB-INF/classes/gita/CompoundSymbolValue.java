/*
 * CompoundSymbolValue.java
 *
 * Created on 21 April 2005, 15:49
 */

package gita;
 
import java.util.LinkedList;
import java.util.Collection;

/**
 * Extension of SymbolValue for a symbol that corresponds to a matched visual language rule e.g. date or time.
 * These are created when a particular grammar rule is recognised.
 *<p>
 * Type/name of the compound symbol, the name of the target of the associated rule, is stored as a string
 * identifier ... the set of compound symbols should
 * be user extensible. ideally, these tags would be stored in a database, along with associated user rules
 * for defining them. Examples would be dates, times, urls, collections of text and words.
 *<p>
 * This class should probably refer back to the internal representation of the grammar rules, as
 * well as refering to a particular rule by name.
 *<p>
 * the value/meaning/significance of such a value depends on a traversal of the list of component symbols
 * and would be wired into application software. some way down the track, we could find a better, more flexible way 
 * of doing this.
 *
 * @see SymbolValue
 * @author dak
 * @since you asked
 */
public class CompoundSymbolValue extends SymbolValue
{
   /**
    * Creates a new instance of CompoundSymbolValue, allocating an empty list for the associated <i>Symbol</i>s
    *
    * @param t the name of the new rule.
    */
    public CompoundSymbolValue(String t)
    {
        name = new String(t);
        symbols = new LinkedList<Symbol>();
    }
    
   /**
    * Creates a new instance of CompoundSymbolValue, populating a list for the associated <i>Symbol</i>s
    *
    * @param t the name of the new rule.
    * @param symi the associated <i>Symbol</i>s
    */
    public CompoundSymbolValue(String t, Symbol ... symi)
    {
        name = new String(t);
        symbols = new LinkedList<Symbol>();
        addSymbols(symi);
    }
    
   /**
    * Creates a new instance of CompoundSymbolValue, populating a list for the associated <i>Symbol</i>s.
    * The passed array is left unaltered.
    *
    * @param symi the associated <i>Symbol</i>s
    * @return true if the operation is successful
    */
    public boolean addSymbols(Symbol[] symi)
    {
        if (symbols == null) {
            symbols = new LinkedList<Symbol>();
        }
        for (int i=0; i<symi.length; i++) {
            symbols.add(symi[i]);
        }
        return true;
    }
    
   /**
    * Creates a new instance of CompoundSymbolValue, populating a list for the associated <i>Symbol</i>s. The
    * passed <i>Collection</i> is left unaltered.
    *
    * @param symi the associated <i>Symbol</i>s
    * @return true if the operation is successful
    */
    public boolean addSymbols(Collection<Symbol> symi)
    {
        if (symbols == null) {
            symbols = new LinkedList<Symbol>();
        }
        for (Symbol i: symi) {
            symbols.add(i);
        }
        return true;
    }
    
   /**
    * Returns the name of this Symbol.
    *
    * @return the name of the symbol
    */
    public String stringValue()
    {
        return name != null? name : "Unknown Compound Symbol";
    }
    
   /**
    * Returns the name of this Symbol.
    *
    * @return the name of the symbol
    */
    public String contentStr()
    {
        String  v = "";
        for (Symbol s: symbols) {
            if (s.type == Symbol.Type.Compound && s.value != null) {
                v = v + ((CompoundSymbolValue)s.value).contentStr();
            } else {
                v = v + s.value.stringValue();
            }
        }
        return v;
    }
    
    /** the name of the target/rule for this compound symbol */
    public String              name=null;
    /** list of the symbols that compose this compound structure */
    public LinkedList<Symbol>  symbols=null;
}
