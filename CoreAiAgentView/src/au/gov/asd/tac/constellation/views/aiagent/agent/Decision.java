/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.agent;

/**
 * The user's decision about a tool call that needs approval.
 *
 * @author doubtsec
 */
public sealed interface Decision {

    /**
     * Run the call as proposed.
     */
    record Approve() implements Decision {
    }

    /**
     * Run the call, and run future calls of the same tool without asking for
     * the rest of the session.
     */
    record AlwaysAllow() implements Decision {
    }

    /**
     * Run the call with edited arguments.
     *
     * @param arguments the new arguments as JSON.
     */
    record Edit(String arguments) implements Decision {
    }

    /**
     * Don't run the call; the reason is returned to the model.
     *
     * @param reason why the call was denied, may be empty.
     */
    record Deny(String reason) implements Decision {
    }

    /**
     * Stop the whole run.
     */
    record Stop() implements Decision {
    }
}
