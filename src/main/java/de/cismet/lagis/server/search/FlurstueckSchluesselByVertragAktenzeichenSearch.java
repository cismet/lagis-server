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
@ServiceProvider(service = RestApiCidsServerSearch.class)
@Getter
@Setter
public class FlurstueckSchluesselByVertragAktenzeichenSearch extends AbstractCidsServerSearch
        implements RestApiCidsServerSearch,
            ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FlurstueckSchluesselByVertragAktenzeichenSearch.class);
    private static final String SQL_TEMPLATE = "SELECT %s "
                + "FROM "
                + "    %s AS flurstueck_schluessel "
                + "LEFT JOIN flurstueck "
                + "  ON flurstueck_schluessel.id = flurstueck.fk_flurstueck_schluessel "
                + ", jt_flurstueck_vertrag "
                + ", vertrag "
                + "WHERE "
                + "    flurstueck.ar_vertraege = jt_flurstueck_vertrag.fk_flurstueck "
                + "    AND jt_flurstueck_vertrag.fk_vertrag = vertrag.id "
                + "    AND vertrag.aktenzeichen LIKE '%s' "
                + ";";

    //~ Instance fields --------------------------------------------------------

    private final SearchInfo searchInfo = initSearchInfo();
    private String aktenzeichenSearchPattern;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlurstueckSchluesselByVertragAktenzeichenSearch object.
     */
    public FlurstueckSchluesselByVertragAktenzeichenSearch() {
    }

    /**
     * Creates a new FlurstueckSchluesselByVertragAktenzeichenSearch object.
     *
     * @param  aktenzeichenSearchPattern  DOCUMENT ME!
     */
    public FlurstueckSchluesselByVertragAktenzeichenSearch(final String aktenzeichenSearchPattern) {
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
        searchInfo.setKey(FlurstueckSchluesselByVertragAktenzeichenSearch.class.getName());
        searchInfo.setName(FlurstueckSchluesselByVertragAktenzeichenSearch.class.getSimpleName());
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
            LOG.fatal("problem during " + FlurstueckSchluesselByVertragAktenzeichenSearch.class.getSimpleName()
                        + " search",
                e);
            return null;
        }
    }
}
