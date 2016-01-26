/*
 * StrokeAlysis.java
 *
 * Created on 14 July 2004, 03:02
 * 
 */

package gita;
 
import zone.Rectangle;
import zone.HTMLwriter;
import zone.Point;
import java.lang.Math;
import java.lang.Float;
import java.util.LinkedList;

/**
 * Main class for basic stroke analysis.
 * It holds bits for just about every analysis we might do to extract features and attempt
 * classification of a generic stroke. typically we'd be a bit less exhaustive
 * than this. other similar classes exist for less excruciating and more specific analysis
 * e.g. for signature data
 *
 * @see Stroke
 * @see StrokeInfo
 * @author dak
 * @since you asked
 */
public class StrokeAlysis extends StrokeInfo
{
    /**
     * Creates a new instance of StrokeAlysis
     *
     * @param s Stroke to analyse. This is a reference, and not a copy.
     */
    public StrokeAlysis(Stroke s)
    {
        super(s);
    }
    

    
/////////////////////////////////////////
// feature calculation routines
////////////////////////////////////////
    
    /**
     * calculates the vector of tangent angles necessary for a (theta,dl) representation of the stroke
     * results are stored in freshly allocated members in this structure
     *
     * @param d an array of InkSample data that constitutes a stroke
     * @param n the real length of <i>d</i> which may or not be the same as <i>d.length</i>
     * @throws Cow
     */
    public void CalculateTheta(int n, InkSample d[]) throws Cow
    {
        if (n <= 0) {
            throw new Cow("StrokeAlysis::CalculateFeature(): zero samples in input");
        }
        dyDx = new float[n];
        d2yDx2 = new float[n];
        tanThetaDiff = new float[n];
        theta = new float[n];
        cumTheta = new float[n];
        cumModTheta = new float[n];
        cumTheta2 = new float[n];
        cumTheta2deg = new float[n];
        
        if (n > 2) {
            d2yDx2[0] = Float.NaN;
            d2yDx2[1] = Float.NaN;
        }
            
        dyDx[0] = Float.NaN;

        tanThetaDiff[0] = Float.NaN;
        theta[0] = Float.NaN;
        cumTheta[0] = cumModTheta[0] = cumTheta2[0] = cumTheta2deg[0] = 0;
        if (n <= 1) {   // should return something
            throw new Cow("no angles");
        }
        tanThetaDiff[1] = Float.NaN;
        theta[1] = Float.NaN;
        cumTheta[1] = cumModTheta[1] = cumTheta2[1] = cumTheta2deg[1] = 0;

        float   lX = d[0].x;
        float   lY = d[0].y;
        float   ldX = 0;
        float   ldY = 0;
        
        boolean closedPath = false;
        boolean forceClosed = false;
        
        if (forceClosed || d[n-1].equals(stroke.sample[0])) {
            closedPath = true;

            ldX = d[n-1].x - d[n-2].x;
            ldY = d[n-1].y - d[n-2].y;
        }
        
        for (int i=1; i<n; i++) {
            float   x = d[i].x;
            float   y = d[i].y;
            float   dX = x - lX;
            float   dY = y - lY;

            if (dX == 0) {
                if (dY == 0) {
                    dyDx[i] = 0; //dyDx[i-1];
                    d2yDx2[i] = 0; //d2yDx2[i-1];
                    tanThetaDiff[i] = 0;
                    theta[i] = 0;
                } else {
                    if (y>lY) {
                        dyDx[i] = Float.POSITIVE_INFINITY;
                    } else {
                        dyDx[i] = Float.NEGATIVE_INFINITY;
                    }
                    if (dyDx[i] == dyDx[i-1]) {
                        d2yDx2[i] = 0; //d2yDx2[i-1];
                    } else if (dyDx[i] > dyDx[i-1]) {
                        d2yDx2[i] = Float.POSITIVE_INFINITY;
                    } else {
                        d2yDx2[i] = Float.NEGATIVE_INFINITY;
                    }
                }
            } else {
                dyDx[i] = (y-lY)/(x-lX);
                if (i > 1 && d2yDx2 != null) {
                    d2yDx2[i] = (dyDx[i]-dyDx[i-1])/(x-lX);
                }
            }

            float   tanumerator = dY*ldX - ldY*dX;
            float   tanominator = dX*ldX + dY*ldY;
            if (tanominator == 0) {
                if (tanumerator < 0) {
                    tanThetaDiff[i] = Float.NEGATIVE_INFINITY;
                    theta[i] = - (float) Math.PI/2;
                } else if (tanumerator > 0) {
                    tanThetaDiff[i] = Float.POSITIVE_INFINITY;
                    theta[i] = (float) Math.PI/2;
                } else {
                    tanThetaDiff[i] = 0;
                    theta[i] = 0;
                }
            } else {
                tanThetaDiff[i] = (dY*ldX - ldY*dX)/(dX*ldX + dY*ldY);
                theta[i] = (float)Math.atan(tanThetaDiff[i]);
//    http.printbr("tan "+i+" "+tanThetaDiff[i]+" "+thetaDiff[i]+" "+tanumerator+" "+tanominator);
                if (tanThetaDiff[i] < 0) {
                    if (tanumerator > 0) {
                        theta[i] += Math.PI;
                    }
                } else {
                    if (tanumerator < 0) {
                        theta[i] -= Math.PI;
                    }
                }
            }
            cumTheta[i] = theta[i] + cumTheta[i-1];
            cumModTheta[i] = (float)Math.abs(theta[i]) + cumModTheta[i-1];
            cumTheta2[i] = theta[i]*theta[i] + cumTheta2[i-1];
            cumTheta2deg[i] = theta[i]*theta[i]*((float)(180*180/(Math.PI*Math.PI))) + cumTheta2deg[i-1];

            ldX = dX;
            ldY = dY;
            lX = x;
            lY = y;
        }
    }
    

