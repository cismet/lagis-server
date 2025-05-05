/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.lagis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.lagis.commons.LagisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class LagisFortfuehrungItemSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(LagisFortfuehrungItemSearch.class);

    public static int FIELD_ID = 0;
    public static int FIELD_FFN = 1;
    public static int FIELD_ANLASSNAME = 2;
    public static int FIELD_BEGINN = 3;
    public static int FIELD_FS_ALT = 4;
    public static int FIELD_FS_NEU = 5;
    public static int FIELD_GEOFIELD = 6;
    public static int FIELD_FLURSTUECK_ID = 7;
    public static int FIELD_FORTFUEHRUNG_ID = 8;

    //~ Instance fields --------------------------------------------------------

    private Date fromDate = null;
    private Date toDate = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FortfuehrungItemSearch object.
     *
     * @param  fromDate  DOCUMENT ME!
     * @param  toDate    DOCUMENT ME!
     */
    public LagisFortfuehrungItemSearch(final Date fromDate, final Date toDate) {
        setFromDate(fromDate);
        setToDate(toDate);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromDate  DOCUMENT ME!
     */
    private void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  toDate  DOCUMENT ME!
     */
    private void setToDate(final Date toDate) {
        this.toDate = toDate;
    }

    @Override
    public Collection performServerSearch() {
        final List<Object[]> items = new ArrayList<Object[]>();
        final String query = "SELECT lookup_alkis_ffn.id AS id "
                    + ", lookup_alkis_ffn.ffn AS ffn "
                    + ", lookup_ffn_anlassarten.anl_bezeichnung AS anlass_name "
                    + ", to_date(lookup_alkis_ffn.beginn, 'DD-Mon-YY') AS beginn_date "
                    + ", flurstueckskennzeichen_alt AS fs_alt "
                    + ", flurstueckskennzeichen_neu AS fs_neu "
                    + ", st_asText(flurstueck.umschreibendes_rechteck) AS geo_field "
                    + ", flurstueck.id AS flurstueck_id "
                    + "FROM lookup_alkis_ffn "
                    + "LEFT JOIN lookup_ffn_anlassarten ON '\\\"' || lookup_ffn_anlassarten.anl_ffn || '\\\"' = lookup_alkis_ffn.anl_ffn "
                    + "LEFT JOIN flurstueck ON lookup_alkis_ffn.ffn = flurstueck.fortfuehrungsnummer "
                    + "WHERE flurstueck.umschreibendes_rechteck IS NOT NULL AND "
                    + "to_date(lookup_alkis_ffn.beginn, 'DD-Mon-YY') >= '" + fromDate
                    + "' AND to_date(lookup_alkis_ffn.beginn, 'DD-Mon-YY') <= '" + toDate + "' "
                    + "ORDER BY lookup_alkis_ffn.ffn ASC;";

        final MetaService metaService = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");

        try {
            for (final ArrayList fields : metaService.performCustomSearch(query)) {
                final String queryFF = "SELECT id FROM fortfuehrung WHERE alkis_ffn Like '"
                            + (String)fields.get(1) + "';";
                final MetaService metaServiceFF = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);

                Integer ffId = null;
                for (final ArrayList fieldsFF : metaServiceFF.performCustomSearch(queryFF)) {
                    ffId = (Integer)fieldsFF.get(0);
                    break;
                }

                items.add(
                    new Object[] {
                        (Integer)fields.get(FIELD_ID),
                        (String)fields.get(FIELD_FFN),
                        (String)fields.get(FIELD_ANLASSNAME),
                        (Date)fields.get(FIELD_BEGINN),
                        (String)fields.get(FIELD_FS_ALT),
                        (String)fields.get(FIELD_FS_NEU),
                        (String)fields.get(FIELD_GEOFIELD),
                        (Integer)fields.get(FIELD_FLURSTUECK_ID),
                        ffId
                    });
            }
        } catch (final Exception ex) {
            LOG.error("problem fortfuehrung item search", ex);
        }

        return items;
    }
}
