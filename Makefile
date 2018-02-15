run: publish
	scp target/scala-2.11/logquery_2.11-0.1.0-SNAPSHOT.jar clusterprod:app/doc_sentence/lib/
publish:
	sbt publish-local 
