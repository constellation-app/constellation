/*
* Copyright 2010-2023 Australian Signals Directorate
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.testing;

import au.gov.asd.tac.constellation.utilities.file.autosave.AutosaveUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import org.testfx.api.FxToolkit;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A Test Listener that is able to simplify test creation and test debugging.
 * A class that can be used to debug testing environments
 * @author capricornunicorn123
 */
public class ConstellationTestListener implements ITestListener {

    //Called prior to test start up.
    @Override
    public void onTestStart(ITestResult arg0) {
        logStuffForMe("onTestStart", arg0.getName());
    }

    @Override
    public void onTestSuccess(ITestResult arg0) {
        logStuffForMe("onTestSuccess", arg0.getName());
    }

    @Override
    public void onTestFailure(ITestResult arg0) {
        logStuffForMe("onTestFailure", arg0.getName());
    }

    @Override
    public void onTestSkipped(ITestResult arg0) {
        logStuffForMe("onTestSkipped", arg0.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        logStuffForMe("onTestFailedButWithinSuccessPercentage", arg0.getName());
    }

    @Override
    public void onStart(ITestContext arg0) {
        logStuffForMe("onStart", arg0.getCurrentXmlTest().getClasses().stream().findFirst().get().getName());
    }

    @Override
    public void onFinish(ITestContext arg0) {
        logStuffForMe("onFinish", arg0.getCurrentXmlTest().getClasses().stream().findFirst().get().getName());
    }
    
    private void logStuffForMe(final String step, final String reference){
        System.out.println(String.format("At step %s of %s, Environment is Headless: %s", step, reference, GraphicsEnvironment.isHeadless()));
        
        System.out.println(String.format("At step %s of %s, FX Application Thread is Running: %s", step, reference, FxToolkit.isFXApplicationThreadRunning()));
        
        File dir = AutosaveUtilities.getAutosaveDir();
        
        if (dir != null){
            System.out.println(String.format("At step %s of %s, Austosave File Count: %s", step, reference, dir.length()));  
            File[] files = dir.listFiles();
            if (files.length > 0){
                System.out.println(String.format("Files Found in Dir: %s", dir.getAbsolutePath()));
                for (File file : files){
                    System.out.println(file.getAbsolutePath());
                }
            }
        } else {
            System.out.println(String.format("At step %s of %s, Austosave Directory could not be found", step, reference)); 
        }
        
    }
    
}
