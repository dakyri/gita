/*
 * gita.java
 *
 * Created on 7 November 2003, 07:58
 */

package gita;
 
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedList;
import java.lang.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.*;

import zone.Point;
import zone.HTMLwriter;
import zone.Attribute;
import zone.PathDataAttribute;
import zone.SVGwriter;

/**
 * Main Java servlet class. Provides the core web and disk io for the Gida system.
 *<p>
 *  Output and intermediate forms are in SVG.
 *  We define an xml namespace for gita so we can store bits and pieces
 *  of gita stuff within an xml format. ie particularly in svg, embedded within the
 *  path attributes of the SVG displaying the strokes:
 *  to be used as a data structure for storing:
 *<ul>
 *    <li> terminal symbol values: "name, name, ..., name" 
 *    <li> associated probabilities: "float, float, ..., float"
 *    <li> chained strokes where any of these terminals are composite symbols: "chained, chained, ..., chained"
 *</ul>
 * <p>
 * other relevant info good to embed in an xml form:
 *<ul>
 *    <li> for each terminal graphic:
 *      <ul>
 *         <li>name
 *         <li>associated personalized/trained values (though careful with security)
 *      </ul>
 *</ul>
 *<p>
 * We store calculated features in a seperate namespace, 'f:'. These could be used
 * as named attributes in a path, and would give maximum flexibility and extensibility
 * 
 * @author  David Karla
 * @version 0.01
 */
public class Gita extends HttpServlet
{
    /**
     * Initializes the servlet.
     * @param config the Servlet config
     */
    public void init(ServletConfig config)
        throws ServletException
   {
        super.init(config);
        String  projectBase = config.getInitParameter("project-base");
        String  gitaMode = config.getInitParameter("gita-mode");
        if (projectBase == null) {
            projectBase = "d:\\dak/java/projects/anoto/Gita-build/build/web/";
// for netbeans 3.6
//            projectBase = "d:\\dak/java/projects/anoto/server/";
        }
        
        if (gitaMode != null) {
            if (gitaMode.equals("demo")) {
                demoMode = true;
            }
        }
        
        logDirectory = new File(projectBase+"processed/");
        tmpDirectory = new File(projectBase+"tmp/");
    }
    
    /**
     * Destroys the servlet.
     */
    public void destroy()
    {
        
    }
    
    
    /**
     * Operates on the given named file.
     *
     * @param logFileName   the file
     */
    protected void processFile(String logFileName)
    {
    }
    
    /**
     * Cleans up the given directory, deleting files that are older than the given use by time
     *
     * @param directory A File object corresponding to a directory
     * @param useBy The ttl of files in this directory (in minutes)
     */
    protected void purgePastUseby(File directory, long useBy)
    {
        File[]  tmpList = directory.listFiles();
        long    now = System.currentTimeMillis();
        long    tmpKeepMillis = useBy * 60 * 1000;
        
        if (tmpList != null) {
            for (short i=0; i<tmpList.length; i++) {
                long   modt = tmpList[i].lastModified();
                if (now - modt > tmpKeepMillis) {
                    tmpList[i].delete();
                }
            }
        }
    }
    
