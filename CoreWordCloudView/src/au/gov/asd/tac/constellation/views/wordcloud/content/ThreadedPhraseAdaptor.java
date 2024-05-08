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
package au.gov.asd.tac.constellation.views.wordcloud.content;

/**
 * Adaptor pattern to connect an arbitrary String data source whose data can be partitioned into groups for threaded processing.
 * Each thread will have its own adaptor; the class implementing using these adaptors should have a method which takes an initial adaptor and then repeatedly calls createNextAdaptor(), 
 * attaching each to the relevant runnable and thread. The data is accessed via called getNextPhrase in a loop, either limited by getWorkload(), or conditional on hasNextPhrase().
 *
 * @author twilight_sparkle
 */
public abstract class ThreadedPhraseAdaptor {

    public abstract int getWorkload();

    public abstract String getNextPhrase();

    public abstract boolean hasNextPhrase();

    // Gets a read lock on the underlying data source
    public abstract void connect();

    // Removes the lock this adaptor has on its underlying data source
    public abstract void disconnect();

    // Returns the 'id' of the element (phrase or somethign with reference to a phrase) currently being accessed in the data. This id should be unique for each element.
    // It need not represent anything about the structure of the underlying data, although the underlying data should be able to interpret this id to process results from objects that this phraseAdaptor connects to.
    public abstract int getCurrentElementID();
}
