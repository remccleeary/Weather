/*
Weather.scala
Ryan McCleeary
1-4-19
 */

//HTTP Response Library (See https://index.scala-lang.org/scalaj/scalaj-http/scalaj-http/2.4.1?target=_2.12 for info)
import scalaj.http._ 

//Config file library (See https://github.com/lightbend/config for info)
import com.typesafe.config._


//JSON handler (See https://github.com/spray/spray-json for info)
import spray.json._
import DefaultJsonProtocol._

//Classes and Protocols for Spray to convert Json to and from Scala Classes
//Class for Json response from DataScienceToolKit (see http://www.datasciencetoolkit.org/developerdocs#geodict for API info)
case class DSTKInfo(val results:List[JsValue], val status:String)
object DSTKInfoJsonProtocol extends DefaultJsonProtocol {
  implicit val dstkInfoFormat = jsonFormat2(DSTKInfo)
}

//grabs sub AST geometry from a result in DSTKInfo
case class DSTKGeometry(val geometry:JsValue)
object DSTKGeometryJsonProtocol extends DefaultJsonProtocol {
  implicit val dstkGeometryFormat = jsonFormat1(DSTKGeometry)
}

//grabs location from geometry in DSTKGeometry
case class DSTKLocation(val location:Map[String,Double])
object DSTKLocationJsonProtocal extends DefaultJsonProtocol {
  implicit val dstkLocationFormat = jsonFormat1(DSTKLocation)
}

//Class for Json Response for OpenWeatherMap (see https://openweathermap.org/current for API info)
case class CityWeather(val main:Option[Map[String,Double]], val name:Option[String])
object CityWeatherJsonProtocol extends DefaultJsonProtocol {
  implicit val cityFormat = jsonFormat2(CityWeather)
}

//importing JSON protocols
import CityWeatherJsonProtocol._
import DSTKInfoJsonProtocol._
import DSTKGeometryJsonProtocol._
import DSTKLocationJsonProtocal._



//Main Object
object Weather {


  //parseLocToZip takes in input from the user and tries to determine a latitude longitude from their input using a DataScienceToolKit query
  //Pre-Conditions:An internet connection, datasciencetoolkit.org is up, a valid string
  //Post-Conditions: Returns the latitude and longitude found from datasciencetoolkit.org if found, if a result is not found it asks for another input
  def parseLocToLatLng(loc:String):(Option[Double],Option[Double]) = 
  {
    //DataScienceToolKit API Call (see http://www.datasciencetoolkit.org/developerdocs#geodict) to get JSON response
    val dstkResponse:HttpResponse[String] =
      Http("http://www.datasciencetoolkit.org/maps/api/geocode/json?").param("address",loc).asString

    //Converting to response to Json Abstract Syntax Tree
    val dstkJsonAst:JsValue = dstkResponse.body.parseJson

    //Getting the results and status from the response from the AST
    val dstkInfo:DSTKInfo = dstkJsonAst.convertTo[DSTKInfo]

  
    if(dstkInfo.status == "OK") //status is from DataScienceToolKit that tells whether the query worked
    {
      //Getting the first result from the results
      val dstkInfoResults:JsValue = dstkInfo.results(0)

      //getting geometry from first result
      val dstkGeometry:JsValue = dstkInfoResults.convertTo[DSTKGeometry].geometry

      //getting location from geometry
      val location:Map[String,Double] = dstkGeometry.convertTo[DSTKLocation].location

      //return lat and lng
      (location.get("lat"),location.get("lng"))
    }
    else //If there was no result as the user to try again with a new input and recurse.
    {
      println("Unable to find your location, please try again.")
      val l = scala.io.StdIn.readLine()
      parseLocToLatLng(l)
    }
  }


  //getCity returns the CityWeather information of a location given by latitude and longitude
  //Pre-Conditions: An internet connection, openweathermap.org is up, a valid openweathermap api key
  //Post-Conditions: Returns the CityWeather info of given location 
  def getCity(lat:String,lng:String,owmKey:String,unit:String):CityWeather =
  {    
    //Get JSON String response from OpenWeatherMap
    //Uses loc given by user, imperial units, and our openWeatherMap API Key
    val owmResponse:HttpResponse[String] =
      Http("https://api.openweathermap.org/data/2.5/weather").param("lat",lat).param("lon",lng).param("units",unit).param("APPID",owmKey).asString

    //Parse response into json Abstract Syntax Tree
    val owmJsonAst = owmResponse.body.parseJson

    owmJsonAst.convertTo[CityWeather]
  }


  //Main method executes the program by prompting user for input, gets a latitude and longitude from datasciencetoolkit using the input given
  //Sends the latitude and longitude to openweathermap to get the current temp and outputs that information
  def main(args: Array[String]): Unit =
  {
    try
    {
      val conf = ConfigFactory.load()
      val owmKey = conf.getString("api-keys.openWeatherAppAPI")
      val unit = conf.getString("settings.unit")

      //Prompt for city location
      print("Where are you? ")
      val loc = scala.io.StdIn.readLine()
      //Get the Lat and Lng using DataScineceToolKit
      val (oLat,oLng) = parseLocToLatLng(loc)
      val lat =
        oLat match
        {
          case Some(l) => l.toString
          case None => throw new IllegalStateException("No lat")
        }
      val lng =
        oLng match
        {
          case Some(l) => l.toString
          case None => throw new IllegalStateException("No lng")
        }

      //Get the City
      val cityInfo = getCity(lat,lng,owmKey,unit)

      //Get the name
      val name =
        cityInfo.name match
        {
          case Some(n) => n
          case None => throw new IllegalStateException("No name found")
        }

      //and the tempurature in rounded fahrenheit
      val cityInfoMain =
        cityInfo.main match
        {
          case Some(m) => m
          case None => throw new IllegalStateException("No city weather info found")
        }
      val tempF =
        cityInfoMain.get("temp") match
        {
          case Some(temp) => Math.round(temp)
          case None => throw new IllegalStateException("No temp found")
        }

      println("Current tempurature for " + name + " at latitude="+lat+" longitude=" + lng + ":")
      print(tempF + " degrees ")
      //From settings give correct unit
      unit match
      {
        case "imperial" => println("Fahrenheit")
        case "metric" => println("Celsius")
        case _ => println("Kelvin")
      }
    }
    catch
    {
      //Catch known exceptions and give info to user
      case internetIssue:java.net.ConnectException =>
        println("Unable to connect to the internet, please check your internet connection")

      case serviceDown:java.net.UnknownHostException=>
        println("Unable to connect to one of our services please check your internet connection and try again")

      case jsonError:spray.json.DeserializationException =>
        println("Your query did not produce results from our system. Please check your application.conf file and try again later.")

      case noneError:IllegalStateException => println("Your query did not produce results from our system. Please check your application.conf file and try again later")

      case jsonEmptyError:spray.json.JsonParser$ParsingException => println("Empty result from one of our providers, please try again soon.")

      case configError:com.typesafe.config.ConfigException$Missing => println("Error in your config file")
    }
  }
}
