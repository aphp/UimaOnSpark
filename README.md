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
1. `/usr/hdp/2.5.0.0-1245/spark2/bin/spark-submit --jars NoteDeid-1.0-SNAPSHOT-standalone.jar,uima-an-dictionary.jar --files DictionaryAnnotator.xml,RegExAnnotator.xml,dictionary.xml,dictionary2.xml --master yarn-client --num-executors 8 --driver-memory 512m --executor-memory 512m --executor-cores 4   logquery_2.11-0.1.0-SNAPSHOT.jar $input_path $output_path

Performances considerations
---------------------------

- test1 : 5 node spark with hadoop (16 core used), 500 texts
- 1 minute

test2 : 1 node without spark (1 code used), 100 texts
- 45 secondes

- Conclusion: Distributing 100 texts on 5 computer would use 45 seconde. It should be possible to run several instances on each. But this would be a complicated process, to log errors, and overhead to distribute calculus. Spark/uima has decent performances while saving time to program and to debug.
