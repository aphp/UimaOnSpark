run:
	sbt publish-local ;\
	cp target/scala-2.11/logquery_2.11-0.1.0-SNAPSHOT.jar /home/nps/Logiciels/spark-2.1.1-bin-hadoop2.7/natus/lib/ ;\
	cp lib/NoteDeid-1.0-SNAPSHOT-standalone.jar /home/nps/Logiciels/spark-2.1.1-bin-hadoop2.7/jars/
