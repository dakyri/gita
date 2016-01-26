/*
 * GitaGrammar.java
 *
 * Created on 19 May 2005, 17:47
 *
 */

package gita;

import zone.HTMLwriter;

import java.util.LinkedList;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.File;
import java.util.Stack;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;

/**
 * This class presents a wrapper around the configurable rule system for gita's visual
 * lexicon, plus a parser to extract these from an xml file.
 *
 * @see GrammarRule
 * @since you asked
 * @author dak
 */
public class GitaGrammar extends DefaultHandler
{
    
    /**
     * Creates a new instance of GitaGrammar
     */
    public GitaGrammar()
    {
        rules = new LinkedList();
        relations = new Stack();
    }
    
    /**
     * Parses a file that contains grammar rules in xml.
     *
     * @param h a HTMLwriter for diagnostic output
     * @param f a File that is the source of the grammar rules
     * @see GrammarRule
     */
    public void parseGrammarFile(HTMLwriter h, File f)
            throws FileNotFoundException
    {
        FileReader  r = new FileReader(f);
        parseGrammar(h, r);
    }
 
    /**
     * Parses a string that contains grammar rules in xml.
     *
     * @param h a HTMLwriter for diagnostic output
     * @param s a String that is the source of the grammar rules
     * @see GrammarRule
     */
    public void parseGrammarString(HTMLwriter h, String s)
    {
        StringReader  r = new StringReader(s);
        parseGrammar(h, r);
    }
 
    /**
     * Parses a the ouput of a Reader that dishes out grammar rules in xml.
     *
     * @param h a HTMLwriter for diagnostic output
     * @param r a Reader that is the source of the grammar rules
     * @see GrammarRule
     */
    public void parseGrammar(HTMLwriter h, Reader r)
    {
        http = h;
        
        XMLReader   xr = new SAXParser();
//	MySAXApp handler = new MySAXApp();
	xr.setContentHandler(this);
	xr.setErrorHandler(this);
        try {
            xr.setFeature("http://xml.org/sax/features/validation", false); 
            xr.setFeature("http://xml.org/sax/features/external-parameter-entities", false); 
        } catch (SAXException e) {
            ;
        }
        
        InputSource is = new InputSource(r);
        
        try {
            xr.parse(is);
        } catch (IOException e) {
            http.printbr("io exception: "+e.getMessage());
        } catch (SAXException e) {
            http.printbr("sax exception: "+e.getMessage());
        }
    }

//////////////////////////////////////////////////////////////////////
// parser event handlers ... overrides from DefaultHandler
//////////////////////////////////////////////////////////////////////
    
   /**
    * Event handler called at the start of an SVG document, overriding the default for the parsing engine
    */
    public void startDocument ()
    {
    }


   /**
    * Event handler called at the end of an SVG document, overriding the default for the parsing engine
    */
    public void endDocument ()
    {
    }

