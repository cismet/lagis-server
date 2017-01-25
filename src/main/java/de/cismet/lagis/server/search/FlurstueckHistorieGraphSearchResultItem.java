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
package de.cismet.lagis.server.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@AllArgsConstructor
public class FlurstueckHistorieGraphSearchResultItem implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final Integer level;
    private final Integer nachfolgerSchluesselId;
    private final String nachfolgerName;
    private final Integer vorgaengerSchluesselId;
    private final String vorgaengerName;
}
