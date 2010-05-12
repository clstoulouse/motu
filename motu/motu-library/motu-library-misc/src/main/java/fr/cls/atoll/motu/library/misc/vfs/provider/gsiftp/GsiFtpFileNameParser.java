package fr.cls.atoll.motu.library.misc.vfs.provider.gsiftp;

import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.URLFileNameParser;

/**
 * Implementation for sftp. set default port to 22
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
