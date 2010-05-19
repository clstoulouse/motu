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
package fr.cls.atoll.motu.processor.threadpools;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-23 14:16:09 $
 */
public class WPSProcessManagement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSProcessManagement.class);
    
    /** The priority blocking queue. */
    private PriorityBlockingQueue<Runnable> priorityBlockingQueue = null;

    /** The thread pool executor. */
    private WPSProcessThreadPoolExecutor threadPoolExecutor = null;


    /**
     * Constructor.
     */
    public WPSProcessManagement() {
        
        this.priorityBlockingQueue = new PriorityBlockingQueue<Runnable>();
        this.threadPoolExecutor = new WPSProcessThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, priorityBlockingQueue);


    }

}