    /**
     * calculates the pixel density
     * @param n number of pixels in a box
     * @param r a box
     * @return the pixel density for the box
     */
    public static float pixelDensity(int n, Rectangle r)
    {
        float   a = r.area();
        return (a==0)? Float.POSITIVE_INFINITY : (n/a);
    }    
    
    /**
     * the distance between two points
     * @param x1 x co-ord of first point
     * @param x2 x co-ord of second point
     * @param y1 y co-ord of first point
     * @param y2 y co-ord of second point
     * @return the distance between the two points
     */
    public static float pointLen(float x1, float y1, float x2, float y2)
    {
        float   w = x2 - x1;
        float   h = y2 - y1;
        return (float)Math.sqrt(w*w + h*h);
    }
    
    
    
    /**
     * displays the rows of theta based features of this analysis as row data in a html table
     *
     * @param http the web page to display on.
     */
    public void DisplayThetaFeatureRows(HTMLwriter http)
    {
        http.tablerow();

        http.tabledata();
        http.print("tan(theta)");
        http.tabledata();
        for (int i=1; i<tanThetaDiff.length; i++) {
            http.tabledata();
            http.print(Float.toString(tanThetaDiff[i]));
        }

        http.tablerow();
        http.tabledata();
        http.print("theta");
        http.tabledata();
        for (int i=1; i<theta.length; i++) {
            http.tabledata();
            http.printbr(Float.toString(theta[i]));
            http.print(Float.toString((float)(360*theta[i]/(2*Math.PI))));
        }

        http.tablerow();
        http.tabledata();
        http.print("sigma(theta)");
        http.tabledata();
        for (int i=1; i<cumTheta.length; i++) {
            http.tabledata();
            http.print(Float.toString(cumTheta[i]));
        }

        http.tablerow();
        http.tabledata();
        http.print("sigma(|theta|)");
        http.tabledata();
        for (int i=1; i<cumModTheta.length; i++) {
            http.tabledata();
            http.print(Float.toString(cumModTheta[i]));
        }

        http.tablerow();
        http.tabledata();
        http.print("sigma(theta^2)");
        http.tabledata();
        for (int i=1; i<cumTheta2.length; i++) {
            http.tabledata();
            http.printbr(Float.toString(cumTheta2[i]));
            http.print(Float.toString(cumTheta2deg[i]));
        }
    }
    
