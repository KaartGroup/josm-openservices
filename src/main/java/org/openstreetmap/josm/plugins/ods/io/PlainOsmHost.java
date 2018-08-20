package org.openstreetmap.josm.plugins.ods.io;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.tools.I18n;

public class PlainOsmHost implements OsmHost {

    @Override
    public String getHostString() {
        String host = Preferences.main().get("osm-server.url");
        if (host == null) {
            host = "https://api.openstreetmap.org/api";
        }
        return host;
    }

    @Override
    public boolean supportsPolygon() {
        return false;
    }

    @Override
    public OsmServerReader getServerReader(DownloadRequest request) {
        if (!request.getBoundary().isRectangular()) {
            throw new UnsupportedOperationException(I18n.tr(
                    "Polygon downloads are not supported for this host: {0}",
                    getHostString()));
        }
        return new BoundingBoxDownloader(request.getBoundary().getBounds());
    }


}
