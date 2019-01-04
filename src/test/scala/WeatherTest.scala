import org.scalatest.FunSuite
import com.typesafe.config._

class WeatherTests extends FunSuite {
    test("Test Long Lat of Samed Named Cities in Diffent States") {
      val (dmIALat,dmIALon) = Weather.parseLocToLatLng("Des Moines IA")
      val (dmWALat,dmWALon) = Weather.parseLocToLatLng("Des Moines WA")
        assert(dmIALat != dmWALat && dmIALon != dmWALon)
    }
  test("Test Other Country")
  {
    val (lonLatO,lonLonO) = Weather.parseLocToLatLng("London UK")
    val lonLat:Double =
      lonLatO match
      {
        case Some(lat) => lat
        case none => {assert(false); 0.0} 
      }
    val lonLon:Double =
      lonLonO match
      {
        case Some(lon) => lon
        case none => {assert(false); 0.0}
      }
    //51.5074, 0.1278° W
    assert (((51.5074 - .01) <= lonLat) && (lonLat <= (51.5074 + .01)))
    assert (((-0.1278 - .01) <= lonLon) <= (lonLon <= (-0.1278 + .01)))
  }
  test("Other Language test")
  {
    val conf = ConfigFactory.load()
    val owmKey = conf.getString("api-keys.openWeatherAppAPI")
    val unit = conf.getString("settings.unit")

    val(mcLatO,mcLonO) = Weather.parseLocToLatLng("Ciudad de México")
    val mcLat =
      mcLatO match
      {
        case Some(lat) => lat.toString
        case none => {assert(false);""}
      }
    val mcLon =
      mcLonO match
      {
        case Some(lon) => lon.toString
        case none => {assert(false);""}
      }

    val mc = Weather.getCity(mcLat,mcLon,owmKey,unit)

    val name =
      mc.name match
      {
        case Some(n) => n
        case None => {assert(false);""}
      }
    assert(name == "Mexico City")
  }

  //Ran low on time for more tests...
}
