package com.mycompany.catalog.entity;

import org.broadleafcommerce.core.catalog.domain.Product;

public interface GeoProduct extends Product {
	public Double getLat();

    public Double getLon();
    
    public void setLon(Double lon);
    
    public void setLat(Double lat);
}
