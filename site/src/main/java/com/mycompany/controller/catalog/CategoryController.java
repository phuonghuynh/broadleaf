/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycompany.controller.catalog;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.extension.ExtensionResultHolder;
import org.broadleafcommerce.common.template.TemplateOverrideExtensionManager;
import org.broadleafcommerce.common.template.TemplateType;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.common.web.TemplateTypeAware;
import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.broadleafcommerce.common.web.deeplink.DeepLinkService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.search.domain.ProductSearchCriteria;
import org.broadleafcommerce.core.search.domain.ProductSearchResult;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.service.SearchService;
import org.broadleafcommerce.core.web.catalog.CategoryHandlerMapping;
import org.broadleafcommerce.core.web.controller.catalog.BroadleafCategoryController;
import org.broadleafcommerce.core.web.service.SearchFacetDTOService;
import org.broadleafcommerce.core.web.util.ProcessorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;

import com.mycompany.catalog.entity.GeoProduct;
import com.mycompany.utility.GeoCoder;

/**
 * This class works in combination with the CategoryHandlerMapping which finds a category based upon
 * the passed in URL.
 */
@Controller("blCategoryController")
public class CategoryController extends BroadleafCategoryController {
	
	 protected static String defaultCategoryView = "catalog/category";
	    protected static String CATEGORY_ATTRIBUTE_NAME = "category";  
	    protected static String PRODUCTS_ATTRIBUTE_NAME = "products";  
	    protected static String FACETS_ATTRIBUTE_NAME = "facets";  
	    protected static String PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME = "result";  
	    protected static String ACTIVE_FACETS_ATTRIBUTE_NAME = "activeFacets";  
	    protected static String ALL_PRODUCTS_ATTRIBUTE_NAME = "blcAllDisplayedProducts";
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	System.out.println("cat controller ************************************");
    	//System.out.println("form ************************************: " + form.getSport());
    	
    	ModelAndView model = new ModelAndView();
    	String postcode = request.getParameter("postcode");
    	if (postcode == null) {
    		postcode = "hp43px";
    	}
    	System.out.println("postcode ************************************: " + postcode);
        
        if (request.getParameterMap().containsKey("facetField")) {
            // If we receive a facetField parameter, we need to convert the field to the 
            // product search criteria expected format. This is used in multi-facet selection. We 
            // will send a redirect to the appropriate URL to maintain canonical URLs
            
            String fieldName = request.getParameter("facetField");
            List<String> activeFieldFilters = new ArrayList<String>();
            Map<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
            
            for (Iterator<Entry<String,String[]>> iter = parameters.entrySet().iterator(); iter.hasNext();){
                Map.Entry<String, String[]> entry = iter.next();
                String key = entry.getKey();
                if (key.startsWith(fieldName + "-")) {
                    activeFieldFilters.add(key.substring(key.indexOf('-') + 1));
                    iter.remove();
                }
            }
            
            parameters.remove(ProductSearchCriteria.PAGE_NUMBER);
            parameters.put(fieldName, activeFieldFilters.toArray(new String[activeFieldFilters.size()]));
            parameters.remove("facetField");
            
            String newUrl = ProcessorUtils.getUrl(request.getRequestURL().toString(), parameters);
            model.setViewName("redirect:" + newUrl);
        } else {
            // Else, if we received a GET to the category URL (either the user clicked this link or we redirected
            // from the POST method, we can actually process the results
            
            Category category = (Category) request.getAttribute(CategoryHandlerMapping.CURRENT_CATEGORY_ATTRIBUTE_NAME);
            System.out.println(category.getName());
            assert(category != null);
            
            List<SearchFacetDTO> availableFacets = getSearchService().getCategoryFacets(category);
            ProductSearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
            
            String searchTerm = request.getParameter(ProductSearchCriteria.QUERY_STRING);
            ProductSearchResult result;
            if (StringUtils.isNotBlank(searchTerm)) {
                result = getSearchService().findProductsByCategoryAndQuery(category, searchTerm, searchCriteria);
            } else {
                result = getSearchService().findProductsByCategory(category, searchCriteria);
            }
            
            System.out.println("&&&&&&&&&&&&&&: " + result.getTotalResults() );
            
            facetService.setActiveFacetResults(result.getFacets(), request);
            
            model.addObject(CATEGORY_ATTRIBUTE_NAME, category);
            //model.addObject(PRODUCTS_ATTRIBUTE_NAME, result.getProducts());
            model.addObject(FACETS_ATTRIBUTE_NAME, result.getFacets());
            model.addObject(PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME, result);
            if (result.getProducts() != null) {
                model.addObject(ALL_PRODUCTS_ATTRIBUTE_NAME, new HashSet<Product>(result.getProducts()));
            }
            
            addDeepLink(model, deepLinkService, category);

            ExtensionResultHolder<String> erh = new ExtensionResultHolder<String>();
            templateOverrideManager.getProxy().getOverrideTemplate(erh, category);

            if (StringUtils.isNotBlank(erh.getResult())) {
                model.setViewName(erh.getResult());
            } else if (StringUtils.isNotEmpty(category.getDisplayTemplate())) {
                model.setViewName(category.getDisplayTemplate());   
            } else {
                model.setViewName(getDefaultCategoryView());
            }
            
            HashMap<Double, Product> productMap = new HashMap<Double, Product>();
            
            //GeoCoder geo = new GeoCoder ();
            LatLng latlong = GeoCoder.getGeometry(postcode);  
            
            if (result.getProducts() == null) {
            	model.addObject(PRODUCTS_ATTRIBUTE_NAME, null);
            	return model;
            }
            
            for (Product product : result.getProducts()) {
            	GeoProduct geoProduct = (GeoProduct) product;
            	double distance = GeoCoder.haversine(geoProduct.getLat(), geoProduct.getLon(), latlong.lat, latlong.lng);
            	productMap.put(new Double(distance), (Product)geoProduct);
            	//System.out.println(distance);
            }
            
            //sort results by distance
            Map<Double, Product> treeMap = new TreeMap<Double, Product>(
        			new Comparator<Double>() {
         
        			@Override
        			public int compare(Double o1, Double o2) {
        				return o1.compareTo(o2);
        			}
         
        		});
            
            	//Map treeMap = new TreeMap(Collections.reverseOrder());
            //SortedMap treeMap = new TreeMap(Collections.reverseOrder());
        		treeMap.putAll(productMap);
            
            model.addObject(PRODUCTS_ATTRIBUTE_NAME, treeMap);
        }
       
        return model; 
    }
    
   
}
