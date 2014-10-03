Upload-Service using Spray and Akka
========================

This project has a dependence which is not available from a public maven repository. Then it is necessary to build and install
this specific dependence.
Steps:
> git clone https://github.com/themadcreator/rabinfingerprint.git

> cd rabinfingerprint

> mvn clean install

After those steps the rabinfingerprint-1.0.0-SNAPSHOT should be installed in your local maven repo.

Now steps to run upload-file project
> git clone https://github.com/mmswkod/upload-file.git

> cd upload-file

> sbt run

To test
----------------------------
The test coverage still not 100% but there are 3 simple scripts to test the upload. The next commit will push code to test the actors.

> sh ./src/test/script/test_upload_bigfile.sh

> sh ./src/test/script/test_upload_shakespeare.sh

> sh ./src/test/script/test_upload_tiny.sh
