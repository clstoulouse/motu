/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.cls.atoll.motu.library.vfs.provider.gsiftp;

import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.URLFileNameParser;


/**
 * Implementation for sftp. set default port to 22
 */
public class GsiFtpFileNameParser extends URLFileNameParser {
    
    /** The Constant INSTANCE. */
    private final static GsiFtpFileNameParser INSTANCE = new GsiFtpFileNameParser();

    /**
     * Instantiates a new gsi ftp file name parser.
     */
    public GsiFtpFileNameParser() {
        super(2811);
    }
    
    /**
     * Gets the single instance of GsiFtpFileNameParser.
     * 
     * @return single instance of GsiFtpFileNameParser
     */
    public static FileNameParser getInstance() {
        return INSTANCE;
    }
}
