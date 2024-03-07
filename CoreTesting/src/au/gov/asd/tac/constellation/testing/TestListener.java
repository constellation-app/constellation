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
package au.gov.asd.tac.constellation.testing;

import java.awt.GraphicsEnvironment;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A class that can be used to debug testing environments
 * @author capricornunicorn123
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult arg0) {
        System.out.println(String.format("Environment is headless: %s", !GraphicsEnvironment.isHeadless()));
    }

    @Override
    public void onTestSuccess(ITestResult arg0) {
        //
    }

    @Override
    public void onTestFailure(ITestResult arg0) {
        //
    }

    @Override
    public void onTestSkipped(ITestResult arg0) {
        //
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        //
    }

    @Override
    public void onStart(ITestContext arg0) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onFinish(ITestContext arg0) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
