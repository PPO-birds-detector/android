package io.github.ppo_birds_detector.android;

/**
 * Created by ike on 14/06/15.
 */

public class Bezier {
    private final float cx;
    private final float bx;
    private final float ax;
    private final float cy;
    private final float by;
    private final float ay;

    public Bezier(float p1x, float p1y, float p2x, float p2y) {
        // Calculate the polynomial coefficients, implicit first and last control points are (0,0) and (1,1).
        cx = 3.0f * p1x;
        bx = 3.0f * (p2x - p1x) - cx;
        ax = 1.0f - cx - bx;
        cy = 3.0f * p1y;
        by = 3.0f * (p2y - p1y) - cy;
        ay = 1.0f - cy - by;
    }

    /**
     * The epsilon value we pass to UnitBezier::solve given that the animation is going to run over |dur| seconds.
     * The longer the animation, the more precision we need in the timing function result to avoid ugly discontinuities.
     * http://svn.webkit.org/repository/webkit/trunk/Source/WebCore/page/animation/AnimationBase.cpp
     */
    private float solveEpsilon(float duration) {
        return 1.0f / (200.0f * duration);
    }

    private float sampleCurveX(float t) {
        // `ax t^3 + bx t^2 + cx t' expanded using Horner's rule.
        return ((ax * t + bx) * t + cx) * t;
    }

    private float sampleCurveY(float t) {
        return ((ay * t + by) * t + cy) * t;
    }

    private float sampleCurveDerivativeX(float t) {
        return (3.0f * ax * t + 2.0f * bx) * t + cx;
    }

    /**
     * Given an x value, find a parametric value it came from.
     * @param x {number} value of x along the bezier curve, 0.0 <= x <= 1.0
     * @param epsilon {number} accuracy limit of t for the given x
     * @return {number} the t value corresponding to x
     */
    private float solveCurveX(float x, float epsilon) {
        float t0, t1, t2, x2, d2;
        int i;

        // First try a few iterations of Newton's method -- normally very fast.
        for (t2 = x, i = 0; i < 8; i++) {
            x2 = sampleCurveX(t2) - x;
            if (Math.abs(x2) < epsilon) {
                return t2;
            }
            d2 = sampleCurveDerivativeX(t2);
            if (Math.abs(d2) < 1e-6) {
                break;
            }
            t2 = t2 - x2 / d2;
        }

        // Fall back to the bisection method for reliability.
        t0 = 0.0f;
        t1 = 1.0f;
        t2 = x;

        if (t2 < t0) {
            return t0;
        }
        if (t2 > t1) {
            return t1;
        }

        while (t0 < t1) {
            x2 = sampleCurveX(t2);
            if (Math.abs(x2 - x) < epsilon) {
                return t2;
            }
            if (x > x2) {
                t0 = t2;
            } else {
                t1 = t2;
            }
            t2 = (t1 - t0) * 0.5f + t0;
        }

        // Failure.
        return t2;
    }

    /**
     * @param x {number} the value of x along the bezier curve, 0.0 <= x <= 1.0
     * @param epsilon {number} the accuracy of t for the given x
     * @return {number} the y value along the bezier curve
     */
    private float solve(float x, float epsilon) {
        return sampleCurveY(solveCurveX(x, epsilon));
    }

    // public interface --------------------------------------------

    /**
     * Find the y of the cubic-bezier for a given x with accuracy determined by the animation duration.
     * @param x {number} the value of x along the bezier curve, 0.0 <= x <= 1.0
     * @param duration {number} the duration of the animation in milliseconds
     * @return {number} the y value along the bezier curve
     */
    public float getValue(float x, float duration) {
        return solve(x, solveEpsilon(duration));
    }
}