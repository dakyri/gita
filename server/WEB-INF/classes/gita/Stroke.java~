/*
 * Stroke.java
 *
 * Created on 17 February 2004, 03:24
 */
 
package gita;
 
import zone.PathDataAttribute;
import zone.Point;
import zone.HTMLwriter;
import zone.Rectangle;

/**
 * Basic class for holding path data for a single stroke, plus the results of various diverse
 * analyses on that data. A stroke should correspond roughly to a "pen-down, squiggle, pen-up".
 *<p>
 * This class contains all the code and data for calculating basic properties/features/analyses that
 * are common to all strokes. The <i>info</i> member contains a reference to some extension of the
 * abstract <i>StrokeInfo</i> class that holds code and data storage for strokes of particular kinds
 * or in particular contexts.
 *<p>
 * Information deduced from feature calculations on a <i>Stroke</i> or a <i>StrokeGroup</i> are stored in
 * <i>Symbol</i> objects.
 *
 * @author dak
 * @since you asked
 * @see StrokeInfo
 * @see StrokeGroup
 * @see Symbol
 */
public class Stroke
{
    /**
     * Creates a new instance of Stroke. It defaults to a <i>penColor</i> of black, and <i>type</i> of
     * Vanilla, and <i>penWidth</i> of 1, with a null <i>id</i> and <i>info</i>.
     */
    public Stroke()
    {
        nSample = 0;
        penColor = new int[3];
        penColor[0] = penColor[1] = penColor[2] = 0;
        penWidth = 1;
        
        type = StrokeType.Vanilla;
        
        id = null;
        
        info = null;
        group = null;
    }
    
    /**
     * Re-allocates the array of sample points for the stroke. Sample count is set to 0.
     *
     * @param maxSample the new length of the sample data.
     */
    public void SetMax(int maxSample)
    {
        sample = new InkSample[maxSample];
        nSample = 0;
    }
    
    /**
     * Returns a PathDataAttribute structure for this stroke
     * @return a PathDataAttribute structure for this stroke
     */
    public PathDataAttribute PathData()
    {
        if (nSample == sample.length) {
            return new PathDataAttribute(sample);
        }
        return PathData(nSample, sample);
    }
    
    /**
     * Returns a PathDataAttribute structure for an array of samples of point data for a stroke
     *
     * @param n the number of samples passed
     * @param s the ink sample data
     * @return a PathDataAttribute structure for the given stroke data
     */
    static public PathDataAttribute PathData(
            int n,
            InkSample[] s)
    {
       Point[] p = new Point[n];
       for (short i=0; i<n; i++) {
           p[i] = s[i];
       }
       return new PathDataAttribute(p);
    }
    
    
    /**
     * Returns a PathDataAttribute structure for an array of samples of point data for a stroke,
     * offset a particular amount
     *
     * @param n the number of samples passed
     * @param s the ink sample data
     * @param xOff the x coordinate to offset the path data by
     * @param yOff the y coordinate to offset the path data by
     * @see Point
     * @return an offset PathDataAttribute structure for the given stroke data
     * @see InkSample
     * @see PathDataAttribute
     */
    static public PathDataAttribute OffsetPath(
            int n,
            InkSample s[],
            float xOff,
            float yOff)
    {
        return OffsetScaledPath(n, s, xOff, yOff, 1, 1);
    }
    
    /**
     * Returns an array of <i>Point</i> for the passed array of samples of point data for a stroke,
     * offset a particular amount, and scaled by a particular amount
     *
     * @param n the number of samples passed
     * @param s the ink sample data
     * @param xOff the x coordinate to offset the path data by
     * @param yOff the y coordinate to offset the path data by
     * @param xScale scale factor in the x direction
     * @param yScale scale factor in the y direction
     * @return an offset and scaled array of <i>Point</i> for the given stroke data
     * @see Point
     * @see InkSample
     */
    static public Point[] OffsetScaledSamples(
            int n, InkSample s[],
            float xOff, float yOff,
            float xScale, float yScale)
    {
        Point[] p = new Point[n];
        float   xMin = Float.MAX_VALUE;
        float   yMin = Float.MAX_VALUE;
        
        for (short i=0; i<n; i++) {
            p[i] = new Point(s[i]);
            if (p[i].x < xMin) xMin = p[i].x;
            if (p[i].y < yMin) yMin = p[i].y;
        }
        for (short i=0; i<n; i++) {
            p[i].x -= xMin; p[i].x *= xScale;
            p[i].y -= yMin; p[i].y *= yScale;
        }
        for (short i=0; i<n; i++) {
            p[i].x += xOff;
            p[i].y += yOff;
        }
        return p;
    }
    
    /**
     * Returns <i>PathDataAttribute</i> for the passed array of samples of point data for a stroke,
     * offset a particular amount, and scaled by a particular amount
     *
     * @param n the number of samples passed
     * @param s the ink sample data
     * @param xOff the x coordinate to offset the path data by
     * @param yOff the y coordinate to offset the path data by
     * @param xScale scale factor in the x direction
     * @param yScale scale factor in the y direction
     * @return an offset and scaled <i>PathDataAttribute</i> for the given stroke data
     * @see Point
     * @see InkSample
     * @see PathDataAttribute
     */
    static public PathDataAttribute OffsetScaledPath(
            int n, InkSample s[],
            float xOff, float yOff,
            float xScale, float yScale)
    {
        return new PathDataAttribute(OffsetScaledSamples(n, s, xOff, yOff, xScale, yScale));
    }
    
