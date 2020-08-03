/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author liam.banks
 */
public enum Dimensions {
    Two, Three;
    
    public static List<String> getOptions() {
        ArrayList<String> list = new ArrayList<>();
        for (Dimensions d : Dimensions.values()) {
            list.add(d.toString());
        } 
        return list;
    }
}

