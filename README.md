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


How to run
----------

1. Run the master:  `sbin/start-master.sh`
1. Run the slave:   `sbin/start-slave.sh spark://nps-HP-ProBook-430-G2:7077`
1. Submit the job:  `bin/spark-submit --files dictionary.xsd,DictionaryAnnotator.xml,RegExAnnotator.xml,dictionary.xml,dictionary2.xml --master spark://nps-HP-ProBook-430-G2:7077 natus/lib/logquery_2.11-0.1.0-SNAPSHOT.jar`
