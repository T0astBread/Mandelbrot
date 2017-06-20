/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.mandelbrot;

/**
 *
 * @author T0astBread
 */
public class MandelbrotMaths
{
    private static final int BAILOUT_VALUE = 4;
    
    private int resolution;

    public int getResolution()
    {
        return resolution;
    }

    public void setResolution(int resolution)
    {
        this.resolution = resolution;
    }
    
    public boolean isInSet(double re, double im)
    {
        return bailoutSteps(re, im) == -1;
    }
    
    public int bailoutSteps(double re, double im)
    {
        return bailoutSteps(re, im, this.resolution);
    }
    
    //NEW APPROACH:
    public int bailoutSteps(double reC, double imC, int n)
    {
        double reZ = 0, imZ = 0;
        for(int i = 0; i < n; i++)
        {
            //SQUARE ROOT:
            //(reZ + imZ * i)^2
            //reZ^2 + 2 * reZ * imZ * i + (imZ * i)^2
            //reZ^2 + 2reZ * imZ * i - imZ^2
            //reZNew = reZ^2 - imZ^2
            //imZNew = 2reZ * imZ
            double reZNew = reZ * reZ - imZ * imZ;
            double imZNew = 2 * reZ * imZ;
            reZ = reZNew;
            imZ = imZNew;
            
            //ADD C:
            reZ += reC;
            imZ += imC;
            
            //CHECK FOR BAILOUT:
            if((reZ*reZ + imZ*imZ) > BAILOUT_VALUE) return i;
        }
        return -1;
    }

//    OLD APPROACH:
//
//    public boolean isInSet(Complex c)
//    {
//        try
//        {
//            z(this.resolution, c);
//        }
//        catch(Bailout ex)
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    private boolean inBounds(Complex c)
//    {
////        return !c.isInfinite() && (c.isNaN() || Math.abs(c.getReal()) < 2 && Math.abs(c.getImaginary()) < 2);
//        return !c.isInfinite() && (c.isNaN() || len(c) <= 2);
//    }
//    
//    public double len(Complex c)
//    {
//        return Math.sqrt(Math.pow(c.getReal(), 2) + Math.pow(c.getImaginary(), 2));
//    }
//    
//    /**
//     * Calculates z of c OR throws a {@link com.t0ast.mandelbrot.MandelbrotMaths.Bailout} if a bailout occurs
//     * @param n The resolution of the calculation
//     * @param c
//     * @return
//     * @throws com.t0ast.mandelbrot.MandelbrotMaths.Bailout 
//     */
//    public Complex z(int n, Complex c) throws Bailout
//    {
//        return n == 0 ? new Complex(0, 0) : sqr(checkForBailout(z(n - 1, c))).add(c);
//    }
//    
//    private Complex sqr(Complex c)
//    {
//        return c.multiply(c);
//    }
//    
//    public Complex checkForBailout(Complex c) throws Bailout
//    {
//        if(!inBounds(c)) throw new Bailout();
//        return c;
//    }
//    
//    public static class Bailout extends Throwable {}
}
