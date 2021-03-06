package client;
/*
 * scrounger.java
 *
 * Created on 20 November 2003, 07:38
 */
 
import java.awt.event.*; 
import java.applet.*;
import java.io.*;
import java.awt.*;
import java.security.*;
import java.net.*;
import gita.Gita;


/**
 * A client side scrounger applet for anoto log files.
 *<p>
 * Scrounger checks the temporary directory for anoto log files, waiting in a timed loop for
 * a log file to appear. Upon finding one, it spits it to the main gita servlet (which will
 * probably be viewed in a different frame of the browser. 
 *<p>
 * The scrounging activity is initiated by a mouse click anywhere in the applet <i>Panel</i> area. Other
 * information sent along with the servlet request and log data are a few query string parameters
 * <ul>
 * <li>mode selection information for gita, <i>"input_mode"</i>
 * <li>data format information in <i>"input_fmt"</i>
 * </ul>
 *<p>
 * For this applet to work, the client side (the pen side) needs promiscuous permissions set for whatever the
 * anoto temporary directory is.
 *<p>
 * The applet also has a set of radio buttons for sending additional contextual parameters along with
 * the found log data. Ideally it would be funkier to do this with something like mouse buttons on the
 * pen... perhaps a couple of different transmit buttons rather than the silly anoto checkbox. MODE_NORMAL is
 * what it implies, MODE_CONTROL is intended for sending streams that would be used for system control, rather than
 * for application access, MODE_CLEAR is intended to do no processing, but to clear gita's logs, caches and
 * temporary directories ... 
 *<p>
 * Nowadays, it generally sends its doo via a http POST, as the log files will
 * quickly get too big for a GET. This behaviour is controlled by the <i>callServerByPost</i> boolean
 * class variable. The complication is in getting the page generated to show in a particular frame. For
 * a GET request, we can simply call up the servlet directly, and specify its output frame with
 * showDocument. For a POST request, we open a <i>URLconnection</i> to the gita servlet, send the POST
 * data, and wait for gita to reply with the url of a temporary page generated on the server ... which
 * we then display in a particular frame using showDocument.
 *<p>
 * In the distribution, it needs to be in a servable web directory. build/WEB-INF and descendants are
 * strictly server side ... and so it should be. The current pages that use this applet (scroungelog.html)
 * expects it to be in $(DOCUMENT_BASE)/classes/client. Also because it is now in a package (client) with the main
 * code of gita, it needs to be refered to via the package, and be in the right spot for being found as
 * part of this package. e.g. the following applet tag works for a Scrounger applet in the
 * $(DOCUMENT_BASE)/classes/client directory....
 *   &lt;applet code="client.Scrounger" codebase="classes" height="30" width="640"&gt;
 *
 * @author David Karla
 * @since you asked
 */
public class Scrounger extends Applet implements Runnable, MouseListener
{
    Checkbox normalModeBox; 
    Checkbox clearModeBox; 
    Checkbox controlModeBox; 
    CheckboxGroup modeRadioGroup;
    
    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
     public void init()
     {
        String  s;
        
        myFont = new Font("TimesRoman", Font.BOLD, 12);

        s = getParameter("anotolog");
	anotoLogName = (s != null)? s : "scrawled.txt";
        
        s = getParameter("anototemp");
        anotemp = new File((s!=null)? s: "D:\\dak/anotemp");
        
        s = getParameter("gitaURL");
// url for tomcat 4, nb 3.5.1
//        gitaURL = (s!=null)?s:"http://localhost:8081/servlet/gita";

// use for tomcat 5, nb3.6 beta
        gitaURL = (s!=null)?s:"http://localhost:8084/gita/servlet/gita";
        
        s = getParameter("logViewFrame");
        logViewFrameName = (s!=null)?s:"upper";
        
        browser = getAppletContext();
        modeRadioGroup = new CheckboxGroup();
        normalModeBox = new Checkbox("Normal", modeRadioGroup,true); 
        clearModeBox = new Checkbox("Clear", modeRadioGroup,false); 
        controlModeBox = new Checkbox("Control", modeRadioGroup,false); 
        normalModeBox.setBounds(220,0,100,10); 
        clearModeBox.setBounds(340,0,100,10); 
        controlModeBox.setBounds(460,0,100,10); 
        add(normalModeBox); 
        add(clearModeBox); 
        add(controlModeBox); 
  
        newLog = false;
        searchSuspended = true;
        inputMode = Gita.MODE_NORMAL;
        
        resize(100, 100);
        addMouseListener(this);
    }
    
     /**
      * Called at the close of business to clean things up.
      */
    public void destroy()
    {
        removeMouseListener(this);
    }
    
