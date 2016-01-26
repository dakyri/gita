/*
 * InkFilta.java
 *
 * Created on 8 February 2005, 11:10
 */

package gita;
 
import zone.HTMLwriter;
import zone.Point;
import java.lang.Float;
import java.util.LinkedList;
import static java.lang.Math.*;


/**
 * This class is a collection of useful static methods for manipulating InkSample and related data.
 * There's no point instantiating it.
 *
 * @see InkSample
 */
public class InkFilta {
    
    /**
     * compacts the given stroke data, removing any null elements
     *
     * @param n the number of samples of input data
     * @param d the input InkSample data
     * @return a more compact array of sample data
     * @see InkSample
     */
    static public InkSample[] CompactStrokeData(int n, InkSample[] d)
    {
        if (n == d.length) { // the array is already compact.
            return d;
        }
        InkSample[]     s = new InkSample[n];
        int             j = 0;
        
        for (int i=0; i<n; i++) {
            if (d[i] != null) {
                s[j++] = d[i];
            }
        }
        return s;
    }
    
    /**
     * removes short strokes from the input sample array, returning the filtered results.
     *
     * @param n the number of samples of input data
     * @param d the input InkSample data
     * @return a more compact array of sample data
     * @see InkSample
     */
    static public InkSample[] ShortStrokeFilter(float threshold, int n, InkSample[] d, float[] dL)
    {
        InkSample[]     nd = new InkSample[n];
        int             nFltSample = 0;
        float           lenTrav = 0;
        
        nd[nFltSample++] = d[0];
        for (int i=1; i<n; i++) {
            lenTrav += dL[i];
            if (lenTrav >= threshold && d[i] != null) {
                lenTrav = 0;
                nd[nFltSample++] = d[i];
            }
        }
        return CompactStrokeData(nFltSample, nd);
    }
    
    /**
     * attempts to remove pen artifacts from a stroke. These are short random bits that sometimes
     * appear at the end and beginning of a stroke, maybe 1-3 sample points, over a few pixels in
     * a direction possibly unrelated to the intent of the stroke ... due to, possibly
     * <ul>
     *  <li>user jitter in the act of applying pressure during pen motion up and down.
     *  <li>instability of the hand before regular pen motion is set.
     *  <li>hand momentum from previous pen stroke, or motion to new start position
     * </ul>
     * <p>
     * There are probably better ways of doing
     * this than pure geometry. Strongly suspect that it if we could read the pen pressure at
     * these points it would be most telling.
     * <p>
     * In fact, over zealousness here would cause difficulties recognizing small strokes that
     * are dependent on short wierd end pieces. Notably, the square brackets '[', ']'. Some
     * better contextual information would be roill helpfull.
     *
     * @param n the number of samples of input data
     * @param d the input InkSample data
     * @param dL array of segment length components
     * @param tangle array of tangent angles (radian)
     * @return a maybe more compact and less noisy array of sample data
     * @see InkSample
     */
    static public InkSample[] PenArtifactFilter(int n, InkSample[] d, float[] dL, float[] tangle)
    {
        if (n < Tolerance.penArtifactFiltaPointThresh) {
            return CompactStrokeData(n, d);
        }
        InkSample[]     nd = new InkSample[n];
        int             nFltSample = 0;
        float           lenTrav = 0;
        
        int             firstValid = 0;
        int             lastValid = n-1;
        int             ind = 0;
  
        while (ind <= Tolerance.penArtifactFiltaPointThresh && ind < n-1 &&
               lenTrav <= Tolerance.penArtifactFiltaLengthThresh) {
            if (abs(tangle[ind+1]) >= Tolerance.penArtifactFiltaTangleThresh) {
                firstValid = ind;
            }
            lenTrav += dL[ind];
            ind++;
        }
        
        ind = n - 1;
        lenTrav = dL[n-1];
        int np = 0;
        while (    np <= Tolerance.penArtifactFiltaPointThresh
                && lenTrav <= Tolerance.penArtifactFiltaLengthThresh
                && ind > firstValid+1) {
            if (abs(tangle[ind]) >= Tolerance.penArtifactFiltaTangleThresh) {
                    lastValid = ind-1;
            }
            lenTrav += dL[ind-1];
            
            ind--;
            np++;
        }
        
        for (int i=firstValid; i<=lastValid; i++) {
            nd[nFltSample++] = d[i];
        }
        return CompactStrokeData(nFltSample, nd);
    }

    
    
