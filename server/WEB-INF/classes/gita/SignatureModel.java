/*
 * SignatureModel.java
 *
 * Created on 24 February 2005, 16:35
 *
 */

package gita;
import java.util.LinkedList;
 
/**
 * Base class for a model signature template generated from training data
 *
 * @author dak
 */
public class SignatureModel {
    
    /**
     * Creates a new instance of SignatureModel
     */
    public SignatureModel()
    {
    }
    
    /** derivative of tangent angle wrt arc len */
    float   curvature[] = null;
    /** corresponding cumulative arc length */
    float   arclen[] = null;
    /** the composite averaged signature template */
    LinkedList<SignatureStroke> strokes = null;
}
