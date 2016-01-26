/*
 * Furrier.java
 *
 * Created on 27 October 2004, 02:27
 */

package gita;
 
import java.lang.*;
import zone.HTMLwriter;

/**
 * Class to perform and hold fourier transform data from floating point arrays.
 *<p>
 * @author  David Karla
 */
public class Furrier {
    
    /**
     * Creates a new instance of Furrier
     */
    public Furrier()
    {
        mag = null;
        phs = null;
        xRe = null;
        xIm = null;
        n = 0;
        log2n = 0;
    }
    
    /**
     * Calculates the fft of real data, yielding the magnitude and phase of the result
     *<p>
     * Input length not necessarily a power of 2.
     * sets the global variables, <i>n</i>, the dimension of the complex coefficients,
     * and <i>log2n</i>, the log base 2 of <i>n</i> and
     * allocates the global array variables <i>xRe</i> and <i>Im</i> which will store the complex
     * coefficients of the result. Result is stored in the global arrays <i>mag</i> and <i>phs</i>, the magnitude and phase
     * of the result ...
     *
     * @param funkshen arrays of real coefficients
     * @param http HTMLwriter for diagnostic output
     * @return false if any disaster not otherwise trapped, else true
     * @see HTMLwriter
     */
    public boolean TrancefumRealToMagPhs(HTMLwriter http, float funkshen[])
    {
// find an n that is a power of 2: funkshen.length aint necessarily so...
//        log2n = (int)(Math.log(n)/Math.log(2));
        n = 1;
        log2n=0;
        for (int i=0; i<31; i++) {
            if (n >= funkshen.length) {
                break;
            }
            n <<= 1;
            log2n++;
        }
        
        if (n < funkshen.length) { // funkshen length must be way ff scale
            return false;
        }
       
        xRe = new float[n];
        xIm = new float[n];
       
        for (int i = 0; i < funkshen.length; i++) {
            xRe[i] = funkshen[i];
//           http.printbr("xRe["+i+"]="+xRe[i]);
           xIm[i] = 0.0f;
        }
        for (int i = funkshen.length; i < n; i++) {
           xRe[i] = xIm[i] = 0.0f;
        }
        
        FFTComplexToComplex(null, xRe, xIm);
       
        mag = new float[n];
        phs = new float[n];
       
        mag[0] = (float) (Math.sqrt(xRe[0]*xRe[0] + xIm[0]*xIm[0]))/n;
	phs[0] = 0;
        for (int i = 1; i < n; i++) {
            mag[i] = 2 * (float) (Math.sqrt(xRe[i]*xRe[i] + xIm[i]*xIm[i]))/n;
            phs[i] = (float) java.lang.Math.atan2(-xIm[i], xRe[i]);
        }

        return true;
    }
    
    /**
     * Calculates the fft of complex data, yielding the magnitude and phase of the result
     *<p>
     * Input length not necessarily a power of 2.
     * sets the global variables, <i>n</i>, the dimension of the complex coefficients,
     * and <i>log2n</i>, the log base 2 of <i>n</i> and
     * allocates the global array variables <i>xRe</i> and <i>Im</i> which will store the complex
     * coefficients of the result. Result is stored in the global arrays <i>mag</i> and <i>phs</i>, the magnitude and phase
     * of the result ...
     *
     * @param funkRe array of real part of input
     * @param funkIm array of real part of input
     * @return false if any disaster not otherwise trapped, else true
     */
    public boolean TrancefumComplexToMagPhs(HTMLwriter http, float funkRe[], float funkIm[])
    {
// find an n that is a power of 2: funkshen.length aint necessarily so...
//        log2n = (int)(Math.log(n)/Math.log(2));
        if (funkRe.length != funkIm.length) {
            return false;
        }
        
        n = 1;
        log2n=0;
        for (int i=0; i<31; i++) {
            if (n >= funkRe.length) {
                break;
            }
            n <<= 1;
            log2n++;
        }
        
        if (n < funkRe.length) { // funkshen length must be way ff scale
            return false;
        }
       
        xRe = new float[n];
        xIm = new float[n];
       
        for (int i = 0; i < funkRe.length; i++) {
            xRe[i] = funkRe[i];
//           http.printbr("xRe["+i+"]="+xRe[i]);
           xIm[i] = funkIm[i];
        }
        for (int i = funkRe.length; i < n; i++) {
           xRe[i] = xIm[i] = 0.0f;
        }
        
        FFTComplexToComplex(null, xRe, xIm);
       
        mag = new float[n];
        phs = new float[n];
       
        mag[0] = (float) (Math.sqrt(xRe[0]*xRe[0] + xIm[0]*xIm[0]))/n;
	phs[0] = 0;
        for (int i = 1; i < n; i++) {
            mag[i] = 2 * (float) (Math.sqrt(xRe[i]*xRe[i] + xIm[i]*xIm[i]))/n;
            phs[i] = (float) java.lang.Math.atan2(-xIm[i], xRe[i]);
        }

        return true;
    }

