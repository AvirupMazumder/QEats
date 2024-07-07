/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import com.crio.qeats.dto.Restaurant;

import java.util.List;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Implement GetRestaurantsResponse.
// Complete the class such that it produces the following JSON during serialization.
// {
//  "restaurants": [
//    {
//      "restaurantId": "10",
//      "name": "A2B",
//      "city": "Hsr Layout",
//      "imageUrl": "www.google.com",
//      "latitude": 20.027,
//      "longitude": 30.0,
//      "opensAt": "18:00",
//      "closesAt": "23:00",
//      "attributes": [
//        "Tamil",
//        "South Indian"
//      ]
//    },
//    {
//      "restaurantId": "11",
//      "name": "Shanti Sagar",
//      "city": "Btm Layout",
//      "imageUrl": "www.google.com",
//      "latitude": 20.0269,
//      "longitude": 30.00,
//      "opensAt": "18:00",
//      "closesAt": "23:00",
//      "attributes": [
//        "Udupi",
//        "South Indian"
//      ]
//    }
//  ]

@Data
@Component
public class GetRestaurantsResponse {
    
  private List<Restaurant> restaurants;

  @Autowired
  public GetRestaurantsResponse(List<Restaurant> restraurants) {
    this.restaurants = restraurants;
  }

  // public void removeNonASCIICharacters() {
  //   if (restaurants != null) {
  //       for (Restaurant restaurant : restaurants) {
  //           restaurant.removeNonASCIICharacters();
  //       }
  //   }
  // }
}

// }
