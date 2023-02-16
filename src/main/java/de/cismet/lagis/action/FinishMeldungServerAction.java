/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.lagis.action;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.sql.Timestamp;

import java.util.Date;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.cids.utils.serverresources.PropertiesServerResource;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.lagis.properties.MeldungenProperties;

import de.cismet.lagis.server.utils.LagisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class FinishMeldungServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FinishMeldungServerAction.class);
    public static final String TASKNAME = "finishMeldung";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        REMOVE
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        try {
            final MetaObjectNode meldungMon = (MetaObjectNode)body;
            boolean remove = false;
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final Object value = sap.getValue();
                    if (sap.getKey().equals(Parameter.REMOVE.toString())) {
                        remove = (Boolean)value;
                    }
                }
            }

            final CidsBean meldungBean = getMetaService().getMetaObject(
                        getUser(),
                        meldungMon.getObjectId(),
                        meldungMon.getClassId(),
                        getConnectionContext())
                        .getBean();

            final Timestamp erledigtAm = new Timestamp(new Date().getTime());
            final String erledigtVon = getUser().getName();
            meldungBean.setProperty("erledigt_am", remove ? null : erledigtAm);
            meldungBean.setProperty("erledigt_von", remove ? null : erledigtVon);
            getMetaService().updateMetaObject(getUser(), meldungBean.getMetaObject(), getConnectionContext());
            return new MetaObjectNode(meldungBean);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MeldungenProperties getProperties() throws Exception {
        return (MeldungenProperties)ServerResourcesLoader.getInstance()
                    .get((PropertiesServerResource)LagisServerResources.MELDUNGEN_PROPERTIES.getValue());
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
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
    public String getTaskName() {
        return TASKNAME;
    }
}