    /**
     * Resamples the given array of InkSample data, removing any points that subtend an angle below a certain
     * threshold. The resampled version will have fewer points, and longer straighter segments. This
     * should make the task of identifying simple geometric figures easier.
     *
     * @param filterAbs The filtering process is based on the absolute angle subtended by the segments to and from a point
     * @param filterCum The filtering process is based on the cumulative angle subtended by consecutive segments 
     * @param threshold the threshold value for resampling the input data
     * @param n the number of samples of input data
     * @param d the input InkSample data
     * @return a straightened array of sample data
     * @see InkSample
     */
    static public InkSample[] LineStraighteningFilter(
            boolean filterAbs,
            boolean filterCum,
            float threshold,
            int n,
            InkSample[] d,
            float[] theta)
    {
        InkSample[]     nd = new InkSample[n];
        int             nFltSample = 0;
        float           cumAngle = 0;
           
        nd[nFltSample++] = d[0];
        for (int i=2; i<n; i++) {
            cumAngle += theta[i];
            if (filterAbs && Math.abs(theta[i]) < threshold) {
            } else if (filterCum && Math.abs(cumAngle) < threshold) {
            } else if (d[i-1] != null) {
                nd[nFltSample++] = d[i-1];
                cumAngle = 0;
            }
        }
        
        if (n > 1) {
            nd[nFltSample++] = d[n-1];
        }
        return CompactStrokeData(nFltSample, nd);
    }
       
