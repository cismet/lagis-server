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
package de.cismet.lagis.cidslayer;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import de.cismet.cids.server.cidslayer.DefaultCidsLayer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AlkisFlurstueckCidsLayer extends DefaultCidsLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static final String QUERY = ""
                + "SELECT "
                + "alkis_flurstueck.id AS id, "
                + "alkis_flurstueck.alkis_id AS alkis_id, "
                + "flurstueck_art.bezeichnung AS flurstueck_art, "
                + "st_asBinary(alkis_flurstueck.geometrie) AS geometrie "
                + "from alkis_flurstueck "
                + "LEFT JOIN flurstueck_schluessel ON alkis_flurstueck.fk_schluessel = flurstueck_schluessel.id "
                + "LEFT JOIN flurstueck_art ON flurstueck_schluessel.fk_flurstueck_art = flurstueck_art.id";

    private static final String[] NAMES = new String[] {
            "id",
            "alkis_id",
            "flurstueck_art",
            "geometrie"
        };

    private static final String[] PROPERTY_NAMES = new String[] {
            "ID",
            "AlkisID",
            "Flurstücksart",
            "Geometrie"
        };

    private static final String[] TYPES = new String[] {
            "java.lang.Integer",
            "java.lang.String",
            "java.lang.String",
            "Geometry"
        };

    //~ Instance fields --------------------------------------------------------

    private final User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WohnlageCidsLayer object.
     *
     * @param  mc    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     */
    public AlkisFlurstueckCidsLayer(final MetaClass mc, final User user) {
        super(mc);
        this.user = user;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getSelectString() {
        return QUERY.replaceAll("#LOGIN_NAME#", user.getName());
    }

    @Override
    public String[] getColumnNames() {
        return NAMES;
    }

    @Override
    public String[] getColumnPropertyNames() {
        return PROPERTY_NAMES;
    }

    @Override
    public String[] getPrimitiveColumnTypes() {
        return TYPES;
    }

    @Override
    public String getGeoField() {
        return "geometrie";
    }

    @Override
    public String getSqlGeoField() {
        return "geometrie";
    }
}
