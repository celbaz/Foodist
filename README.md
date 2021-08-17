# _Foodist_ - Restaurant Finding App

# Introduction

Foodist is an android application for the "utmost foodie folks". Leveraging the Foursquare API, Foodist will show users
places to eat nearby.

# Getting Started

1. Install Android Studio or IntelliJ IDEA, if you don't already have it.
2. Download the repo.
3. Get a set of API keys from [Foursquare](https://foursquare.com/developers/apps) and
   [Google Maps](https://developers.google.com/maps/documentation/places/android-sdk/get-api-key)
4. Add a local.properties file in your project level directory containing the following:
    ```
    # Foursquare
    FOURSQUARE_CLIENT_ID=YOUR_TOKEN
    FOURSQUARE_CLIENT_SECRET=YOUR_TOKEN
    # Google Maps
    GOOGLE_MAPS_API_KEY=YOUR_TOKEN
    ```
5. Build and Run!

# Decisions

* Given the complexity of this application, I have chosen to call `ApiServices` directly. If this were a production
  application, I would add a layer of abstraction to leave the project with flexibility to switch the underlying request
  library.
* In a production application I would call the search api via a backend service. Currently, due to the project requirements I
  have hardcoded the foursquare restaurant category id but, this will make things painful in the long term if in the future we
  wanted to allow users to filter by category. 