# Documentation
This file is for the explanation of choices made while coding this project as well as brief information about dependent libraries and APIs.
 
## [OpenWeatherMap API](https://openweathermap.org/current)
The  OpenWeatherMap has multiple ways to search for weather at a current location. My first attempt at this problem was to directly query using their search. The issue that quickly arrised from this is that OpenWeatherMap does only stores City Name and [ISO-3116 Country Code](https://www.iso.org/iso-3166-country-codes.html). This caused issues when trying to differentiate from two cities with the same name in the US but in different states e.g. Des Moines, IA and Des Moines, WA. This lead me to search for a way to get a better way to query their API. When I found that you are able to query via zip code I created a zip code look up and tried that out. After a few tests I quickly realized that OpenWeatherMap often returned the wrong information based off of zip code queries. I then remembered a quite great resource that I had found a while back called DataScienceToolKit.

### [DataScienceToolKit API](http://www.datasciencetoolkit.org)
 DataScienceToolKit is an API that allows for a wealth of data to be quickly asked for via their API. They also allow you to clone the server fully if you would prefer to have your own personal copy running. They provide a geocode API (no API key required) that allows for search engine like queries and return back a wealth of information, one of which included longitude and latitude, which OpenWeatherMap also supports. I then decided to instead get the longitude and latitude from here. The program now simply gets the longitude and latitude from DataScienceToolKit and gives that to OpenWeatherMap to get a more accurate result from a search engine like query.
 
## [scalaj-http](https://index.scala-lang.org/scalaj/scalaj-http/scalaj-http/2.4.1?target=_2.12)
This scala library scalaj-http has always been my go to for an http client in scala, it works well and has easy to handle exceptions. 

## [spray-json](https://github.com/spray/spray-json)
While I had originally tried out the old scala.util.parsing.json in the standard library it is now depricated, so I looked for a third party Json parser as the results from the API calls from DataScienceToolKit and OpenWeatherMap returned Json data (OpenWeatherMap did also support XML but DataScienceToolKit did not). I found a Scala library called spray-json, which seemed to truly embrace Scala's typing system and decided try it out. It was quite intuitive to quickly convert Json AST into Scala classes and types. 

