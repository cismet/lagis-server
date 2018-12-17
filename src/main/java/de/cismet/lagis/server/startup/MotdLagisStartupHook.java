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
package de.cismet.lagis.server.startup;

import Sirius.server.middleware.interfaces.domainserver.DomainServerStartupHook;

import java.util.Properties;

import de.cismet.cids.custom.wunda_blau.startuphooks.MotdStartupHook;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.lagis.server.utils.LagisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = DomainServerStartupHook.class)
public class MotdLagisStartupHook extends MotdStartupHook {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getDomain() {
        return "LAGIS";
    }

    @Override
    public Properties getProperties() throws Exception {
        return ServerResourcesLoader.getInstance().loadProperties(LagisServerResources.MOTD_PROPERTIES.getValue());
    }
}
