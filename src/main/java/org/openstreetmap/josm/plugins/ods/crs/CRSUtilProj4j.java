package org.openstreetmap.josm.plugins.ods.crs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.tools.I18n;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

//import org.osgeo.proj4j.CoordinateReferenceSystem;

/**
 * A Proj4j based implementation of CRSUtils. The opengis Mathtransform has
 * issues with EPSG:28992
 * 
 * @author gertjan
 * 
 */
public class CRSUtilProj4j extends CRSUtil {
    private final static Long OSM_SRID = 4326L;
    private final static JTSCoordinateTransformFactory ctFactory = new Proj4jCRSTransformFactory();
    private final static PrecisionModel OSM_PRECISION_MODEL = new PrecisionModel(
            10000000);
    public final static GeometryFactory OSM_GEOMETRY_FACTORY = new GeometryFactory(
            OSM_PRECISION_MODEL, OSM_SRID.intValue());
    private static Map<CoordinateReferenceSystem, JTSCoordinateTransform> toOsmTransforms = new HashMap<>();
    private static Map<CoordinateReferenceSystem, JTSCoordinateTransform> fromOsmTransforms = new HashMap<>();

    public synchronized Geometry transform(SimpleFeature feature)
            throws CRSException {
        JTSCoordinateTransform transform = getToOsmTransform(feature.getType()
                .getCoordinateReferenceSystem());
        try {
            return transform.transform((Geometry) feature.getDefaultGeometry());
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        }
    }

    private static JTSCoordinateTransform getToOsmTransform(
            CoordinateReferenceSystem crs) {
        JTSCoordinateTransform transform = toOsmTransforms.get(crs);
        if (transform == null) {
            transform = createToOsmTransform(crs);
        }
        return transform;
    }

    private static synchronized JTSCoordinateTransform createToOsmTransform(
            CoordinateReferenceSystem crs) {
        Long sourceSRID = getSRID(crs);
        JTSCoordinateTransform transform = ctFactory
                .createJTSCoordinateTransform(sourceSRID, OSM_SRID, 10000000.0);
        toOsmTransforms.put(crs, transform);
        return transform;
    }

    private static Long getSRID(CoordinateReferenceSystem crs) {
        String srs = CRS.toSRS(crs);
        return Long.parseLong(srs.substring(5));
    }

    private static JTSCoordinateTransform getFromOsmTransform(
            CoordinateReferenceSystem crs) {
        JTSCoordinateTransform transform = fromOsmTransforms.get(crs);
        if (transform == null) {
            transform = createFromOsmTransform(crs);
        }
        return transform;
    }

    private static synchronized JTSCoordinateTransform createFromOsmTransform(
            CoordinateReferenceSystem crs) {
        Long sourceSRID = getSRID(crs);
        JTSCoordinateTransform transform = ctFactory
                .createJTSCoordinateTransform(OSM_SRID, sourceSRID);
        fromOsmTransforms.put(crs, transform);
        return transform;
    }

    public static Envelope toEnvelope(Bounds bounds) {
        return new Envelope(bounds.getMinLon(), bounds.getMaxLon(),
                bounds.getMinLat(), bounds.getMaxLat());
    }

    /**
     * Create a ReferencedEnvelope from a Josm bounds object, using the supplied
     * CoordinateReferenceSystem
     * 
     * @param crs
     * @param bounds
     * @return
     * @throws TransformException
     */
    public synchronized ReferencedEnvelope createBoundingBox(CoordinateReferenceSystem crs,
            Bounds bounds) throws CRSException {
        Envelope envelope = toEnvelope(bounds);
        JTSCoordinateTransform transform;
        try {
            transform = getFromOsmTransform(crs);
            Envelope targetEnvelope = transform.transform(envelope);
            return new ReferencedEnvelope(targetEnvelope, getCrs(4326L));
        } catch (MismatchedDimensionException e) {
            throw new CRSException(I18n.tr(e.getMessage()));
        }
    }
}
