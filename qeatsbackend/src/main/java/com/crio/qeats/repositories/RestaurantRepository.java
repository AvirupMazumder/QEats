/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.utils.GeoLocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {

  // List<RestaurantEntity> findByLocationNear(GeoLocation point, Distance distance);
  @Query("{ 'restaurantId' : ?0 }")
  Optional<RestaurantEntity> findRestaurantByRestaurantId(String restaurantId);

  @Query("{ 'name' : ?0 }")
  Optional<List<RestaurantEntity>> findRestaurantsByNameExact(String name);

}

