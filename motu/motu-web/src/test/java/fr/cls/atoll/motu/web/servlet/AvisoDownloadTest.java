package fr.cls.atoll.motu.web.servlet;

import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class AvisoDownloadTest extends SeleneseTestCase {
    @Override
    public void setUp() throws Exception {
        setUp("http://localhost:8080/", "*chrome");
    }

    public void testAvisoDownload() throws Exception {
        selenium.open("/atoll-motuservlet/Aviso");
        selenium.click("link=Absolute Dynamic Topography");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Downloading and extraction service");
        selenium.waitForPageToLoad("30000");
        selenium.select("t_lo_0", "label=2009-04-29");
        selenium.select("t_hi_0", "label=2009-04-30");
        selenium.click("variable");
        selenium.click("//input[@value='Download']");
        selenium.waitForPageToLoad("30000");
    }
}
