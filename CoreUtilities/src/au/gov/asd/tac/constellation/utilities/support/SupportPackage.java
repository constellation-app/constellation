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
package au.gov.asd.tac.constellation.utilities.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.openide.modules.Places;

/**
 * Create a support package
 *
 * @author arcturus
 */
public class SupportPackage {

    /**
     * A convenient method to create a support package
     *
     * @param sourceDirectory The folder to zip
     * @param destinationDirectory The destination zip filename
     * @throws java.io.IOException
     */
    public void createSupportPackage(final File sourceDirectory, final File destinationDirectory) throws IOException {
        final List<String> list = new ArrayList<>();
        generateFileList(sourceDirectory, list, sourceDirectory.getPath());
        zipFolder(sourceDirectory.getPath(), list, destinationDirectory.getPath());
    }

    /**
     * Zip the source folder recursively
     *
     * @param sourceFolder The folder to zip
     * @param files A list of files to zip within the sourceFolder
     * @param destinationZipFilename The destination zip filename
     */
    void zipFolder(final String sourceFolder, final List<String> files, final String destinationZipFilename) throws IOException {
        byte[] buffer = new byte[1024];

        final FileOutputStream fileOutputStream = new FileOutputStream(destinationZipFilename);
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (final String filename : files) {
                if (!filesToIgnore(filename)) {
                    final ZipEntry zipEntry = new ZipEntry(filename);
                    zipOutputStream.putNextEntry(zipEntry);

                    try (final FileInputStream fileInputStream = new FileInputStream(sourceFolder + File.separator + filename)) {
                        int len;
                        while ((len = fileInputStream.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, len);
                        }
                    }
                }
            }
            zipOutputStream.closeEntry();
        }
    }

    /**
     * Create a list of files to zip
     * <p>
     * Note this is a recursive method
     *
     * @param node The starting folder
     * @param list A list of files
     * @param startFolder The starting folder
     */
    void generateFileList(final File node, final List<String> list, final String startFolder) {
        if (node.isFile()) {
            list.add(generateZipEntry(node.getAbsoluteFile().toString(), startFolder));
        }

        if (node.isDirectory()) {
            for (final String filename : node.list()) {
                generateFileList(new File(node, filename), list, startFolder);
            }
        }
    }

    /**
     * The directory where log files are saved to
     *
     * @return A String of the directory the user log files are saved
     */
    public static String getUserLogDirectory() {
        return String.format("%s%svar%slog", (Places.getUserDirectory() != null ? Places.getUserDirectory().getPath() : new File(System.getProperty("user.home"))), File.separator, File.separator);
    }

    private String generateZipEntry(final String file, final String sourcePath) {
        return file.substring(sourcePath.length() + 1, file.length());
    }

    /**
     * Files to ignore
     * <p>
     * Ignore the heapdump file as it will be pretty large
     *
     * @param filename The filename
     * @return True to ignore the file, False otherwise
     */
    private boolean filesToIgnore(final String filename) {
        return "heapdump.hprof".equals(filename) || "heapdump.hprof.old".equals(filename);
    }
}
