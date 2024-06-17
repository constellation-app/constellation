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
package au.gov.asd.tac.constellation.spellcheck;

import java.util.concurrent.Future;
import org.openide.modules.OnStart;

/**
 * The purpose of this class is to trigger the SpellChecker.LANGTOOL_LOAD to run
 * on startup.
 *
 * @author Auriga2
 */
@OnStart
public class LanguageToolStartUpLoad implements Runnable {

    @Override
    public void run() {
        final Future<Void> f = SpellChecker.LANGTOOL_LOAD;
    }
}
