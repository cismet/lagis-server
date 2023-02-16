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
    private static final String PROP_BETREFF_TEMPLATE = "BETREFF_TEMPLATE";
    private static final String PROP_NACHRICHT_TEMPLATE = "NACHRICHT__TEMPLATE";
    private static final String DEFAULT_CMD = "";
    private static final String DEFAULT_BETREFF_TEMPLATE = "neue Meldung zum Flurstück __FLURSTUECK__ - __TITEL__";
    private static final String DEFAULT_NACHRICHT_TEMPLATE =
        "es wurde eine neue Meldung gesetzt => http://localhost:19000/loadMeldung?id=__ID__ \n\n__TEXT__";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeldungenProperties object.
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBetreffTemplate() {
        try {
            return getProperties().getProperty(PROP_BETREFF_TEMPLATE, DEFAULT_BETREFF_TEMPLATE);
        } catch (final Exception ex) {
            LOG.info(String.format("Property %s not set", PROP_BETREFF_TEMPLATE), ex);
            return DEFAULT_BETREFF_TEMPLATE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getNachrichtTemplate() {
        try {
            return getProperties().getProperty(PROP_NACHRICHT_TEMPLATE, DEFAULT_NACHRICHT_TEMPLATE);
        } catch (final Exception ex) {
            LOG.info(String.format("Property %s not set", PROP_NACHRICHT_TEMPLATE), ex);
            return DEFAULT_NACHRICHT_TEMPLATE;
        }
    }
}
