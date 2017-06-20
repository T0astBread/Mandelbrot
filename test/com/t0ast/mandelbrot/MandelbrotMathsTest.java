/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.mandelbrot;

import org.apache.commons.math3.complex.Complex;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author T0astBread
 */
public class MandelbrotMathsTest
{

    /**
     * Test of isInSet method, of class MandelbrotMaths.
     */
    @Test
    public void testIsInSet()
    {
        MandelbrotMaths m = new MandelbrotMaths();
        m.setResolution(100);
        Assert.assertTrue(m.isInSet(0, 0));
        Assert.assertTrue(m.isInSet(-1, 0));
        Assert.assertTrue(m.isInSet(0, 1));
        Assert.assertTrue(m.isInSet(-2, 0));
        Assert.assertFalse(m.isInSet(0, 2));
        Assert.assertFalse(m.isInSet(3, 0));
        Assert.assertFalse(m.isInSet(1, 0));
        Assert.assertFalse(m.isInSet(1, 1));
    }
    
//    @Test
//    public void testLen()
//    {
//        MandelbrotMaths m = new MandelbrotMaths();
//        Assert.assertEquals(0, m.len(new Complex(0)), 0);
//        Assert.assertEquals(2, m.len(new Complex(0, 2)), 0);
//        Assert.assertEquals(2, m.len(new Complex(2, 0)), 0);
//        Assert.assertEquals(1, m.len(new Complex(0, 1)), 0);
//        Assert.assertEquals(2.82842712, m.len(new Complex(2, 2)), 1e-8);
//    }
    
}
