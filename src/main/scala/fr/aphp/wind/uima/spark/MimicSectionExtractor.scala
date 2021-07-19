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
import org.apache.spark.sql.{SparkSession, DataFrame, SaveMode, SQLContext}
import fr.aphp.wind.uima.segmenter.pojo.SectionSegmenterPojo
import java.io._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

import org.apache.avro.mapred.{AvroInputFormat, AvroWrapper}
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.io.NullWritable

object MimicSectionSegmenter {
  @transient val tt = new fr.aphp.wind.uima.segmenter.pojo.SectionSegmenterPojo("ref_doc_section.csv");
  def main(args: Array[String]) {
    val output_path = args(0)
    val result_path_file = args(1)
    val noteFilePath = args(2)
    val numberPartition = args(3).toInt

    val spark = SparkSession
      .builder()
      .appName("UIMA Section Extractor")
      .getOrCreate()

    import spark.implicits._
    val noteDS = spark.read
      .option("wholeFile", true)
      .option("multiline",true)
      .option("header", true)
      .option("quote", "\"")
      .option("escape", "\"")
      .option("inferSchema", "true")
      .option("dateFormat", "yyyy-MM-dd")
      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
      .csv(noteFilePath)
      .select('ROW_ID.as[Int],'TEXT.as[String])
      .repartition(numberPartition)

    noteDS
      .map(row => {
           tt.analyzeText(
             row._1  // row_id
           , 1  // category always 1 in this context
           , row._2  // text
      )
    }).write.mode(SaveMode.Overwrite).text(output_path)

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

