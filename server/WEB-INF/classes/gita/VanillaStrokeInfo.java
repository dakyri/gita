/*
 * VanillaStroke.java
 *
 * Created on 2 June 2005, 18:43
 *
 */

package gita;

import zone.Rectangle;
import zone.Point;
import zone.HTMLwriter;

/**
 * a simpler class for stroke info and features than the original <i>StrokeAlysis</i>.
 *
 * @author dak
 * @since you asked
 */
public class VanillaStrokeInfo extends StrokeCurveInfo
{
    /**
     * Creates a new instance of VanillaStroke
     *
     * @param s the stroke for which this contains info
     */
    public VanillaStrokeInfo(Stroke s)
    {
        super(s);
    }
    
    /**
     * do the feature calculations pertinent to a VanillaStroke structure
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
        super.CalculateFeatures(http);
    }
}
