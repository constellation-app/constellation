/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;


/**
 *
 * @author liam.banks
 */
class TreeFactory {

    static AbstractTree create(GraphReadMethods wg, Dimensions d){
        switch (d) {
            case Two:
                return new QuadTree(wg);
            case Three:
                return new OctTree(wg);
            default:
                return null;
        }
    }
}
