/*
 * StrokeCurveInfo.java
 *
 * Created on 2 June 2005, 18:58
 *
 */

package gita;

import zone.HTMLwriter;
import static java.lang.Math.*;

/**
 * Basic StrokeInfo extension to hold and calculate info about curvature properties of a stroke.
 * Currently that is just about all Strokes in all situations. At some stage, we might optimize
 * that out.
 * <p>
 * At the moment, we keep track of the length filtered sample data, the pen artifact filtered
 * sample data, the angle/dL representation of the sample data, and a few statistical bits and
 * pieces collected on the way while arriving at all this.
 * 
 * @author dak
 * @since you asked
 */
public class StrokeCurveInfo extends StrokeInfo
{
    /**
     * Creates a new instance of StrokeCurveInfo
     *
     * @param s the stroke for which this contains info
     */
    public StrokeCurveInfo(Stroke s)
    {
        super(s);
    }
    
    /**
     * do the feature calculations pertinent to a StrokeInfo structure
     * effectively this involves
     * <ul>
     *  <li>filtering out short segments, which eliminates degeneracy and common points
     *  <li>elimination of short high angle segments at end points ... pen noise
     *  <li>conversion to a (dL, dTheta) representation,
     *  <li>calculation of the curvature vector, dTheta/dL
     *  <li>a few stats based on dTheta
     * </ul>
     *
     * @param http HTMLwriter used for diagnostic output
     * @throws Cow
     * @see HTMLwriter
     */
    public void CalculateFeatures(HTMLwriter http) throws Cow
    {
// length filter data to eliminate degeneracy. the filter operation, of course, invalidates the segment
// lengths used to calculate the filtered data
        dL = InkFilta.SegmentLengths(stroke.nSample, stroke.sample);
        lengthFilteredStroke = InkFilta.ShortStrokeFilter(
                                            (float)1.0,
                                            stroke.nSample,
                                            stroke.sample,
                                            dL);
        
// convert (x,y) representation to a (dTheta, dL) representation
        dL = InkFilta.SegmentLengths(lengthFilteredStroke.length, lengthFilteredStroke);
// get the tangent angle array
        tangle = InkFilta.DelTheta(lengthFilteredStroke.length, lengthFilteredStroke, false); 

        artifactFilteredStroke = InkFilta.PenArtifactFilter(
                                            lengthFilteredStroke.length,
                                            lengthFilteredStroke, dL, tangle);
        dL = InkFilta.SegmentLengths(artifactFilteredStroke.length, artifactFilteredStroke);
        tangle = InkFilta.DelTheta(artifactFilteredStroke.length, artifactFilteredStroke, false); 

// get some totals and stats on the angles therein contained
        pathLength = 0;
        for (int i=0; i<dL.length; i++) {
            pathLength += dL[i];
        }
        
// normalise length segment vector to a total length of 1. note that some useful information
// is innate in the raw pixel lengths
        dL = InkFilta.NormalizeLengthVector(dL);
     
        float   sumAbsTangle = 0;
        sumTangle = 0;
        weightedSumTangle = 0;
        tangle[0] = 0; // set to NaN by DelTheta()
        float       sumTSquare = 0;
        for (int i=1; i<tangle.length; i++) {
            sumAbsTangle += abs(tangle[i]);
            sumTangle += tangle[i];
            weightedSumTangle += tangle[i]*(dL[i-1]+dL[i])/2;
            sumTSquare += tangle[i]*tangle[i];
        }
        meanTangle = sumTangle/(tangle.length);
        meanAbsTangle = sumAbsTangle/tangle.length;
        devTangle = (float) sqrt(((sumTSquare/tangle.length) - (meanTangle*meanTangle))); 
        
// calculate 1st derivative dTheta/dL
        dTangledL = InkFilta.DyDx(tangle, dL);
        
// try for a quick and crude first pass analysis of the geometry
        type = Type.Complex;
        if (abs(sumTangle) < Tolerance.straightTangleSumThresh) {          // maybe a straight line 
            if (abs(meanTangle) < Tolerance.straightTangleAveThresh) { 
                type = Type.Straight;
            }
        } else if (abs(sumTangle) < Tolerance.slightTangleThresh) {     // maybe an arc 
            if (abs(devTangle) < Tolerance.tangleCurvedDevThresh) {
                if (sumTangle < 0)
                    type = Type.SlightNegCurve;
                else
                    type = Type.SlightPosCurve;
            }
        } else if (abs(sumTangle) < Tolerance.strongTangleThresh) {     // maybe a big loop 
            if (abs(devTangle) < Tolerance.tangleCurvedDevThresh) {
                if (sumTangle < 0)
                    type = Type.StrongNegCurve;
                else
                    type = Type.StrongPosCurve;
            }
            
        } else {    // maybe a really big loop... full circle or spiral
            if (abs(devTangle) < Tolerance.tangleCurvedDevThresh) {
                if (sumTangle < 0)
                    type = Type.FullNegCurve;
                else
                    type = Type.FullPosCurve;
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
        http.table(1);
        if (tangle != null) {
            http.tablerow("tangent angle", "%.3g", tangle);
            http.tablerow("segment length", "%.3g", dL);
            http.tablerow("curvature", "%.3g", dTangledL);
        }
        http.closetag();
        http.printf("total path length %.3g, total tangle %.3g, weighted sum %.3g, ave tangle %.3g, ave abs tangle %.3g, standard dev %.3g<br><br>",
                        pathLength, sumTangle, weightedSumTangle, meanTangle, meanAbsTangle, devTangle);
    }
    
    /** type values used in the categorization of the basic geometry of an individual stroke */
    public enum Type {
      /** a stroke that is, to a reasonable approximation, straight. Cumulative tangent angle close to zero, average tangent angle close to zero. */
        Straight,
      /** a stroke that is, to a reasonable approximation, slightly curved, in a positive theta direction. Cumulative tangent angle small positive, majority of angles positive */
        SlightPosCurve,
      /** a stroke that is, to a reasonable approximation, strongly curved, in a positive theta direction. Cumulative tangent angle large positive, majority of angles positive */
        StrongPosCurve,
      /** a stroke that is, to a reasonable approximation, almost a full loop, in a positive theta direction. Cumulative tangent angle near or > 2pi, majority of angles positive */
        FullPosCurve,
      /** a stroke that is, to a reasonable approximation, slightly curved, in a negative theta direction. Cumulative tangent angle small negative, majority of angles negative */
        SlightNegCurve,
      /** a stroke that is, to a reasonable approximation, strongly curved, in a negative theta direction. Cumulative tangent angle large negative, majority of angles negative */
        StrongNegCurve,
      /** a stroke that is, to a reasonable approximation, almost a full loop, in a negative theta direction. Cumulative tangent angle near or < -2pi, majority of angles negative */
        FullNegCurve,
      /** a stroke that follows too complex a path to fit into any of the above categorizations */
        Complex
    }

    /** version of stroke, filtered to remove short segments. this should be kept, as the artifact filtering process might remove key data */
    public InkSample[]      lengthFilteredStroke = null;
    /** version of stroke, filtered to remove pen artifracts at the beginning and end of strokes */
    public InkSample[]      artifactFilteredStroke = null;
    /**
     * vector of segment legnths for each sample point. Given an array of samples, data[0 .. i .. n-1],
     * the length array element dL[i] is the distance between points (i-1:i).
     * This vector is based on the final filtered version of the stroke.
     */
    public float[]          dL = null;
    /**
     * vector of tangent angles at each sample point.  Given an array of samples, data[0 .. i .. n-1],
     * the angle array element tangle[i] is the angle subtended by segments (i-2:i-1:i)
     * This vector is based on the final filtered version of the stroke.
     */
    public float[]          tangle = null;
    /** vector of curvature values at each sample point */
    public float[]          dTangledL = null;
    /** cumulative value of tangent angle. For this to be truly meaningful, we may have to eliminate short length outliers at either end of a stroke e.g. there may be a bit of "noise" when a pen is put down or lifted from a page. */
    public float            sumTangle = 0;
    /** cumulative value of tangent angle, weighted by the length of the segments. */
    public float            weightedSumTangle = 0;
    /** mean value of tangent angle.  */
    public float            meanTangle = 0;
    /** mean value of absolute value of tangent angle.  */
    public float            meanAbsTangle = 0;
    /** standard deviation of tangent angle.  */
    public float            devTangle = 0;
    /** cumulative value of dL. i.e. the length of the stroke */
    public float            pathLength = 0;
    /** rough categorization of stroke based on basic geometry */
    public Type             type = Type.Complex;
}