    /**
     * Resamples a stroke s.t. it is composed of equal length segments
     *
     * @param http  HTMLwriter for diagnostics
     * @param n the number of points in the input to be resampled
     * @param resampLen the length of segments in the resampled stroke
     * @param d the InkSample data
     * @return an array of resampled ink data
     * @see InkSample
     * @see HTMLwriter
     */
    static public InkSample[] DaEqualizaFilta(
            HTMLwriter http,
            int n,
            InkSample[] d,
            float resampLen)
    {
        if (n <= 1) {
            return CompactStrokeData(n, d);
        }
        float   aSegCoeff[] = new float[n-1];
        float   bSegCoeff[] = new float[n-1];
        
        for (int i=0; i<n-1; i++) {
            float   dn = d[i+1].x - d[i].x;
            Point   p3 = d[i];
            Point   p4 = d[i+1];
            if (dn == 0) {
                aSegCoeff[i] = Float.POSITIVE_INFINITY;
                bSegCoeff[i] = Float.POSITIVE_INFINITY;
            } else {
                aSegCoeff[i] = (p4.y - p3.y)/dn;
                bSegCoeff[i] = (p3.y * p4.x - p4.y * p3.x)/dn;
            }
        }
        
        int     seg = 0;
        float   lSq = resampLen * resampLen;
        float   rangeTolerance = (float)(resampLen*.01);
        
        LinkedList  points = new LinkedList();
        int         nResampledPoints = 0;
        
        Point   p1 = d[0];
        points.add(new InkSample(p1)); nResampledPoints++;
        
        while (seg < n-1) {
// for each original segment
//     find next resampled point
// have a quadratic form for the intersection of a line of 
// known length from a known point to a line of known endpoints
// do a search for the first line segment for which this
// intersection fits into the segment
            float   a=1,
                    b=0;                   // coefficient of the line {y=aX+b}
            int     nextSeg = seg;
            Point   segBnd1=null;
            Point   segBnd2=p1;
            Point   nearestMiss = null;
            int     nearestSeg = seg;
            float   nearestMissDistance = Float.POSITIVE_INFINITY;
            
            boolean found = false;
            
            InkSample   newSample = null;
            
            if (http != null) {
                http.printbr("point "+nResampledPoints+" is "+seg+" p1 "+p1.x+", "+p1.y);
            }
            while (nextSeg < n-1) {
                a = aSegCoeff[nextSeg];
                b = bSegCoeff[nextSeg];
                segBnd1 = segBnd2;
                segBnd2 = d[nextSeg+1];
                if (http != null) {
                    http.printbr("&nbsp&nbsp try "+nextSeg+" b1 "+segBnd1.x+","+segBnd1.y+" b2 "+segBnd2.x+","+segBnd2.y+" a "+a+" b "+b);
                }
                if (a == Float.POSITIVE_INFINITY || b == Float.POSITIVE_INFINITY || Math.abs(a) > 10) {
// degenerate case: segment is a vertical stroke, x = c;
// also cover the case of a steep gradient which will be a bit on the ill-conditioned
// side ...
                    float   c = (d[nextSeg].x + d[nextSeg+1].x)/2;
                    float   xD = (c-p1.x);

                    float   root = (float)Math.sqrt(lSq - xD*xD);
                    float   yRn = p1.y + root;
                    float   yRp = p1.y - root;
                    
                    if (http != null) {
                        http.printbr("&nbsp&nbsp&nbsp&nbsp degen seg " + seg + " nextseg " + nextSeg + " x "+c+" yp "+yRp+" yn "+yRn);
                    }

                    if (InRange(yRp, segBnd1.y, segBnd2.y, rangeTolerance)) {
                        float   newY = p1.y;
                        found = true;
                        if (InRange(yRn, segBnd1.y, segBnd2.y, rangeTolerance)) {
// both possible interpolation points intersect this segment.
                            float npd, npu;
                            
//  interpolate to the closest point to an original point:
//                            npd = Math.min(
//                                       Math.abs(yRn-segBnd1.y),
//                                       Math.abs(yRn-segBnd2.y));
//                            npu = Math.min(
//                                       Math.abs(yRp-segBnd1.y),
//                                       Math.abs(yRp-segBnd2.y));
                            
//  interpolate to the closest point to the first encountered boundary, segBnd1
                            npd = Math.abs(yRn-segBnd1.y);
                            npu = Math.abs(yRp-segBnd1.y);
                            
                            if (npu > npd) {
                                newY = yRn;
                            } else {
                                newY = yRp;
                            }
                            newSample = new InkSample(Xtrapolator(a, b, c, newY), newY);
                        } else {
                            newSample = new InkSample(Xtrapolator(a, b, c, yRp), yRp);
                        }
                        seg = nextSeg;
                        break;
                    } else if (InRange(yRn, segBnd1.y, segBnd2.y, rangeTolerance)) {
                        found = true;
                        seg = nextSeg;
                        newSample = new InkSample(Xtrapolator(a, b, c, yRn), yRn);
                        break;
                    } else {
 // skip this trial and look at the next segment
 // keep a track of the nearest interpolation to fitting into a line,
 // in case we get a negative discriminant (geometric impossiblility)
 // this nearestMissDistance is a bit shonky... an absolute comparison slipping willynilly
 // between x  and y scales. possibly uclidean distance will be better... hopefully this
 // is just as a fallback, so we don't miss a point close to the boundary just
 // through ill-conditioning, ... and the shonkiness will be acceptable
                        float npd, npu;
                        npd = Math.min(
                                  Math.abs(yRn-segBnd1.y),
                                  Math.abs(yRn-segBnd2.y));
                        npu = Math.min(
                                  Math.abs(yRp-segBnd1.y),
                                  Math.abs(yRp-segBnd2.y));
                        if (npd < npu) {
                            if (npd < nearestMissDistance) {
                                nearestMissDistance = npd;
                                nearestSeg = nextSeg;
                                nearestMiss = new Point(Xtrapolator(a, b, c, yRn), yRn);
                            }
                        } else {
                            if (npu < nearestMissDistance) {
                                nearestMissDistance = npu;
                                nearestSeg = nextSeg;
                                nearestMiss = new Point(Xtrapolator(a, b, c, yRp), yRp);
                            }
                        }
                    }
                    
                } else {
// usual case solve a quadratic for the next point
                    float   xRn, xRp;     // possible new x points (quadratic roots) 
                    float   qA, qB, qC;   // coeffs of the quadratic form
                    float   discriminant;
                    
                    qA = a*a+1;
                    qB = 2*(a*b - p1.x - a*p1.y);
                    qC = p1.x*p1.x + (b-p1.y)*(b-p1.y) - lSq;

                    discriminant = qB*qB - 4*qA*qC;
                    if (discriminant >= 0) {
                        float   sqrtDisc = (float) Math.sqrt(discriminant);
                        float   newX=p1.x;
                        
                        xRp = (-qB + sqrtDisc) / (2*qA);
                        xRn = (-qB - sqrtDisc) / (2*qA);
                        if (http != null) {
                            http.printbr("&nbsp&nbsp&nbsp&nbsp normal seg " + nextSeg + " disc "+discriminant+" x "+xRp+","+xRn+" bnd "+segBnd1.x+","+segBnd2.x);
                        }
                        if (InRange(xRp, segBnd1.x, segBnd2.x, rangeTolerance)) {
                            found = true;
                            if (InRange(xRn, segBnd1.x, segBnd2.x, rangeTolerance)) {
// both roots are in: pick the best one:
                                float npu, npd;
                                
// pick the closest to an existing point
//                                npd = Math.min(
//                                           Math.abs(xRn-segBnd1.x),
//                                           Math.abs(xRn-segBnd2.x));
//                                npu = Math.min(
//                                           Math.abs(xRp-segBnd1.x),
//                                           Math.abs(xRp-segBnd2.x));

// pick the closest of to the first point of the segment, segBnd1
                                npd = Math.abs(xRn-segBnd1.x);
                                npu = Math.abs(xRp-segBnd1.x);

                                if (npu > npd) {
                                    newX = xRn;
                                } else {
                                    newX = xRp;
                                }

                            } else {
                                // this is the right one
                                newX = xRp;
                            }
                            newSample = new InkSample(newX, a*newX+b);
                            seg = nextSeg;
                            break;
                        } else if (InRange(xRn, segBnd1.x, segBnd2.x, rangeTolerance)) {
                            // this is the right one
                            found = true;
                            seg = nextSeg;
                            newX = xRn;
                            newSample = new InkSample(newX, a*newX+b);
                            break;
                        } else {
 // skip this trial and look at the next segment
 // keep a track of the nearest interpolation to fitting into a line,
 // in case we get a negative discriminant (geometric impossiblility)
 // this nearestMissDistance is a bit shonky... an absolute comparison slipping willynilly
 // between x  and y scales. possibly euclidean distance will be better... hopefully this
 // is just as a fallback, so we don't miss a point close to the boundary just
 // through ill-conditioning, ... and the shonkiness will be acceptable
                            float npd, npu;
                            npd = Math.min(
                                      Math.abs(xRn-segBnd1.x),
                                      Math.abs(xRn-segBnd2.x));
                            npu = Math.min(
                                      Math.abs(xRp-segBnd1.x),
                                      Math.abs(xRp-segBnd2.x));
                            if (npd < npu) {
                                if (npd < nearestMissDistance) {
                                    nearestMissDistance = npd;
                                    nearestMiss = new Point(xRn, a*xRn+b);
                                    nearestSeg = nextSeg;
                                }
                            } else {
                                if (npu < nearestMissDistance) {
                                    nearestMissDistance = npu;
                                    nearestMiss = new Point(xRp, a*xRp+b);
                                    nearestSeg = nextSeg;
                                }
                            }
                        }
                    } else {
// oooooops! we missed big time
// probably should phreak out and phuq off
// then again, it's possible that there may be an impossible interpolation between
// viable resampled points. perhaps we should keep searching for a bit. then again
// perhaps not... in the interests of reasonable computing time and minimizing
// combinatorial second guessing
                        if (http != null) {
                            http.printbr("negative discriminant: bailing, disc = "+discriminant);
                        }
// should vet the nearest miss, for not being too much of a miss. if it's way outta da
// field we probably should plow on.
// this mechanism sits over and above the boundary tolerance used in range checking.
// maybe that could be eliminated ... though it really only covers stuff that is very
// close to the mark ...
                        if (nearestMiss != null) {
                            newSample = new InkSample(nearestMiss);
                            nextSeg = nearestSeg;
                            found = true;
                            if (http != null) {
                                http.printbr("interpolating with near miss at "+nearestMiss.x+","+nearestMiss.y+" (dist "+nearestMissDistance+")");
                            }
                        }
                        break;
                    }
                }
                nextSeg++;
                
            }
            if (found) {
                points.add(newSample); nResampledPoints++;
                p1 = newSample;
                seg = nextSeg;
            } else if (nextSeg == n-1) {
                newSample = new InkSample(d[n-1]);
                points.add(newSample); nResampledPoints++;
                seg = nextSeg;
                break;
            } else {
                if (http != null) {
                    http.printbr("no interpolated point found: bailing pointlessly");
                }
                break;
            }
        }
        InkSample[] resampledPts = new InkSample[nResampledPoints];
        resampledPts = (InkSample[])(points.toArray(resampledPts));
        
        return CompactStrokeData(nResampledPoints, resampledPts);
    }
    
