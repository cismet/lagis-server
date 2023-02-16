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
import Sirius.server.middleware.types.MetaObjectNode;

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
public class MeldungenSearch extends AbstractCidsServerSearch implements RestApiCidsServerSearch,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MeldungenSearch.class);
    private static final String SQL_TEMPLATE = "SELECT %s FROM %s WHERE fk_flurstueck = %d";
    private static final String SQL_TEMPLATE_ALL = "SELECT %s FROM %s";

    //~ Instance fields --------------------------------------------------------

    @Getter public final SearchInfo searchInfo;
    @Getter @Setter private Integer flurstueckId;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeldungenSearch object.
     */
    public MeldungenSearch() {
        this((Integer)null);
    }

    /**
     * Creates a new MeldungenSearch object.
     *
     * @param  flurstueckId  DOCUMENT ME!
     */
    public MeldungenSearch(final Integer flurstueckId) {
        this.searchInfo = new SearchInfo();
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

        setFlurstueckId(flurstueckId);
    }

    /**
     * Creates a new MeldungenSearch object.
     *
     * @param  flurstueckMon  DOCUMENT ME!
     */
    public MeldungenSearch(final MetaObjectNode flurstueckMon) {
        this((flurstueckMon != null) ? flurstueckMon.getObjectId() : null);
    }

    /**
     * Creates a new MeldungenSearch object.
     *
     * @param  flurstueckBean  DOCUMENT ME!
     */
    public MeldungenSearch(final CidsBean flurstueckBean) {
        this((flurstueckBean != null) ? flurstueckBean.getPrimaryKeyValue() : null);
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
    public Collection performServerSearch() {
        try {
            final MetaClass mcMeldungen = CidsBean.getMetaClassFromTableName(
                    LagisConstants.DOMAIN_LAGIS,
                    "MELDUNG",
                    getConnectionContext());

            final String fields = String.format(
                    "%d, %s.%s",
                    mcMeldungen.getID(),
                    mcMeldungen.getTableName(),
                    mcMeldungen.getPrimaryKey());
            final String sql = (getFlurstueckId() != null)
                ? String.format(SQL_TEMPLATE, fields, mcMeldungen.getTableName(), getFlurstueckId())
                : String.format(SQL_TEMPLATE_ALL, fields, mcMeldungen.getTableName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }

            final MetaService metaService = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);
            return Arrays.asList(metaService.getMetaObjectNode(getUser(), sql, getConnectionContext()));
        } catch (final Exception e) {
            LOG.fatal("problem during meldungen search", e);
            return null;
        }
    }
}
