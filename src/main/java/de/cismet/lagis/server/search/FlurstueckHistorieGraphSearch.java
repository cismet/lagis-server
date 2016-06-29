/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.lagis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.SearchParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

import de.cismet.lagis.commons.LagisConstants;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerSearch.class)
public class FlurstueckHistorieGraphSearch extends AbstractCidsServerSearch implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FlurstueckHistorieGraphSearch.class);

    private static final String QUERY_TEMPLATE = "SELECT * \n"
                + "FROM (\n"
                + "    SELECT DISTINCT ON (nachfolger_schluessel_id, vorgaenger_schluessel_id) \n"
                + "        level, \n"
                + "        nachfolger_schluessel.id AS nachfolger_schluessel_id, \n"
                + "        CASE WHEN nachfolger_schluessel.fk_flurstueck_art = (SELECT id FROM flurstueck_art  WHERE bezeichnung = 'pseudo') \n"
                + "            THEN 'pseudo Schluessel' || nachfolger_schluessel.id \n"
                + "            ELSE (nachfolger_gemarkung.bezeichnung || ' ' || nachfolger_schluessel.flur || ' ' || nachfolger_schluessel.flurstueck_zaehler || '/' || nachfolger_schluessel.flurstueck_nenner) \n"
                + "        END AS nachfolger_name, \n"
                + "        vorgaenger_schluessel.id AS vorgaenger_schluessel_id, \n"
                + "        CASE WHEN vorgaenger_schluessel.fk_flurstueck_art = (SELECT id  FROM flurstueck_art  WHERE bezeichnung = 'pseudo') \n"
                + "            THEN 'pseudo Schluessel' || vorgaenger_schluessel.id \n"
                + "            ELSE (vorgaenger_gemarkung.bezeichnung || ' ' || vorgaenger_schluessel.flur || ' ' || vorgaenger_schluessel.flurstueck_zaehler || '/' || vorgaenger_schluessel.flurstueck_nenner) \n"
                + "        END AS vorgaenger_name \n"
                + "    FROM ( \n"
                + "        ( \n"
                + "            WITH RECURSIVE historie_zurueck as ( \n"
                + "                SELECT fk_vorgaenger, fk_nachfolger, 0 AS level \n"
                + "                FROM flurstueck_historie \n"
                + "                WHERE fk_nachfolger = {FLURSTUECK_ID} \n"
                + "            UNION ALL \n"
                + "                SELECT \n"
                + "                     s.fk_vorgaenger, \n"
                + "		        s.fk_nachfolger, \n"
                + "		        d.level - CASE WHEN flurstueck_schluessel.fk_flurstueck_art = (SELECT id FROM flurstueck_art  WHERE bezeichnung = 'pseudo') \n"
                + "                         THEN 0 \n"
                + "                         ELSE 1 \n"
                + "                     END \n"
                + "                FROM historie_zurueck AS d \n"
                + "                    JOIN flurstueck_historie AS s ON d.fk_vorgaenger = s.fk_nachfolger \n"
                + "                    LEFT JOIN flurstueck ON flurstueck.id = d.fk_vorgaenger \n"
                + "                    LEFT JOIN flurstueck_schluessel ON flurstueck_schluessel.id = flurstueck.fk_flurstueck_schluessel \n"
                + "            ) SELECT * FROM historie_zurueck WHERE level <= 0 AND level > {PREDECESSOR_LEVEL} \n"
                + "        ) UNION ( \n"
                + "            WITH RECURSIVE historie_vor AS ( \n"
                + "                SELECT fk_vorgaenger, fk_nachfolger, 1 AS level \n"
                + "                FROM flurstueck_historie \n"
                + "                WHERE fk_vorgaenger = {FLURSTUECK_ID} \n"
                + "            UNION ALL \n"
                + "                SELECT \n"
                + "                     s.fk_vorgaenger, \n"
                + "		        s.fk_nachfolger, \n"
                + "		        d.level + CASE WHEN flurstueck_schluessel.fk_flurstueck_art = (SELECT id FROM flurstueck_art  WHERE bezeichnung = 'pseudo') \n"
                + "                         THEN 0 \n"
                + "                         ELSE 1 \n"
                + "                     END \n"
                + "                FROM historie_vor AS d \n"
                + "                    JOIN flurstueck_historie AS s ON d.fk_nachfolger = s.fk_vorgaenger \n"
                + "                    LEFT JOIN flurstueck ON flurstueck.id = d.fk_nachfolger \n"
                + "                    LEFT JOIN flurstueck_schluessel ON flurstueck_schluessel.id = flurstueck.fk_flurstueck_schluessel \n"
                + "            ) SELECT * FROM historie_vor WHERE level > 0 AND level <= {SUCCESSOR_LEVEL} \n"
                + "        ) UNION ( \n"
                + "            WITH RECURSIVE historie_vor_nachbarn AS ( \n"
                + "                SELECT fk_vorgaenger, fk_nachfolger, 0 AS level \n"
                + "                FROM flurstueck_historie \n"
                + "                WHERE fk_vorgaenger IN ( SELECT fk_vorgaenger  FROM flurstueck_historie  WHERE fk_nachfolger = {FLURSTUECK_ID} ) AND fk_nachfolger != {FLURSTUECK_ID} \n"
                + "            UNION ALL \n"
                + "                SELECT \n"
                + "                     s.fk_vorgaenger, \n"
                + "		        s.fk_nachfolger, \n"
                + "		        d.level + CASE WHEN flurstueck_schluessel.fk_flurstueck_art = (SELECT id FROM flurstueck_art  WHERE bezeichnung = 'pseudo') \n"
                + "                         THEN 0 \n"
                + "                         ELSE 1 \n"
                + "                     END \n"
                + "                FROM historie_vor_nachbarn AS d \n"
                + "                    JOIN flurstueck_historie AS s ON d.fk_nachfolger = s.fk_vorgaenger \n"
                + "                    LEFT JOIN flurstueck ON flurstueck.id = d.fk_nachfolger \n"
                + "                    LEFT JOIN flurstueck_schluessel ON flurstueck_schluessel.id = flurstueck.fk_flurstueck_schluessel \n"
                + "            ) SELECT * FROM historie_vor_nachbarn WHERE level > -1 AND level <= {SIBBLING_LEVEL} \n"
                + "        ) \n"
                + "    ) AS historie \n"
                + "    LEFT JOIN flurstueck AS vorgaenger ON historie.fk_vorgaenger = vorgaenger.id \n"
                + "    LEFT JOIN flurstueck_schluessel AS vorgaenger_schluessel ON vorgaenger.fk_flurstueck_schluessel = vorgaenger_schluessel.id \n"
                + "    LEFT JOIN gemarkung AS vorgaenger_gemarkung ON vorgaenger_schluessel.fk_gemarkung = vorgaenger_gemarkung.id \n"
                + "    LEFT JOIN flurstueck AS nachfolger ON historie.fk_nachfolger = nachfolger.id \n"
                + "    LEFT JOIN flurstueck_schluessel AS nachfolger_schluessel ON nachfolger.fk_flurstueck_schluessel = nachfolger_schluessel.id \n"
                + "    LEFT JOIN gemarkung AS nachfolger_gemarkung ON nachfolger_schluessel.fk_gemarkung = nachfolger_gemarkung.id \n"
                + ") AS unsorted \n"
                + "ORDER BY level, vorgaenger_name, nachfolger_name;";

    //~ Instance fields --------------------------------------------------------

    @Getter private final SearchInfo searchInfo;

    @Getter @Setter private Integer flurstueckId;
    @Getter @Setter private Integer predecessorLevel;
    @Getter @Setter private Integer successorLevel;
    @Getter @Setter private Integer sibblingLevel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LockedEntitySearch object.
     */
    public FlurstueckHistorieGraphSearch() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search Lagis Historie Graph");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<SearchParameterInfo>();
        final SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("flurstueckId");
        searchParameterInfo.setType(Type.INTEGER);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.JAVA_SERIALIZABLE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    /**
     * Creates a new LockedEntitySearch object.
     *
     * @param  flurstueckId      DOCUMENT ME!
     * @param  predecessorLevel  DOCUMENT ME!
     * @param  successorLevel    DOCUMENT ME!
     * @param  sibblingLevel     DOCUMENT ME!
     */
    public FlurstueckHistorieGraphSearch(final Integer flurstueckId,
            final Integer predecessorLevel,
            final Integer successorLevel,
            final Integer sibblingLevel) {
        this();
        setFlurstueckId(flurstueckId);
        setPredecessorLevel(predecessorLevel);
        setSuccessorLevel(successorLevel);
        setSibblingLevel(sibblingLevel);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        try {
            final MetaService metaService = (MetaService)getActiveLocalServers().get(LagisConstants.DOMAIN_LAGIS);

            final String query = QUERY_TEMPLATE.replace("{FLURSTUECK_ID}", Integer.toString(flurstueckId))
                        .replace("{PREDECESSOR_LEVEL}", Integer.toString(predecessorLevel))
                        .replace("{SUCCESSOR_LEVEL}", Integer.toString(successorLevel))
                        .replace("{SIBBLING_LEVEL}", Integer.toString(sibblingLevel));
            
            LOG.debug("History SQL-Query: <br>" +  org.apache.commons.lang.StringEscapeUtils.escapeHtml(query));
            final Collection<ArrayList> fieldsColl = metaService.performCustomSearch(query);
            final Collection<FlurstueckHistorieGraphSearchResultItem> items =
                new ArrayList<FlurstueckHistorieGraphSearchResultItem>(fieldsColl.size());
            for (final ArrayList fields : fieldsColl) {
                final FlurstueckHistorieGraphSearchResultItem item = new FlurstueckHistorieGraphSearchResultItem(
                        (Integer)fields.get(0),
                        (Integer)fields.get(1),
                        (String)fields.get(2),
                        (Integer)fields.get(3),
                        (String)fields.get(4));
                items.add(item);
            }
            return items;
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return null;
        }
    }
}