    /**
     * Do the business end of this applet.
     *
     * @param log a file of anoto log poop.
     */
    public boolean processLog(File log)
    {
        URL             url;
        String          logData;
        FileInputStream infile;
        byte            cbuf[];
        final int       cbuf_len=128;
        cbuf = new byte[cbuf_len];
        
        newLog = true;
        try {
            String logpath = log.getPath();
            infile = new FileInputStream(log);
            showStatus("... processing log "+logpath);
            
            if (normalModeBox.getState()) {
                inputMode = Gita.MODE_NORMAL;
            } else if (clearModeBox.getState()) {
                inputMode = Gita.MODE_CLEAR;
            } else if (controlModeBox.getState()) {
                inputMode = Gita.MODE_CONTROL;
            }
            
            logData = "";
            int     n_c_read = 0;
            int     tot_c_read = 0;
            while ((n_c_read=infile.read(cbuf, 0, cbuf_len)) > 0) {
                tot_c_read += n_c_read;
                logData += URLEncoder.encode(new String(cbuf, 0, n_c_read) /*, "UTF-8"*/);
            }
            infile.close();

            encLogLength = logData.length();
            rawLogLength = tot_c_read;
            
            showStatus("... read log "+logpath);
            if (callServerByPost) {
                showStatus("... post log "+logpath+" to "+gitaURL);
                
                String processorURL = gitaURL;
                try {
                    url = new URL(processorURL);
                    URLConnection connection = url.openConnection();
// Make sure browser doesn't cache this URL. ... and tell it to allow input and output
// POST requests are required to have Content-Length Netscape sets the Content-Type to multipart/form-data
// by default. So, if you want to send regular form data, you need to set it to
// application/x-www-form-urlencoded, which is the default for Internet Explorer. If you send
// serialized POST data with an ObjectOutputStream, the Content-Type is irrelevant, so you could
// omit this step.
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    String postData =
                        "ink_data="+logData+
                        "&"+
                        Gita.PARAM_FMT+"="+Gita.FMT_LOG+
                        "&"+
                        "input_mode="+inputMode;
                    String lengthString = String.valueOf(postData.length());

                    connection.setRequestProperty
                        ("Content-Length", lengthString);
                    connection.setRequestProperty
                        ("Content-Type", "application/x-www-form-urlencoded");

// open an output connection. must be done after SetRequestProperty, else
// we get an illegal state error...
// print must be flushed for a print writer, but not println
                    PrintWriter out =
                            new PrintWriter(connection.getOutputStream());
                    out.print(postData);
                    out.flush();
                    
                    BufferedReader in =
                        new BufferedReader(new InputStreamReader
                                             (connection.getInputStream()));
                    String line;
                    int cnt = 0;

                    URL ur;

                    try {
                        ur = new URL(in.readLine());
                        browser.showDocument(
                            ur,
                            logViewFrameName);
                    } catch (AccessControlException ioe){
                        errorMsg = "page access .... "+ioe.getMessage();
                        return false;
                    } 

                    showStatus("displayed "+ ur.toString()+
                                " to frame "+logViewFrameName);
                  
                } catch (AccessControlException ioe){
                    errorMsg = "page AccessControlException "+ioe;
                    return false;
                } catch(IOException ioe) {
                    errorMsg = "post IOException: " + ioe;
                    return false;
                } catch(IllegalStateException ioe) {
                    errorMsg = "IllegalStateException: " + ioe;
                    return false;
                }

//                catch (MalformedURLException mfe) {
//                    showStatus("URL "+processorURL+" not well formed");
//                    return false;
//                }
            } else {
                String processorURL = gitaURL+"?ink_data="+logData+
                        "&"+
                        Gita.PARAM_FMT+"="+Gita.FMT_LOG+
                        "&input_mode="+inputMode;
                try {
                    url = new URL(processorURL);
                    browser.showDocument(url, logViewFrameName);
//                  showStatus(logViewFrameName);
                } catch (MalformedURLException mfe) {
                    showStatus("URL "+processorURL+" not well formed");
                    return false;
                }
            }
        } catch (FileNotFoundException fnfe) {
            errorMsg = "File "+log.getName()+": not found exception";
            return false;
        } catch (IOException ioe) {
            errorMsg = "IO exception";
            return false;
        }
//        showStatus("processed happily");
        return true;
    }
    
