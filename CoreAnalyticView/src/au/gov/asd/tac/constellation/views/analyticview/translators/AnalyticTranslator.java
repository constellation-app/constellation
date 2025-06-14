/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.AnalyticVisualisation;

/**
 *
 * @author cygnus_x-1
 *
 * @param <R>
 * @param <V>
 */
public abstract class AnalyticTranslator<R extends AnalyticResult<?>, V extends AnalyticVisualisation> {

    protected AnalyticQuestion<?> question;
    protected R result;
    protected boolean active;

    public void setQuestion(final AnalyticQuestion<?> question) {
        this.question = question;
    }

    public void setResult(final R result) {
        this.result = result;
    }
    
    public R getResult() {
        return result;
    }
    
    public AnalyticQuestion<?> getQuestion() {
        return question;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(final boolean active) {
        this.active = active;
    }

    public abstract String getName();

    @SuppressWarnings("rawtypes") //raw type needed for AnyToReport implementation
    public abstract Class<? extends AnalyticResult> getResultType();
}