    /**
     * Tests whether x is in a given range, within a particular tolerance. True if the x value is in the
     * range, or if x is within +/- tol of either of the bounding points, b1 and b2
     *
     * @param x the x value in question
     * @param b1 the lower edge of the range
     * @param b2 the upper edge of the range
     * @param tol the tolerance value to apply to the test
     * @return extrapolated point
     */
    static public boolean InRange(float x, float b1, float b2, float tol)
    {
        if (b1 < b2) {
            return ((b1-tol)<=x) && (x<=(b2+tol));
        } else {
            return ((b2-tol)<=x) && (x<=(b1+tol));
        }
    }
    
    /**
     * extrapolates x from y (on the curve y = ax + b)from a line used for steep or vertical line with previously
     * extrapolated point, c
     *
     * @param a the gradient of the interpolating line
     * @param b the constant offset of the interpolating line
     * @param c a value to return in degenerate cases (such as infinite gradient or constant)
     * @param y the y value to extrapolate to
     * @return extrapolated point
     */
    static public float Xtrapolator(float a, float b, float c, float y)
    {
         if (a == Float.POSITIVE_INFINITY || b == Float.POSITIVE_INFINITY ||
                    a == Float.NEGATIVE_INFINITY || b == Float.NEGATIVE_INFINITY || a == 0) {
             return c;
         }
         return (y-b)/a;
    }
    
