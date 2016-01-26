/*
 * SvgStrokeParser.java
 *
 * Created on 2 September 2004, 08:35
 *
 *   Gita currently stores stroke data in SVG files, along with as many other pieces of
 *   useful information as it can cram into xml attributes in its own namespace.
 *   Stroke data is converted to SVG path elements, with attributes:
 *          "id"=<name>: normal svg element name holds the stroke name
 *          "d"=<data>: normal svg attribute for path data holds the stroke data
 *          "gita:vastd"=<user>: this stroke is an enclosing box for signature training data for <user>
 *
 *   builds up internal structures for a page of strokes stored as an SVG document
 *      creates:
 *          - PageData structure, plus substructures:
 *              - SignatureContainer structure for signature data
 *              - SignatureStroke structure for strokes that are part of a signature
 */
package gita;
 
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.lang.*;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
//import javax.xml.parsers.SAXParser;
import org.apache.crimson.parser.Parser2;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;

import zone.HTMLwriter;
import zone.PathDataAttribute;
import zone.Point;
import zone.ZoneException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import gita.*;

/**
 * class for processing input SVG data, and processing it into the standard internal form
 * for input strokes, a PageData structure
 *<p>
 * Throughout the XML parse, a stack of SVG groups is maintained- the innermost recent and active
 * group is at the top of the stack. An attempt is made to map the svg groups onto gita StrokeGroup
 * structures, in particular for signatures, and signature training data.
 *
 * @see PageData
 * @see Stroke
 * @see StrokeGroup
 */
public class SvgStrokeParser extends DefaultHandler
{
    /**
     * Creates a new instance of SvgStrokeParser
     */
    public SvgStrokeParser()
    {
        maxX = maxY = 0;
    }
    

    /**
     * calls up the xml parser to process an svg file, building a page data structure from the strokes and stroke
     * groups that are churned out by the xml parser.
     *
     * @param logData a string containing svg data for a page of stroke input
     * @param h a HTMLwriter for sending diagnostic output
     * @return a PageData holding all the read and processed strokes
     * @throws LogParseException
     */
    PageData parseLogData(String logData, HTMLwriter h)
           throws LogParseException
   {
       http = h;
       
// initialise all the basic structures to build up a fresh page
        strokeList = new LinkedList();
        groupList = new LinkedList();
        groupStack = new Stack();
        maxX = maxY = 0;
        
        XMLReader   xr = new SAXParser();
	xr.setContentHandler(this);
	xr.setErrorHandler(this);
        try {
            xr.setFeature("http://xml.org/sax/features/validation", false); 
            xr.setFeature("http://xml.org/sax/features/external-parameter-entities", false); 
        } catch (SAXException e) {
            ;
        }

        StringReader r = new StringReader(logData);
        InputSource is = new InputSource(r);
        
        strokeList.clear();
        
        try {
            xr.parse(is);
        } catch (IOException e) {
            http.printbr("io exception: "+e.getMessage());
        } catch (SAXException e) {
            http.printbr("sax exception: "+e.getMessage());
        }

        PageData page = new PageData(strokeList, groupList, maxX, maxY); 
        page.SetupStructures(http); 
        return page;
    }

   /**
    *  Converts a pile of path data into a stroke. If the top active group is a signature related
    * group,  add the stroke to a stroke group list.
    *<p>
    * Possibly we should embed strokes from groups embedded within groups, but currently only look at
    * the top, innermost level... this could be done as we pop groups off the stack.
    * full blown visual language parser will need to use some structure like this.
    *<p>
    * All tags are looked at... hopefully one of them is a "d" attribute that provides path data for
    * the returned stroke. 
    *
    * @param atts an array of the attributes that came with this path tag.
    * @return a stroke formed from the attributes of this path tag
    */
   public Stroke getStrokeFromPath(Attributes atts)
       throws LogParseException, ZoneException
   {
        int i;
        
        Pattern pathStyleElementPattern = Pattern.compile(
                "");
        
        Stroke  s = new Stroke();
        if (!groupStack.empty()) {
            StrokeGroup   g = groupStack.peek();
            switch (g.type) {
                case Signature: {
                   g.add(s);
                    break;
                }
                case SignatureTraining: {
                    g.add(s);
                    break;
                }
                case Vanilla: {
                    break;
                }
                default: {
                    break;
                }
            }
        }
         int nattr = atts.getLength();
         for (i=0; i<nattr; i++) {
//                http.printbr(" - "+atts.getQName(i) + " = " + atts.getValue(i));
            String  qualifiedName = atts.getQName(i);
            String  localName = atts.getLocalName(i);
            if (qualifiedName.equals("id")) {
                /*
                 * text based id of the stroke
                 */
                s.id = new String(atts.getValue(i));
            } else if (qualifiedName.equals("gita:vastd")) {
                /*
                 * this stroke is a containter for other signature training data
                 *  the group created here will be filled later in the peace, when all strokes have been read.
                 */
                s.group = new StrokeGroup(StrokeGroup.Type.SignatureTraining);
                s.group.info = new SignatureContainer(atts.getValue(i), s.group);
                groupList.add(s.group);
            } else if (qualifiedName.equals("gita:signature")) {
                /* 
                 * this stroke is a containter for strokes that
                 * are an attempt to validate against a previously trained signature
                 * stored elsewhere in the system
                 */
                s.group = new StrokeGroup(StrokeGroup.Type.Signature);
                s.group.info = new SignatureContainer(atts.getValue(i), s.group);
                groupList.add(s.group);
            } else if (qualifiedName.equals("d")) {
                /*
                 * the path data for the stroke
                 */
                 PathDataAttribute  p = new PathDataAttribute(atts.getValue(i));
                 LinkedList<Point>  coords = p.decompilePathData();
                 s.nSample = coords.size();
                 s.sample = new InkSample[s.nSample];
                 int ind = 0;
                 for (Point pt: coords) {
                     s.sample[ind++] = new InkSample(pt);
                 }
                 s.CalculateBounds(); 

                 if (p.maxX > maxX) {
                     maxX = p.maxX;
                 }
                 if (p.maxY > maxY) {
                     maxY = p.maxY;
                 }
            }

        }
        return s;
    }
   
