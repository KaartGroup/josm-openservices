package org.openstreetmap.josm.plugins.ods.jts;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.crs.UnclosedWayException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Boundary {
    private final static GeoUtil geoUtil = GeoUtil.getInstance();
    
    private LinearRing ring;
    private MultiPolygon multiPolygon;
    private Envelope envelope;
    private Bounds bounds;
    private boolean rectangular;

    public Boundary(Way way) {
        super();
        rectangular = false;
        if (!way.isClosed()) {
            throw new IllegalArgumentException();
        }
        try {
            this.ring = geoUtil.toLinearRing(way);
        } catch (UnclosedWayException e) {
            // Won't happen because we know the way is closed
        }
        Polygon polygon = geoUtil.createPolygon(ring, null);
        this.multiPolygon = geoUtil.toMultiPolygon(polygon);
        this.envelope = ring.getEnvelopeInternal();
    }
    
    public Boundary(Bounds bounds) {
        rectangular = true;
        this.bounds = bounds;
        this.envelope = new Envelope(bounds.getMinLon(), bounds.getMaxLon(),
                bounds.getMinLat(), bounds.getMaxLat());
        List<Coordinate> coords = new ArrayList<>(5);
        coords.add(new Coordinate(bounds.getMinLon(), bounds.getMinLat()));
        coords.add(new Coordinate(bounds.getMinLon(), bounds.getMaxLat()));
        coords.add(new Coordinate(bounds.getMaxLon(), bounds.getMaxLat()));
        coords.add(new Coordinate(bounds.getMaxLon(), bounds.getMinLat()));
        coords.add(new Coordinate(bounds.getMinLon(), bounds.getMinLat()));
        this.ring = geoUtil.toLinearRing(coords);
        Polygon polygon = geoUtil.createPolygon(ring, null);
        this.multiPolygon = geoUtil.toMultiPolygon(polygon);        
    }

    public boolean isRectangular() {
        return rectangular;
    }
    
    public LinearRing getRing() {
        return ring;
    }
    
    public MultiPolygon getMultiPolygon() {
        return multiPolygon;
    }
    
    public Envelope getEnvelope() {
        return envelope;
    }
    
    public Bounds getBounds() {
        Envelope e = getEnvelope();
        if (bounds == null) {
            bounds = new Bounds(e.getMinY(), e.getMinX(), e.getMaxY(), e.getMaxX());
        }
        return bounds;
    }
    
//    public void filter(DataSet dataSet) {
//        MultiPolygonFilter filter = new MultiPolygonFilter(polygon);
//        filter.filter(dataSet);
//    }
}
