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
package au.gov.asd.tac.constellation.utilities.nifi;

import java.util.List;

/**
 * A Niagara Files Configuration.
 *
 * @author cygnus_x-1
 */
public interface NifiConfig {

    public List<String> getNifiNodes();

    public String getNifiUri();

    public boolean sslEnabled();

    public boolean duplicateFilterEnabled();

//    public String getServerKeystore();
//
//    public String getServerKeystorePassword();
//
//    public String getServerTruststore();
//
//    public String getServerTruststorePassword();
//
//    public String getExtCamKeysFile();
//
//    public String getTheCatalogFile();
//
//    public String getDirectIngestJsonSchema();
//
//    public String getBuildInfo();
//
//    public Boolean getHvsCommitEnabled();
//
//    public String getHvsGitUri();
//
//    public String getHvsGitDir();
//
//    public String getHvsFileGitSuffix();
//
//    public String getHvsFile();
//
//    public String getMappingParentDir();
}
