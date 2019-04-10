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

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

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
public class FlurstueckSchluesselByMipaAktenzeichenSearch extends AbstractCidsServerSearch
        implements RestApiCidsServerSearch,
            ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FlurstueckSchluesselByMipaAktenzeichenSearch.class);
    private static final String SQL_TEMPLATE = "SELECT %s "
                + "FROM %s AS flurstueck_schluessel "
                + "LEFT JOIN alkis_flurstueck "
                + "  ON alkis_flurstueck.fk_schluessel = flurstueck_schluessel.id "
                + "LEFT JOIN gemarkung "
                + "  ON flurstueck_schluessel.fk_gemarkung = gemarkung.id "
                + ", mipa "
                + "LEFT JOIN geom AS mipa_geom "
                + "  ON mipa.fk_geom = mipa_geom.id "
                + "WHERE "
                + "  aktenzeichen ilike '%s' "
                + "  AND ST_intersects(alkis_flurstueck.geometrie, mipa_geom.geo_field) "
                + "  AND flurstueck_schluessel.id IS NOT NULL "
                + ";";

    //~ Instance fields --------------------------------------------------------

    private final SearchInfo searchInfo;
    private String aktenzeichenSearchPattern;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AlkisFlurstueckGeomSearch object.
     */
    public FlurstueckSchluesselByMipaAktenzeichenSearch() {
        this.searchInfo = new SearchInfo();
        initSearchInfo();
    }

    /**
     * Creates a new AlkisFlurstueckGeomSearch object.
     *
     * @param  aktenzeichenSearchPattern  DOCUMENT ME!
     */
    public FlurstueckSchluesselByMipaAktenzeichenSearch(final String aktenzeichenSearchPattern) {
        this();
        setAktenzeichen(aktenzeichenSearchPattern);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initSearchInfo() {
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
     * DOCUMENT ME!
     *
     * @param  aktenzeichenSearchPattern  DOCUMENT ME!
     */
    public final void setAktenzeichen(final String aktenzeichenSearchPattern) {
        this.aktenzeichenSearchPattern = aktenzeichenSearchPattern;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAktenzeichenSearchPattern() {
        return aktenzeichenSearchPattern;
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public Collection performServerSearch() {
        try {
            final MetaClass mcFlurstueckSchluessel = CidsBean.getMetaClassFromTableName(
                    LagisConstants.DOMAIN_LAGIS,
                    "FLURSTUECK_SCHLUESSEL",
                    getConnectionContext());

            final String fields = mcFlurstueckSchluessel.getID() + ", " + mcFlurstueckSchluessel.getTableName() + "."
                        + mcFlurstueckSchluessel.getPrimaryKey();
            final String sql = String.format(
                    SQL_TEMPLATE,
                    fields,
                    mcFlurstueckSchluessel.getTableName(),
                    "%"
                            + getAktenzeichenSearchPattern().replaceAll("\\*", "%")
                            + "%");
            final MetaService metaService = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);
            return Arrays.asList(metaService.getMetaObjectNode(getUser(), sql, getConnectionContext()));
        } catch (final Exception e) {
            LOG.fatal("problem during FlurstueckSchluesselByMipaAktenzeichenGeomSearch search", e);
            return null;
        }
    }
}
