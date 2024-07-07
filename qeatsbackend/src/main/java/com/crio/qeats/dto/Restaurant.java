
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Component;


// TODO: CRIO_TASK_MODULE_SERIALIZATION
//  Implement Restaurant class.
// Complete the class such that it produces the following JSON during serialization.
// {
//  "restaurantId": "10",
//  "name": "A2B",
//  "city": "Hsr Layout",
//  "imageUrl": "www.google.com",
//  "latitude": 20.027,
//  "longitude": 30.0,
//  "opensAt": "18:00",
//  "closesAt": "23:00",
//  "attributes": [
//    "Tamil",
//    "South Indian"
//  ]
// }
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Restaurant {
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;
  private String restaurantId;
  private String name;
  private String city;
  private String imageUrl;
  private double latitude;
  private double longitude;
  private String opensAt;
  private String closesAt;
  private String[] attributes;

  // public void removeNonASCIICharacters() {
  //   //this.id = removeNonASCII(this.id);
  //   //this.restaurantId = removeNonASCII(this.restaurantId);
  //   this.name = removeNonASCII(this.name);
  //   //this.city = removeNonASCII(this.city);
  //   //this.imageUrl = removeNonASCII(this.imageUrl);
  //   //this.opensAt = removeNonASCII(this.opensAt);
  //   //this.closesAt = removeNonASCII(this.closesAt);
  //   if (attributes != null) {
  //       this.attributes = removeNonASCII(this.attributes);
  //   }
  // }

  // private String removeNonASCII(String str) {
  //   if (str == null) {
  //       return null;
  //   }
  //   // Remove non-ASCII characters and replace with whitespace
  //   return str.replaceAll("[^\\x00-\\x7F]", "?");
  // }

  // private String[] removeNonASCII(String[] arr) {
  //   // Remove non-ASCII characters from each element in the array
  //   return List.of(arr)
  //           .stream()
  //           .map(this::removeNonASCII)
  //           .toArray(String[]::new);
  // }
    
}

