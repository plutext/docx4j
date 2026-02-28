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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.util.Internal;

/**
 * A small base class for the various factories, e.g. WorkbookFactory,
 * SlideShowFactory to combine common code here.
 */
@Internal
public final class DocumentFactoryHelper {
	
    /**
     * Some OPCPackages are packed in side an OLE2 container.
     * If encrypted, the {@link DirectoryNode} is called {@link Decryptor#DEFAULT_POIFS_ENTRY "EncryptedPackage"},
     * otherwise the node is called "Package"
     */
    public static final String OOXML_PACKAGE = "Package";
	
    private DocumentFactoryHelper() {
    }

    /**
     * Wrap the OLE2 data in the {@link POIFSFileSystem} into a decrypted stream by using
     * the given password.
     *
     * @param fs The OLE2 stream for the document
     * @param password The password, null if the default password should be used
     * @return A stream for reading the decrypted data
     * @throws IOException If an error occurs while decrypting or if the password does not match
     */
    public static InputStream getDecryptedStream(final POIFSFileSystem fs, String password)
    throws IOException {
        // wrap the stream in a FilterInputStream to close the POIFSFileSystem
        // as well when the resulting OPCPackage is closed
        return new FilterInputStream(getDecryptedStream(fs.getRoot(), password)) {
            @Override
            public void close() throws IOException {
                fs.close();
                super.close();
            }
        };
    }

    /**
     * Wrap the OLE2 data of the DirectoryNode into a decrypted stream by using
     * the given password.
     *
     * @param root The OLE2 directory node for the document
     * @param password The password, null if the default password should be used
     * @return A stream for reading the decrypted data
     * @throws IOException If an error occurs while decrypting or if the password does not match
     */
    public static InputStream getDecryptedStream(final DirectoryNode root, String password)
    throws IOException {
        // first check if the node contains an plain package
        if (root.hasEntryCaseInsensitive(OOXML_PACKAGE)) {
            return root.createDocumentInputStream(OOXML_PACKAGE);
        }

        EncryptionInfo info = new EncryptionInfo(root);
        Decryptor d = Decryptor.getInstance(info);

        try {
            boolean passwordCorrect = false;
            if (password != null && d.verifyPassword(password)) {
                passwordCorrect = true;
            }
            if (!passwordCorrect && d.verifyPassword(Decryptor.DEFAULT_PASSWORD)) {
                passwordCorrect = true;
            }

            if (passwordCorrect) {
                return d.getDataStream(root);
            } else if (password != null) {
                throw new EncryptedDocumentException("Password incorrect");
            } else {
                throw new EncryptedDocumentException("The supplied spreadsheet is protected, but no password was supplied");
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
