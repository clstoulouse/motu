package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
public class ProductCacheManager implements IProductCacheManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private ProductCacheThread productCacheDaemonThread;

    private List<Product> productList;

    @Override
    public void init() {
        productList = Collections.synchronizedList(new ArrayList<Product>());

        productCacheDaemonThread = new ProductCacheThread(this) {

            /** {@inheritDoc} */
            @Override
            public void onThreadStopped() {
                super.onThreadStopped();
                synchronized (ProductCacheManager.this) {
                    ProductCacheManager.this.notify();
                }
            }

        };
        productCacheDaemonThread.start();
    }

    @Override
    public void stop() {
        productCacheDaemonThread.setDaemonStoppingASAP(true);
        synchronized (this) {
            if (!productCacheDaemonThread.isDaemonStopped()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("Error during wait while stopping daemon: " + productCacheDaemonThread.getName());
                }
            }
        }
    }

    @Override
    public Product getProduct(String productId) {
        Product p = null;
        Iterator<Product> iP = productList.iterator();
        while (p == null && iP.hasNext()) {
            Product curP = iP.next();
            if (curP != null && curP.getProductId() != null && curP.getProductId().equalsIgnoreCase(productId)) {
                p = curP;
            }
        }
        return p;
    }

    /**
     * .
     * 
     * @param currentProduct
     */
    public void setProduct(Product product) {
        Product pInList = getProduct(product.getProductId());
        if (pInList != null) {
            productList.remove(pInList);
        }
        productList.add(product);
    }

}
