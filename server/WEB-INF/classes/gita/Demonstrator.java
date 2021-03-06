/*
 * Demonstrator.java
 *
 *   hook for a system demo ... to be filled in!
 * Created on 21 April 2005, 16:53
 */

package gita;

import java.util.LinkedList;

import zone.HTMLwriter;
import zone.SVGwriter;

import zone.Attribute;
import zone.ZoneException;
import zone.DoubleLinkNode;
import zone.DoubleLinkList;
import zone.DoubleLinkIterator;

/**
 * Basic hook for a system demo. In its current guise, it doesn't particularly use any deeper features
 * of the GIDA patent: it is mainly meant to demonstrate a process flow using the system.
 *<p>
 * A lot of assumptions have been made to enable this to be knocked up relatively quickly from the
 * completed parts of the system. <b>It would be important to keep these assumption in mind
 * during any system demonstrations using this code</b>.
 * <ul>
 *  <li>The concept of geometric adjacency regardless
 *    of position in the input stream, encapsulated in the gita grammar relationships, is
 *    completely ignored. Adjacency in the stream implies immediate geometric adjacency.
 *    A character is composed of consecutive strokes, a data or a time of consecutive symbols.
 *    This implies that for this the methods below, the order of strokes must be preserved.
 *  <li>The textual baseline runs parallel to the page. This means that recognitions don't have to
 *    be rotationally invariant.
 *  <li>All recognition is entirely unambiguous. There are no fuzzy branches in the SymbolList
 *    dealt with or generated.
 * </ul>
 *
 * @author dak
 * @since you asked
 */
public class Demonstrator
{
    
   /**
    * Creates a new instance of Demonstrator
    */
    public Demonstrator()
    {
    }
    
   /**
    * Determines whether a stroke match a slash.
    * 
    * @param s the stroke in question
    * @return true if we have a match
    */
    public boolean matchesSlash(Stroke s)
    {
        try {
            StrokeCurveInfo inf = (StrokeCurveInfo) s.info;
            if (inf.type == StrokeCurveInfo.Type.Straight) {
                Point startp = inf.artifactFilteredStroke[0];
                Point endp = inf.artifactFilteredStroke[inf.artifactFilteredStroke.length-1];
                float len = dist(startp< endp)
            }
        } catch (ClassCastException c) { // not an info structure i can make a decision on
            ;
        }
        return false;
    }

   /**
    * Determines whether a stroke matches a full stop.
    * 
    * @param s the stroke in question
    * @return true if we have a match
    */
    public boolean matchesFullstop(Stroke s)
    {
        if (s.bounds.diagLen() < Tolerance.fullStopBoundsDiag) { 
            return true;
        }
        return false;
    }

    /**
     * processes a page of stroke data, producing a list of symbols
     *
     * @param page a PageData structure containing all the strokes on an input page
     * @return a SymbolList containing analytical results from the input page
     */
    public SymbolList processPageData(PageData page)
    {
        SymbolList  symlist = new SymbolList();
///////////////////////////////////////////////////////////////////////////////////
// create a basic list of symbols, and check for unambiguous single stroke marks...
///////////////////////////////////////////////////////////////////////////////////
       for (Stroke b:page.strokes) {
            if (matchesFullstop(b)) {
               symlist.add(Symbol.Type.Slash, 1, b);
            } else if (matchesSlash(b)) {
                symlist.add(Symbol.Type.Fullstop, 1, b);
            } else {
                symlist.add(Symbol.Type.Vanilla, 1, b);
            }
       }
       
///////////////////////////////////////////////////////////////////////
// TODO: code to recognise a colon (two vertically aligned fullstops)
///////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////
// TODO: code to recognise single digits
///////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////
// TODO: code to recognise composite structures, date and time
///////////////////////////////////////////////////////////////////////
    
       return symlist;
    }
    
    /**
     * generates output appropriate to the processed data
     *
     * @param http a html output destination
     * @param svg an svg output destination, which will be embedded within the web page for <i>http</i>
     * @param page a PageData structure containing all the strokes on an input page
     * @param symbols prior analytical results on the page data.
     */
    public void generateInterpretedPage(HTMLwriter http, SVGwriter svg, PageData page, SymbolList symbols)
    {
        try {
            svg.xmldecl();
            svg.svg(400, 400);
            
///////////////////////////////////////////////////////////////////////
// TODO: display headings, icons, and graphics appropriate to the demo
///////////////////////////////////////////////////////////////////////
            
//            Attribute[] a = { 
//                  new Attribute("style", "fill-opacity:1.7; stroke:green; stroke-width:0.3cm;")
//            };
//            svg.circle(200, 200, 100, "#000000", "blue", 2);
//            svg.circle(100, 100, 50, "#000000", "red", 2);
//            svg.circle(50, 50,50, "#000000", "yellow", 2);
//            svg.image(200, 200, 100, 100, "../images/mose.jpg");
//            svg.text(100,100,"Hello :)","Arial",20,"orange");
//            svg.line(100,50, 200, 50, "stroke", 16);
//            svg.polygon(56,"sd",12);
            
// display stroke data, appropriately for particular interpretations of the data
            svg.g("stroke-data");
            for (Symbol sym: symbols) {
                switch(sym.type) {
                    
                    case Vanilla: {
                        for (Stroke s: sym.strokes) {
                            svg.path("stroke", "fill:none;stroke:blue;stroke-width:1;", s.PathData());
                        }
                        break;
                    }

// for any symbol we don't specifically recognise, generate an exception
// in a demo situation, it would be more prudent to comment this line out, and attempt to
// push on regardless
                    default: {
                        throw new Cow("Unexpected symbol type: "+sym.type);
                    }
                }
            }
            svg.closetag("g");
            
            svg.closetag("svg");

        } catch (ZoneException e) {
            http.printbr("<br>Library error:"+e.getMessage());
        } catch (Cow c) {
            http.printbr("<br>Internal gita error:"+c.getMessage());
        }
    }
    
}
