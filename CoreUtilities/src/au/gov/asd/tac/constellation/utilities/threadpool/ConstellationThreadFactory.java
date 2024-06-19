/*
* Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.threadpool;

import java.util.concurrent.ThreadFactory;

/**
 * A thread Factory that allows for thread pools to be created with a identifiable name for enhanced debugging.
 * 
 * @author capricornunicorn123
 */
public class ConstellationThreadFactory implements ThreadFactory{
    private static int threadCount = 0;
    private final String poolName;
    
    public ConstellationThreadFactory(){
        this.poolName = "Constellation-Thread";
    }
    
    public ConstellationThreadFactory(final String poolName){
        this.poolName = poolName;
    }
    
    public static int getCount() {
        return threadCount++;
    }

    @Override
    public Thread newThread(final Runnable r) {
        return new Thread(r, String.format("%s-Pool - Thread-%s", poolName, getCount()));
    }
    
}
