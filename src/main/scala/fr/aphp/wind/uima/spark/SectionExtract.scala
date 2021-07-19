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
import org.apache.spark.broadcast.Broadcast
import fr.aphp.wind.uima.segmenter.pojo.SectionSegmenterPojo
import java.io._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

import org.apache.avro.mapred.{AvroInputFormat, AvroWrapper}
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.io.NullWritable
/**
 * Executes a roll up-style query against Apache logs.
 *
 * Usage: LogQuery [logFile]
 */
object SectionSegmenter {
  //@transient val tt = new org.apache.uima.dkpro.spark.SectionSegmenterPojo(Array(1,2,3), Array(2,2,2),Array("section1","section2","section3"));
  @transient val tt = new fr.aphp.wind.uima.segmenter.pojo.SectionSegmenterPojo("ref_doc_section.csv");
  def main(args: Array[String]) {
    val output_path = args(0)
    val result_path_file = args(1)
    val avro_path = args(2)
    val numberPartition = args(3)

    val sparkConf = new SparkConf().setAppName("Uima Section Segmenter")
    val sc = new SparkContext(sparkConf)
    val accum = sc.longAccumulator("My Accumulator")
    val avroRDD = sc.hadoopFile[AvroWrapper[GenericRecord], NullWritable, AvroInputFormat[GenericRecord]](avro_path, minPartitions=numberPartition.toInt)

    //val br = sc.broadcast(fruit)
    avroRDD.map(row => {
    accum.add(1)
    tt.analyzeText(
          row._1.datum.get(0).asInstanceOf[Integer]  //pdf id
        , row._1.datum.get(1).asInstanceOf[Integer]  //pdf type
        , row._1.datum.get(2).toString  // pdf content
    )
    }).saveAsTextFile(output_path)
    val file = result_path_file
    merge(output_path, file)
    println(accum)
    sc.stop()
  }

  //cf: https://dzone.com/articles/spark-write-csv-file
  def merge(srcPath: String, dstPath: String): Unit =  {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), false, hadoopConfig, null)

  }
}
// scalastyle:on println

