# MimicSectionSegmenter


## Run on the segmenter

- NOTE: spark only works on linux/MacosX

1. download spark 2.2 or higher, and unpack it in `<spark_folder>`
1. clone and compile the uima-root library
1. clone this project
1. put the `uima-root/uima-segmenter/target/uima-segmenter-1.0-standalone.jar`[1] under the `UimaOnSpark/lib/` folder
1. compile this project with `sbt publish-local` 
1. copy the `target/scala-2.11/uimaonspark_2.11-0.1.0-SNAPSHOT.jar`[2] 
1. copy `NOTEEVENTS.csv.gz`, `ref_doc_section.csv`, [1] and [2] into a `<working_folder>`
1. run the spark command
1. the resulting csv will be in t

## Spark Command

### Standalone

- The two steps below run a spark environment with 4 executors. 
- The csv is split into 200 tasks that will be consumed by the 4 executors.
- The /tmp/ directory will be used as a temporary folder to get the 200 results.
- The results will be concatened into the note_nlp.csv file into the working folder

```
<spark_folder>/sbin/start-master.sh
<spark_folder>/sbin/start-slave.sh  spark://nps-HP-ProBook-430-G2:7077 -c 4
```

```
<spark_folder>/bin/spark-submit \
--class fr.aphp.wind.uima.spark.MimicSectionSegmenter \
--jars uima-segmenter-1.0-SNAPSHOT-standalone.jar,uimaonspark_2.11-0.1.0-SNAPSHOT.jar \
--files ref_doc_section.csv \
--master spark://0.0.0.0:7077 \
--executor-cores 1 \
uimaonspark_2.11-0.1.0-SNAPSHOT.jar \
/tmp/ \
note_nlp.csv \
NOTEEVENTS.csv.gz \
200
```


# NotePhiAnnotator

Run UIMA pipelines over Spark
==============================

UIMAfit
-------

Apparently, no problem thanks to simplifiing and removing xml stuff

UIMA
----

When loading an existing pipe from xml descriptor into UIMAfit pipeline keep in mind:

- put them on the spark folder
- the initialize Analysis Engine (the one providing the empty CAS) needs to be
  a UIMA pipe. Moreover, it needs to get all typeSystems from all descending
  pipes

General Notes
-------------

- the UIMAfit pipeline needs to be packaged as jar (accordingly to documentation)
- the resulting jar needs to be put in the spark folder `jar`
- the `resources/uima-an-dictionary.jar` need to be in the `jar` folder too
- all the resources (xml...) needs to be passed to slaves (--files ) but it cannot build folder. For this reason all of them are in the base folder of the UIMA project, and spark folder


Performances considerations
---------------------------

1. config 1: classic UIMAfit, 1 core
1. config 2: classic UIMAfit, 2 cores (parallel run of half dataset)
1. config 3: spark, 1 slave / 2 cores
1. config 4: spark, 1 slave / 4 cores

- test 1 (256 texts)
	- config 1: 3 min 20
	- config 2: 2 min 20
	- config 3: 2 min 20
	- config 4: 1 min 50

Apparently, running separate instances of UIMAfit is equivalent in terms of performances to running them into spark. However, while adding a new layer with spark, this allows to distribute the pipelines over multiple computers, in parallell from one command. It is then possible to scale from one to thouthand of computers easily.

NoteDeid
========

How to run (standalone)
----------

1. Run the master:  `sbin/start-master.sh`
1. Run the slave:   `sbin/start-slave.sh spark://nps-HP-ProBook-430-G2:7077`
1. Submit the job:  `bin/spark-submit --files dictionary.xsd,DictionaryAnnotator.xml,RegExAnnotator.xml,dictionary.xml,dictionary2.xml --master spark://nps-HP-ProBook-430-G2:7077 natus/lib/logquery_2.11-0.1.0-SNAPSHOT.jar`


How to run (yarn)
----------------

1. push all jars, xml, txt files on one of the computer cluster
1. push all the txt files on hdfs (=input\_path)
1. `/usr/hdp/2.5.0.0-1245/spark2/bin/spark-submit --jars NoteDeid-1.0-SNAPSHOT-standalone.jar,uima-an-dictionary.jar --files DictionaryAnnotator.xml,RegExAnnotator.xml,dictionary.xml,dictionary2.xml --master yarn-client --num-executors 8 --driver-memory 512m --executor-memory 512m --executor-cores 1   logquery_2.11-0.1.0-SNAPSHOT.jar $input_path $output_path
1. it is crucial to put only one executor core. It looks like the CAS is shared otherwize, and this leeds job to fail. In the case of 1 core executor, the pipes looks like to be run independently on multiple cores (paradoxaly)



Run SectionSegmentation
=======================


RUN
---

- STANDALONE: `/bin/spark-submit --class org.apache.spark.examples.SectionSegmenter --jars jars/NoteDeid-1.0-SNAPSHOT-standalone.jar,natus/lib/logquery_2.11-0.1.0-SNAPSHOT.jar --files SectionSegmenterDescriptor.xml --executor-cores 1 --master spark://nps-HP-ProBook-430-G2:7077 natus/lib/logquery_2.11-0.1.0-SNAPSHOT.jar /tmp/tata/ /tmp/result.csv`
- YARN: `/usr/hdp/2.5.0.0-1245/spark2/bin/spark-submit --jars NoteDeid-1.0-SNAPSHOT-standalone.jar,logquery_2.11-0.1.0-SNAPSHOT.jar --files SectionSegmenterDescriptor.xml --class org.apache.spark.examples.SectionSegmenter --num-executors 8  --executor-cores 1  --master yarn  NoteDeid-1.0-SNAPSHOT-standalone.jar tata/ result.csv`

NEEDS
-----

- a uima pipeline jar in the lib folder 

INPUT
-----

- takes an AVRO file

OUTPUT
-----

- produces a csv file without header


HOW
---

- this runs an UIMA pipeline over all text
- then, this produces a csv per each partition
- each csv are merged into one large csv
- this csv is supposed to be sent to postgresql



TODO
----

- AVRO READER (from sqoop)




Run SectionSegmentation
=======================


RUN
---

- YARN: `/usr/hdp/2.5.0.0-1245/spark2/bin/spark-submit --jars NoteDeid-1.0-SNAPSHOT-standalone.jar,logquery_2.11-0.1.0-SNAPSHOT.jar --class org.apache.spark.examples.SectionSegmenter --num-executors 16  --executor-cores 1  --master yarn  NoteDeid-1.0-SNAPSHOT-standalone.jar tata/ result.csv`

NEEDS
-----

- a uima pipeline jar in the lib folder 

INPUT
-----

- takes an AVRO file

OUTPUT
-----

- produces a csv file without header


HOW
---

- this runs an UIMA pipeline over all text
- then, this produces a csv per each partition
- each csv are merged into one large csv
- this csv is supposed to be sent to postgresql



TODO
----

- AVRO READER (from sqoop)
- 
