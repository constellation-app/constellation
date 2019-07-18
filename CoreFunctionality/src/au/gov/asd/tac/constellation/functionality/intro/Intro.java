/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package au.gov.asd.tac.constellation.functionality.intro;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author imranraza83
 */
@ServiceProvider(service = IntroProvider.class)
public class Intro extends IntroProvider{

    @Override
    public String getResource() {
        return "intro.html";
    }

}
