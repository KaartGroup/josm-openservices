package org.openstreetmap.josm.plugins.ods.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.osm.OsmPrimitiveFactory;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Default GeometryMapper implementation.
 * 
 * @author Gertjan Idema
 *
 */
public class DefaultGeometryMapper<T extends Entity> implements GeometryMapper<T> {
  private OsmPrimitiveFactory primitiveBuilder;
  private String targetPrimitive;
  private final Boolean merge = false;
  
  @Override
  public final void setObjectFactory(OsmPrimitiveFactory primitiveBuilder) {
    this.primitiveBuilder = primitiveBuilder;
  }

  public final void setTargetPrimitive(String targetPrimitive) {
    this.targetPrimitive = targetPrimitive;
  }

  @Override
  public List<OsmPrimitive> createPrimitives(Geometry geometry,
      Map<String, String> tags, DataSet dataSet) {
    if (geometry instanceof GeometryCollection && !targetPrimitive.equals("MULTIPOLYGON")) {
      return createPrimitives((GeometryCollection)geometry, tags, dataSet);
    }
    return Collections.singletonList(createPrimitive(geometry, tags, dataSet));
  }
  
  protected OsmPrimitive createPrimitive(Geometry geometry,
      Map<String, String> tags, DataSet dataSet) {
    OsmPrimitive primitive = null;
    if (targetPrimitive.equals("WAY")) {
      if (geometry instanceof LineString) {
        primitive = primitiveBuilder.buildWay((LineString)geometry, tags);
      }
      else if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon) geometry;
        if (polygon.getNumInteriorRing() == 0) {
          primitive = primitiveBuilder.buildWay(polygon, tags);
        }
        else {
          primitive = primitiveBuilder.buildMultiPolygon(polygon, tags);
        }
      }
    } else if (targetPrimitive.equals("MULTIPOLYGON")) {
      if (geometry instanceof Polygon) {
        primitive = primitiveBuilder.buildMultiPolygon((Polygon) geometry, tags);
      } else if (geometry instanceof MultiPolygon) {
        primitive = primitiveBuilder.buildMultiPolygon((MultiPolygon) geometry, tags);
      }
    }
    else if (targetPrimitive.equals("NODE")) {
      primitive = primitiveBuilder.buildNode((Point)geometry, tags, merge);
    } else if (targetPrimitive.equals("POLYGON")) {
      primitive = primitiveBuilder.buildArea((MultiPolygon)geometry, tags);
    }
    if (primitive != null) {
      for (Entry<String, String> entry : tags.entrySet()) {
        primitive.put(entry.getKey(), entry.getValue());
      }
    }
    return primitive;
  }

  private List<OsmPrimitive> createPrimitives(GeometryCollection gc,
    Map<String, String> tags, DataSet dataSet) {
    List<OsmPrimitive> primitives = new ArrayList<>(gc.getNumGeometries());
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      primitives.add(createPrimitive(gc.getGeometryN(i), tags, dataSet));
    }
    return primitives;
  }
}