    /**
     * displays the rows of derivative based features of this analysis as row data in a html table
     *
     * @param http the web page to display on.
     */
    public void DisplayDerivativeFeatureRows(HTMLwriter http)
    {
        http.tablerow();
        http.tabledata();
        http.print("dy/dx");
        http.tabledata();
        for (int i=1; i<dyDx.length; i++) {
            http.tabledata();
            http.print(Float.toString(dyDx[i]));
        }
        if (d2yDx2.length > 2) {
            http.tablerow();
            http.tabledata();
            http.print("d2y/dx2");
            http.tabledata();
            http.tabledata();
            for (int i=2; i<d2yDx2.length; i++) {
                http.tabledata();
                http.print(Float.toString(d2yDx2[i]));
            }
        }
    }
                
    /**
     * Displays passed positional data to a HTMLwriter as data in a html table
     * @param http the HTMLwriter for output
     * @param n the number of columns to show, which may be different to d.length
     * @param d the ink sample data
     * @param la the segment length data
     * @see HTMLwriter
     */
    public void DisplaySampleRows(
            HTMLwriter http,
            int n,
            InkSample d[],
            float la[])
    {
        http.tablerow();
        http.tabledata();
        http.print("x");
        for (int i=0; i<n; i++) {
            http.tabledata();
            http.print(Float.toString(d[i].x));
        }
        http.tablerow();
        http.tabledata();
        http.print("y");
        for (int i=0; i<n; i++) {
            http.tabledata();
            http.print(Float.toString(d[i].y));
        }
        http.tablerow();
        http.tabledata();
        http.print("dL");
        http.tabledata();
        for (int i=1; i<la.length; i++) {
            http.tabledata();
            http.print(Float.toString(la[i]));
        }
    }
    
    
    /**
     * calculates the full range of features used to analyse a Stroke.
     * basic features include:
     * <ul>
     *     <li> bounding box
     *     <li> centroid
     *     <li> mean centre: average position
     *     <li> variance about mean centre
     *     <li> skewness (E((x-mean)^3)/dev^3) = ((n *sum(x^3))/((n-1)(n-2)dev^3). A measure of the symmetry of points around the mean point.
     *     <li> kurtosis (E((x-mean)^4)/dev^4). A measure of the flatness of points around the mean point. Negative is a flat distribution, positive is peaked, normal is 0.
     *     <li> density (point / sq pixel)
     *	   <li> Cosine of the stroke starting angle.
     *	   <li> Sine of the stroke starting angle.
     *	   <li> Length of the diagonal of the bounding box.
     *	   <li> Angle between start and end of the stroke.
     *	   <li> Distance between start and end of the stroke.
     *	   <li> Normalised distance between start and end of stroke.
     *	   <li> Cosine of the angle between the start and end of the stroke.
     * 	   <li> Sine of the angle between the start and end of the stroke.
     *	   <li> Average length of each segment.
     *	   <li> Variance in the length of each segment.
     *	   <li> Sum of the angles between each sample-delineated segment of the stroke.
     *	   <li> Fourier and wavelet transforms of the sample data.
     * </ul>
     * <p>
     * Other obvious items of analytical interest that haven't been calculated
     * <ul>
     *	   <li> Total path length of the stroke.
     *	   <li> Total path length normalised to the diagonal length of the bounding box.
     *	   <li> Relative length from start of stroke to first sharp change in second derivative. 
     *	   <li> Relative length from end of stroke to last sharp change in second derivative.
     *	   <li> Variance of the distance from each point to the centroid.
     * </ul>
     *<p>
     * This routine also calculate resampled forms of the input contour
     *<ul>
     *<li>filtered to remove short segments and repeated points
     *<li>the length filtered version filtered to straighten out soft angles
     *<li>a resampled form, consisting of a fixed numer of segments of equal length matched to fit the contour.
     *</ul>
     *<p>
     * Resampling to a fixed length segment was hoped to give a scale invariant means
     * of meaningfully comparing shapes, which would initially be consisting of a totallty
     * arbitrary number of sample points.
     * The tuning of the resampling length will have a great deal of bearing on
     * the resampling of a contour: whether it is meaningfully close to the original
     * contour to give accurate subsequent analyses for recognition. etc.
     *<ul>
     *  <li> small divisions are therefore preferable
     *  <li> some sort of scaling relative to the "size" of the contour (currently we use
     *     diagonal length of the raw bounding box). we would like roughly the same
     *     ballpark number of divisions. This should give more directly comparable
     *     results for comparing transforms, etc.
     *  <li> without a minimum bound, we get into some degenerate situations in DaEqualiza,
     *     and possibly more interpolation points than we'd reasonably want on top of all
     *     the other stuff going on...
     *  <li> we sould hope that transforms we take of this data will be approximately scaled
     *     for various sized versions of the same shape... though I think the kinetics of
     *     handwriting, and the limits of the sample resolution of the digital pen
     *     technology will have some bearing on this.
     *  <li> this will have something to bear on the tuning of the tolerances of DaEqualiza
     *</ul>
     *
     * @param http a HTMLwriter to use for diagnostic output
     * @see Stroke
     * @see StrokeInfo
     * @see Furrier
     */
    public void CalculateFeatures(HTMLwriter http)
    {
// basic statistical analysis
        float           sum2X = 0;
        float           sum2Y = 0;
        
        if (stroke.bounds == null || stroke.mean == null || stroke.centroid == null) {
            stroke.CalculateBounds();
        }

        for (int i=0; i<stroke.nSample; i++) {
            float   x = stroke.sample[i].x;
            float   y = stroke.sample[i].y;
            sum2X += x*x;
            sum2Y += y*y;
        }
        
        float meanX = stroke.mean.x;
        float meanY = stroke.mean.y;
        
        varX = (sum2X/stroke.nSample) - (meanX*meanX);
        varY = (sum2Y/stroke.nSample) - (meanY*meanY);
        
        if (varX < 0) varX = 0;
        if (varY < 0) varY = 0;
        
        devX = (float) Math.sqrt(varX);
        devY = (float) Math.sqrt(varY);
       
        Rectangle sigma1bounds = new Rectangle(
                                    meanX-devX, meanY-devY,
                                    meanX+devX, meanY+devY);
        Rectangle sigma2bounds = new Rectangle(
                                    meanX-2*devX, meanY-2*devY,
                                    meanX+2*devX, meanY+2*devY);
        
        density = pixelDensity(stroke.nSample, stroke.bounds);
        densitySigma1 = pixelDensity(stroke.nSample, sigma1bounds);
        densitySigma2 = pixelDensity(stroke.nSample, sigma2bounds);
        
        rawBoxDiagLen = stroke.bounds.diagLen();
        sigma1BoxDiagLen = sigma1bounds.diagLen();
        sigma2BoxDiagLen = sigma2bounds.diagLen();
    
        rawBoxDiagAngle = stroke.bounds.diagAngleDeg();
        sigma1BoxDiagAngle = sigma1bounds.diagAngleDeg();
        sigma2BoxDiagAngle = sigma2bounds.diagAngleDeg();
        
        start2endLen = pointLen(
                    stroke.sample[0].x,
                    stroke.sample[0].y,
                    stroke.sample[stroke.nSample-1].x,
                    stroke.sample[stroke.nSample-1].y
                    );
        normStart2endLen = start2endLen/rawBoxDiagLen;

        pathLength = 0;
       
        
// calculate 3rd and 4th statistical moments, skew and kurtosis
        float   sum3X = 0, sum3Y = 0, sum4X = 0, sum4Y = 0;
        
        for (int i=0; i<stroke.nSample; i++) {
            float   x = stroke.sample[i].x;
            float   y = stroke.sample[i].y;
            
            float dx = x - meanX;
            float dy = y - meanY;
            
            sum3X += dx*dx*dx;
            sum4X += dx*dx*dx*dx;
            sum3Y += dy*dy*dy;
            sum4Y += dy*dy*dy*dy;
        }
        
        skewX = sum3X/(devX*devX*devX);
        skewY = sum3Y/(devY*devY*devY);
        kurtX = sum4X/(devX*devX*devX*devX);
        kurtY = sum4Y/(devY*devY*devY*devY);

        if (http != null) {
            if (stroke.id != null) {
                    http.printbr("stroke: "+stroke.id);
            }
            http.printbr("bounds: "
                    +stroke.bounds.left+", "+stroke.bounds.top+", "
                    +stroke.bounds.right+", "+stroke.bounds.bottom);
            http.printbr("centroid: "+stroke.centroid.x+", "+stroke.centroid.y);
            http.printbr("mean: "+stroke.mean.x+", "+stroke.mean.y);
            http.printbr("deviation: "+devX+", "+devY);
            http.printbr("length: "+pathLength); 
            http.printbr("skew: "+skewX+", "+skewY);
            http.printbr("kurtosis: "+kurtX+", "+kurtY);
            http.printbr("density: "+density);
            http.printbr("sigma1 density: "+densitySigma1);
            http.printbr("sigma2 density: "+densitySigma2);
            http.printbr("bound angle/length: "+rawBoxDiagAngle+" "+rawBoxDiagLen);
            http.printbr("sigma1 angle/length: "+sigma1BoxDiagAngle+" "+sigma1BoxDiagLen);
            http.printbr("sigma2 angle/length: "+sigma2BoxDiagAngle+" "+sigma2BoxDiagLen);
            http.printbr("start to end length: "+start2endLen);
            http.printbr("normalized start to end length: "+normStart2endLen);
        }

        if (stroke.nSample > 1) {         
            dL = InkFilta.SegmentLengths(stroke.nSample, stroke.sample);  
            if (http != null && displayRawContourValues) {
                http.table(1);
                if (displaySampleValues) {
                    DisplaySampleRows(http, stroke.nSample, stroke.sample, dL);
                }
                http.closetag();
            }
            
// do some basic filtering ... the short stroke filter removes points based on distance
// between segments. short ones, duplicates in particular, are removed.
// segment angle, theta is calculated for use by next filter.
            InkSample   lengthFiltered[] = 
                            InkFilta.ShortStrokeFilter(
                                Tolerance.lengthThreshold,
                                stroke.nSample,
                                stroke.sample,
                                dL);   
            lengthendStroke = lengthFiltered;
            dLfiltered = InkFilta.SegmentLengths(lengthFiltered.length, lengthFiltered);   
            try {
                CalculateTheta(lengthFiltered.length, lengthFiltered);
            } catch (Cow c) {   // one thing to throw a cow.. another thing altogether to catch it
            }
            if (http != null && displayLengthenedContourValues) {
                http.table(1);
                if (displaySampleValues) {
                    DisplaySampleRows(http, lengthFiltered.length, lengthFiltered, dLfiltered);
                }
                if (displayDerivativeValues) {
                    DisplayDerivativeFeatureRows(http);
                }
                if (displayThetaValues) {
                    DisplayThetaFeatureRows(http);
                }
                http.closetag(); 
            }
            lengthendTheta = theta;
            lengthenDthetaDl = new float[theta.length];
            lengthenD2thetaDl2 = new float[theta.length];
            lengthenDthetaDl[0] = 0;
            lengthenD2thetaDl2[0] = 0;
            for (int i=1; i<theta.length; i++) {
                float   len = dLfiltered[i]+dLfiltered[i-1];
                lengthenDthetaDl[i] = theta[i]/len;
                lengthenD2thetaDl2[i] = (lengthenDthetaDl[i]-lengthenDthetaDl[i-1])/len;
            }
            
// keep this result for theta
            rawishTheta = theta;
            rawishTheta[0] = 0; //we set it to NaN as a notational thing but this get messy ....)

// next bit of filtering ... the line straightening filter removes points based on low or
// zero thete between segments. hopefully we get lots of straight lines
// segment angle, theta is calculated for use by next filter.
            InkSample   angleFiltered[] =
                            InkFilta.LineStraighteningFilter(
                                false,
                                true,
                                Tolerance.absoluteAngleFilterThreshold,
                                lengthFiltered.length,
                                lengthFiltered,
                                theta);
            dLfiltered = InkFilta.SegmentLengths(angleFiltered.length, angleFiltered);   
            try {
                CalculateTheta(angleFiltered.length, angleFiltered);
            } catch (Cow c) {
            }
            if (http != null && displayStraightenedContourValues) {
                http.printbr("<B>Straightened form: "+angleFiltered.length+" interpolated points</B>");
                http.table(1);
                if (displaySampleValues) {
                    DisplaySampleRows(http, angleFiltered.length, angleFiltered, dLfiltered);
                }
                if (displayDerivativeValues) {
                    DisplayDerivativeFeatureRows(http);
                }
                if (displayThetaValues) {
                    DisplayThetaFeatureRows(http);
                }
                http.closetag();
            }
// keep this result for theta
            straightendTheta = theta;
            straightenDl = dLfiltered;
            straightendTheta[0] = 0; //we set it to NaN as a notational thing but this gets messy ....)
            
            straightenDthetaDl = new float[theta.length];
            straightenD2thetaDl2 = new float[theta.length];
            straightenDthetaDl[0] = 0;
            straightenD2thetaDl2[0] = 0;
            for (int i=1; i<theta.length; i++) {
                float   len = dLfiltered[i]+dLfiltered[i-1];
                straightenDthetaDl[i] = theta[i]/len;
                straightenD2thetaDl2[i] = (straightenDthetaDl[i]-straightenDthetaDl[i-1])/len;
            }
            
            straightendStroke = angleFiltered;
        }
        
                
        if (http != null) {
            http.printbr("");
            http.printbr("");
        }
         if (stroke.nSample > 1) {  
 // calculate the resampled form
            float  equalSegLength = rawBoxDiagLen/50;
           if (equalSegLength < Tolerance.minimumContourResampleSegment) {
               equalSegLength = (float)Tolerance.minimumContourResampleSegment;
           }
           if (http != null) {
               http.printbr("segmenting into equal lengths of "+equalSegLength);
           }
           InkSample[] esr = InkFilta.DaEqualizaFilta(null, //http, 
                                        stroke.nSample,
                                        stroke.sample,
                                        equalSegLength);
            equalisedStroke = esr;
            dLfiltered = InkFilta.SegmentLengths(esr.length, esr);     
            try {
                CalculateTheta(esr.length, esr);
            } catch (Cow c) {   // someone in there keeps throwing them
                
            }
            
            equalisedTheta = theta;
            equalisedTheta[0] = 0;

// a few test impulses hacked in to test/calibrate the fourier code
//            equalisedTheta = new float[128];
//            for (short i=0; i<equalisedTheta.length; i++) {
//                equalisedTheta[i] = (float) Math.sin(2*Math.PI*((float)i)/((float)equalisedTheta.length));
//            }
//            for (short i=0; i<equalisedTheta.length; i++) {
//                equalisedTheta[i] = 0;
//            }
//            equalisedTheta[0] = 0;
//            equalisedTheta[1] = 1;
//            equalisedTheta[2] = 1;
            
            boolean gotFurrier=false;
            furrier = new Furrier();

// a few test impulses hacked in to test/calibrate the fourier code
//            straightendTheta = equalisedTheta;
//            for (short i=0; i<straightendTheta.length; i++) {
//                straightendTheta[i] = 0;
//            }
//            straightendTheta[0] = 2;
           
            if (calculateTransforms) {
                gotFurrier = furrier.TrancefumRealToMagPhs(http, equalisedTheta);
                equalisedSignatureRe = furrier.xRe;
                equalisedSignatureIm = furrier.xIm;
                equalisedSignatureMg = furrier.mag;
                equalisedSignaturePh = furrier.phs;

                gotFurrier = furrier.TrancefumComplexToMagPhs(http, straightendTheta, straightenDl);
                straightendSignatureRe = furrier.xRe;
                straightendSignatureIm = furrier.xIm;
                straightendSignatureMg = furrier.mag;
                straightendSignaturePh = furrier.phs;
            }
            if (http != null && displayEqualisedContourValues) {
                http.printbr("<B>Resampled form: "+esr.length+" interpolated points</B>");
                http.table(1);
                if (displaySampleValues) {
                    DisplaySampleRows(http, esr.length, esr, dLfiltered);
                }
//                DisplayDerivativeFeatureRows(http);
                if (displayThetaValues) {
                    DisplayThetaFeatureRows(http);
                }
                http.closetag();
                http.printbr("");
                http.printbr("");
                
                if (gotFurrier && displayTransformValues) {
                    if (!gotFurrier) {
                        http.printbr("transform failed");
                    } else {
                        http.printbr("<B>Transformed: "+equalisedSignatureRe.length+" transform points</B>");
                        http.table(1);
                        http.tablerow();
                        http.tabledata();
                        http.print("xre");
                        for (int i=0; i<equalisedSignatureRe.length; i++) {
                            http.tabledata();
                            http.print(Float.toString(equalisedSignatureRe[i]));
                        }
                        http.tablerow();
                        http.tabledata();
                        http.print("xim");
                        for (int i=0; i<equalisedSignatureIm.length; i++) {
                            http.tabledata();
                            http.print(Float.toString(equalisedSignatureIm[i]));
                        }
                        http.tablerow();
                        http.tabledata();
                        http.print("mag");
                        for (int i=0; i<equalisedSignatureMg.length; i++) {
                            http.tabledata();
                            http.print(Float.toString(equalisedSignatureMg[i]));
                        }
                        http.tablerow();
                        http.tabledata();
                        http.print("phs");
                        for (int i=0; i<equalisedSignaturePh.length; i++) {
                            http.tabledata();
                            http.print(Float.toString(equalisedSignaturePh[i]));
                        }
                        http.closetag();
                    }
                }
             }
         }
    }
    