    /**
     * returns an array of segment lengths for the input InkSample data
     *
     * @param n the number of points in the input to be resampled
     * @param s the InkSample data
     * @return an array of segment lengths
     * @see InkSample
     */
    static public float[] SegmentLengths(int n, InkSample[] s)
    {
        float[] la = new float[n];
        float   lX = s[0].x;
        float   lY = s[0].y;
        la[0] = 0;

        for (int i=1; i<n; i++) {
            float   x = s[i].x;
            float   y = s[i].y;
            float   dX = x - lX;
            float   dY = y - lY;

            la[i] = (float)Math.sqrt(dX*dX + dY*dY);
            lX = x;
            lY = y;
        }
        return la;
    }
    
    
    /**
     * returns an array of tangent angles for the input InkSample data. Angles are measured in radians
     * <p>
     * The tangent at a particular sample point is the angle subtended by the previous two segments.
     * <p>
     * If a path is closed (i.e. the first point and last point are the same), the first tangent angle is that
     * subtended by the last two segments of the path, and the second tangent angle is that subtended
     * by the point of closure. 
     *
     * @param n the number of points in the input to be resampled
     * @param d the InkSample data
     * @param forceClosed forces the assumption that the path is a closed one.
     * @return an array of tangent angles
     * @see InkSample
     */
    static public float [] DelTheta(
            int n,
            InkSample d[],
            boolean forceClosed) throws Cow
    {
        if (n < 1) {
            throw new Cow("no point");
        }
        
        float[]     theta;
        float       tanThetaDiff;
        
        theta = new float[n];
 
        if (n < 2) {
            return theta;
//            throw new Cow("not much point");
        }
        
        theta[0] = Float.NaN;
        theta[1] = Float.NaN;

        float   lX = d[0].x;
        float   lY = d[0].y;
        float   ldX = 0;
        float   ldY = 0;
        
        boolean closedPath = false;
        
        if (forceClosed || d[n-1].equals(d[0])) {
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
                    tanThetaDiff = 0;
                    theta[i] = 0;
                } else {
                }
            } else {
            }