    /**
     * Performs a bit reversal on integer input
     *
     * @param j bits to be reversed
     * @return bit reversed result
     */
    private int bitrev(int j)
    {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= log2n; i++) {
            j2 = j1/2;
            k  = 2*k + j1 - 2*j2;
            j1 = j2;
        }
        return k;
    }
    
    /**
     * Performs an fft of complex data, yielding the complex result of the input 
     *<p>
     * The complex result is returned destructively in the passed arrays, <i>xRe</i>, <i>xIm</i>
     *<p>
     * Assume that <i>n</i>, which is the dimension of <i>xRe</i> and <i>xIm</i> is already set, and is a power of 2,
     * and that <i>log2n</i>, the log base 2 of <i>n</i> is set
     * @param xRe real part of complex input
     * @param xIm imaginary part of complex input
     * @return false if any disaster not otherwise trapped or true if successful
     */
    public boolean FFTComplexToComplex(HTMLwriter http, float xRe[], float xIm[])
    {
        int n2 = n/2;
        int nu1 = log2n - 1;
        float tr, ti, p, arg, c, s;
        int k = 0;

        for (int l = 1; l <= log2n; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitrev (k >> nu1);
                    arg = 2 * (float) Math.PI * p / n;
                    c = (float) Math.cos (arg);
                    s = (float) Math.sin (arg);
                    tr = xRe[k+n2]*c + xIm[k+n2]*s;
                    ti = xIm[k+n2]*c - xRe[k+n2]*s;
                    xRe[k+n2] = xRe[k] - tr;
                    xIm[k+n2] = xIm[k] - ti;
                    xRe[k] += tr;
                    xIm[k] += ti;
//                    http.printbr("l="+l+",k="+k+",i="+i+",p="+p+",arg="+arg+",xre[k]="+xRe[k]+",xim[k]="+xIm[k]+",xRe[k+n2]="+xRe[k+n2]+",xRe[k+n2]"+xRe[k+n2]);
                    k++;
                    
                    
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 = n2/2;
        }
        k = 0;
        int r;
        while (k < n) {
            r = bitrev (k);
            if (r > k) {
                tr = xRe[k];
                ti = xIm[k];
                xRe[k] = xRe[r];
                xIm[k] = xIm[r];
                xRe[r] = tr;
                xIm[r] = ti;
            }
            k++;
        }

        return true;
    }

    /**
     * Performs an fft of complex data, yielding the complex result of the input 
     *<p>
     * The complex result is returned destructively in the passed arrays, <i>ar</i>, <i>ai</i>
     *<p>
     * @param ar real part of complex input
     * @param ai imaginary part of complex input
     * @param sign direction to go in
     * @param n length of input data
     */
    public static void FFTComplexToComplex2(int sign, int n,
                                        float ar[], float ai[]) {
        float scale = (float)Math.sqrt(1.0f/n);

// code ripped from FftLab
//
        int i,j;
        for (i=j=0; i<n; ++i) {
            if (j>=i) {
	            float tempr = ar[j]*scale;
	            float tempi = ai[j]*scale;
	            ar[j] = ar[i]*scale;
	            ai[j] = ai[i]*scale;
	            ar[i] = tempr;
	            ai[i] = tempi;
            }
            int m = n/2;
            while (m>=1 && j>=m) {
	            j -= m;
	            m /= 2;
            }
            j += m;
        }
    
        int mmax,istep;
        for (mmax=1,istep=2*mmax; mmax<n; mmax=istep,istep=2*mmax) {
            float delta = (float)sign*3.141592654f/(float)mmax;
            for (int m=0; m<mmax; ++m) {
	            float w = (float)m*delta;
	            float wr = (float)Math.cos(w);
	            float wi = (float)Math.sin(w);
	            for (i=m; i<n; i+=istep) {
	                j = i+mmax;
	                float tr = wr*ar[j]-wi*ai[j];
	                float ti = wr*ai[j]+wi*ar[j];
	                ar[j] = ar[i]-tr;
	                ai[j] = ai[i]-ti;
	                ar[i] += tr;
	                ai[i] += ti;
	            }
            }
            mmax = istep;
        }
    }
    
    /**
     * Performs an fft of real data, yielding the magnitude and phase result of the input 
     *<p>
     * Not really using this one, and it doesn't actually do much but serve as a bit of sample code
     * for the others!
     *<p>
     * @param func input data
     */
    void FFTRealMag1(double func[])
    // code ripped from falstad fourier applet
    {
        int maxTerms = 160;
        final int sampleCount = 720;
        final int halfSampleCount = sampleCount/2;
        final double halfSampleCountFloat = sampleCount/2;
        double magcoef[]=new double[maxTerms];
        double phasecoef[]=new double[maxTerms];
        final double pi = 3.14159265358979323846;
        final double step = 2 * pi / sampleCount;

        int x, y;
        double epsilon = .00001;
        for (y = 0; y != maxTerms; y++) {
            double coef = 0;
            for (x = 0; x != sampleCount+1; x++) {
                int simp = (x == 0 || x == sampleCount) ? 1 : ((x&1)+1)*2;
                double s = java.lang.Math.cos(step*(x-halfSampleCount)*y);
                coef += s*func[x]*simp;
            }
            // simpson = 2pi/(3*sampleCount) (f(0) + 4f(1) + 2f(2) ...)
            // integral(...)/pi
            // result = coef * 2/3*sampleCount
            double acoef = coef*(2.0/(3.0*sampleCount));
            //System.out.print("acoef " + y + " " + coef + "\n");
            coef = 0;
            for (x = 0; x != sampleCount+1; x++) {
                int simp = (x == 0 || x == sampleCount) ? 1 : ((x&1)+1)*2;
                double s = java.lang.Math.sin(step*(x-halfSampleCount)*y);
                coef += s*func[x]*simp;
            }
            double bcoef = coef*(2.0/(3.0*sampleCount));
            if (acoef < epsilon && acoef > -epsilon) acoef = 0;
            if (bcoef < epsilon && bcoef > -epsilon) bcoef = 0;
            if (y == 0) {
                magcoef[0] = acoef / 2;
                phasecoef[0] = 0;
            } else {
                magcoef[y] = java.lang.Math.sqrt(acoef*acoef+bcoef*bcoef);
                phasecoef[y] = java.lang.Math.atan2(-bcoef, acoef);
            }
            // System.out.print("phasecoef " + phasecoef[y] + "\n");
        }
    }
    
    /** actual length of data to be transformed */
    private int         n;
    /** the log base 2 of <i>n</i> */
    private int         log2n;
    /** magnitude of transform result    */
    public float[]      mag=null;
    /** phase of transform result     */
    public float[]      phs=null;
    /** real part of transform result */
    public float[]      xRe=null;
    /** imaginary part of transform result */
    public float[]      xIm=null;
}
