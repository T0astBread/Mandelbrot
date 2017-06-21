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
public class RenderConfig
{
    private boolean debug;
    private int nrOfRenderingThreads;
    private float resulutionScaling, renderScale;

    public RenderConfig()
    {
        this(false, 4, .25f, 1);
    }

    public RenderConfig(boolean debug, int nrOfRenderingThreads, float resulutionScaling, float renderScale)
    {
        this.debug = debug;
        this.nrOfRenderingThreads = nrOfRenderingThreads;
        this.resulutionScaling = resulutionScaling;
        this.renderScale = renderScale;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public int getNrOfRenderingThreads()
    {
        return nrOfRenderingThreads;
    }

    public float getResulutionScaling()
    {
        return resulutionScaling;
    }

    public float getRenderScale()
    {
        return renderScale;
    }
}
