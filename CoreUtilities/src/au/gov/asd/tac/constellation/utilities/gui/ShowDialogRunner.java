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
package au.gov.asd.tac.constellation.utilities.gui;

import org.openide.NotifyDescriptor;

/**
 * {@link Runnable} that opens a user notification with selection options. Once
 * the user makes a selection it is accessible through {@link ShowDialog#getSelection()}.
 * 
 * @author formalhaunt
 */
public class ShowDialogRunner implements Runnable {
    private final NotifyDescriptor descriptor;
        
    private Object selection;

    public ShowDialogRunner(final NotifyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public void run() {
        selection = org.openide.DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     * Gets the user selection.
     *
     * @return the selected option in the notifier
     */
    public Object getSelection() {
        return selection;
    }
}
