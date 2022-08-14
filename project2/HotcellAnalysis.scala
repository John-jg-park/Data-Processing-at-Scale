package cse512

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession, functions}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._
import scala.math.sqrt

object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
{
  // Load the original data from a data source
  var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter",";").option("header","false").load(pointPath);
  pickupInfo.createOrReplaceTempView("nyctaxitrips")
  //pickupInfo.show()
  //pickupInfo.select("_c5").show(false)

  // Assign cell coordinates based on pickup points
  spark.udf.register("CalculateX",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 0)
    )))
  spark.udf.register("CalculateY",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 1)
    )))
  spark.udf.register("CalculateZ",(pickupTime: String)=>((
    HotcellUtils.CalculateCoordinate(pickupTime, 2)
    )))
  pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
  var newCoordinateName = Seq("x", "y", "z")
  pickupInfo = pickupInfo.toDF(newCoordinateName:_*)
  //pickupInfo.show()
  pickupInfo.createOrReplaceTempView("pickupInfo")

  // Define the min and max of x, y, z
  val minX = -74.50/HotcellUtils.coordinateStep
  val maxX = -73.70/HotcellUtils.coordinateStep
  val minY = 40.50/HotcellUtils.coordinateStep
  val maxY = 40.90/HotcellUtils.coordinateStep
  val minZ = 1
  val maxZ = 31
  val numCells = (maxX - minX + 1)*(maxY - minY + 1)*(maxZ - minZ + 1)
  //println("numCells = ", numCells)
  // YOU NEED TO CHANGE THIS PART
  val countedDf = spark.sql("select x,y,z,count(*) as c from pickupInfo group by x,y,z")
  //countedDf.show()
  countedDf.createOrReplaceTempView("countedDf")

  //mean std
  val sum = spark.sql("select sum(c) from countedDf").first()(0).asInstanceOf[Long]
  val mean = sum / numCells
  println("mean= " + mean)
  val sum2 = spark.sql("select sum(c*c) from countedDf").first()(0).asInstanceOf[Long]
  val std = sqrt((sum2/numCells)-(mean*mean))
  println("std = " + std)


  //val testDf = spark.sql("select x,y,z from countedDf where x=7450 or x=7370 or y=4050 or y=4090 or z=1 or z=31 order by x, y, z")
  //testDf.show()


  spark.udf.register("CalculateW",(x: Int, y: Int, z: Int)=>((
    HotcellUtils.CalculateW(x, y, z)
    )))
  val sumDf= spark.sql("select df1.x, df1.y, df1.z, CalculateW(df1.x, df1.y, df1.z) as w, sum(df2.c) as sum from countedDf as df1, countedDf as df2 " +
    "where df1.x<=df2.x + 1 and df1.x >= df2.x - 1 and df1.y<=df2.y + 1 and df1.y >= df2.y - 1 and df1.z<=df2.z + 1 and df1.z >= df2.z - 1 group by df1.x, df1.y, df1.z")
  //sumDf.show()
  sumDf.createOrReplaceTempView("sumDf")

  //val test2Df = spark.sql("select x,y,z,w from sumDf order by w")
  //test2Df.show()

  var resultDf= sumDf.withColumn("z-score", (col("sum") - (col("w") * mean)) / functions.sqrt(((col("w")*numCells) - (col("w")*col("w")))/(numCells-1)) / std )
  //resultDf.show()
  resultDf = resultDf.orderBy(desc("z-score"))
  //resultDf.show()
  resultDf = resultDf.drop("w", "sum", "z-score")
  //resultDf.show()




  return resultDf.coalesce(1) //YOU NEED TO CHANGE THIS PART

}

}


