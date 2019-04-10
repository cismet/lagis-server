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
public class FlurstueckSchluesselByBaumCrossreferencesSearch extends AbstractCidsServerSearch
        implements RestApiCidsServerSearch,
            ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FlurstueckSchluesselByBaumCrossreferencesSearch.class);
    private static final String SQL_TEMPLATE = "SELECT %s "
                + "FROM %s AS flurstueck_schluessel "
                + "LEFT JOIN gemarkung "
                + "  ON flurstueck_schluessel.fk_gemarkung = gemarkung.id "
                + ", (SELECT %s::text AS gemarkung_bezeichnung, %s::text AS flur, %s::text AS flurstueck_zaehler, %s::text AS flurstueck_nenner) AS values "
                + "WHERE "
                + "  gemarkung.bezeichnung ILIKE values.gemarkung_bezeichnung "
                + "  AND flurstueck_schluessel.flur = values.flur::integer "
                + "  AND CASE WHEN values.flurstueck_zaehler IS NOT NULL THEN flurstueck_schluessel.flurstueck_zaehler = values.flurstueck_zaehler::integer ELSE true END "
                + "  AND CASE WHEN values.flurstueck_nenner  IS NOT NULL THEN flurstueck_schluessel.flurstueck_nenner  = values.flurstueck_nenner::integer  ELSE true END "
                + ";";

    //~ Instance fields --------------------------------------------------------

    private final SearchInfo searchInfo = initSearchInfo();
    private String gemarkungBezeichnung;
    private String flur;
    private String flurstueckZaehler;
    private String flurstueckNenner;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlurstueckSchluesselByMipaCrossreferencesSearch object.
     */
    public FlurstueckSchluesselByBaumCrossreferencesSearch() {
    }

    /**
     * Creates a new FlurstueckSchluesselByMipaCrossreferencesSearch object.
     *
     * @param  gemarkungBezeichnung  DOCUMENT ME!
     * @param  flur                  DOCUMENT ME!
     * @param  flurstueckZaehler     DOCUMENT ME!
     * @param  flurstueckNenner      DOCUMENT ME!
     */
    public FlurstueckSchluesselByBaumCrossreferencesSearch(final String gemarkungBezeichnung,
            final String flur,
            final String flurstueckZaehler,
            final String flurstueckNenner) {
        setGemarkungBezeichnung(gemarkungBezeichnung);
        setFlur(flur);
        setFlurstueckZaehler(flurstueckZaehler);
        setFlurstueckNenner(flurstueckNenner);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static SearchInfo initSearchInfo() {
        final SearchInfo searchInfo = new SearchInfo();
        searchInfo.setKey(FlurstueckSchluesselByBaumCrossreferencesSearch.class.getName());
        searchInfo.setName(FlurstueckSchluesselByBaumCrossreferencesSearch.class.getSimpleName());
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
                    (getGemarkungBezeichnung() != null) ? ("'" + getGemarkungBezeichnung() + "'") : "NULL",
                    (getFlur() != null) ? ("'" + getFlur() + "'") : "NULL",
                    (getFlurstueckZaehler() != null) ? ("'" + getFlurstueckZaehler() + "'") : "NULL",
                    (getFlurstueckNenner() != null) ? ("'" + getFlurstueckNenner() + "'") : "NULL");
            final MetaService metaService = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);
            return Arrays.asList(metaService.getMetaObjectNode(getUser(), sql, getConnectionContext()));
        } catch (final Exception e) {
            LOG.fatal("problem during " + FlurstueckSchluesselByBaumCrossreferencesSearch.class.getSimpleName()
                        + " search",
                e);
            return null;
        }
    }
}
