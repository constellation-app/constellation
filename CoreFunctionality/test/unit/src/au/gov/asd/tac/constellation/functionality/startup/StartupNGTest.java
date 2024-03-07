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
package au.gov.asd.tac.constellation.functionality.startup;

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTestListener;
import javax.swing.JFrame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
@Listeners(ConstellationTestListener.class)
public class StartupNGTest {

    @Test
    public void runInHeadless() {
        try (
                final MockedStatic<ConstellationSecurityManager> constellationSecurityManagerMockedStatic = Mockito.mockStatic(ConstellationSecurityManager.class);
                final MockedStatic<FontUtilities> fontUtilitiesMockedStatic = Mockito.mockStatic(FontUtilities.class);
                final MockedStatic<ProxyUtilities> proxyUtilitiesMockedStatic = Mockito.mockStatic(ProxyUtilities.class);) {
            System.setProperty("java.awt.headless", "true");

            new Startup().run();

            constellationSecurityManagerMockedStatic.verify(() -> ConstellationSecurityManager.startSecurityLater(isNull()));

            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseOutputFontPreferenceOnFirstUse);
            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseApplicationFontPreferenceOnFirstUse);

            proxyUtilitiesMockedStatic.verify(() -> ProxyUtilities.setProxySelector(isNull()));
        } finally {
            System.clearProperty("java.awt.headless");
        }
    }

    @Test
    public void runSetsTitleEnvironmentNotNull() {
        try (
                final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class);
                final MockedStatic<FontUtilities> fontUtilitiesMockedStatic = Mockito.mockStatic(FontUtilities.class);
                final MockedStatic<ProxyUtilities> proxyUtilitiesMockedStatic = Mockito.mockStatic(ProxyUtilities.class);) {
            System.setProperty("constellation.environment", "development");

            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);

            doAnswer(mockInvocation -> {
                final Runnable runnable = (Runnable) mockInvocation.getArgument(0);

                final JFrame frame = mock(JFrame.class);
                when(windowManager.getMainWindow()).thenReturn(frame);

                runnable.run();

                verify(frame).setTitle("Constellation development - (under development)");

                return null;
            }).when(windowManager).invokeWhenUIReady(any(Runnable.class));

            new Startup().run();

            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseOutputFontPreferenceOnFirstUse);
            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseApplicationFontPreferenceOnFirstUse);

            proxyUtilitiesMockedStatic.verify(() -> ProxyUtilities.setProxySelector(isNull()));
        } finally {
            System.clearProperty("constellation.environment");
        }
    }

    @Test
    public void runSetsTitleEnvironmentIsNull() {
        try (
                final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class);
                final MockedStatic<FontUtilities> fontUtilitiesMockedStatic = Mockito.mockStatic(FontUtilities.class);
                final MockedStatic<ProxyUtilities> proxyUtilitiesMockedStatic = Mockito.mockStatic(ProxyUtilities.class);) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);

            doAnswer(mockInvocation -> {
                final Runnable runnable = (Runnable) mockInvocation.getArgument(0);

                final JFrame frame = mock(JFrame.class);
                when(windowManager.getMainWindow()).thenReturn(frame);

                runnable.run();

                verify(frame).setTitle("Constellation - (under development)");

                return null;
            }).when(windowManager).invokeWhenUIReady(any(Runnable.class));

            new Startup().run();

            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseOutputFontPreferenceOnFirstUse);
            fontUtilitiesMockedStatic.verify(FontUtilities::initialiseApplicationFontPreferenceOnFirstUse);

            proxyUtilitiesMockedStatic.verify(() -> ProxyUtilities.setProxySelector(isNull()));
        }
    }
}
