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

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.NetbeansUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The default implementation of CustomIconProvider, saves and loads custom
 * icons to and from the CONSTELLATION user directory.
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = ConstellationIconProvider.class),
    @ServiceProvider(service = CustomIconProvider.class)})
public class DefaultCustomIconProvider implements CustomIconProvider {

    public static final String USER_ICON_DIR = "Icons";
    private static final Logger LOGGER = Logger.getLogger(DefaultCustomIconProvider.class.getName());

    private static final Map<ConstellationIcon, File> CUSTOM_ICONS = new HashMap<>();

    public DefaultCustomIconProvider() {
        loadIcons();
    }

    @Override
    public boolean addIcon(final ConstellationIcon icon) {
        boolean added = false;

        // If the icon is the same as a built-in or existing user icon, ignore it.
        if (!IconManager.iconExists(icon.getExtendedName()) && DefaultCustomIconProvider.getIconDirectory() != null) {
            final File iconDirectoryFile = DefaultCustomIconProvider.getIconDirectory();
            final String iconDirectory = iconDirectoryFile.getAbsolutePath();
            final File iconFile = new File(iconDirectory, icon.getExtendedName() + ConstellationIcon.DEFAULT_ICON_SEPARATOR + ConstellationIcon.DEFAULT_ICON_FORMAT);
            if (!iconFile.exists()) {
                try {
                    final BufferedImage image = icon.buildBufferedImage();
                    if (image != null) {
                        ImageIO.write(image, ConstellationIcon.DEFAULT_ICON_FORMAT, iconFile);
                        CUSTOM_ICONS.put(icon, iconFile);
                        icon.setEditable(true);
                        added = true;
                    }
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return added;
    }

    @Override
    public boolean removeIcon(final String iconName) {
        boolean removed = false;

        final ConstellationIcon icon = IconManager.getIcon(iconName);
        if (icon != null && icon.isEditable()) {
            final File iconFile = CUSTOM_ICONS.get(icon);
            try {
                Files.delete(iconFile.toPath());
            } catch (IOException ioex) {
                iconFile.deleteOnExit();
            }
            CUSTOM_ICONS.remove(icon);
            removed = true;
        }

        return removed;
    }

    /**
     * Check for the presence of a specific icon in the local cache
     *
     * @param iconName name of the icon entry we're looking for
     * @return <b>true</b> if iconName is found in the local cache, otherwise
     * <b>false</b>
     */
    public static boolean containsIcon(final String iconName) {
        for (final ConstellationIcon custIcon : CUSTOM_ICONS.keySet()) {
            // check the name of each icon in the local cache
            if (custIcon.getExtendedName().equalsIgnoreCase(iconName)) {
                return true;
            }
        }
        return false;
    }

    public static void reloadIcons() {
        // clear the local cache
        CUSTOM_ICONS.clear();
        // clear the IconManager cache
        IconManager.removeIcon("");
        // load the updated/current set of icons
        loadIcons();
        // rebuild cache (done indirectly as part of the iconExists call)
        IconManager.iconExists("Unknown");
    }

    @Override
    public List<ConstellationIcon> getIcons() {
        return new ArrayList<>(CUSTOM_ICONS.keySet());
    }

    public static File getIconDirectory() {
        // If for whatever reason we are not running as a netbeans application then it doesn't make sense to check preferences for a user icon directory.
        if (!NetbeansUtilities.isNetbeansApplicationRunning()) {
            return null;
        }
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File iconDir = new File(userDir, USER_ICON_DIR);
        if (!iconDir.exists()) {
            iconDir.mkdir();
        } else if (!iconDir.isDirectory()) {
            LOGGER.warning(String.format("Icon directory '%s' is not a directory", USER_ICON_DIR));
        }
        return iconDir.isDirectory() ? iconDir : null;
    }

    public static void loadIcons() {
        final File iconDirectory = DefaultCustomIconProvider.getIconDirectory();
        if (iconDirectory != null) {
            try (final Stream<Path> filePathStream = Files.walk(iconDirectory.toPath())) {
                filePathStream.forEach(filePath -> {
                    final File file = filePath.toFile();
                    final String fileName = filePath.getFileName().toString();
                    if (file.isFile() && fileName.endsWith(ConstellationIcon.DEFAULT_ICON_FORMAT)) {
                        final String extensionlessFileName = fileName.replace(SeparatorConstants.PERIOD + ConstellationIcon.DEFAULT_ICON_FORMAT, "");
                        final String[] iconNameComponents = extensionlessFileName.split("\\" + ConstellationIcon.DEFAULT_ICON_SEPARATOR);
                        final String iconName = iconNameComponents[iconNameComponents.length - 1];
                        if (!containsIcon(iconName)) {
                            final List<String> iconCategories = Arrays.asList(Arrays.copyOfRange(iconNameComponents, 0, iconNameComponents.length - 1));
                            final ConstellationIcon customIcon = new ConstellationIcon.Builder(iconName, new FileIconData(file))
                                    .addCategories(iconCategories)
                                    .setEditable(true)
                                    .build();
                            CUSTOM_ICONS.put(customIcon, file);
                        }
                    }
                });
            } catch (final IOException ex) {
                NotificationDisplayer.getDefault().notify("User Icon Loading",
                        UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                        String.format("Could not load icons from %s:%n%s", iconDirectory, ex.getMessage()),
                        null
                );
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