    /**
     * Display relevant features calculated in the preceding mishmash
     *
     * @param http HTMLwriter used for diagnostic output
     * @throws Cow
     * @see HTMLwriter
     */
    public void DisplayFeatures(HTMLwriter http) 
    {
          DisplaySampleRows(http, equalisedStroke.length, equalisedStroke, dLfiltered);
    }
  
    /** variance in x co-ordinate */
    public float            varX = 0;
    /** variance in y co-ordinate */
    public float            varY = 0;
    /** standard deviation in x co-ordinate */
    public float            devX = 0;
    /** standard deviation in y co-ordinate */
    public float            devY = 0;
    /** skewness in x co-ordinate */
    public float            skewX = 0;
    /** skewness in y co-ordinate */
    public float            skewY = 0;
    /** kurtosis in x co-ordinate */
    public float            kurtX = 0;
    /** kurtosis in y co-ordinate */
    public float            kurtY = 0;
    
    /** path length of the stroke in pixels */
    public float            pathLength = 0;

    /** bounds rectangle constructed as one standard deviation about the mean position */
    public Rectangle        sigma1bounds = null;
    /** bounds rectangle constructed as two standard deviation about the mean position */
    public Rectangle        sigma2bounds = null;
    
    /** pixel density in the normal bounding box */
    public float            density = 0;
    /** pixel density in the sigma1 bounding box */
    public float            densitySigma1 = 0;
    /** pixel density in the sigma2 bounding box */
    public float            densitySigma2 = 0;
    
