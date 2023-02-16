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
package de.cismet.lagis.properties;

import lombok.Getter;

import de.cismet.cids.utils.serverresources.DefaultServerResourcePropertiesHandler;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
public class MeldungenProperties extends DefaultServerResourcePropertiesHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MeldungenProperties.class);

    private static final String PROP_CMD = "CMD";
    private static final String DEFAULT_CMD = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PotenzialflaechenProperties object.
     */
    public MeldungenProperties() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCmd() {
        try {
            return getProperties().getProperty(PROP_CMD, DEFAULT_CMD);
        } catch (final Exception ex) {
            LOG.info(String.format("Property %s not set", PROP_CMD), ex);
            return DEFAULT_CMD;
        }
    }
}
