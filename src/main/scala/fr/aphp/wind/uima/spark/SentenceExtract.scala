/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package fr.aphp.wind.uima.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.uima.dkpro.spark.SentenceSegmenterPojo
import java.io._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

import org.apache.hadoop.io.NullWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._

/**
 * Executes a roll up-style query against Apache logs.
 *
 * Usage: LogQuery [logFile]
 */
object SentenceSegmenter {
  //@transient val tt = new org.apache.uima.dkpro.spark.SectionSegmenterPojo(Array(1,2,3), Array(2,2,2),Array("section1","section2","section3"));
  @transient val tt = new org.apache.uima.dkpro.spark.SentenceSegmenterPojo();

  case class Text(text:String)

  def main(args: Array[String]) {
    val output_path = args(0)
    val result_path_file = args(1)
    val partitionNum = args(2).toInt
    val warehouseLocation = "/user/edsprod/warehouse"
    val spark = SparkSession
      .builder()
      .appName("UIMA Sentence Extractor")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

      import spark.implicits._
    val df = spark.sql(" SELECT text FROM  edsprod.doc_tr WHERE text is not null")
    .select(col("text").alias("text").as[String])
    .as[Text]
    .repartition(partitionNum)
    .select(col("text").alias("text").as[String]).as[Text]
    //.mapPartitions(iter => {iter.map(x => if(x==null) null else tt.analyzeText(x.text).asInstanceOf[String])})
    .map(x => tt.analyzeText(x.text).asInstanceOf[String])
    .rdd
    .saveAsTextFile(output_path)

    merge(output_path, result_path_file)
  }

  //cf: https://dzone.com/articles/spark-write-csv-file
  def merge(srcPath: String, dstPath: String): Unit =  {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), false, hadoopConfig, null)

  }
}
// scalastyle:on println