            float   tanumerator = dY*ldX - ldY*dX;
            float   tanominator = dX*ldX + dY*ldY;
            if (tanominator == 0) {
                if (tanumerator < 0) {
                    tanThetaDiff = Float.NEGATIVE_INFINITY;
                    theta[i] = - (float) Math.PI/2;
                } else if (tanumerator > 0) {
                    tanThetaDiff = Float.POSITIVE_INFINITY;
                    theta[i] = (float) Math.PI/2;
                } else {
                    tanThetaDiff = 0;
                    theta[i] = 0;
                }
            } else {
                tanThetaDiff = tanumerator / tanominator;
                theta[i] = (float)Math.atan(tanThetaDiff);
                if (tanThetaDiff < 0) {
                    if (tanumerator > 0) {
                        theta[i] += Math.PI;
                    }
                } else {
                    if (tanumerator < 0) {
                        theta[i] -= Math.PI;
                    }
                }
            }

            ldX = dX;
            ldY = dY;
            lX = x;
            lY = y;
        }
        return theta;
    }
    
    /**
     * Creates an array of gradients/derivatives, according to the x and y direction deltas passed as
     * parameters. Infinite gradients (dx == 0) are set to Float.NEGATIVE_INFINITY or Float.POSITIVE_INFINITY,
     * and any error value in a delta will give a gradient of Float.NaN
     *
     * @param dy an array of lengths along the y axis
     * @param dx an array of lengths along the x axis
     * @return an array of gradients, or null of there is a mismatch in the lengths of dx and dy
     */
    static public float[] DyDx(float dy[], float dx[])
    {
        if (dy.length != dx.length || dy.length == 0) {
            return null;
        }
        int     i;
        float[] dYdX = new float[dy.length];
        
        for (i=0; i<dy.length; i++) {
            if (dy[i] == Float.NaN || dx[i] == Float.NaN) {
                dYdX[i] = Float.NaN;
            } else if (dx[i] == 0) {
                if (dy[i] < 0) {
                    dYdX[i] = Float.NEGATIVE_INFINITY;
                } else {
                    dYdX[i] = Float.POSITIVE_INFINITY;
                }
            } else {
                dYdX[i] = dy[i]/dx[i];
            }
        }
        return dYdX;
    }
    
    /**
     * Normalize a length vector, such that the total length of the line has a value of 1.0
     *
     * @param dL an array of lengths
     * @return an array of normalized lengths
     */
    static public float[] NormalizeLengthVector(float []dL)
    {
        float[]     nudl = null;
        float       len = 0;
                
        nudl = new float[dL.length];
        for (int i=0; i<dL.length; i++) {
            len += dL[i];
        }
        for (int i=0; i<dL.length; i++) {
            nudl[i] = dL[i]/len;
        }
        return nudl;
    }
}
