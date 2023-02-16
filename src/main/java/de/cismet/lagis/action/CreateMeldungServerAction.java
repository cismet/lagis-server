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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;

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

import de.cismet.lagis.commons.LagisConstants;

import de.cismet.lagis.properties.MeldungenProperties;

import de.cismet.lagis.server.utils.LagisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class CreateMeldungServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateMeldungServerAction.class);
    public static final String TASKNAME = "createMeldung";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        NAME, TEXT, TARGET
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String name = null;
        String text = null;
        String target = null;

        try {
            final MetaObjectNode flurstueckMon = (MetaObjectNode)body;
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final Object value = sap.getValue();
                    if (sap.getKey().equals(Parameter.NAME.toString())) {
                        name = (String)value;
                    } else if (sap.getKey().equals(Parameter.TEXT.toString())) {
                        text = (String)value;
                    } else if (sap.getKey().equals(Parameter.TARGET.toString())) {
                        target = (String)value;
                    }
                }
            }

            final CidsBean flurstueckBean = getMetaService().getMetaObject(
                        getUser(),
                        flurstueckMon.getObjectId(),
                        flurstueckMon.getClassId(),
                        getConnectionContext())
                        .getBean();

            final Timestamp timestamp = new Timestamp(new Date().getTime());
            final String creator = getUser().getName();
            final CidsBean meldungBean = CidsBean.createNewCidsBeanFromTableName(
                    LagisConstants.DOMAIN_LAGIS,
                    "MELDUNG");
            meldungBean.setProperty("name", name);
            meldungBean.setProperty("text", text);
            meldungBean.setProperty("timestamp", timestamp);
            meldungBean.setProperty("creator", creator);
            meldungBean.setProperty("target", target); // in Zukunft könnte hier der Username des Empfängers
            // drin stehen
            meldungBean.setProperty("erledgit", false);
            meldungBean.setProperty("fk_flurstueck", flurstueckBean);
            final CidsBean persistedMeldung = metaService.insertMetaObject(
                        getUser(),
                        meldungBean.getMetaObject(),
                        getConnectionContext())
                        .getBean();

            final MeldungenProperties properties = getProperties();

            final String betreff = properties.getBetreffTemplate()
                        .replaceAll("__FLURSTUECK__", flurstueckBeantoString(flurstueckBean))
                        .replaceAll("__TITEL__", (name != null) ? name : "ohne Titel");
            final String nachricht = properties.getNachrichtTemplate()
                        .replaceAll("__ID__", Integer.toString(meldungBean.getPrimaryKeyValue()))
                        .replaceAll("__TEXT__", (text != null) ? text.replaceAll("\"", "\\\"") : "");

            final String cmd = properties.getCmd();
            final String[] cmdParts = cmd.split(" ");
            for (int i = 0; i < cmdParts.length; i++) {
                cmdParts[i] = cmdParts[i].replaceAll("__BETREFF__", betreff).replaceAll("__NACHRICHT__", nachricht);
            }

            executeCmd(cmdParts);

            return new MetaObjectNode(persistedMeldung);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flurstueckBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String flurstueckBeantoString(final CidsBean flurstueckBean) {
        if (flurstueckBean == null) {
            return null;
        }
        try {
            final Integer id = (Integer)flurstueckBean.getProperty("fk_flurstueck_schluessel.id");
            final Integer flur = (Integer)flurstueckBean.getProperty("fk_flurstueck_schluessel.flur");
            final String gemarkung = (String)flurstueckBean.getProperty(
                    "fk_flurstueck_schluessel.fk_gemarkung.bezeichnung");
            final Integer zaehler = (Integer)flurstueckBean.getProperty("fk_flurstueck_schluessel.flurstueck_zaehler");
            final Integer nenner = (Integer)flurstueckBean.getProperty("fk_flurstueck_schluessel.flurstueck_nenner");
            final boolean isEchterSchluessel = !"pseudo".equals(((String)flurstueckBean.getProperty(
                            "fk_flurstueck_schluessel.fk_flurstueck_art.bezeichnung")));
            if (isEchterSchluessel) {
                if (nenner != null) {
                    return String.format("%s %d %d/%d", gemarkung, flur, zaehler, nenner);
                } else {
                    return String.format("%s %d %d", gemarkung, flur, zaehler);
                }
            } else {
                return String.format("pseudo Schluessel%d", id);
            }
        } catch (Exception ex) {
            LOG.error("Eine oder mehrere Felder der Entität sind null", ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cmdParts  cmd DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static String executeCmd(final String[] cmdParts) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(cmdParts);
        final Process process = builder.start();
        final InputStream is = process.getInputStream();
        return IOUtils.toString(new InputStreamReader(is));
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
