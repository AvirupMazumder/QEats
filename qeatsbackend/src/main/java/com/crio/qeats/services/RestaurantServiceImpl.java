
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.configs.AsyncConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import com.crio.qeats.repositoryservices.RestaurantRepositoryServiceImpl;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl extends Thread implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;

  @Autowired
  private AsyncConfiguration asyncConfiguration;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    if (currentTime.isAfter(LocalTime.of(7, 59)) && currentTime.isBefore(LocalTime.of(10, 1))
        || currentTime.isAfter(LocalTime.of(12, 59)) && currentTime.isBefore(LocalTime.of(14, 1))
        || currentTime.isAfter(LocalTime.of(18, 59)) && currentTime.isBefore(LocalTime.of(21, 1))) {
      return new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
          getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), currentTime,
          peakHoursServingRadiusInKms));
    } else {
      return new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
          getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), currentTime,
          normalHoursServingRadiusInKms));
    }

    // return null;
  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  // @Override
  // public GetRestaurantsResponse findRestaurantsBySearchQuery(GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

  //       Double servingRadiusInKms = isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;
  //       String searchFor = getRestaurantsRequest.getSearchFor(); 
  //       List<List<Restaurant>> listOfRestaurantLists = new ArrayList<>();
  //       List<Restaurant> restaurantList=new ArrayList<>();
  //      if(!getRestaurantsRequest.getSearchFor().equals("")){

        
  //       listOfRestaurantLists.add(restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(),
  //       getRestaurantsRequest.getLongitude(), searchFor, currentTime, servingRadiusInKms));
  //       listOfRestaurantLists.add(restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(),
  //       getRestaurantsRequest.getLongitude(), searchFor,
  //       currentTime, servingRadiusInKms));
  //       listOfRestaurantLists.add(restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(),
  //       getRestaurantsRequest.getLongitude(), searchFor,
  //       currentTime, servingRadiusInKms));
  //       listOfRestaurantLists.add(restaurantRepositoryService.findRestaurantsByItemAttributes(getRestaurantsRequest.getLatitude(),
  //       getRestaurantsRequest.getLongitude(), searchFor,
  //       currentTime, servingRadiusInKms));
  //       Set<String> restaurantSet = new HashSet<>();
        
  //       for (List<Restaurant> restoList : listOfRestaurantLists) { 
  //         for (Restaurant restaurant : restoList) {
  //           if (!restaurantSet.contains(restaurant.getRestaurantId())) { 
  //             restaurantList.add(restaurant);
  //             restaurantSet.add(restaurant.getRestaurantId());
  //           }
  //         }
  //       }
  //       return new GetRestaurantsResponse(restaurantList);
  //     }else{
  //       return new GetRestaurantsResponse(restaurantList);
  //     } 

  // }

  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    log.info("Fetching restaurant details searched for: " + getRestaurantsRequest.getSearchFor());
    GetRestaurantsResponse getRestaurantsResponse = null;
    List<Restaurant> restaurants = new ArrayList<>();
    List<List<Restaurant>> listOfRestaurants = new ArrayList<>();
    if (!getRestaurantsRequest.getSearchFor().isEmpty()) {
      if ((currentTime.isAfter(LocalTime.of(7, 59, 59)) 
          && currentTime.isBefore(LocalTime.of(10, 00, 01)))
          || (currentTime.isAfter(LocalTime.of(12, 59, 59)) 
          && currentTime.isBefore(LocalTime.of(14, 00, 01)))
          || (currentTime.isAfter(LocalTime.of(18, 59, 59)) 
          && currentTime.isBefore(LocalTime.of(21, 00, 01)))) {

        listOfRestaurants.add(restaurantRepositoryService.findRestaurantsByName
          (getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, peakHoursServingRadiusInKms));
        listOfRestaurants.add(restaurantRepositoryService.findRestaurantsByAttributes
          (getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, peakHoursServingRadiusInKms));
        listOfRestaurants.add(restaurantRepositoryService.findRestaurantsByItemAttributes
          (getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, peakHoursServingRadiusInKms));
        listOfRestaurants.add
          (restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, peakHoursServingRadiusInKms));
        Set<String> restaurantIdList = new HashSet<>();
        for(List<Restaurant> restaurantList : listOfRestaurants) {
          for(Restaurant restaurant : restaurantList) {
            if(!restaurantIdList.contains(restaurant.getRestaurantId())) {
              restaurants.add(restaurant);
            }
          }
        }
        getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
      } else {
        restaurants.addAll
          (restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, normalHoursServingRadiusInKms));
        restaurants.addAll
          (restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(),
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, normalHoursServingRadiusInKms));
        restaurants.addAll
          (restaurantRepositoryService.findRestaurantsByItemAttributes(getRestaurantsRequest.getLatitude(), 
            getRestaurantsRequest.getLongitude(), 
            getRestaurantsRequest.getSearchFor(), 
            currentTime, normalHoursServingRadiusInKms));
        restaurants.addAll
          (restaurantRepositoryService.findRestaurantsByItemName
            (getRestaurantsRequest.getLatitude(), 
              getRestaurantsRequest.getLongitude(), 
              getRestaurantsRequest.getSearchFor(), 
              currentTime, normalHoursServingRadiusInKms));
        Set<String> restaurantIdList = new HashSet<>();
        for(List<Restaurant> restaurantList : listOfRestaurants) {
          for(Restaurant restaurant : restaurantList) {
            if(!restaurantIdList.contains(restaurant.getRestaurantId())) {
              restaurants.add(restaurant);
            }
          }
        }
        getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
      }
    } else {
      getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    }
     return getRestaurantsResponse;
  }
  
  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
     ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) asyncConfiguration.taskExecutor();
     List<Future<List<Restaurant>>> futures = new ArrayList<>();
    List<Restaurant> restaurants = new ArrayList<>();

     futures.add(taskExecutor.submit(() -> restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(), 
        getRestaurantsRequest.getLongitude(), 
        getRestaurantsRequest.getSearchFor(), 
        currentTime, normalHoursServingRadiusInKms)));
     futures.add(taskExecutor.submit(() -> restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(),
        getRestaurantsRequest.getLongitude(), 
        getRestaurantsRequest.getSearchFor(), 
        currentTime, normalHoursServingRadiusInKms)));
     futures.add(taskExecutor.submit(() -> restaurantRepositoryService.findRestaurantsByItemAttributes(getRestaurantsRequest.getLatitude(), 
        getRestaurantsRequest.getLongitude(), 
        getRestaurantsRequest.getSearchFor(), 
        currentTime, normalHoursServingRadiusInKms)));
     futures.add(taskExecutor.submit(() -> restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(), 
       getRestaurantsRequest.getLongitude(), 
       getRestaurantsRequest.getSearchFor(), 
       currentTime, normalHoursServingRadiusInKms)));
       for (Future<List<Restaurant>> future : futures) {
        try {
            List<Restaurant> restaurant = future.get(); // this will block until the result is available
            if (restaurant != null) {
                restaurants.addAll(restaurant);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    return new GetRestaurantsResponse(restaurants);
  }

}