    /**
     * Generates a temporary file name to use for system-created svg versions of the anoto log files
     *
     * @param logId Arbitrary number tagging particular sequences of archived logs
     * @param newLogSeqNo   The sequence number of this log file
     */
    String logName(int logId, int newLogSeqNo)
    {
        return "log-"+Integer.toString(logId)+
                "-"+Integer.toString(newLogSeqNo)+".svg";
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *<p>
     * log file could be quite big (40pages of page) so
     * we dont want to GET data, but receive by POST
     * and we want to direct html output to an arbitrary browser frame
     * so we construct a temporary html file on server, and display that.
     * The other option is to use LiveConnect and javascript.... this is a tad cleaner
     *
     * @param request servlet request
     * @param response servlet response
     * @param posted true if the request is a POST request, else it's a GET
     */
    protected void processRequest(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    boolean posted)
        throws ServletException, IOException
    {
        
        response.setContentType("text/html");

        List        uploadedFiles=null;
        int         fileCount = 0;
        if (posted) {
            fileCount = 0;
            try {
                DiskFileUpload  upload = new DiskFileUpload();

                uploadedFiles = upload.parseRequest(request);
                fileCount = uploadedFiles.size();
            } catch (FileUploadException e1) {
                fileCount = 0;
// means we have received the data dumped straight down the line
            }
        } else {
            fileCount = 0;
        }

        String    logData;
        String    inputMode;
        String    inputFmt;
        
        logData = null;
        inputMode = MODE_NORMAL;
        inputFmt = FMT_LOG;
        
        PrintWriter writer;
        File        httpfile=null;
        FileOutputStream    httpPageStream=null;
        
        if (fileCount > 0) {
            writer = response.getWriter();
            generateTemporaryPage = false;
        } else {
            httpfile = File.createTempFile("result", ".html", tmpDirectory);
            httpPageStream = new FileOutputStream(httpfile);
            writer = new PrintWriter(httpPageStream);
        }
        HTMLwriter http = new HTMLwriter(writer);
 
        http.html();
        http.head();
        http.title(demoMode? "Gita: bringing you the hits" : "Gita: mangling the strokes that bring you the information...");
        if (demoMode) {
            http.stylesheet("../style/demo.css");
        } else {
            http.stylesheet("../style/zone.css");
        }
        http.closetag(); // head
        http.body();
        
        if (!demoMode) {
            http.h1("Scanning log file uploads");
            http.paragraph();
        }
        
        if (fileCount > 0) {
            if (!demoMode) {
                http.printbr("uploaded file log data");
            }
            Iterator iter = uploadedFiles.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString();
                    if (!demoMode) {
                        http.printbr("Form item "+name+"=\""+value+"\"");
                    }
                    if (name.equals(PARAM_FMT)) {
                        if (value.length() > 0){
                            inputFmt = value;
                        }
                    } else if (name.equals(PARAM_MODE)) {
                        if (value.length() > 0){
                            inputMode = value;
                        }
                    }
                } else {
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    String contentType = item.getContentType();
                    boolean isInMemory = item.isInMemory();
                    long sizeInBytes = item.getSize();

                    if (!demoMode) {
                        http.printbr("Form file: "+
                            "field=\""+fieldName+"\""+
                            "file=\""+fileName+"\""+
                            "type=\""+contentType+"\""+
                            "size=\""+sizeInBytes+"\"");
                    }
                    processFile(fileName);
                    
                    if (contentType.equals("image/svg+xml")) {
                        inputFmt = FMT_SVG;
                    }

                    logData = item.getString();
                }
            }
        } else {
            if (!demoMode) {
                http.printbr("form log data");
            }
            
            Enumeration paramNames = request.getParameterNames();
            while(paramNames.hasMoreElements()) {
                String paramName = (String)paramNames.nextElement();
                String paramValue = "";
                if (!demoMode) {
                    http.print("param "+paramName);
                }
                if (paramName.equals("ink_data")) {
                    String[] paramValues = request.getParameterValues(paramName);
                    if (paramValues.length >= 1) {
                        paramValue = paramValues[0];
                        if (paramValue.length() > 0){
                            logData = paramValue;
                        }
                    }
                } else if (paramName.equals("input_fmt")) {
                    String[] paramValues = request.getParameterValues(paramName);
                    if (paramValues.length >= 1) {
                        paramValue = paramValues[0];
                        if (paramValue.length() > 0){
                            inputFmt = paramValue;
                        }
                    }
                } else if (paramName.equals("input_mode")) {
                    String[] paramValues = request.getParameterValues(paramName);
                    if (paramValues.length >= 1) {
                        paramValue = paramValues[0];
                        if (paramValue.length() > 0){
                            inputMode = paramValue;
                        }
                    }
                } else { // very strange parameter
                    ;
                }
                if (!demoMode) {
                    http.printbr("");
//               http.printbr(" value "+paramValue);
                }
           }
       }

       File[]   logList = logDirectory.listFiles();
       Pattern  logFilePattern = Pattern.compile("log-(\\d*)-(\\d*).svg");
       
       int      highestLogSeqNo = 0;
       int      newLogSeqNo = 1;
       int      logId = 1;

       if (logList == null) {
           throw new IOException(logDirectory.toString()+" is not a directory");
       }
       
       for (short i=0; i<logList.length; i++) {
           String   leaf = logList[i].getName();
           Matcher  m = logFilePattern.matcher(leaf);
           if (m.matches() && Integer.parseInt(m.group(1)) == logId) {
               int  logSeqNo = Integer.parseInt(m.group(2));
               if (logSeqNo > highestLogSeqNo) {
                   highestLogSeqNo = logSeqNo;
               }
           }
       }
      
       newLogSeqNo = highestLogSeqNo + 1;
       if (!demoMode) {
           http.printbr("Generating log "+ Integer.toString(newLogSeqNo));
       }
       
       String  currentLogName = logName(logId, newLogSeqNo);
       File    logPage = new File(logDirectory, currentLogName);
      
