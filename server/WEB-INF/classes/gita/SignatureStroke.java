/*
 * SignatureStroke.java
 *
 * Created on 9 February 2005, 11:41
 */

package gita;
 
import zone.Rectangle;
import zone.Point;
import zone.HTMLwriter;

/**
 *  Implementation of StrokeInfo pertaining to specifically signature relevant features of a stroke
 *  belonging to a signature group. Strokes are converted to a (theta, l) representation,
 *  which is translationally and rotationally invariant.
 *
 * @see StrokeInfo
 * @see Stroke
 * @author dak
 * @since you asked
 */
public class SignatureStroke extends StrokeCurveInfo
{
    
    /**
     * Creates a new instance of SignatureStroke
     *
     * @param s the stroke for which this is the StrokeInfo structure
     * @see StrokeInfo
     */
    public SignatureStroke(Stroke s)
    {
        super(s); 
        groupRelativeBounds = new Rectangle();
        groupRelativeMean = new Point();
    }

    /**
     * do the feature calculations pertinent to a StrokeInfo structure
     * effectively this involves filtering out short strokes, which eliminates degeneracy
     * and common points, conversion to a (dL, dTheta) representation, and calculation
     * of the curvature vector, dTheta/dL
     *
     * @param http HTMLwriter used for diagnostic output
     * @throws Cow
     * @see HTMLwriter
     */
    public void CalculateFeatures(HTMLwriter http) throws Cow
    {
        // length filter data to eliminate degeneracy
        dL = InkFilta.SegmentLengths(stroke.nSample, stroke.sample); 
        http.print("doing a bit of squeezing from "+dL.length);
        lfData = InkFilta.ShortStrokeFilter(
                                            (float)1.0,
                                            stroke.nSample,
                                            stroke.sample,
                                            dL);
        http.printbr(" to "+ lfData.length);
        // convert (x,y) to (dTheta, dL)
        dL = InkFilta.SegmentLengths(lfData.length, lfData);
        dL = InkFilta.NormalizeLengthVector(dL);
        float len = 0;
        arcLen = new float[dL.length];
        for (int i=0; i<dL.length; i++) {
            len += dL[i];
            arcLen[i] = len;
        }
        tangle = InkFilta.DelTheta(lfData.length, lfData, false);
        // calculate 1st derivative dTheta/dL
        dTangledL = InkFilta.DyDx(tangle, dL);
    }
    
    /** bounding box of this stroke relative to the bounding box of the signature of which this stroke is a part. calculared by SignatureContainer.CalculateFeatures() */
    Rectangle   groupRelativeBounds = null;
    /** mean point of this stroke relative to the bounding box of the signature of which this stroke is a part. calculared by SignatureContainer.CalculateFeatures() */
    Point       groupRelativeMean = null;
    /** vector of relative arc lengths for each sample in the stroke */
    float[]     arcLen = null;
    /** the length filtered form of the InkSample data of the stroke */
    InkSample[] lfData = null;
}
