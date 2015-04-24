package com.mycompany.catalog.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;

@Entity
@Table(name = "blc_geoproduct")
@Inheritance(strategy = InheritanceType.JOINED) 
@AdminPresentationClass(friendlyName = "Geography")
public class GeoProductImpl extends ProductImpl implements GeoProduct {

    private static final long serialVersionUID = 1L;
    
    @AdminPresentation(friendlyName = "Latitude",
            group = Presentation.Group.Name.General, groupOrder = Presentation.Group.Order.General, 
            prominent = true, gridOrder = 3, columnWidth = "200px")
    @Column(name = "LAT")
    public Double lat;

    @AdminPresentation(friendlyName = "Longitude",
            group = Presentation.Group.Name.General, groupOrder = Presentation.Group.Order.General, 
            prominent = true, gridOrder = 3, columnWidth = "200px")
    @Column(name = "LON")
    public Double lon;
    
    @Override
	public Double getLat() {
		return lat;
	}
    
    @Override
	public void setLat(Double lat) {
		this.lat = lat;
	}
    
    @Override
	public Double getLon() {
		return lon;
	}
    
    @Override
	public void setLon(Double lon) {
		this.lon = lon;
	}
    
	

}

