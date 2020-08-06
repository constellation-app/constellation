/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.expression;

import au.gov.asd.tac.constellation.graph.value.IndexedReadable;

/**
 *
 * @author darren
 */
public interface IndexedReadableProvider {
    
    IndexedReadable<?> getIndexedReadable(String name);
    
}