    /**
     * Hard working main thread. Yeah right.
     */
    public void run()
    {
	Thread  me = Thread.currentThread();
	boolean needsRepaint = false;
        int     delay;
        File    tempListing[];

        showStatus("rstrt..");
        repaint();

        rawLogLength = 0;
        encLogLength = 0;
        while (scrounger == me) {
            delay = repaintDelay;
  //        errorMsg = null;
  //          showStatus("running..");
            if (!searchSuspended) {
                try {
                    tempListing = anotemp.listFiles();
                    File    latestLog;
//                    found = "";
                    for (short i=0; i<tempListing.length; i++) {
//                        found += ":"+tempListing[i].getName();
                        if (anotoLogName.equals(tempListing[i].getName())) {
                            showStatus(errorMsg == null?
                                "processing: ":
                                "processing "+errorMsg+":");
// bad hack to avoid synchronisation problems without having to go to nio...
// file locking etc
                            if ((System.currentTimeMillis() - tempListing[i].lastModified())
                                        < fileLockPause) {
                                showStatus(errorMsg == null?
                                    "pausing: ":
                                    "pausing "+errorMsg+":");
                                try {
                                    Thread.sleep(fileLockPause);
                                } catch (InterruptedException e){}
                            }
                            showStatus(errorMsg == null?
                                "proceeding: ":
                                "proceeding "+errorMsg+":");
                            if (processLog(tempListing[i])) {
                                errorMsg = null;
                                if (!tempListing[i].delete()) {
                                    errorMsg = "couldn't delete";
                                }
                           } else {
                                searchSuspended = true;
                                showStatus("process log fails: "+errorMsg);
                            }
                            newLog = false;
                        }
                    }
    //                nFound = tempListing.length;

                    tempListing = null;
                } catch (AccessControlException e){
                    showStatus("Access Control Stuffup: "+e.getMessage());
                }
            } 
            repaint();

//          if (newLog) {
//              showStatus("Your logfile in lights");
//          } else {
//              showStatus("");
//          }
//          newLog = false;

//            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e){}
	}
    }
    
    /**
     * Draw all the bits in the Applet panel
     * 
     * @param g the graphics context to doodle into
     */
    public void paint(Graphics g)
    {
	g.setFont(myFont);
        if (searchSuspended) {
            g.drawString("not collecting wooden nickels ...", 10, 10);
        } else if (errorMsg != null) {
            g.drawString(errorMsg, 10, 10);
        } else {
            if (newLog) {
                g.drawString("newLog set... ", 10, 10);
            } else {
                g.drawString("log data length found "+Integer.toString(rawLogLength)+"/"+Integer.toString(encLogLength), 10, 10);
            }
        }
    }

    /**
     * Start the main thread
     */
    public void start()
    {
	scrounger = new Thread(this);
	scrounger.start();
    }
    
    /**
     * Stop the main thread
     */
    public void stop()
    {
	scrounger = null;
    }
    
    /**
     * Return yet another useless piece of information
     */
    public String getAppletInfo()
    {
        return "Look for logfiles and send them to the write place.";
    }

    /**
     * Old skool event handling
     *
     * @param e the MouseEvent info of what just transpired
     * @see MouseEvent
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * Old skool event handling
     *
     * @param e the MouseEvent info of what just transpired
     * @see MouseEvent
     */
    public void mousePressed(MouseEvent e)
    {
        e.consume();
        searchSuspended = !searchSuspended;
        if (!searchSuspended) {
            notify();
        }
    }

    /**
     * Old skool event handling
     *
     * @param e the MouseEvent info of what just transpired
     * @see MouseEvent
     */
    public void mouseReleased(MouseEvent e)
    {
        e.consume();
    }

    /**
     * Old skool event handling
     *
     * @param e the MouseEvent info of what just transpired
     * @see MouseEvent
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * Old skool event handling
     *
     * @param e the MouseEvent info of what just transpired
     * @see MouseEvent
     */
    public void mouseExited(MouseEvent e)
    {
    }
    
    /** full pathname on the local hard drive for the path to the anoto log dump */
    String              anotoLogName;
    /** browser frame name for displaying results received from gita */
    String              logViewFrameName;
    /** url to access the main gita servlet */
    String              gitaURL;
    
    /** display font for the applet */
    Font                myFont;
    /** main thread for the applet */
    Thread              scrounger;
    /** boolean enabling and disabling the search and transmit of anoto logs to the engine */
    boolean             searchSuspended;
    /** stock standard repaint delay for applet display */
    int                 repaintDelay = 50;
    /** crucial hack parameter to prevent simultaneous access of log to anoto pen
     * in absence of decent file locking between java and xp. value in milli seconds of minimum 
     * time between now and last modified time of the log */
    int                 fileLockPause = 3000;
    /** file used for reading the anoto log data */
    File                anotemp;
    /** number of bytes of log data read from local file system */
    int                 rawLogLength;
    /** length of the found log data when URLencoded */
    int                 encLogLength;
    /** error message to be displayed at the status bar, at the next available opportunity */
    String              errorMsg;
    /** true for the period between the discovery of an anoto log file, and being ready for the next one */
    boolean             newLog;
    /** home, sweet, home ... yeah right ... this is where we're called from, anyway */
    AppletContext       browser;
    /** true if we are calling the gita server by post, rather than get. This is necessary for the transmission of any data of non-trivial size */
    boolean             callServerByPost=true;
    /** the operational mode, set according to selection of the radio buttons on the applet panel, and transmitted along with the log as a http parameter */
    String              inputMode;
}
