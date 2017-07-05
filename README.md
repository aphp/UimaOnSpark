Run UIMA pipeline over Spark
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

Performances considerations
---------------------------

config 1: classic UIMAfit, 1 core
config 2: spark, 1 slave/6 cores

- test 1 (128 texts)
	- config 1: 1 min
	- config 2: 1 min

- test 2 (256 texts)
	- config 1: 3 min 20
	- config 2: 2 min 30
