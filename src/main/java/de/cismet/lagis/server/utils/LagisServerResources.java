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
package de.cismet.lagis.server.utils;

import lombok.Getter;

import de.cismet.cids.utils.serverresources.PropertiesServerResource;
import de.cismet.cids.utils.serverresources.ServerResource;
import de.cismet.cids.utils.serverresources.TextServerResource;

import de.cismet.lagis.properties.MeldungenProperties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public enum LagisServerResources {

    //~ Enum constants ---------------------------------------------------------

    MOTD_PROPERTIES(new TextServerResource("/motd/lagis.properties")),
    MELDUNGEN_PROPERTIES(new PropertiesServerResource("/meldungen/meldungen.properties", MeldungenProperties.class));

    //~ Instance fields --------------------------------------------------------

    @Getter private final ServerResource value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  value  DOCUMENT ME!
     */
    private LagisServerResources(final ServerResource value) {
        this.value = value;
    }
}
