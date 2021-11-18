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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a quick and dirty LCG RNG to enable reproducibility between this code
 * and the original C++.
 * <p>
 * When complete, revert to Random.
 *
 * @author algol
 */
public class Lcg {

    private static final Logger LOGGER = Logger.getLogger(Lcg.class.getName());

    private static final long M = 4294967296L;
    private static final long A = 69069;
    private static final long C = 1;

    private long s;

    public Lcg() {
        s = 0;
    }

    public void seed(final long newSeed) {
        s = newSeed;
        Logf.printf("{{seed %d}}\n", newSeed);
    }

    public int nextInt() {
        s = (A * s + C) % M;

        return (int) (s & 0x7fffffffL);
    }

    public int nextInt(final int modulo) {
        return nextInt() % (modulo + 1);
    }

    public int randInt(final int modulo) {
        return nextInt(modulo);
    }

    public double nextDouble() {
        s = (A * s + C) % M;
        return s / (double) M;
    }

    public static void main(final String[] args) {
        final Lcg rand = new Lcg();
        rand.seed(1984);

        for (int i = 0; i < 100; i++) {
            final String log = String.format("%d %d %d%n", i, rand.nextInt(10), rand.s);
            LOGGER.log(Level.INFO, log);
        }
    }
}