    /** diagonal length in pixels of the normal bounding box */
    public float            rawBoxDiagLen = 0;
    /** diagonal length in pixels of the sigma1 bounding box */
    public float            sigma1BoxDiagLen = 0;
    /** diagonal length in pixels of the sigma2 bounding box */
    public float            sigma2BoxDiagLen = 0;
    
    /** diagonal angle in radians of the normal bounding box */
    public float            rawBoxDiagAngle = 0;
    /** diagonal angle in radians of the sigma1 bounding box */
    public float            sigma1BoxDiagAngle = 0;
    /** diagonal angle in radians of the sigma2 bounding box */
    public float            sigma2BoxDiagAngle = 0;
    
    /** length in pixels between first and last points */
    public float            start2endLen = 0;
    /** length in pixels between first and last points, normalised by the length of the stroke */
    public float            normStart2endLen = 0;
    
    /** conventional derivative of contour */
    public float            dyDx[] = null;
    /** conventional second derivative of contour */
    public float            d2yDx2[] = null;
    /** tan of the tangent angle */
    public float            tanThetaDiff[] = null;
    /** vector of tangent angles */
    public float            theta[] = null;
    /** vector of the cumulative tangent angle */
    public float            cumTheta[] = null;
    /** vector of the cumulative absolute value of tangent angle */
    public float            cumModTheta[] = null;
    /** vector of the cumulative square of the tangent angle */
    public float            cumTheta2[] = null;
    /** vector of the cumulative square of the tangent angle, measured in degrees */
    public float            cumTheta2deg[] = null;

