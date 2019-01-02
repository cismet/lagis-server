/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.lagis.server.search;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.cidsx.server.api.types.SearchInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */

@RequiredArgsConstructor
public abstract class GeomServerSearch extends AbstractCidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    @Getter public final SearchInfo searchInfo;

    @Getter @Setter private Geometry geometry;
    @Getter @Setter private String crs;
}
