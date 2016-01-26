
package gita;

/** 
 * wrapper around tolerances used by gita ... attempt to centralize as much config stuff
 * as possible
 */
public class Tolerance
{
    /** maximum size of a bounding box for a stroke to be considered a full stop */
    public static final float fullStopBoundsDiag = (float)3.0;
    /** minimum angle from vertical for a slash (in degrees) */
    public static final float slashMinimumAngle = (float)5;
    /** maximum angle from vertical for a slash (in degrees) */
    public static final float slashMaximumAngle = (float)45;
    /** minimum length of a slash (in pixels) */
    public static final float slashMinimumLength = (float)10;
    /** maximum length of a slash (in pixels) */
    public static final float slashMaximumLength = (float)40;
////////////////////////////////////////////////////////////////////////////////////////////////////////////
// constants relating to small strokes ... we want to drop out dots, and noisy pieces of signature from
// main contention, as they'll have, very likely, confusing curvature maps, and be placed all over the shop
// perhaps we should have a look at them a little bit later, particularly with regards placement.
///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** number of samples in a stroke too small to worry about */
    public static final int    nSmallStrokeSamples = 4; 
   /** size below which a stroke is too small */
    public static final float  smallStrokeSize = (float) 3.0;
////////////////////////////////////////////////////////////////////////
// global threshold values, used by StrokeAlysis
//////////////////////////////////////////////////////////////////////
    /** length in pixels below which a segment is dropped */
    public static float    lengthThreshold = (float)1.0;
    /** angle in radians, below which a bent segment is considered straight */
    public static float    absoluteAngleFilterThreshold = (float)0.22;
    /** smallest size segment used when resampling the original contour. this prevents a fair amount of degeneracy in the resampled results */
    public static float    minimumContourResampleSegment = (float)1.0;
////////////////////////////////////////////////////////////////////////
// global threshold values, used by Stroke
//////////////////////////////////////////////////////////////////////
    /** Threshold constant used by <i>classifySegment</i> for a tangent angle (scaled to the range [0,1]) of a segment to be considered "straight". */
    public static final float   straightSegmentTangleThresh = (float)0.1;
    /** Threshold constant used by <i>classifySegment</i> for a tangent angle (scaled to the range [0,1]) of a segment to be considered "a medium curve". */
    public static final float   slowSegmentTangleThresh = (float)0.3;
    /** Threshold constant used by <i>classifySegment</i> for a tangent angle (scaled to the range [0,1]) of a segment to be considered "a sharp curve". */
    public static final float   sharpSegmentTangleThresh = (float)0.9;
////////////////////////////////////////////////////////////////////////
// global threshold values, used by StrokeCurveInfo
//////////////////////////////////////////////////////////////////////
    /** threshold value within which a stroke is considered straight. In radians. */
    public static final float straightTangleSumThresh = (float) 0.4;
    /** threshold value within which a stroke is considered straight. In radians. */
    public static final float straightTangleAveThresh = (float) 0.1;
    /** threshold value within which the angles in strokes are considered even. In radians. */
    public static final float tangleCurvedDevThresh = (float) 0.3;
    /** threshold value within which a stroke is considered slight, somewhere between straight and a quarter of a circle. In radians. */
    public static final float slightTangleThresh = (float) 1.0;
    /** threshold value within which a stroke is considered a strong curve, anything from a quarter arc to nearly a full circle. In radians. */
    public static final float strongTangleThresh = (float) 6.0;
////////////////////////////////////////////////////////////////////////
// global threshold values, used by InkFilta
//////////////////////////////////////////////////////////////////////
    /** minimum angle threshold for a possible pen up/down artifact, ave pi/3 */
    public static final float penArtifactFiltaTangleThresh = (float) 1.0 ;
    /** maximum length of a possible pen up/down artifact at the begin/end of a stroke */
    public static final float penArtifactFiltaLengthThresh = (float) 4.0;
    /** threshold value within which a stroke is considered a strong curve, anything from a quarter arc to nearly a full circle. In radians. */
    public static final int penArtifactFiltaPointThresh = 3;
   
}

