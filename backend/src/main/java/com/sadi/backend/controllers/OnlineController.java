package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.TempOnlineRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OnlineController {
    // To import un imported class, just hover over the class name and press Alt + Enter (or Option + Enter on Mac) and select the import option.
    /**
     * RestController used to handle rest api
     * Slf4j gives us a log object. Which can be used to log messages.
     * log.info("message") will log the message at info level. log.debug("message") will log the message at debug level.
     * log.info("message {}", variable) will log the message at info level with the variable value.
     */


//    @GetMapping("/v1/online")
//    public ResponseEntity<String> online() {
//        /**
//         * This creates a GET endpoint at /v1/online
//         * Here ResponseEntity is just a wrapper this allows us to pass a certain status code along with the response body.
//         * Here we are passing a 200 OK status code along with the response body "Sadi".
//         * You can also do ResponseEntity<Void> to return empty response body with a 200 OK status code. But you will have to return ReponseEntity.ok().build().
//         * For empty bodies the build is a must.
//         * If you have a custom object you can do ResponseEntity<YourCustomClassName>
//         * Typically we define a record in com.sadi.backend.dtos.responses and then use it to pass the  response body.
//         * ReponseEntity also has noContent() which return status 204 but can't have a body.
//         * ResponseEntity also has created() which returns a 201 Created status code. This is typically used when you create a new resource.
//         * But beaware created requires a URI to be passed.
//         * If you have id of an object you just created, check the code below for how to create a URI using ServletUriComponentsBuilder.
//         */
//
//        log.info("Online endpoint hit");
////        Long id = 1L;
////        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
////                .path("/{id}").buildAndExpand(id).toUri()
////        This will pass the URI as /v1/online/1 in the location header of the response.
////        return ResponseEntity.created(uri).build();
//        return ResponseEntity.ok("Sadi");
//    }


//    @GetMapping("/v1/online/{id}")
//    public ResponseEntity<String> online(
//            @PathVariable Long id
//    ) {
//        /**
//         * This creates a GET endpoint at /v1/online/1
//         * The id will be passed as a path variable. Here spring will automatically convert 1 to Long and pass it to id
//         * If the id is not a valid Long, it will throw an exception and return a 400 Bad Request response.
//         * You can also use String or any other type as a path variable.
//         */
//
//        log.info("Online endpoint hit");
//        return ResponseEntity.ok("Sadi");
//    }

//    @GetMapping("/v1/online")
//    public ResponseEntity<String> online(
//            @RequestParam Long id
//    ) {
//        /**
//         * This creates a GET endpoint at /v1/online?id=1
//         * The id will be passed as a query param. Here spring will automatically convert 1 to Long and pass it to id
//         * If the id is not a valid Long, it will throw an exception and return a 400 Bad Request response.
//         * You can also use String or any other type as a path variable.
//         */
//
//        log.info("Online endpoint hit");
//        return ResponseEntity.ok("Sadi");
//    }

//    @GetMapping("/v1/online")
//    public ResponseEntity<String> online(
//            @RequestParam(required = false, defaultValue = "1") Long id
//    ) {
//        /**
//         * You can also make the query parameter optional by using required = false and defaultValue = "1"
//         * defaultValue must be a String, so if you want to use a number, you need to convert it to String.
//         * Spring will automatically convert the String to Long and pass it to id.
//         */
//
//        log.info("Online endpoint hit");
//        return ResponseEntity.ok("Sadi");
//    }
//    @DeleteMapping("/v1/online")
//    public ResponseEntity<String> online(
//    ) {
//        /**
//         * This creates a DELETE endpoint at /v1/online
//         * DeleteMapping can also accept path variables and query parameters just like GetMapping.
//         */
//        log.info("Online endpoint hit");
//        return ResponseEntity.ok("Sadi");
//    }

//    @PostMapping("/v1/online")
//    public ResponseEntity<Void> online(
//            @Valid @RequestBody TempOnlineRequest request
//    ) {
//        /**
//         * This creates a POST endpoint at /v1/online
//         * The request body will be passed as a TempOnlineRequest object.
//         * Spring will automatically convert the JSON request body to TempOnlineRequest object.
//         * If the request body is not a valid JSON or does not match the TempOnlineRequest object, it will throw an exception and return a 400 Bad Request response.
//         * @Valid annotation is used to validate the request body. This is absolutely necessary if you want to enable validation otherwise spring won't validate.
//         * Usually we create a record in com.sadi.backend.dtos.requests and use it to pass the request
//         * path variable and query parameters can also be used in the same way as GetMapping and DeleteMapping.
//         * PutMapping is exactly the same as PostMapping, the only difference is that you need to use @PutMapping instead of @PostMapping
//         */
//
//        log.info("Online endpoint hit");
//        return ResponseEntity.ok().build();
//    }
}
