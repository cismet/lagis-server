/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.lagis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cidsx.server.api.types.SearchInfo;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class AlkisLandparcelSearch extends GeomServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AlkisLandparcelSearch.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AlkisLandparcelSearch object.
     */
    public AlkisLandparcelSearch() {
        super(initSearchInfo());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static SearchInfo initSearchInfo() {
        final SearchInfo searchInfo = new SearchInfo();
        return searchInfo;
    }

    @Override
    public String getCrs() {
        return "EPSG:25832";
    }

    @Override
    public Collection performServerSearch() {
        try {
            final Geometry geometry = (Geometry)getGeometry();

            final String sql = "SELECT DISTINCT alkis_landparcel.id "
                        + "FROM "
                        + "   alkis_landparcel, "
                        + "   geom "
                        + "WHERE "
                        + "   geom.id = alkis_landparcel.geometrie AND "
                        + "   GeomFromText('" + geometry.toText() + "', " + geometry.getSRID()
                        + ") && geom.geo_field AND"
                        + "   ST_intersects(GeomFromText('" + geometry.toText() + "', " + geometry.getSRID()
                        + "), geom.geo_field)";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            final MetaService metaService = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");
            final ArrayList<ArrayList> result = metaService.performCustomSearch(sql);

            final ArrayList<Integer> ids = new ArrayList<Integer>();
            for (final ArrayList fields : result) {
                ids.add((Integer)fields.get(0));
            }

            return ids;
        } catch (final Exception e) {
            LOG.fatal("problem during landparcel search", e);
            return null;
        }
    }
}
