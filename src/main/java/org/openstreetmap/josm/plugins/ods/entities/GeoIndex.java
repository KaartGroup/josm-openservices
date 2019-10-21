package org.openstreetmap.josm.plugins.ods.entities;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

public interface GeoIndex<T extends Entity> extends Index<T> {

    public List<T> intersection(Geometry geometry);

}