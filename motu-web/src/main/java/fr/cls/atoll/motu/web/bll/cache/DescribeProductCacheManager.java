package fr.cls.atoll.motu.web.bll.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DescribeProductCacheManager implements IDescribeProductCacheManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private DescribeProductCacheThread describeProductCacheDaemonThread;

    @Override
    public void init() {
        describeProductCacheDaemonThread = new DescribeProductCacheThread() {

            /** {@inheritDoc} */
            @Override
            public void onThreadStopped() {
                super.onThreadStopped();
                synchronized (DescribeProductCacheManager.this) {
                    DescribeProductCacheManager.this.notify();
                }
            }

        };
        describeProductCacheDaemonThread.start();
    }

    @Override
    public void stop() {
        describeProductCacheDaemonThread.setDaemonStoppingASAP(true);
        synchronized (this) {
            if (!describeProductCacheDaemonThread.isDaemonStopped()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("Error during wait while stopping daemon: " + describeProductCacheDaemonThread.getName());
                }
            }
        }
    }

    @Override
    public ProductMetadataInfo getDescribeProduct(String productId) {
        return describeProductCacheDaemonThread.getProductDescription(productId);
    }

}
