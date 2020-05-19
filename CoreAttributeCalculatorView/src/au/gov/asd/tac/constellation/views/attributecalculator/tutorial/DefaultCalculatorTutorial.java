/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributecalculator.tutorial;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class DefaultCalculatorTutorial extends AbstractCalculatorTutorial {

    private static final List<String> TUTORIAL_PAGES = Arrays.asList("ForPythonUsers.html", "Contents.html");

    @Override
    public List<String> getPages() {
        return TUTORIAL_PAGES;
    }

}