    /** a page of stroke data received for processing */
        PageData        page = null;
       if (logData != null && logData.length() > 0) {
           if (inputFmt.equals(FMT_SVG)) {
               try { 
                   SvgStrokeParser   logParse = new SvgStrokeParser();
                   page = logParse.parseLogData(logData, http);
               } catch (LogParseException e) {
                   http.printbr("Log parsing exception while parsing svg log data");
               }
           } else if (inputFmt.equals(FMT_LOG)) {
               try {
    // process strokes from current hit of log data
                   AnotoLogParser   logParse = new AnotoLogParser();
                   page = logParse.parseLogData(logData, http);

                   PrintWriter  file = new PrintWriter(
                                            new FileOutputStream(logPage));
                   SVGwriter    svg = new SVGwriter(file); 

                   svg.xmldecl();
// quick fix... reading the dtd causes xerce to make a connection when parsing
// ?? maybe downloading the external dtd and hanging at rmit firewall
//    ... an unnecessary niecty for us at moment
//            svg.doctype();
                    svg.svg(
                        page.bounds.right,
                        page.bounds.bottom,
                        gitaSvgXmlnsAtts);
                    svg.title(page.info);

// generate paths from immediately predecessing svg log data
                    if (highestLogSeqNo > 0) {
                        String lastLogName = logName(logId, highestLogSeqNo);
                    }

// generate paths from current page                
                    svg.g();
                    int i = 0;
                    for (Stroke s: page.strokes) {
                        PathDataAttribute    p = s.PathData();
                        svg.path("stroke_"+i,
                            svg.styleValueStr(null, s.penColor, 1),
                            p);
                        i++;
                    }

                    svg.closetag(); // g

                    svg.closetag(); // svg

                    file.close();
                } catch (FileNotFoundException e) {
                    http.printbr("File not found exception while parsing log data");
                } catch (SecurityException e) {
                    http.printbr("Security exception while parsing log data");
                } catch (LogParseException e) {
                    http.printbr("Log parsing exception");
                }
                http.embedSVG("temp", "../processed/"+currentLogName, 400, 600);
                http.printbr("");
           } else {
              http.printbr("Gita error: unknown format in uploaded data ("+inputFmt+")"); 
           }
           if (page != null) {
               try { 
                   if (demoMode) {
                        File splitPage = new File(
                                logDirectory, "demonstration.svg");
                        PrintWriter  file = new PrintWriter(
                                                new FileOutputStream(splitPage));
                        SVGwriter    svg = new SVGwriter(file);
                        
                       Demonstrator d = new Demonstrator();
                       SymbolList   symbols = d.processPageData(page);
                       d.generateInterpretedPage(http, svg, page, symbols); 
                       
                       file.close();
                       http.print("<center>");
                       http.embedSVG(
                            "temp",
                            "../processed/demonstration.svg",
                            (int)svg.width, (int)svg.height);
                       http.print("</center>");
                       
                   } else {
                       boolean hasSignatureTrainingData = false;
                        for (StrokeGroup g: page.groups) {
                            if (g.type == StrokeGroup.Type.SignatureTraining) {
                                hasSignatureTrainingData = true;
                                break;
                            }
                        }
                       page.AnalyseStrokes(http);
                       if (hasSignatureTrainingData) {
                            LinkedList<SignatureContainer>   sigs = new LinkedList();
                            for (StrokeGroup g: page.groups) {
                                switch (g.type) {
                                    case SignatureTraining: {
                                        sigs.add((SignatureContainer)g.info);
                                        break;
                                    }
                                }
                            }
                            SignaturEater  cigar = new SignaturEater();

                            cigar.buildSignatureModel(sigs);
                            if (mapSignatures) {
                               displaySignatureAnalysis(http, sigs);
                            } 
                       }

                       if (mapFiltered) {
                           displayStrokeData(http, page);
                       }

                       if (mapTransforms) {
                           displayTransformData(http, page);
                       }
                   }
                   
               } catch (FileNotFoundException e) {
                   String msg = e.getMessage();
                   http.printbr("File not found exception while parsing svg log data"+(msg!=null?msg:""));
               } catch (SecurityException e) {
                   String msg = e.getMessage();
                   http.printbr("Security exception while parsing svg log data"+(msg!=null?msg:""));
               } catch (Cow e) {
                   String msg = e.getMessage();
                   http.printbr("Analysis throws a cow"+(msg!=null?msg:""));
               }
           } else {
           }
        } else {
           if (logData != null) {
               http.printbr("No log data received");
           } else {
               http.printbr("Empty log received");
           }
        }
        http.closetag(); // body
        http.closetag(); // html
        
        
        writer.flush();
        writer.close();
        writer = null;
//        httpPageStream.flush();
//        httpPageStream.close(); 
        httpPageStream = null;
        
