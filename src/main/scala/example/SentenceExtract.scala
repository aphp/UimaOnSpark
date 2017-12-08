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
package org.apache.spark.examples

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.uima.dkpro.spark.SentenceSegmenterPojo
import java.io._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

import org.apache.hadoop.io.NullWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
/**
 * Executes a roll up-style query against Apache logs.
 *
 * Usage: LogQuery [logFile]
 */
object SentenceSegmenter {
  //@transient val tt = new org.apache.uima.dkpro.spark.SectionSegmenterPojo(Array(1,2,3), Array(2,2,2),Array("section1","section2","section3"));
  @transient val tt = new org.apache.uima.dkpro.spark.SentenceSegmenterPojo();
  def main(args: Array[String]) {
    val output_path = args(0)
    val result_path_file = args(1)
   // val sparkConf = new SparkConf().setAppName("Uima Sentence Segmenter")
   // val sc = new SparkContext(sparkConf)
    //val spark = new org.apache.spark.sql.SQLContext(sc)
    val warehouseLocation = "/user/edsedev/warehouse"

     val spark = SparkSession
      .builder()
      .appName("UIMA Sentence Extractor")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()
    val df = spark.sql("SELECT lexical_variant FROM edsomop.note_nlp")
    val rows: RDD[Row] = df.rdd
    rows.map( row => tt.analyzeText(row.get(0).asInstanceOf[String]) ).saveAsTextFile(output_path)
    val file = result_path_file
    merge(output_path, file)
  }

  //cf: https://dzone.com/articles/spark-write-csv-file
  def merge(srcPath: String, dstPath: String): Unit =  {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), false, hadoopConfig, null)

  }
}
// scalastyle:on println