    /** vector of length components for each segment */
    public float            dL[] = null;
    /** vector of length components for the  length filtered contour */
    public float            dLfiltered[] = null;
    /** vector of segment length components for the straightened contour */
    public float            straightenDl[] = null;
    /** vector of curvature, dTheta/dL components for the straightened contour */
    public float            straightenDthetaDl[] = null;
    /** vector of change of curvature, d2Theta/dL2 components for the straightened contour */
    public float            straightenD2thetaDl2[] = null;
    /** vector of tangent angle components for the length filtered contour */
    public float            lengthendTheta[] = null;
    /** vector of curvature, dTheta/dL components for the length filtered contour */
    public float            lengthenDthetaDl[] = null;
    /** vector of change of curvature, d2Theta/dL2 components for the length filtered contour */
    public float            lengthenD2thetaDl2[] = null;

    /** vector of theta component for the equal length resamples contour */
    public float            rawishTheta[] = null;
    /** vector of theta component for the equal length resamples contour */
    public float            equalisedTheta[] = null;
    /** vector of theta component for the equal length resamples contour */
    public float            straightendTheta[] = null;
    
    /** vector of real part of fourier transform of theta component for the equal length resamples contour */
    public float            equalisedSignatureRe[] = null;
    /** vector of imaginary part of fourier transform of theta component for the equal length resamples contour */
    public float            equalisedSignatureIm[] = null;
    /** vector of magnitude part of fourier transform of theta component for the equal length resamples contour */
    public float            equalisedSignatureMg[] = null;
    /** vector of phase part of fourier transform of theta component for the equal length resamples contour */
    public float            equalisedSignaturePh[] = null;
    
