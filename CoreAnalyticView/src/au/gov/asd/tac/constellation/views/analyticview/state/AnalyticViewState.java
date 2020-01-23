/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.state;

import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane.SelectableAnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticInfo;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all AnalyticQuestion currently active in the Analytic View.
 *
 * @author cygnus_x-1
 */
public class AnalyticViewState {

    private int currentAnalyticQuestionIndex;
    private final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions;
    private final List<List<SelectableAnalyticPlugin>> activeSelectablePlugins;

    public AnalyticViewState() {
        this(0, new ArrayList<>(), new ArrayList<>());
    }

    public AnalyticViewState(final AnalyticViewState state) {
        this.currentAnalyticQuestionIndex = state.getCurrentAnalyticQuestionIndex();
        this.activeAnalyticQuestions = new ArrayList<>(state.getActiveAnalyticQuestions());
        this.activeSelectablePlugins = new ArrayList<>(state.getActiveSelectablePlugins());
    }

    public AnalyticViewState(final int currentQuestionIndex, final List<AnalyticQuestionDescription<?>> activeQuestions, final List<List<SelectableAnalyticPlugin>> activePlugins) {
        this.currentAnalyticQuestionIndex = currentQuestionIndex;
        this.activeAnalyticQuestions = activeQuestions;
        this.activeSelectablePlugins = activePlugins;
    }

    public int getCurrentAnalyticQuestionIndex() {
        return currentAnalyticQuestionIndex;
    }

    public void setCurrentAnalyticQuestionIndex(final int currentAnalyticQuestionIndex) {
        this.currentAnalyticQuestionIndex = currentAnalyticQuestionIndex;
    }

    public List<AnalyticQuestionDescription<?>> getActiveAnalyticQuestions() {
        return activeAnalyticQuestions;
    }

    public List<List<SelectableAnalyticPlugin>> getActiveSelectablePlugins() {
        return activeSelectablePlugins;
    }

    public void addAnalyticQuestion(final AnalyticQuestionDescription<?> question, final List<SelectableAnalyticPlugin> selectablePlugins) {
        System.out.println("start add analyticquestion");
        System.out.println("size: " + activeSelectablePlugins.size());
        activeAnalyticQuestions.add(question);
        for(int i = 0; i < selectablePlugins.size();i++){
            final List<SelectableAnalyticPlugin> plugin = new ArrayList<>();
            final SelectableAnalyticPlugin sap = selectablePlugins.get(i);
            plugin.add(sap);
            activeSelectablePlugins.add(plugin);
        }       
        System.out.println("mid add analyticquestion");
        System.out.println("size: " + activeSelectablePlugins.size());
        int removeIndex = -1;
        for(int i = 0; i < activeSelectablePlugins.size();i++){
            for(int j = i+1; j < activeSelectablePlugins.size();j++){
                if(!activeSelectablePlugins.get(i).isEmpty() && isSamePluginName(activeSelectablePlugins.get(i).get(0),activeSelectablePlugins.get(j).get(0))){
                    System.out.println(activeSelectablePlugins.get(i).get(0).getPlugin().getClass().getName() + " same as " + activeSelectablePlugins.get(j).get(0).getPlugin().getClass().getName());
                    //System.out.println("same name found here !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    removeIndex = i;
                }
            }
        }
        System.out.println("size: " + activeSelectablePlugins.size());
        if(removeIndex != -1){
            System.out.println("Deleting index : "  + removeIndex + " : " + activeSelectablePlugins.get(removeIndex).get(0).getPlugin().getClass().getName());
            activeSelectablePlugins.remove(removeIndex);
            
        }   
        System.out.println("size aftter delete 1 : " + activeSelectablePlugins.size());
        removeIndex = -1;
        for(int i = 0; i < activeSelectablePlugins.size();i++){
            for(int j = i+1; j < activeSelectablePlugins.size();j++){
                if(!activeSelectablePlugins.get(i).isEmpty() && isSamePluginName(activeSelectablePlugins.get(i).get(0),activeSelectablePlugins.get(j).get(0))){
                    System.out.println(activeSelectablePlugins.get(i).get(0).getPlugin().getClass().getName() + " same as " + activeSelectablePlugins.get(j).get(0).getPlugin().getClass().getName());
                    //System.out.println("same name found here !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    removeIndex = i;
                }
            }
        }
        if(removeIndex != -1){
            System.out.println("Deleting index : "  + removeIndex + " : " + activeSelectablePlugins.get(removeIndex).get(0).getPlugin().getClass().getName());
            activeSelectablePlugins.remove(removeIndex);
            
        }   
        System.out.println("end add analyticquestion");
    }
    
    // shallow compare two plugins if they are the same type (not the exact same object)
    private boolean isSamePluginName(final SelectableAnalyticPlugin p1, final SelectableAnalyticPlugin p2){
        if(p1.getPlugin().getClass().getName().equals(p2.getPlugin().getClass().getName())){
            //System.out.println("Deleting : " + p1.getPlugin().getClass().getName());
        }
        //System.out.println("p1 :" + p1.getPlugin().getClass().getName() + " p2 : " + p2.getPlugin().getClass().getName());
        return p1.getPlugin().getClass().getName().equals(p2.getPlugin().getClass().getName());
    }

    public void removeAnalyticQuestion(final AnalyticQuestionDescription<?> question) {
        activeSelectablePlugins.remove(activeAnalyticQuestions.indexOf(question));
        activeAnalyticQuestions.remove(question);
    }
    
    public void removeAnalyticQuestions(final AnalyticQuestionDescription<?> question, final List<SelectableAnalyticPlugin> selectablePlugins) {
        System.out.println("start remove analyticquestion");
        List<List<SelectableAnalyticPlugin>> removearray = new ArrayList<>();
        
        for(int i = 0; i < selectablePlugins.size();i++){
            for(int j = 0; j < activeSelectablePlugins.size();j++){
                if(selectablePlugins.get(i).getClass().getName().equals(activeSelectablePlugins.get(i).get(0).getClass().getName())){
                    // when the plugins are the same name
                    removearray.add(activeSelectablePlugins.get(i));
                }
            }
        }
        System.out.println("mid remove analyticquestion");
        activeSelectablePlugins.removeAll(removearray);
        activeAnalyticQuestions.remove(question);
        System.out.println("end remove analyticquestion");
    }
    
    public void clearAnalyticQuestions() {
        activeAnalyticQuestions.clear();
        activeSelectablePlugins.clear();
    }

    public void removePluginsMatchingCategory(String currentCategory) {
            List<List<SelectableAnalyticPlugin>> removearray = new ArrayList<>();
            for(int i = 0; i < activeSelectablePlugins.size();i++){
                //if(currentCategory.equals(selectedPluginList.get(0).plugin.getClass().getAnnotation(AnalyticInfo.class).analyticCategory())){
                if(!activeSelectablePlugins.get(i).isEmpty() && activeSelectablePlugins.get(i).get(0).getPlugin().getClass().getAnnotation(AnalyticInfo.class).analyticCategory().equals(currentCategory)){
                    // when same catelog
                    removearray.add(activeSelectablePlugins.get(i));
                }
            }
            activeSelectablePlugins.removeAll(removearray);
    }
}
