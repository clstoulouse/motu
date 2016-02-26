/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.cas.util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

import cls.java.net.PublicInMemoryCookieStore;
import fr.cls.atoll.motu.library.cas.UserBase;

//import sun.net.www.protocol.http.InMemoryCookieStore;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 */

public class CookieStoreHolder {
	   /**
     * ThreadLocal to hold the CookieStore for Threads to access.
     */
    private static final ThreadLocal<CookieStore> threadLocal = new ThreadLocal<CookieStore>();
    
    public static void initCookieManager() {
    	
    	CookieHandler cookieHandler = CookieHandler.getDefault();
    	
    	if (cookieHandler == null) {
    		CookieStore cookieStore = CookieStoreHolder.initInMemoryCookieStore();
            CookieManager cm = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cm);    		
    	} else {
    		CookieStoreHolder.initInMemoryCookieStore();    		
    	}
    	
    }

    public static CookieStore getCookieStore() {
        return threadLocal.get();
    }

    
    public static void setCookieStore(CookieStore cookieStore) {    	
    	threadLocal.set(cookieStore);
    }

    public static CookieStore initInMemoryCookieStore() {
    	CookieStore cookieStore = CookieStoreHolder.getCookieStore();
    	if (cookieStore == null) { 
    		cookieStore = new PublicInMemoryCookieStore(); 
    		CookieStoreHolder.setCookieStore(cookieStore);
    	} else {
    		cookieStore.removeAll();
    	}
    	
    	return cookieStore;
    }

}