    /**
     * Perform the non standard feature calculations using virtuals provided by instantiations of the
     * <i>StrokeInfo</i> abstract class. The values are set in the StrokeInfo structure
     *
     * @param http a wrapper for html output to the relevant ouput page
     */
    public void CalculateFeatures(HTMLwriter http) throws Cow
    {
        switch (type) {
            case Signature: {
                if (info == null) {
                } else {
                }
                break;
            }
            
            case Xtreme: {
                if (info == null) { // put the stroke through all the hooops
                    info = new StrokeAlysis(this);
                }
                info.CalculateFeatures(http);
                break;
            }
            
            case Vanilla:
            default: {
                if (info == null) { // vanilla stroke. just look at curvature 
                    info = new VanillaStrokeInfo(this);
                }
                info.CalculateFeatures(http);
                break;
            }
        }
        
        if (group != null) {
            group.CalculateFeatures(http);
        } else {
            
        }
    }
    
    /**
     * Perform the standard feature calculations common to all strokes: bound, mean, and centroid. The values
     * are set in the corresponding class variables
     */
    public void CalculateBounds()
    {
        float           sumX = 0;
        float           sumY = 0;
        float           minX = 0,
                        minY = 0,
                        maxX = 0,
                        maxY = 0;
        
        if (nSample > 0) {
             minX = maxX = sample[0].x;
             minY = maxY = sample[0].y;
        }
        
        for (int i=0; i<nSample; i++) {
            float   x = sample[i].x;
            float   y = sample[i].y;
            
            sumX += x;
            sumY += y;
            
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        
        float meanX = sumX/nSample;
        float meanY = sumY/nSample;
        
        float medX = (maxX + minX)/2;
        float medY = (maxY + minY)/2;
        
        bounds = new Rectangle(minX, minY, maxX, maxY);
        centroid = new Point(medX, medY);
        mean = new Point(meanX, meanY);
    }
    
    /**
     * Classifies a segment according to the passed value, which is expected to be
     * a value in the range [0,1]. The base score then is abs(theta/pi)
     * 
     * @param score the score value for a segment
     * @return the type of a segment according to the given rating
     */
    public static SegmentType classifySegment(float score)
    {
        if (score < Tolerance.straightSegmentTangleThresh) {
            return SegmentType.Straight;
        } else if (score < Tolerance.slowSegmentTangleThresh) {
            return SegmentType.SlowCurve;
        } else if (score < Tolerance.sharpSegmentTangleThresh) {
            return SegmentType.SharpCurve;
        } else {
            return SegmentType.Reversal;
        }
    }

    /** Type values for the tangent angles of stroke segments */
    public enum SegmentType    {
        /** The angle between two consecutive segments is very slight, quite close to straight */
        Straight,
        /** The angle between two consecutive segments is slight */
        SlowCurve,
        /** The angle between two consecutive segments is strong */
        SharpCurve,
        /** The angle between two consecutive segments is almost a full reversal. Such segments will
         * often appear as quite close to either -pi or +pi in normal variations of hand drawn strokes
         */
        Reversal
    }
    /**
     * Type values for a stroke, controlling the information that needs to be calculated
     * from the point data. Among other things, this dictates the kind of StrokeInfo member
     * used to store and calculate information about this stroke. Earlier versions of the
     * system used this information for cast operations... it's done a little bit more
     * sanely (and I hope safely) now, via calls to getClass()
     */
    public enum StrokeType     {
        /** The stroke is a part of a signature. These strokes receive a much more heavy handed
         * mathematical analysis, and uses a SignatureStroke object for its info member */
        Signature,
        /** The stroke is a convoluted stroke, with a StrokeAlysis info member */
        Xtreme,
        /** The stroke is simply a regular stroke, with a VanillaStroke info member */
        Vanilla
    }
    
    /** Type value for this stroke. This controls the type of <i>StrokeInfo</i> structure used */
    public StrokeType      type;
    
    /** RGB values for the color of this stroke */
    public int             penColor[] = null;
    /** The pen width with which to stroke the stroke */
    public float           penWidth;

    /** The number of actual points constituting the stroke. This may be shorter than sample.length */
    public int             nSample;
    /** The actual point data for the stroke */
    public InkSample       sample[] = null;

    /** String name for this stroke: the id attribute of the corresponding svg path */
    public String          id;

    /** The bounding box of the stroke, the extreme values of the point data in <i>sample</i> */
    public Rectangle       bounds=null;
    /** The centre point of the bounding box of the stroke */
    public Point           centroid=null;
    /** The mean values of the point values of <i>sample</i>. Note that this is different to <i>centroid</i> */
    public Point           mean=null;

    /**
     * Set to a <i>StrokeGroup</i> structure if this stroke is a container for other strokes, otherwise null
     */
    public StrokeGroup     group = null;
    
    /**
     * The <i>StrokeInfo</i> structure of this stroke. It may be of several kinds, depending on the value of <i>type</i>
     */
    public StrokeInfo      info = null;
//    SignatureStroke sigStroke = null;
//    StrokeAlysis    sally = null;
}
