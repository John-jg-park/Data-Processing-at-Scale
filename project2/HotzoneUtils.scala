package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = {
    val rectangle = queryRectangle.split(",")
    val point = pointString.split(",")
    if (rectangle(0).toDouble <= point(0).toDouble && rectangle(2).toDouble >= point(0).toDouble
      && rectangle(1).toDouble <= point(1).toDouble && rectangle(3).toDouble >= point(1).toDouble)
    {
      return true
    }
    else {
      return false
    }
  }

}
