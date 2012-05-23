GSMProfiler
By: Luis Delgado Romera and Albert Yera Gómez

When using a mobile device, we would like to have different profiles depending on our location. It is common to forget to mute the phone at the library, or forget the WI-FI connected when leaving home. 

There exist some location-aware applications that make use of GPS as it’s the easiest way to get a location. However, the battery life when using it decreases considerably, which makes GPS not suitable for background applications.

It is also possible to get location information from the GSM cells. All the GSM cells have an unique ID and a location area ID. This information is usually used with a Google API (Geolocation API) to obtain the geographical location through the Internet. But this system requires a network connection, which also increases battery consumption and requires an additional cost for the user. 

GSMProfiler is an Android application that handles different configurations of our device depending on the user location. This location is obtained without use of GPS or network connection. Our idea is to build a system that learns which cells are visible from a list of user-defined locations, and run different configurations once it detects that the user has changed his/her location.
