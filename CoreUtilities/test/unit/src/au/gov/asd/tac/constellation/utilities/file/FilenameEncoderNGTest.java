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
package au.gov.asd.tac.constellation.utilities.file;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class FilenameEncoderNGTest {

    @Test
    public void testDecode1() {
        final String s = "myname at 2014-08-06 08:49:14 EST";
        final String result = FilenameEncoder.decode(FilenameEncoder.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecode2() {
        final String s = "~!@#$%^&*()_+";
        final String result = FilenameEncoder.decode(FilenameEncoder.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecode3() {
        final String s = ":;\\|<>[]{}/?";
        final String result = FilenameEncoder.decode(FilenameEncoder.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecodeBad1() {
        final String result = FilenameEncoder.decode(SeparatorConstants.UNDERSCORE);
        Assert.assertNull(result);
    }

    @Test
    public void testDecodeBad2() {
        final String result = FilenameEncoder.decode("_12");
        Assert.assertNull(result);
    }

    @Test
    public void testDecodeBad3() {
        final String result = FilenameEncoder.decode("_123q");
        Assert.assertNull(result);
    }
}
