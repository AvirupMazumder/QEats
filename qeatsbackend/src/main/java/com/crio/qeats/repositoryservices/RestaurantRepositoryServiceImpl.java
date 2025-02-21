/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
@Log4j2
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
  
    // TODO: CRIO_TASK_MODULE_REDIS
    // We want to use cache to speed things up. Write methods that perform the same functionality,
    // but using the cache if it is present and reachable.
    // Remember, you must ensure that if cache is not present, the queries are directed at the
    // database instead.

    List<Restaurant> restaurants = new ArrayList<>();
    if (redisConfiguration.isCacheAvailable()) {
      restaurants = findAllRestaurantsCloseByFromCache(latitude, 
        longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants = findAllRestaurantsCloseByFromDb(latitude, 
        longitude, currentTime, servingRadiusInKms);
    }
    
    log.info("Resturning list of restaurants of size: {} ",restaurants.size());
    return restaurants;
 
  }

  public void putRestaurantsListInCache(List<Restaurant> restaurants, Double latitude, 
      Double longitude, Double servingRadiusInKms) {

        String geoHash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
        String redisKey = geoHash;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
          Jedis jedis = redisConfiguration.getJedisPool().getResource();
          String jsonData = objectMapper.writeValueAsString(restaurants);
          jedis.setex(redisKey, RedisConfiguration.REDIS_ENTRY_EXPIRY_IN_SECONDS , jsonData);
          log.info("Cache miss. Caching list of restaurants of size: {}", restaurants.size());
        } catch (JsonProcessingException e) {
          log.error("Error converting list of restaurants to JSON: ", e);
        } catch (Exception e) {
          log.error("Error writing to Redis: ", e);
        }

  }

  public List<Restaurant> findAllRestaurantsCloseByFromDb(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    ModelMapper modelMapper =  modelMapperProvider.get();
    
    List<RestaurantEntity> restaurantEntities  =  restaurantRepository.findAll();
    for (RestaurantEntity  restaurantEntity :  restaurantEntities) {
      if (isOpenNow(currentTime, restaurantEntity)) {
       
        if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, 
            latitude, longitude, servingRadiusInKms)) {

          restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
        }
      }
            
    }
    putRestaurantsListInCache(restaurants,latitude,longitude,servingRadiusInKms);
    return restaurants;

  }


  public List<Restaurant> findAllRestaurantsCloseByFromCache(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    String geoHash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
    String redisKey = geoHash; //+ "|" + servingRadiusInKms;
    List<Restaurant> restaurants = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      Jedis jedis = redisConfiguration.getJedisPool().getResource();
      String cachedData = jedis.get(redisKey);
      if (cachedData != null) {
        restaurants = objectMapper.readValue(cachedData, new TypeReference<List<Restaurant>>() {});
        log.info("Cache hit. Returning list of restaurants of size: {}", restaurants.size());
      } else {
        restaurants = findAllRestaurantsCloseByFromDb(latitude, longitude, currentTime, servingRadiusInKms);
      }
    } catch (IOException e) {
      log.info("Error processing JSON from cache: ", e);
    } catch (Exception e) {
      log.info("Error accessing Redis: ", e);
    }
    return restaurants;
  
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    ModelMapper modelMapper =  modelMapperProvider.get();
        
    List<RestaurantEntity> restaurantEntities  =  restaurantRepository.findAll();
    for (RestaurantEntity  restaurantEntity :  restaurantEntities) {
      if (isOpenNow(currentTime, restaurantEntity)) {
         
        if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, 
            latitude, longitude, servingRadiusInKms) && restaurantEntity.getName().contains(searchString)) {
  
          restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
          }
        }
              
    }

     return restaurants;
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    ModelMapper modelMapper =  modelMapperProvider.get();
            
    List<RestaurantEntity> restaurantEntities  =  restaurantRepository.findAll();
    for (RestaurantEntity  restaurantEntity :  restaurantEntities) {
      if (isOpenNow(currentTime, restaurantEntity)) {
         
        if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, 
            latitude, longitude, servingRadiusInKms)) {
          List<String> attributes = restaurantEntity.getAttributes();
          for(String attribute : attributes) {
            if(attribute.contains(searchString)) {
              restaurants.add(modelMapper.map(restaurantEntity,Restaurant.class));
              break;
            }
          }

        }
      }
        
    }
     return restaurants;
  }




  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        List<Restaurant> restaurants = new ArrayList<>();
        ModelMapper modelMapper =  modelMapperProvider.get();
        List<String> itemIdList = new ArrayList<String>();
            
        List<ItemEntity> itemEntities  =  itemRepository.findAll();
        for (ItemEntity  itemEntity :  itemEntities) {
          if(itemEntity.getName().contains(searchString)) {
            //items.add(modelMapper.map(itemEntity, Item.class));
            itemIdList.add(itemEntity.getItemId());   
          }       
        }
        List<MenuEntity> menuEntities= menuRepository.findMenusByItemsItemIdIn(itemIdList).get();
        for(MenuEntity menuEntity : menuEntities) {
          String restaurantId = menuEntity.getRestaurantId();
          RestaurantEntity restaurantEntity = restaurantRepository.findRestaurantByRestaurantId(restaurantId).get();
          if (isOpenNow(currentTime, restaurantEntity)) {
         
            if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, 
                latitude, longitude, servingRadiusInKms)) {
      
              restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
            }
          }
        }

        
         return restaurants;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        List<Restaurant> restaurants = new ArrayList<>();
        ModelMapper modelMapper =  modelMapperProvider.get();
        List<String> itemIdList = new ArrayList<String>();
            
        List<ItemEntity> itemEntities  =  itemRepository.findAll();
        for (ItemEntity  itemEntity :  itemEntities) {
          List<String> attributes = itemEntity.getAttributes();
          for(String attribute : attributes) {
            if(attribute.contains(searchString)) {
              itemIdList.add(itemEntity.getItemId());
            }
          }   
        }
        List<MenuEntity> menuEntities= menuRepository.findMenusByItemsItemIdIn(itemIdList).get();
        for(MenuEntity menuEntity : menuEntities) {
          String restaurantId = menuEntity.getRestaurantId();
          RestaurantEntity restaurantEntity = restaurantRepository.findRestaurantByRestaurantId(restaurantId).get();
          if (isOpenNow(currentTime, restaurantEntity)) {
         
            if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, 
                latitude, longitude, servingRadiusInKms)) {
      
              restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
            }
          }
        }

        
         return restaurants;
  }





  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      Double val = GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude());
      return val < servingRadiusInKms;
    }

    return false;
  }



}