        if (generateTemporaryPage) {
// send name of the temporary web page we've constructed back up the line     
            response.getWriter().print("http://localhost:8084/gita/tmp/"+httpfile.getName());
        }
// do some cleanup. delete temporary web page older than 60 mins
        purgePastUseby(tmpDirectory, 60);
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response, false);
    }
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response, true);
    }
    
    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Tactical response document processor";
    }
    
    /**
     * Displays results of a signature analysis run
     *
     * @param http a HTMLwriter to write data to.
     * @param sigs LinkedList of all the signature data
     * @throws FileNotFoundException
     */
    public void displaySignatureAnalysis(HTMLwriter http, LinkedList<SignatureContainer> sigs)
        throws FileNotFoundException
    {
        if (!sigs.isEmpty()) {
           for (SignatureContainer s:sigs) {
                s.DisplayFeatures(http);
           }
/*
 * hope for the moment that we don't get sigs for different people in the same document!!
 */
           int nse = sigs.getFirst().group.size();
           http.printbr(nse + " signature components");

           File splitPage = new File(logDirectory, "signature-graphs.svg");
           PrintWriter  file = new PrintWriter(
                                    new FileOutputStream(splitPage));
           SVGwriter    svg = new SVGwriter(file); 
           svg.xmldecl();
           svg.svg(700, 1000);
           svg.title("and so on and so forth");
           svg.g();
           float    baseX = 0, baseY = 0;
           String[]stylez = new String[5];
           stylez[0] = "fill:none;stroke:#aa2222;stroke-width:1;";
           stylez[1] = "fill:none;stroke:#22aa22;stroke-width:1;";
           stylez[2] = "fill:none;stroke:#2222aa;stroke-width:1;";
           stylez[3] = "fill:none;stroke:#aaaa22;stroke-width:1;";
           stylez[4] = "fill:none;stroke:#22aaaa;stroke-width:1;";

           int stind = 0;
           baseY += 150;
           for (SignatureContainer s:sigs) {
               if (s != null && s.sigTangle != null && s.sigdTangledL != null) {
                   int np = s.sigdTangledL.length;
                   http.printbr("mapping components for uberstroke. np="+np);
                   Point[] p = new Point[np];
                   p[0] = new Point(baseX, baseY);
                   svg.g();
                   for (short j=1; j<np; j++) {
//                       p[j] = new Point(baseX + stf.normL[j]*700, baseY + 50*stf.dTheta[j]);
                       float px = baseX + s.uberSig[j].arklen*700;
                       float py = baseY + s.sigdTangledL[j]/10;

                       if (s.uberSig[j].keypoint > 0) {
                           svg.circle(px, py, 4, "#000000", null, (float)1);
                       }
                       p[j] = new Point(px, py);
                   }
                   PathDataAttribute pa = new PathDataAttribute(p);
                   svg.path("resampld_plein",
                        stylez[(stind++)%stylez.length],
                        pa);

                   svg.closetag();   //g
               }

           }
           
           baseY += 150;
           stind = 0;
           for (SignatureContainer s:sigs) {
               if (s != null && s.sigTangle != null && s.sigdTangledL != null) {
                   int np = s.nClump;
                   http.printbr("mapping components for uberstroke. np="+np);
                   Point[] p = new Point[np+1];
                   p[0] = new Point(baseX, baseY);
                   svg.g();
                   for (short j=0; j<np; j++) {
                       float px = baseX + s.clumpedArk[j]*700;
                       float py = baseY + s.clumpedTheta[j]*3;

                       p[j+1] = new Point(px, py);
                        svg.text(
                            px, 
                            py, 
                            "("+s.clumpLabel[j]+")",
                            "Verdana", (float)10, "#000000"
                            );
                       
                   }
                   PathDataAttribute pa = new PathDataAttribute(p);
                   svg.path("resampld_plein",
                        stylez[(stind++)%stylez.length],
                        pa);

                   svg.closetag();   //g
               }

           }

           for (int i=0; i<nse; i++) {
               stind = 0;
               baseY += 150;
               http.printbr("graphing signature componenent "+i);
               for (SignatureContainer s:sigs) {
                   Stroke   stroak = null;
                   if (i < s.group.strokes.size()) {
       /*
        * this is so wrong... this should generate an exception for short lists, but it 
        * doesnt aaaaaaaaaaaaaarg. so much for garbage collection 
        *   ... not the first or last time either!
        */
                        stroak = s.group.strokes.get(i);
                   }
                   if (stroak != null) {
                       SignatureStroke stf = (SignatureStroke)stroak.info;
                       if (stf != null && stf.tangle != null && stf.dTangledL != null) {
                           int np = stf.dTangledL.length;
                           http.printbr("mapping component for this stroke. np="+np);
                           Point[] p = new Point[np];
                           p[0] = new Point(baseX, baseY);

                           for (short j=1; j<np; j++) {
        //                                               p[j] = new Point(baseX + stf.normL[j]*700, baseY + 50*stf.dTheta[j]);
                               p[j] = new Point(baseX + stf.arcLen[j]*700, baseY + stf.dTangledL[j]/3);
                           }
                           PathDataAttribute pa = new PathDataAttribute(p);
                           svg.path("resampld_plein",
                                stylez[(stind++)%stylez.length],
                                pa);
                       }
                   } else {
                       http.printbr("null stroke");
                   }
               }
           }
            svg.closetag(); // g

            svg.closetag(); // svg

            file.close();
            http.printbr("");
            http.printbr("");
            http.printbr("<B>Graphed signature analysis</B>");
            http.printbr("");
            http.embedSVG(
                "temp",
                "../processed/signature-graphs.svg",
                700, 1000);

        /*
        * draw signature bits
        */
           splitPage = new File(logDirectory, "signature-pix.svg");
           file = new PrintWriter(new FileOutputStream(splitPage));
           svg = new SVGwriter(file); 
           svg.xmldecl();
           svg.svg(700, 1000);
           svg.title("and so on and so forth");
            svg.g();
           baseX = 0;
           baseY = 0;
           for (SignatureContainer s:sigs) {
               stind = 0;
               baseY += 150;
               int np = s.nSigSample;
               Point[]  p = new Point[np];
               for (int i=0; i<np; i++) {
                   p[i] = new Point(s.uberSig[i]);
               }
               PathDataAttribute pa = new PathDataAttribute(p);
               svg.path("resampld_plein",
                                "fill:none;stroke:#446688;stroke-width:2;",
                                pa);

               for (int i=0; i<s.group.strokes.size(); i++) {
                   stind++;
                   http.printbr("graphing signature componenent "+i);
                   Stroke   stroak = null;
                   stroak = s.group.strokes.get(i);
                   if (stroak != null && stroak.info.getClass() == SignatureStroke.class) {
                       SignatureStroke stf = (SignatureStroke) stroak.info;
                       if (stf != null && stf.lfData != null) {
                           np = stf.lfData.length;
                           http.printbr("mapping component for this stroke. np="+np);

                           pa = new PathDataAttribute(stf.lfData);
                           svg.path("resampld_plein",
                                stylez[(stind++)%stylez.length],
                                pa);
                           SVGpointNumbers(svg, stf.lfData);
                       }
                   } else {
                       http.printbr("null stroke");
                   }
               }
           }
            svg.closetag(); // g

            svg.closetag(); // svg

            file.close();
            http.printbr("");
            http.printbr("");
            http.printbr("<B>Signature pix</B>");
            http.printbr("");
            http.embedSVG(
                "temp",
                "../processed/signature-pix.svg",
                700, 1000);
        }
    }
    
    /**
     * Displays results of a regular stroke analysis run
     *
     * @param http a HTMLwriter to write data to.
     * @param page the input page to display
     * @throws FileNotFoundException
     */
    public void displayStrokeData(HTMLwriter http, PageData page)
        throws FileNotFoundException
    {
        File splitPage = new File(
                logDirectory, "filtered-overlayed.svg");
        PrintWriter  file = new PrintWriter(
                                new FileOutputStream(splitPage));
        SVGwriter    svg = new SVGwriter(file); 
        svg.xmldecl();
        svg.svg(
            page.bounds.right,
            page.bounds.bottom);
        svg.title(page.info);
        svg.g();
        int i=0;
        for (Stroke s: page.strokes) {
            if (s.info != null) {
                if (s.info.getClass() == StrokeAlysis.class) {
                    StrokeAlysis        sally = (StrokeAlysis) s.info;
                    if (sally.lengthendStroke != null) {
                        http.printbr("resampled length "+ sally.lengthendStroke.length);
                        PathDataAttribute    p = s.PathData();
                        svg.path("stroke_"+i,
                            "fill:none;stroke:#338888;stroke-width:3;", p);
                        p = Stroke.PathData(
                                sally.lengthendStroke.length,
                                sally.lengthendStroke
                            );
                        svg.path("filtered_"+i,
                            "fill:none;stroke:#aa2222;stroke-width:1;", p);
                        SVGpointNumbers(svg, sally.lengthendStroke);
                    }
                    if (drawResampledComparison && sally.equalisedStroke != null) {
                        Point   osp[] = null;
                        osp = Stroke.OffsetScaledSamples(
                                s.sample.length,
                                s.sample,
                                s.sample[0].x, s.sample[0].y+30,
                                1, 1);
                        svg.path("org_plein"+i,
                                "fill:none;stroke:#aa2222;stroke-width:1;",
                                new PathDataAttribute(osp));
                        osp = Stroke.OffsetScaledSamples(
                                sally.equalisedStroke.length,
                                sally.equalisedStroke,
                                s.sample[0].x,
                                s.sample[0].y+70,
                                1, 1);
                        svg.path("resampld_plein"+i,
                               "fill:none;stroke:#aa2222;stroke-width:1;",
                                new PathDataAttribute(osp));
                    }
                }  else if (s.info.getClass() == VanillaStrokeInfo.class) {
                    VanillaStrokeInfo   sinf = (VanillaStrokeInfo)s.info;
                    PathDataAttribute    p = new PathDataAttribute(sinf.lengthFilteredStroke);
                    svg.path("stroke_"+i, "fill:none;stroke:#338888;stroke-width:3;", p);
                    p = new PathDataAttribute(sinf.artifactFilteredStroke);
                    svg.path("stroke_"+i, "fill:none;stroke:#883333;stroke-width:1;", p);
                    SVGpointNumbers(svg, sinf.artifactFilteredStroke);
                    sinf.DisplayFeatures(http);
                } else {
                    s.info.DisplayFeatures(http);
                }

                i++;
            }
        }

        svg.closetag(); // g

        svg.closetag(); // svg

        file.close();
        http.printbr("");
        http.printbr("");
        http.printbr("<B>Filtered ink data results</B>");
        http.printbr("");
        http.embedSVG(
            "temp",
            "../processed/filtered-overlayed.svg",
            (int)svg.width, (int)svg.height);
    }
    

    /**
     * Displays graphical transform data from a regular stroke analysis run
     *
     * @param http a HTMLwriter to write data to.
     * @param page the input page to display
     * @throws FileNotFoundException
     */
    public void displayTransformData(HTMLwriter http, PageData page)
        throws FileNotFoundException
    {
        int     i = 0;
        for (Stroke s: page.strokes) {
           if (s.info != null && s.info.getClass() == StrokeAlysis.class) {
               StrokeAlysis    sally = (StrokeAlysis) s.info;
               File splitPage = new File(
                        logDirectory, "transform-graph-"+i+".svg");
               PrintWriter  file = new PrintWriter(
                                        new FileOutputStream(splitPage));
               SVGwriter    svg = new SVGwriter(file); 
               svg.xmldecl();
               svg.svg(
                    page.bounds.right,
                    400);
               svg.title("transforms graphed");
               svg.g();

               float scaleW=2;
               if (sally.equalisedTheta != null) {
                   scaleW = 2*page.bounds.right/sally.equalisedTheta.length;
               } else if (sally.straightendTheta != null) {
                   scaleW = 2*page.bounds.right/sally.straightendTheta.length;
               } else if (sally.rawishTheta != null) {
                   scaleW = page.bounds.right/sally.rawishTheta.length;
               }
               http.printbr("");
               http.printbr("<B>stroke "+i+"</B>");

               float    graphY = 0;

               if (sally.rawishTheta != null) {
                   SVGraphit(svg, "rawish theta", sally.rawishTheta, "#772277", 2, scaleW, 30, 0, graphY += graphIte);
               }
               if (sally.straightendTheta != null) {
                   SVGraphit(svg, "straightened theta", sally.straightendTheta, "#227777", 2, scaleW, 30, 0, graphY += graphIte);
               }
               if (sally.equalisedTheta != null) {
                   SVGraphit(svg, "equalised theta", sally.equalisedTheta, "#222277", 2, scaleW, 30, 0, graphY += graphIte);
               }
               if (displayTransformGraphs) {
                   SVGraphit(svg, "straightened real" , sally.straightendSignatureRe, "#227722", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "straightened im", sally.straightendSignatureIm, "#772222", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "straightened magnitude" , sally.straightendSignatureMg, "#227722", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "straightened phase", sally.straightendSignaturePh, "#772222", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "equalised real" , sally.equalisedSignatureRe, "#227722", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "equalised im", sally.equalisedSignatureIm, "#772222", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "equalised magnitude" , sally.equalisedSignatureMg, "#227722", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphit(svg, "equalised phase", sally.equalisedSignaturePh, "#772222", 2, scaleW, 30, 0, graphY += graphIte);
               }
               if (sally.straightenDthetaDl != null) {
                   float dl[] = new float[sally.straightenDl.length];
                   dl[0] = 0;
                   for (short k=1; k<dl.length; k++) {
                       dl[k] = dl[k-1]+sally.straightenDl[k];
                   }

                   SVGraphit(svg, "straightend dTheta/dL", sally.straightenDthetaDl, "#222277", 2, scaleW, 30, 0, graphY += graphIte);
                   SVGraphitXy(svg, "straightend dTheta/dL(L)", dl, sally.straightenDthetaDl, "#222277", 2, page.bounds.right, 30, 0, graphY += graphIte);
               }
               if (sally.straightenD2thetaDl2 != null) {
                   SVGraphit(svg, "straightend d2Theta/dL2", sally.straightenD2thetaDl2, "#222277", 2, scaleW, 30, 0, graphY += graphIte);
               }

               if (s.sample != null) {
                   Point        p[] = 
                            Stroke.OffsetScaledSamples(
                                s.sample.length,
                                s.sample,
                                0, 0, 3, 3);
                  SVGpathit(
                           svg,
                           "raw_stroke_"+i, p,
                           new Point(
                                30,
                                (graphY += graphIte)), "#aa2222", 3,
                          null, 1);
               }

               if (sally.lengthendStroke != null) {
                   float    td[] = new float[sally.lengthendTheta.length];
                   for (short j=0; j<td.length; j++) {
                       td[j] = (float)Math.round(sally.lengthenDthetaDl[j]*180.0/Math.PI);
                   }
                   Point    p[] =
                           Stroke.OffsetScaledSamples(
                                sally.lengthendStroke.length,
                                sally.lengthendStroke,
                                0, 0, 3, 3);
                   SVGpathit(
                           svg,
                           "lengthend_stroke_"+i, p, 
                           new Point(
                                30,
                                (graphY += graphIte)), "#aa2222", 3, 
                           td, 1);
               }

               if (sally.straightendStroke != null) {
                   float    td[] = new float[sally.straightendTheta.length];
                   for (short j=0; j<td.length; j++) {
                       td[j] = (float)Math.round(sally.straightenDthetaDl[j]*180.0/Math.PI);
                   }
                   Point    p[] =
                            Stroke.OffsetScaledSamples(
                                sally.straightendStroke.length,
                                sally.straightendStroke,
                                0, 0, 3, 3);
                   SVGpathit(
                           svg,
                           "straight_stroke_"+i, p,
                           new Point(
                                30,
                                (graphY += graphIte)), "#aa2222", 3, 
                           td, 1);
               }

               if (sally.equalisedStroke != null) {
                   Point    p[] =
                           Stroke.OffsetScaledSamples(
                                sally.equalisedStroke.length,
                                sally.equalisedStroke,
                                0, 0, 3, 3);
                   SVGpathit(
                           svg,
                           "eq_stroke_"+i, p,
                           new Point(
                                30,
                                (graphY += graphIte)), "#aa2222", 3, 
                           null, 1);
               }


               svg.closetag(); // g

               svg.closetag(); // svg

               file.close();
               http.embedSVG(
                    "temp",
                    "../processed/transform-graph-"+i+".svg",
                    (int)page.bounds.right*2, (int)800);
            }
            i++;
        }
    }

    /**
     * Displays graphical transform data from a regular stroke analysis run
     *
     * @param svg a SVGwriter to write data to.
     * @param label string label for the svg path id
     * @param is an array of Point data for the path
     * @param os base of the path, Points in is will be offset by (os-is[0])
     * @param color String color to use for the path, in the form #RRGGBB
     * @param fontSize  font size to use for tags
     * @param floatTags numeric values to use as text labels for each point
     * @param tagOff offset for labels into the floatTags array
     * @throws FileNotFoundException
     */
    public void SVGpathit(
            SVGwriter svg,
            String label,
            Point is[],
            Point os, String color, float fontSize,
            float floatTags[], int tagOff)
    {
        Point[]          pts = new Point[is.length];

        for (int j=0; j<pts.length; j++) {
           pts[j] = new Point(
                        is[j].x+os.x,
                        is[j].y+os.y);
        }
        svg.path(label,
                "fill:none;stroke:"+color+";stroke-width:2;",
                new PathDataAttribute(pts));
        if (floatTags != null)
            for (int j=0; j<pts.length; j++) {
                if (j+tagOff < floatTags.length) {
                    svg.text(
                        pts[j].x, 
                        pts[j].y, 
                        Float.toString(floatTags[j+tagOff]),
                        "Verdana", (float)fontSize, "#000000"
                     );
                }
            }

    }
    
    /**
     * Adds numeric text labels to the points in the given ink sample array
     *
     * @param svg SVGwriter to which to generate output
     * @param p array of ink samples to annotate
     */
    public void SVGpointNumbers(
            SVGwriter svg,
            InkSample[] p)
    {
        for (int j=0; j<p.length; j++) {
            svg.text(
                p[j].x, 
                p[j].y, 
                Integer.toString(j),
                "Verdana", (float)1.5, "#000000"
             );
        }
    }
    
    /**
     * Generates a graph from floating point y values
     *
     * @param svg SVGwriter to which to generate output
     * @param label name to use as the id attribute of the path data generated
     * @param d array of floating point data
     * @param color String color to use for the path, in the form #RRGGBB
     * @param lineWidth line width to use for the path data
     * @param xScale scale factor for the path in the x direction
     * @param yScale scale factor for the path in the x direction
     * @param offX x co-ord to which to offset the path data for the graph
     * @param offY y co-ord to which to offset the path data for the graph
     */
    public void SVGraphit(
            SVGwriter svg,
            String label,
            float d[],
            String color,
            float lineWidth,
            float xScale,
            float yScale,
            float offX,
            float offY)
    {
        float   max = 0;
        for (short i=0; i<d.length; i++) {
            if (Math.abs(d[i]) > max) {
                max = Math.abs(d[i]);
            }
        }
        Point[] p = new Point[d.length];
        for (short i=0; i<d.length; i++) {
            p[i] = new Point(xScale*i+offX, yScale*(d[i]/max)+offY);
        }
        
        Point [] q = new Point[2];
        q[0] = new Point(offX, offY);
        q[1] = new Point(offX+d.length*xScale, offY);
        
        svg.path(
            label,
            "fill:none;stroke:"+color+";stroke-width:"+lineWidth+";",
            new PathDataAttribute(p));
        svg.text(
            p[0].x, 
            p[0].y, 
            label + "("+d.length+" points)",
            "Verdana", (float)10, "#000000"
            );
        svg.path(
            label+" axis",
            "fill:none;stroke:#000000;stroke-width:1;",
            new PathDataAttribute(q));
    }

    /**
     * Generates a graph in SVG from floating point (x,y) values
     *
     * @param svg SVGwriter to which to generate output
     * @param label name to use as the id attribute of the path data generated
     * @param dx array of floating point x co-ordinate data
     * @param dy array of floating point y co-ordinate data
     * @param color String color to use for the path, in the form #RRGGBB
     * @param lineWidth line width to use for the path data
     * @param xScale scale factor for the path in the x direction
     * @param yScale scale factor for the path in the x direction
     * @param offX x co-ord to which to offset the path data for the graph
     * @param offY y co-ord to which to offset the path data for the graph
     */
    public void SVGraphitXy(
            SVGwriter svg,
            String label,
            float dx[],
            float dy[],
            String color,
            float lineWidth,
            float xScale,
            float yScale,
            float offX,
            float offY)
    {
        float   maxy = 0, maxx=0;
        for (short i=0; i<dy.length; i++) {
            if (Math.abs(dy[i]) > maxy) {
                maxy = Math.abs(dy[i]);
            }
        }
        for (short i=0; i<dx.length; i++) {
            if (Math.abs(dx[i]) > maxx) {
                maxx = Math.abs(dx[i]);
            }
        }
        Point[] p = new Point[dx.length];
        for (short i=0; i<dx.length; i++) {
            p[i] = new Point(xScale*(dx[i]/maxx)+offX, yScale*(dy[i]/maxy)+offY);
        }
        
        Point [] q = new Point[2];
        q[0] = new Point(offX, offY);
        q[1] = new Point(offX+dx.length*xScale, offY);
        
        svg.path(
            label,
            "fill:none;stroke:"+color+";stroke-width:"+lineWidth+";",
            new PathDataAttribute(p));
        svg.text(
            p[0].x, 
            p[0].y, 
            label + "("+dx.length+" points)",
            "Verdana", (float)10, "#000000"
            );
        svg.path(
            label+" axis",
            "fill:none;stroke:#000000;stroke-width:1;",
            new PathDataAttribute(q));
    }
    
    /**
     * Attributes that are used to specify the gita name space, in particular for an
     * SVG document. Currently this has two entrries, one for the namespace "gita" ("xmlns:gita" attribute)
     * and one for the namespace "f" ("xmlns:f" attribute). Typically these are used in the
     * opening "svg" tag of an svg document
     */
    public static final Attribute   gitaSvgXmlnsAtts[] = {
        new Attribute("xmlns:gita", "http://ns.gita.org/gita-ns-1.0"),
        new Attribute("xmlns:f", "http://ns.gita.org/feature-ns-1.0")
    };
        
    /** String name for mode parameter, passed by web browser (either applet or form)*/
    public static final String PARAM_MODE = "input_mode";
    /** String value for mode parameter, for normal stroke data passed to gita*/
    public static final String MODE_NORMAL = "normal";
    /** String value for mode parameter, for internal control stroke data passed to gita*/
    public static final String MODE_CONTROL = "control";
    /** String value for mode parameter, for gita to clear current accumulated logs*/
    public static final String MODE_CLEAR = "clear";
    
    /** String name for format parameter, passed by web browser (either applet or form)*/
    public static final String PARAM_FMT = "input_fmt";
    /** String value for format parameter, for unspecified format data passed to gita */
    public static final String FMT_CRAP = "crap";
    /** String value for format parameter, for anoto log data passed to gita*/
    public static final String FMT_LOG = "log";
    /** String value for format parameter, for svg data passed to gita*/
    public static final String FMT_SVG = "svg";
    
    /** global control variable, true if we want to display the graphic results of contour filtering and resampling */
    boolean mapFiltered = true;
    /** global control variable, true if we want to display any results of fourier transforms */
    boolean mapTransforms = false;
    /** global control variable, true if we want to display a comparison between raw and resampled paths */
    boolean drawResampledComparison = false;
    /** global control variable, true if we want to display results of signature processing */
    boolean mapSignatures = true;
    /** global control variable, true if we want to display the graphic results of transforms of contour data */
    boolean  displayTransformGraphs = false;
    
    /** puts gita into demo mode... runs with minimal diagnositics, a different style sheet, and processes through the Demonstrator class */
    boolean demoMode = false;
    
    /** forces gita to display html via the generation of temporary pages on a server rather than
     * by the direct transmission of html data. This allows the client browser interface to display
     * gita's results in different browser frames to that which made the call to the browser.
     * This, in turn, allows gita's results to be viewed without reloading the Scrounger applet. */
    boolean     generateTemporaryPage=true;
    
    /** directory used for storing generated files and svg pages */
    File            logDirectory = null;
    /** directory used for temporarily generated html pages */
    File            tmpDirectory = null;
    
    /** display height of a transform graph*/
    float    graphIte = 60;
}