   /**
    * Event handler called at the start of an SVG tag, overriding the default for the parsing engine
    */
    public void startElement (String uri, String name,
			      String qName, Attributes atts)
    {
//	http.print("Start element: " + qName);
	if (!uri.equals ("")) {
//	    http.print(" {" + uri + "}");
        }
//      http.printbr("");
        int nattr = atts.getLength();
        
        String  id = null;
        float   tolerance = 0;
        float   scale = 1;
        float   angle = 0;
        int     boxIndex = -1;
        int     tgtIndex = -1;
        int     index = -1;
        int     n = 5;
        String  label = null;
        String  target = null;
        char    c = '!';
        
        // process all the attributes first. hopefully the dtd will ensure that
        // codes are sufficiently well formed that attributes are appropriate.
        for (int i=0; i<nattr; i++) {
//                http.printbr(" - "+atts.getQName(i) + " = " + atts.getValue(i));
            String  qualName = atts.getQName(i);
            String  localName = atts.getLocalName(i);
            if (qualName.equals("id")) {
                id = new String(atts.getValue(i));
            } else if (qualName.equals("target")) {
                target = new String(atts.getValue(i));
            } else if (qualName.equals("label")) {
                label = atts.getValue(i);
            } else if (qualName.equals("tolerance")) {
                tolerance = Float.valueOf(atts.getValue(i));
            } else if (qualName.equals("scale")) {
                scale = Float.valueOf(atts.getValue(i)); 
            } else if (qualName.equals("angle")) {
                angle = Float.valueOf(atts.getValue(i));
            } else if (qualName.equals("box-index")) {
                boxIndex = Integer.valueOf(atts.getValue(i));
            } else if (qualName.equals("tgt-index")) {
                tgtIndex = Integer.valueOf(atts.getValue(i));
            } else if (qualName.equals("index")) {
                index = Integer.valueOf(atts.getValue(i));
            } else if (qualName.equals("n")) {
                n = Integer.valueOf(atts.getValue(i));
            } else if (qualName.equals("char")) {
                if (atts.getValue(i).length() > 0) {
                    c = atts.getValue(i).charAt(0);
                }
             }
        }
        
        if (qName.equals("grammar")) {
        } else if (qName.equals("rule")) {
            if (target != null) {
                if (id == null) {
                    id = target;
                }
                currentRule = new GrammarRule(id, target);
                relations.clear();
            }
        } else if (qName.equals("ref")) {
            if (target != null) {
                GrammarRule.Nonterm e = new GrammarRule.Nonterm(target);
                if (relations.peek() != null) relations.peek().add(e);
                if (currentRule != null) currentRule.add(e);
            }
            
        } else if (qName.equals("mark")) {
            Symbol.Type     t = Symbol.Type.Vanilla;
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.charSymbolType(c));
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("number")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Number);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("digit")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Digit);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("word")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Word);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("letter")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Letter);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("stroke")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Vanilla);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("line")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Line);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("triangle")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Triangle);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("arrow")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Arrow);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("box")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Box);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("circle")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Circle);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("broken-line")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Brokenline);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("dotted-line")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Dottedline);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("v-head")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Vhead);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("diamond")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Diamondhead);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("rounded-box")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Roundedbox);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
        } else if (qName.equals("polygon")) {
            GrammarRule.Term  e = new GrammarRule.Term(Symbol.Type.Polygon);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            
        } else if (qName.equals("group")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Group);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("contains")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Contains);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("follow")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Follow);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("right")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Right);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("row")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Row);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("column")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Column);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("underline")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Underline);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("below")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Below);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("empty")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Empty);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("sides")) {
            GrammarRule.Relationship  e = new GrammarRule.CountRelationship(GrammarRule.Type.Sides, n);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("convex")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Convex);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("straight")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Straight, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("horizontal")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Horizontal, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("vertical")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Vertical, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("overlay")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Overlay, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("connect")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Connect);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("endpoint")) {
            GrammarRule.Relationship  e = new GrammarRule.LabelRelationship(GrammarRule.Type.Endpoint, label);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("branchpoint")) {
            GrammarRule.Relationship  e = new GrammarRule.LabelRelationship(GrammarRule.Type.Branchpoint, label);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("headconnect")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Headconnect);
            if (relations.peek() != null) relations.peek().add(e);
            relations.push(e);
        } else if (qName.equals("tailconnect")) {
             GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Tailconnect);
             if (relations.peek() != null) relations.peek().add(e);
             if (currentRule != null) currentRule.add(e);
             relations.push(e);
        } else if (qName.equals("angle")) {
            GrammarRule.Relationship  e = new GrammarRule.AngleRelationship(GrammarRule.Type.Angle, angle, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("label")) {
            GrammarRule.Relationship  e = new GrammarRule.LabelRelationship(GrammarRule.Type.Label, label);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("width")) {
            GrammarRule.Relationship  e = new GrammarRule.ScaleRelationship(GrammarRule.Type.Width, scale, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("height")) {
            GrammarRule.Relationship  e = new GrammarRule.ScaleRelationship(GrammarRule.Type.Width, scale, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("size")) {
            GrammarRule.Relationship  e = new GrammarRule.ScaleRelationship(GrammarRule.Type.Width, scale, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("angleconnect")) {
            GrammarRule.Relationship  e = new GrammarRule.AngleRelationship(GrammarRule.Type.Angleconnect, angle, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("tail")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Tail);
            if (relations.peek() != null) relations.peek().add(e);
            relations.push(e);
        } else if (qName.equals("fill")) {
            GrammarRule.Relationship  e = new GrammarRule.Relationship(GrammarRule.Type.Fill);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("parallel")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Parallel, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            relations.push(e);
        } else if (qName.equals("intersection")) {
            GrammarRule.Relationship  e = new GrammarRule.AngleRelationship(GrammarRule.Type.Intersection, angle, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("cross")) {
            GrammarRule.Relationship  e = new GrammarRule.TolRelationship(GrammarRule.Type.Cross, tolerance);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("edgeconnect")) {
            GrammarRule.Relationship  e = new GrammarRule.EnclCnxRelationship(GrammarRule.Type.Edgeconnect, boxIndex, tgtIndex);
            if (relations.peek() != null) relations.peek().add(e);
            relations.push(e);
        } else if (qName.equals("vertexconnect")) {
            GrammarRule.Relationship  e = new GrammarRule.EnclCnxRelationship(GrammarRule.Type.Vertexconnect, boxIndex, tgtIndex);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("partition")) {
            GrammarRule.Relationship  e = new GrammarRule.LabelRelationship(GrammarRule.Type.Partition, label);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else if (qName.equals("compartment")) {
            GrammarRule.Relationship  e = new GrammarRule.PartIndexRelationship(GrammarRule.Type.Compartment, index);
            if (relations.peek() != null) relations.peek().add(e);
            if (currentRule != null) currentRule.add(e);
            relations.push(e);
        } else {
//            throw new Cow("Grammar rule reader error: unknown tag "+qName);
        }
    }


   /**
    * Event handler called at the close of an SVG tag, overriding the default for the parsing engine
    */
    public void endElement (String uri, String name, String qName)
    {
//	http.print("End element: " + qName);
        if (!uri.equals ("")) {
        }
        
        if (qName.equals("grammar")) {
        } else if (qName.equals("rule")) {
            if (currentRule != null) {
                rules.add(currentRule);
                currentRule = null;
            }
        } else if (qName.equals("ref")) {
            
        } else if (qName.equals("mark")) {
        } else if (qName.equals("number")) {
        } else if (qName.equals("digit")) {
        } else if (qName.equals("word")) {
        } else if (qName.equals("letter")) {
        } else if (qName.equals("stroke")) {
        } else if (qName.equals("line")) {
        } else if (qName.equals("triangle")) {
        } else if (qName.equals("arrow")) {
        } else if (qName.equals("box")) {
        } else if (qName.equals("circle")) {
        } else if (qName.equals("broken-line")) {
        } else if (qName.equals("dotted-line")) {
        } else if (qName.equals("v-head")) {
        } else if (qName.equals("diamond")) {
        } else if (qName.equals("rounded-box")) {
        } else if (qName.equals("polygon")) {
            
        } else if (qName.equals("group")) {
            relations.pop();
        } else if (qName.equals("contains")) {
            relations.pop();
        } else if (qName.equals("follow")) {
            relations.pop();
        } else if (qName.equals("right")) {
            relations.pop();
        } else if (qName.equals("row")) {
            relations.pop();
        } else if (qName.equals("column")) {
            relations.pop();
        } else if (qName.equals("underline")) {
            relations.pop();
        } else if (qName.equals("below")) {
            relations.pop();
        } else if (qName.equals("sides")) {
            relations.pop();
        } else if (qName.equals("convex")) {
            relations.pop();
        } else if (qName.equals("straight")) {
            relations.pop();
        } else if (qName.equals("horizontal")) {
            relations.pop();
        } else if (qName.equals("vertical")) {
            relations.pop();
        } else if (qName.equals("overlay")) {
            relations.pop();
        } else if (qName.equals("connect")) {
            relations.pop();
        } else if (qName.equals("endpoint")) {
            relations.pop();
        } else if (qName.equals("branchpoint")) {
            relations.pop();
        } else if (qName.equals("headconnect")) {
            relations.pop();
        } else if (qName.equals("tailconnect")) {
            relations.pop();
        } else if (qName.equals("angle")) {
            relations.pop();
        } else if (qName.equals("label")) {
            relations.pop();
        } else if (qName.equals("width")) {
            relations.pop();
        } else if (qName.equals("height")) {
            relations.pop();
        } else if (qName.equals("size")) {
            relations.pop();
        } else if (qName.equals("angleconnect")) {
            relations.pop();
        } else if (qName.equals("tail")) {
            relations.pop();
        } else if (qName.equals("fill")) {
            relations.pop();
        } else if (qName.equals("parallel")) {
            relations.pop();
        } else if (qName.equals("intersection")) {
            relations.pop();
        } else if (qName.equals("cross")) {
            relations.pop();
        } else if (qName.equals("edgeconnect")) {
            relations.pop();
        } else if (qName.equals("vertexconnect")) {
            relations.pop();
        } else if (qName.equals("partition")) {
            relations.pop();
        } else if (qName.equals("compartment")) {
            relations.pop();
        } else {
//            throw new Cow("Grammar rule reader error: unknown tag "+qName);
        }
//      http.printbr("");
        
    }


   /**
    * Event handler called at the discovery of character data in an SVG document, overriding the default for the parsing engine
    */
    public void characters (char ch[], int start, int length)
    {
//	http.print("Characters:    \"");
//      http.printQuoted(ch, start, length);
//	http.printbr("\"");
    }
    
    /** a list of the rules for gita's driving grammar */
    public LinkedList<GrammarRule>  rules=null;
    /** diagnostic output writer */
    HTMLwriter                      http = null;
    Stack<GrammarRule.Relationship> relations = null;
    GrammarRule                     currentRule = null;
   
}
