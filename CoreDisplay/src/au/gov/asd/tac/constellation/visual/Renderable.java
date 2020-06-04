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
package au.gov.asd.tac.constellation.visual;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;


public interface Renderable extends Comparable<Renderable> {
    public abstract int getPriority();
    public abstract void dispose(final AutoDrawable drawable);
    public abstract void init(final AutoDrawable drawable);
    public abstract void reshape(final int x, final int y, final int width, final int height);
    public abstract void update(final AutoDrawable drawable);
    public void display(final AutoDrawable drawable, final Matrix44f pMatrix);    
}
