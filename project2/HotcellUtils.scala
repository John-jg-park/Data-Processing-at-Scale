package cse512

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession, functions}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

import scala.math.sqrt

object HotcellUtils {
  val coordinateStep = 0.01

  def CalculateCoordinate(inputString: String, coordinateOffset: Int): Int =
  {
    // Configuration variable:
    // Coordinate step is the size of each cell on x and y
    var result = 0
    coordinateOffset match
    {
      case 0 => result = Math.floor((inputString.split(",")(0).replace("(","").toDouble/coordinateStep)).toInt
      case 1 => result = Math.floor(inputString.split(",")(1).replace(")","").toDouble/coordinateStep).toInt
      // We only consider the data from 2009 to 2012 inclusively, 4 years in total. Week 0 Day 0 is 2009-01-01
      case 2 => {
        val timestamp = HotcellUtils.timestampParser(inputString)
        result = HotcellUtils.dayOfMonth(timestamp) // Assume every month has 31 days
      }
    }
    return result
  }

  def timestampParser (timestampString: String): Timestamp =
  {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val parsedDate = dateFormat.parse(timestampString)
    val timeStamp = new Timestamp(parsedDate.getTime)
    return timeStamp
  }

  def dayOfYear (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_YEAR)
  }

  def dayOfMonth (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_MONTH)
  }


    // YOU NEED TO CHANGE THIS PART
  def CalculateW(x : Int, y : Int, z : Int): Int =
  {
    // Configuration variable:
    // Coordinate step is the size of each cell on x and y
    var result = 27
    if (x == -7450 || x == -7370 || y == 4050 || y == 4090 || z == 1 || z == 31) {
      result = 18
    }
    if ( ((x == -7450 || x == -7370) && (y == 4050 || y == 4090)) || ((y == 4050 || y == 4090) &&
      (z == 1 || z == 31)) || ((x == -7450 || x == -7370) && (z == 1 || z == 31)) ) {
      result = 12
    }
    if ((x == -7450 || x == -7370) && (y == 4050 || y == 4090) && (z == 1 || z == 31)) {
      result = 8
    }
    return result
  }

}
