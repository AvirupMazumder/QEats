
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.net.SocketException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  
  private final Double peakHoursServingRadiusInKms = 3.0;

  
  private final Double normalHoursServingRadiusInKms = 5.0;


  private RestaurantRepositoryService restaurantRepositoryService;

  @Autowired
  public RestaurantServiceImpl(RestaurantRepositoryService restaurantRepositoryService) {
    this.restaurantRepositoryService = restaurantRepositoryService;
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    log.info("Fetching all Restraurants near latitude " + getRestaurantsRequest.getLatitude() + " and longitude " + getRestaurantsRequest.getLongitude());

    GetRestaurantsResponse getRestaurantsResponse = null;
    if ((currentTime.isAfter(LocalTime.of(7, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(10, 00, 01)))
        || (currentTime.isAfter(LocalTime.of(12, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(14, 00, 01)))
        || (currentTime.isAfter(LocalTime.of(18, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(21, 00, 01)))) {

      long startTimeInMillis = System.currentTimeMillis();
      getRestaurantsResponse = 
      new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        currentTime, peakHoursServingRadiusInKms));

      long endTimeInMillis = System.currentTimeMillis();

      log.info("Your function took :" + (endTimeInMillis - startTimeInMillis));
    } else {
      getRestaurantsResponse = 
      new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
         getRestaurantsRequest.getLatitude(),getRestaurantsRequest.getLongitude(), 
         currentTime, normalHoursServingRadiusInKms));
      }
      return getRestaurantsResponse;
    }

    //getRestaurantsResponse.removeNonASCIICharacters();
    

  






  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    log.info("Fetching restaurant details searched for: " + getRestaurantsRequest.getSearchFor());
    GetRestaurantsResponse getRestaurantsResponse = null;
    List<Restaurant> restaurants = new ArrayList<>();
    if ((currentTime.isAfter(LocalTime.of(7, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(10, 00, 01)))
        || (currentTime.isAfter(LocalTime.of(12, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(14, 00, 01)))
        || (currentTime.isAfter(LocalTime.of(18, 59, 59)) 
        && currentTime.isBefore(LocalTime.of(21, 00, 01)))) {

      restaurants.addAll(restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemAttributes(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
      getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    } else {
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemAttributes(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
      restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
      getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    }
     return getRestaurantsResponse;
  }

}

