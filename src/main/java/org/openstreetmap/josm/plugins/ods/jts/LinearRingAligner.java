package org.openstreetmap.josm.plugins.ods.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;

/**
 * The WayAligner aligns two linearRings according to a given tolerance.
 * A point in the second ring that is within 'tolerance' distance from a point
 * from the first ring, will get the coordinates of that point.
 * A lineSegment from either of the rings, that is within 'tolerance'
 * distance from a point on the other ring, will be split by adding that
 * point in between the start and end point of that segment.
 * 
 * @author gertjan
 *
 */
public class LinearRingAligner {
    private GeoUtil geoUtil;
    private LinearRing ring1;
    private LinearRing ring2;
    private SegmentIterator it1;
    private SegmentIterator it2;
    private Double tolerance;

    public LinearRingAligner(GeoUtil geoUtil, LinearRing ring1, LinearRing ring2,
            Double tolerance) {
        this.geoUtil = geoUtil;
        this.ring1 = ring1;
        this.ring2 = ring2;
        this.tolerance = tolerance;
    }
    
    public void run() {
        it1 = new SegmentIterator(geoUtil, ring1, true);
        it2 = new SegmentIterator(geoUtil, ring2, false);
        fix(it1, it2);
        fix(it2, it1);    
        if (it1.isModified()) {
            ring1 = it1.getResult();
        }
        if (it2.isModified()) {
            ring2 = it2.getResult();
        }
    }
    
    private void fix(SegmentIterator it11, SegmentIterator it21) {
        it11.reset(0);
        while (it11.hasNext()) {
            Coordinate c1 = it11.next().p0;
            it21.reset(0);
            while (it21.hasNext()) {
                LineSegment ls2 = it21.next();
                if (ls2.distance(c1) < tolerance) {
                    if (ls2.p0.distance(c1) < tolerance) {
                        it21.updateStartPoint(c1);
                    }
                    else if (ls2.p1.distance(c1) < tolerance) {
                        it21.updateEndPoint(c1);
                    }
                    else {
                        it21.snap(c1, tolerance);
                    }
                }
            }
        }
    }

    public boolean ring1Modified() {
        return it1.isModified();
    }
    
    public boolean ring2Modified() {
        return it2.isModified();
    }

    public LinearRing getRing1() {
        return ring1;
    }

    public LinearRing getRing2() {
        return ring2;
    }
}
