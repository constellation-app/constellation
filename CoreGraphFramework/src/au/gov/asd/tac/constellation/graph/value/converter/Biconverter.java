/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.converter;

/**
 *
 * @author darren
 */
public interface Biconverter<S1, S2, D> {
    public D convert(S1 source1, S2 source2);
}
