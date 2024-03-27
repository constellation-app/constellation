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
package au.gov.asd.tac.constellation.utilities.icon;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining character icons.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class CharacterIconProvider implements ConstellationIconProvider {

    private static final String CODE_NAME_BASE = "au.gov.asd.tac.constellation.utilities";

    private static final String CHARACTER_CATEGORY = "Character";

    public static final ConstellationIcon CHAR_0020 = new ConstellationIcon.Builder("Space", new FileIconData("modules/ext/icons/0020.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0021 = new ConstellationIcon.Builder("Exclaimation Mark", new FileIconData("modules/ext/icons/0021.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0022 = new ConstellationIcon.Builder("Quotation Mark", new FileIconData("modules/ext/icons/0022.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0023 = new ConstellationIcon.Builder("Hash", new FileIconData("modules/ext/icons/0023.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0024 = new ConstellationIcon.Builder("Dollar Symbol", new FileIconData("modules/ext/icons/0024.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0025 = new ConstellationIcon.Builder("Percent Symbol", new FileIconData("modules/ext/icons/0025.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0026 = new ConstellationIcon.Builder("Ampersand", new FileIconData("modules/ext/icons/0026.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0027 = new ConstellationIcon.Builder("Inverted Comma", new FileIconData("modules/ext/icons/0027.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0028 = new ConstellationIcon.Builder("Opening Round Bracket", new FileIconData("modules/ext/icons/0028.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0029 = new ConstellationIcon.Builder("Closing Round Bracket", new FileIconData("modules/ext/icons/0029.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002A = new ConstellationIcon.Builder("Asterisk", new FileIconData("modules/ext/icons/002a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002B = new ConstellationIcon.Builder("Plus", new FileIconData("modules/ext/icons/002b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002C = new ConstellationIcon.Builder("Comma", new FileIconData("modules/ext/icons/002c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002D = new ConstellationIcon.Builder("Dash", new FileIconData("modules/ext/icons/002d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002E = new ConstellationIcon.Builder("Full Stop", new FileIconData("modules/ext/icons/002e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_002F = new ConstellationIcon.Builder("Forward Slash", new FileIconData("modules/ext/icons/002f.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0030 = new ConstellationIcon.Builder("0", new FileIconData("modules/ext/icons/0030.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0031 = new ConstellationIcon.Builder("1", new FileIconData("modules/ext/icons/0031.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0032 = new ConstellationIcon.Builder("2", new FileIconData("modules/ext/icons/0032.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0033 = new ConstellationIcon.Builder("3", new FileIconData("modules/ext/icons/0033.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0034 = new ConstellationIcon.Builder("4", new FileIconData("modules/ext/icons/0034.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0035 = new ConstellationIcon.Builder("5", new FileIconData("modules/ext/icons/0035.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0036 = new ConstellationIcon.Builder("6", new FileIconData("modules/ext/icons/0036.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0037 = new ConstellationIcon.Builder("7", new FileIconData("modules/ext/icons/0037.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0038 = new ConstellationIcon.Builder("8", new FileIconData("modules/ext/icons/0038.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0039 = new ConstellationIcon.Builder("9", new FileIconData("modules/ext/icons/0039.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003A = new ConstellationIcon.Builder("Colon", new FileIconData("modules/ext/icons/003a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003B = new ConstellationIcon.Builder("Semi-Colon", new FileIconData("modules/ext/icons/003b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003C = new ConstellationIcon.Builder("Less Than", new FileIconData("modules/ext/icons/003c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003D = new ConstellationIcon.Builder("Equals", new FileIconData("modules/ext/icons/003d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003E = new ConstellationIcon.Builder("Greater Than", new FileIconData("modules/ext/icons/003e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_003F = new ConstellationIcon.Builder("Question Mark", new FileIconData("modules/ext/icons/003f.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0040 = new ConstellationIcon.Builder("At Symbol", new FileIconData("modules/ext/icons/0040.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0041 = new ConstellationIcon.Builder("A", new FileIconData("modules/ext/icons/0041.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0042 = new ConstellationIcon.Builder("B", new FileIconData("modules/ext/icons/0042.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0043 = new ConstellationIcon.Builder("C", new FileIconData("modules/ext/icons/0043.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0044 = new ConstellationIcon.Builder("D", new FileIconData("modules/ext/icons/0044.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0045 = new ConstellationIcon.Builder("E", new FileIconData("modules/ext/icons/0045.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0046 = new ConstellationIcon.Builder("F", new FileIconData("modules/ext/icons/0046.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0047 = new ConstellationIcon.Builder("G", new FileIconData("modules/ext/icons/0047.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0048 = new ConstellationIcon.Builder("H", new FileIconData("modules/ext/icons/0048.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0049 = new ConstellationIcon.Builder("I", new FileIconData("modules/ext/icons/0049.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004A = new ConstellationIcon.Builder("J", new FileIconData("modules/ext/icons/004a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004B = new ConstellationIcon.Builder("K", new FileIconData("modules/ext/icons/004b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004C = new ConstellationIcon.Builder("L", new FileIconData("modules/ext/icons/004c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004D = new ConstellationIcon.Builder("M", new FileIconData("modules/ext/icons/004d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004E = new ConstellationIcon.Builder("N", new FileIconData("modules/ext/icons/004e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_004F = new ConstellationIcon.Builder("O", new FileIconData("modules/ext/icons/004f.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0050 = new ConstellationIcon.Builder("P", new FileIconData("modules/ext/icons/0050.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0051 = new ConstellationIcon.Builder("Q", new FileIconData("modules/ext/icons/0051.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0052 = new ConstellationIcon.Builder("R", new FileIconData("modules/ext/icons/0052.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0053 = new ConstellationIcon.Builder("S", new FileIconData("modules/ext/icons/0053.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0054 = new ConstellationIcon.Builder("T", new FileIconData("modules/ext/icons/0054.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0055 = new ConstellationIcon.Builder("U", new FileIconData("modules/ext/icons/0055.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0056 = new ConstellationIcon.Builder("V", new FileIconData("modules/ext/icons/0056.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0057 = new ConstellationIcon.Builder("W", new FileIconData("modules/ext/icons/0057.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0058 = new ConstellationIcon.Builder("X", new FileIconData("modules/ext/icons/0058.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0059 = new ConstellationIcon.Builder("Y", new FileIconData("modules/ext/icons/0059.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005A = new ConstellationIcon.Builder("Z", new FileIconData("modules/ext/icons/005a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005B = new ConstellationIcon.Builder("Opening Square Bracket", new FileIconData("modules/ext/icons/005b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005C = new ConstellationIcon.Builder("Back Slash", new FileIconData("modules/ext/icons/005c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005D = new ConstellationIcon.Builder("Closing Square Bracket", new FileIconData("modules/ext/icons/005d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005E = new ConstellationIcon.Builder("Carot", new FileIconData("modules/ext/icons/005e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_005F = new ConstellationIcon.Builder("Underscore", new FileIconData("modules/ext/icons/005f.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0060 = new ConstellationIcon.Builder("Back Tick", new FileIconData("modules/ext/icons/0060.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0061 = new ConstellationIcon.Builder("a", new FileIconData("modules/ext/icons/0061.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0062 = new ConstellationIcon.Builder("b", new FileIconData("modules/ext/icons/0062.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0063 = new ConstellationIcon.Builder("c", new FileIconData("modules/ext/icons/0063.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0064 = new ConstellationIcon.Builder("d", new FileIconData("modules/ext/icons/0064.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0065 = new ConstellationIcon.Builder("e", new FileIconData("modules/ext/icons/0065.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0066 = new ConstellationIcon.Builder("f", new FileIconData("modules/ext/icons/0066.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0067 = new ConstellationIcon.Builder("g", new FileIconData("modules/ext/icons/0067.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0068 = new ConstellationIcon.Builder("h", new FileIconData("modules/ext/icons/0068.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0069 = new ConstellationIcon.Builder("i", new FileIconData("modules/ext/icons/0069.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006A = new ConstellationIcon.Builder("j", new FileIconData("modules/ext/icons/006a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006B = new ConstellationIcon.Builder("k", new FileIconData("modules/ext/icons/006b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006C = new ConstellationIcon.Builder("l", new FileIconData("modules/ext/icons/006c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006D = new ConstellationIcon.Builder("m", new FileIconData("modules/ext/icons/006d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006E = new ConstellationIcon.Builder("n", new FileIconData("modules/ext/icons/006e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_006F = new ConstellationIcon.Builder("o", new FileIconData("modules/ext/icons/006f.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0070 = new ConstellationIcon.Builder("p", new FileIconData("modules/ext/icons/0070.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0071 = new ConstellationIcon.Builder("q", new FileIconData("modules/ext/icons/0071.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0072 = new ConstellationIcon.Builder("r", new FileIconData("modules/ext/icons/0072.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0073 = new ConstellationIcon.Builder("s", new FileIconData("modules/ext/icons/0073.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0074 = new ConstellationIcon.Builder("t", new FileIconData("modules/ext/icons/0074.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0075 = new ConstellationIcon.Builder("u", new FileIconData("modules/ext/icons/0075.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0076 = new ConstellationIcon.Builder("v", new FileIconData("modules/ext/icons/0076.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0077 = new ConstellationIcon.Builder("w", new FileIconData("modules/ext/icons/0077.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0078 = new ConstellationIcon.Builder("x", new FileIconData("modules/ext/icons/0078.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_0079 = new ConstellationIcon.Builder("y", new FileIconData("modules/ext/icons/0079.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_007A = new ConstellationIcon.Builder("z", new FileIconData("modules/ext/icons/007a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_007B = new ConstellationIcon.Builder("Opening Curly Bracket", new FileIconData("modules/ext/icons/007b.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_007C = new ConstellationIcon.Builder("Pipe", new FileIconData("modules/ext/icons/007c.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_007D = new ConstellationIcon.Builder("Closing Curly Bracket", new FileIconData("modules/ext/icons/007d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_007E = new ConstellationIcon.Builder("Tilder", new FileIconData("modules/ext/icons/007e.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_2620 = new ConstellationIcon.Builder("Skull & Crossbones", new FileIconData("modules/ext/icons/2620.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_262D = new ConstellationIcon.Builder("Hammer & Sickle", new FileIconData("modules/ext/icons/262d.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_2639 = new ConstellationIcon.Builder("Sad Face", new FileIconData("modules/ext/icons/2639.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();
    public static final ConstellationIcon CHAR_263A = new ConstellationIcon.Builder("Smiley Face", new FileIconData("modules/ext/icons/263a.png", CODE_NAME_BASE))
            .addCategory(CHARACTER_CATEGORY)
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        final List<ConstellationIcon> characterIcons = new ArrayList<>();
        characterIcons.add(CHAR_0020);
        characterIcons.add(CHAR_0021);
        characterIcons.add(CHAR_0022);
        characterIcons.add(CHAR_0023);
        characterIcons.add(CHAR_0024);
        characterIcons.add(CHAR_0025);
        characterIcons.add(CHAR_0026);
        characterIcons.add(CHAR_0027);
        characterIcons.add(CHAR_0028);
        characterIcons.add(CHAR_0029);
        characterIcons.add(CHAR_002A);
        characterIcons.add(CHAR_002B);
        characterIcons.add(CHAR_002C);
        characterIcons.add(CHAR_002D);
        characterIcons.add(CHAR_002E);
        characterIcons.add(CHAR_002F);
        characterIcons.add(CHAR_0030);
        characterIcons.add(CHAR_0031);
        characterIcons.add(CHAR_0032);
        characterIcons.add(CHAR_0033);
        characterIcons.add(CHAR_0034);
        characterIcons.add(CHAR_0035);
        characterIcons.add(CHAR_0036);
        characterIcons.add(CHAR_0037);
        characterIcons.add(CHAR_0038);
        characterIcons.add(CHAR_0039);
        characterIcons.add(CHAR_003A);
        characterIcons.add(CHAR_003B);
        characterIcons.add(CHAR_003C);
        characterIcons.add(CHAR_003D);
        characterIcons.add(CHAR_003E);
        characterIcons.add(CHAR_003F);
        characterIcons.add(CHAR_0040);
        characterIcons.add(CHAR_0041);
        characterIcons.add(CHAR_0042);
        characterIcons.add(CHAR_0043);
        characterIcons.add(CHAR_0044);
        characterIcons.add(CHAR_0045);
        characterIcons.add(CHAR_0046);
        characterIcons.add(CHAR_0047);
        characterIcons.add(CHAR_0048);
        characterIcons.add(CHAR_0049);
        characterIcons.add(CHAR_004A);
        characterIcons.add(CHAR_004B);
        characterIcons.add(CHAR_004C);
        characterIcons.add(CHAR_004D);
        characterIcons.add(CHAR_004E);
        characterIcons.add(CHAR_004F);
        characterIcons.add(CHAR_0050);
        characterIcons.add(CHAR_0051);
        characterIcons.add(CHAR_0052);
        characterIcons.add(CHAR_0053);
        characterIcons.add(CHAR_0054);
        characterIcons.add(CHAR_0055);
        characterIcons.add(CHAR_0056);
        characterIcons.add(CHAR_0057);
        characterIcons.add(CHAR_0058);
        characterIcons.add(CHAR_0059);
        characterIcons.add(CHAR_005A);
        characterIcons.add(CHAR_005B);
        characterIcons.add(CHAR_005C);
        characterIcons.add(CHAR_005D);
        characterIcons.add(CHAR_005E);
        characterIcons.add(CHAR_005F);
        characterIcons.add(CHAR_0060);
        characterIcons.add(CHAR_0061);
        characterIcons.add(CHAR_0062);
        characterIcons.add(CHAR_0063);
        characterIcons.add(CHAR_0064);
        characterIcons.add(CHAR_0065);
        characterIcons.add(CHAR_0066);
        characterIcons.add(CHAR_0067);
        characterIcons.add(CHAR_0068);
        characterIcons.add(CHAR_0069);
        characterIcons.add(CHAR_006A);
        characterIcons.add(CHAR_006B);
        characterIcons.add(CHAR_006C);
        characterIcons.add(CHAR_006D);
        characterIcons.add(CHAR_006E);
        characterIcons.add(CHAR_006F);
        characterIcons.add(CHAR_0070);
        characterIcons.add(CHAR_0071);
        characterIcons.add(CHAR_0072);
        characterIcons.add(CHAR_0073);
        characterIcons.add(CHAR_0074);
        characterIcons.add(CHAR_0075);
        characterIcons.add(CHAR_0076);
        characterIcons.add(CHAR_0077);
        characterIcons.add(CHAR_0078);
        characterIcons.add(CHAR_0079);
        characterIcons.add(CHAR_007A);
        characterIcons.add(CHAR_007B);
        characterIcons.add(CHAR_007C);
        characterIcons.add(CHAR_007D);
        characterIcons.add(CHAR_007E);
        characterIcons.add(CHAR_2620);
        characterIcons.add(CHAR_262D);
        characterIcons.add(CHAR_2639);
        characterIcons.add(CHAR_263A);
        return characterIcons;
    }
}