    /** vector of real part of fourier transform of theta component for the straightened contour */
    public float            straightendSignatureRe[] = null;
    /** vector of imaginary part of fourier transform of theta component for the straightened contour */
    public float            straightendSignatureIm[] = null;
    /** vector of magnitude part of fourier transform of theta component for the straightened contour */
    public float            straightendSignatureMg[] = null;
    /** vector of phase part of fourier transform of theta component for the straightened contour */
    public float            straightendSignaturePh[] = null;
   
    /** resampled form of input sample. same contour resampled for to eliminate short segments and repeated points */
    public InkSample        lengthendStroke[] = null;
    /** resampled form of input sample. same contour resampled for to eliminate minor curvatures, hopefully giving us more straight line geometry */
    public InkSample        straightendStroke[] = null;
    /** resampled form of input sample. same contour, resampled for segments of a given fixed length */
    public InkSample        equalisedStroke[] = null;

    /** fourier analysis component */
    Furrier                 furrier = null;
    
///////////////////////////////////////////////////////////////////////
// display and calculation control parameters
//////////////////////////////////////////////////////////////////////
    /** display results of transforms */
    boolean         displayTransformValues = false;
    /** display results for derivatives */
    boolean         displayDerivativeValues = false;
    /** display calculated theta values */
    boolean         displayThetaValues = true;
    /** display resampled values */
    boolean         displaySampleValues = true;
    
    /** display results of analyses of raw contour */
    boolean         displayRawContourValues = true;
    /** display results of analyses of length filtered contour */
    boolean         displayLengthenedContourValues = false;
    /** display results of analyses of straightened contour */
    boolean         displayStraightenedContourValues = true;
    /** display results of analyses of contour resampled in equal lengths */
    boolean         displayEqualisedContourValues = false;
   
    /** do calculations of transforms of contours */
    boolean         calculateTransforms = false;
}
