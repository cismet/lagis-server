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

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
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

    private final SearchInfo searchInfo = initSearchInfo();
    private String aktenzeichenSearchPattern;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlurstueckSchluesselByMipaAktenzeichenSearch object.
     */
    public FlurstueckSchluesselByMipaAktenzeichenSearch() {
    }

    /**
     * Creates a new FlurstueckSchluesselByMipaAktenzeichenSearch object.
     *
     * @param  aktenzeichenSearchPattern  DOCUMENT ME!
     */
    public FlurstueckSchluesselByMipaAktenzeichenSearch(final String aktenzeichenSearchPattern) {
        setAktenzeichenSearchPattern(aktenzeichenSearchPattern);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static SearchInfo initSearchInfo() {
        final SearchInfo searchInfo = new SearchInfo();
        searchInfo.setKey(FlurstueckSchluesselByMipaAktenzeichenSearch.class.getName());
        searchInfo.setName(FlurstueckSchluesselByMipaAktenzeichenSearch.class.getSimpleName());
        searchInfo.setDescription(
            "Builtin Legacy Search to delegate the operation getLightweightMetaObjectsByQuery to the cids Pure REST Search API.");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<>();
        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.ENTITY_REFERENCE);
        searchInfo.setResultDescription(resultParameterInfo);

        return searchInfo;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
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
            LOG.fatal("problem during " + FlurstueckSchluesselByMipaAktenzeichenSearch.class.getSimpleName()
                        + " search",
                e);
            return null;
        }
    }
}
