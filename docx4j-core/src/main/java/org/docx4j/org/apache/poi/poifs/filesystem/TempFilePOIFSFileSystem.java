/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.docx4j.org.apache.poi.poifs.filesystem;

import org.docx4j.org.apache.poi.poifs.nio.FileBackedDataSource;
import org.docx4j.org.apache.poi.util.Beta;
import org.docx4j.org.apache.poi.util.TempFile;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * An experimental POIFSFileSystem to support the encryption of large files
 *
 * @since 4.1.1
 */
@Beta
public class TempFilePOIFSFileSystem extends POIFSFileSystem {
	protected static Logger log = LoggerFactory.getLogger(TempFilePOIFSFileSystem.class);	
    File tempFile;

    @Override
    protected void createNewDataSource() {
        try {
            tempFile = TempFile.createTempFile("poifs", ".tmp");
            _data = new FileBackedDataSource(tempFile, false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create data source", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.debug("temp file was already deleted (probably due to previous call to close this resource)");
                }
            }
        }
    }

}
