/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import java.util.concurrent.Future;
import org.openide.modules.OnStart;

/**
 * The purpose of this class is purely to trigger the process in RunPane.FILTER_LOAD to run on startup.
 *
 * @author antares
 */
@OnStart
public class FilterStartUpLoad implements Runnable {

    @Override
    public void run() {
        //pointless statement to trigger RunPane proesses
        final Future<Void> f = RunPane.FILTER_LOAD;
    }
    
}
