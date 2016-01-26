/*
 * SignaturEater.java
 *
 * Created on 24 February 2005, 16:20
 */

package gita;
 
import java.util.LinkedList;
import java.lang.Math;

/**
 * Class to do main top level processing of signature data.
 *<p>
 * At the moment, still in development.
 * <ul>
 *  <li>Generates a model signature for matching from a set of training data
 *  <li>Eats signature data and spits out validations of such against a model signature
 * </ul>
 *
 * @author dak
 */
public class SignaturEater
{
    /** 
     * A list of "keying" points that a signature template has. These would be significant points
     * that have a numerical certainty of existing in some data at a particular point.
     */
    public class KeypointNode
    {
        /**
         * creates a new KeypointNode
         */
        public KeypointNode()
        {
            keys = new LinkedList();
        }

        /**
         * calculate the mean and variance for a set of keypoints
         */
        public void sumAte()
        {
            meanArk = 0;
            varArk = 0;
            double   sumA = 0;
            double   sumA2 = 0;
            float   nArk = 0;
            for (SignatureSample s:keys) {
                nArk++;
                sumA += s.arklen;
                sumA2 += s.arklen * s.arklen;
            }
            meanArk = sumA / nArk;
            varArk = Math.sqrt((sumA2/nArk) - (meanArk*meanArk));
        }

        LinkedList<SignatureSample>  keys = null;
        double   meanArk = 0;
        double   varArk = 0;
    }
    
    /**
     * Creates a new instance of SignaturEater
     */
    public SignaturEater()
    {
    }

    
    /**
     * builds a new master template from an input list of signature data
     *<p>
     * Questions and requirements for/from/about a master signature template
     *<ul>
     *  <li> has (at least) as many substroke keypoints as the maximum no of substroke keypoints in any of the sigs
     *  <li> get a rough alignment of substroke keypoints
     *  <li> get a set of mean substroke keypoints
     *  <li> get the variance for each element of this set
     *  <li> get a ranked significance rating of the keypoints depending on the number of signatures that have this as a keypoint
     *  <li> case where the substrokes align their beginning and end in different spots is a bit tricky
     *  <li> align peaks ???? <b>how do we scale this</b>
     *  <li> calculate the mean uberSig
     *  <li> calculate some measure of variance within the mean uberSig
     *</ul>
     */
    public void buildSignatureModel(LinkedList<SignatureContainer> sigs)
    {
        
        for (SignatureContainer s:sigs) {
            
        }
        
    }
    
}
