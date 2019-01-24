/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.lagis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.SearchParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.lagis.commons.LagisConstants;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = RestApiCidsServerSearch.class)
public class ReBeGeomSearch extends GeomServerSearch implements RestApiCidsServerSearch, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ReBeGeomSearch.class);
    private static final String SQL_TEMPLATE =
        "SELECT %s FROM %s, geom WHERE geom.id = rebe.fk_geom AND ST_intersects(GeomFromText('%s', %s), geom.geo_field) AND (rebe.datum_loeschung IS NULL OR rebe.datum_loeschung > now())";

    //~ Instance fields --------------------------------------------------------

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ReBeGeomSearch object.
     */
    public ReBeGeomSearch() {
        super(new SearchInfo());
        getSearchInfo().setKey(this.getClass().getName());
        getSearchInfo().setName(this.getClass().getSimpleName());
        getSearchInfo().setDescription(
            "Builtin Legacy Search to delegate the operation getLightweightMetaObjectsByQuery to the cids Pure REST Search API.");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<>();
        getSearchInfo().setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.ENTITY_REFERENCE);
        getSearchInfo().setResultDescription(resultParameterInfo);
    }

    /**
     * Creates a new ReBeGeomSearch object.
     *
     * @param  geometry  DOCUMENT ME!
     */
    public ReBeGeomSearch(final Geometry geometry) {
        this();
        setGeometry(geometry);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public String getCrs() {
        return "EPSG:25832";
    }

    @Override
    public Collection performServerSearch() {
        try {
            final Geometry geometry = getGeometry();

            final MetaClass mcRebe = CidsBean.getMetaClassFromTableName(
                    LagisConstants.DOMAIN_LAGIS,
                    "REBE",
                    getConnectionContext());

            final String fields = mcRebe.getID() + ", " + mcRebe.getTableName() + "." + mcRebe.getPrimaryKey();
            final String sql = String.format(
                    SQL_TEMPLATE,
                    fields,
                    mcRebe.getTableName(),
                    geometry.toText(),
                    geometry.getSRID());
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }

            final MetaService metaService = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);
            return Arrays.asList(metaService.getMetaObjectNode(getUser(), sql, getConnectionContext()));
        } catch (final Exception e) {
            LOG.fatal("problem during rebe search", e);
            return null;
        }
    }
}