   /**
    * called when an SVG group tag is recognised.
    *<p>
    * this routine maintains the stack of groups that gita uses for building StrokeGroup structures.
    * it allocates, on this stack, and StrokeGroup, and StrokeGroupInfo structures that are appropriate
    * to the type and attributes of the recognised tag.
    *
    * @param atts the attributes of the recognised group tag.
    */
   public void startGroup(Attributes atts)
   {
         StrokeGroup        g = null;
         int                nattr = atts.getLength();
         String             id = null;
         String             uid = null;
         StrokeGroup.Type   type = StrokeGroup.Type.Vanilla;
         
         for (int i=0; i<nattr; i++) {
//                http.printbr(" - "+atts.getQName(i) + " = " + atts.getValue(i));
            String  qualifiedName = atts.getQName(i);
            String  localName = atts.getLocalName(i);
            if (qualifiedName.equals("id")) {
                /*
                 * text based id of the stroke
                 */
                type = StrokeGroup.Type.Vanilla;
                id = atts.getValue(i);
            } else if (qualifiedName.equals("gita:vastd")) {
                /*
                 * this stroke is a containter for other signature training data
                 */
                type = StrokeGroup.Type.SignatureTraining;
                uid = atts.getValue(i);
            } else if (qualifiedName.equals("gita:signature")) {
                /* 
                 * this stroke is a containter for strokes that
                 * are an attempt to validate against a trained signature
                 */
                type = StrokeGroup.Type.Signature;
                uid = atts.getValue(i);
            }
         }
         g = groupStack.push(new StrokeGroup(type, id));
         switch (type) {
             case Signature:
             case SignatureTraining: {
                 g.info = new SignatureContainer(uid, g);
                 break;
             }
         }
   }

   /**
    * called when the parser recognises a close group tag.
    *<p>
    * Does necessary bookkeeping for maintaining the group stack, and building StrokeGroup structures
    * relevant to the document 
    */
   public void endGroup()
   {
       StrokeGroup  g = null;
       
       g = groupStack.pop();
       
        switch (g.type) {
            case Signature: {
                groupList.add(g);
                break;
            }
            case SignatureTraining: {
                groupList.add(g);
                break;
            }
            case Vanilla: {
                break;
            }
            default: {
                break;
            }
        }
   }
   
    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////


   /**
    * Event handler called at the start of an SVG document, overriding the default for the parsing engine
    */
    public void startDocument ()
    {
//	http.printbr("Start document");
    }


   /**
    * Event handler called at the end of an SVG document, overriding the default for the parsing engine
    */
    public void endDocument ()
    {
//	http.printbr("End document");
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
        
        if (qName.equals("path")) {
            try {
                strokeList.add(getStrokeFromPath(atts));
            } catch (ZoneException e) {
                http.printbr(e.getMessage());
            } catch (LogParseException e) {
                http.printbr(e.getMessage());
            }
        } else if (qName.equals("g")) {
            startGroup(atts);
        } else {
        }
    }


   /**
    * Event handler called at the close of an SVG tag, overriding the default for the parsing engine
    */
    public void endElement (String uri, String name, String qName)
    {
//	http.print("End element: " + qName);
        if (qName.equals("g")) {
            endGroup();
        } else if (!uri.equals ("")) {
//	    http.print(" {" + uri + "}");
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
    
    /** stack of StrokeGroup structures used to track any interesting and useful group structure this document might have ...
     *  especially in the form of signature or training data */
    Stack<StrokeGroup>      groupStack = null;
    /** list of strokes that we have gleaned from path data in the course of processing a document */
    LinkedList<Stroke>       strokeList = null;
    /** a list of group structures built up as we parse the document */ 
    LinkedList<StrokeGroup>  groupList = null;
    /** HTMLwriter used for diagnostic output, set up by the calling method. This is global, as the overridden event handlers don't provide any other parameters. */
    HTMLwriter              http;
    /** maximum path x value, used to set up page bounding box */
    float                   maxX;
    /** maximum path y value, used to set up page bounding box */
    float                   maxY;
}
