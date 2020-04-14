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
package au.gov.asd.tac.constellation.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graphics Card Utilities
 *
 * @author sirius
 */
public class GraphicsCardUtilities {

    private static final Logger LOGGER = Logger.getLogger(GraphicsCardUtilities.class.getName());
    private static final Object LOCK = new Object();

    private static boolean loaded = false;
    private static String graphicsCard = null;
    private static String graphicsDriver = null;
    private static String dxDiagInfo = null;
    private static Throwable error = null;

    public static String getGraphicsCard() {
        loadGraphicsCardInfo();
        return graphicsCard;
    }

    public static String getGraphicsDriver() {
        loadGraphicsCardInfo();
        return graphicsDriver;
    }

    public static String getDxDiagInfo() {
        loadGraphicsCardInfo();
        return dxDiagInfo;
    }

    public static Throwable getError() {
        loadGraphicsCardInfo();
        return error;
    }

    private static void loadGraphicsCardInfo() {
        synchronized (LOCK) {
            if (!loaded) {
                loaded = true;

                try {
                    final String tmp = System.getProperty("user.home") + "/dxdiag.txt";
                    final File file = new File(tmp);
                    if (file.exists()) {
                        final boolean fileIsDeleted = file.delete();
                        if (!fileIsDeleted) {
                            //TODO: Handle case where file not successfully deleted
                        }
                    }

                    final long startTime = System.currentTimeMillis();
                    Runtime.getRuntime().exec("dxdiag /64bit /t " + tmp);
                    while (!file.exists()) {
                        try {
                            LOCK.wait(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    final long endTime = System.currentTimeMillis();
                    LOGGER.log(Level.INFO, "Took {0} seconds to retrieve the graphics card capabilities", (endTime - startTime) / 1000);

                    final StringBuilder builder = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                        String line = in.readLine();
                        while (line != null) {
                            builder.append(line);
                            builder.append("\n");

                            if (graphicsCard == null) {
                                int cardNameIndex = line.indexOf("Card name: ");
                                if (cardNameIndex >= 0) {
                                    graphicsCard = line.substring(cardNameIndex + 11).trim();
                                }
                            }

                            if (graphicsDriver == null) {
                                int driverIndex = line.indexOf("Driver Version: ");
                                if (driverIndex >= 0) {
                                    graphicsDriver = line.substring(driverIndex + 16).trim();
                                }
                            }

                            line = in.readLine();
                        }
                    }

                    dxDiagInfo = builder.toString();
                } catch (Exception e) {
                    error = e;
                }
            }
        }
    }
}
