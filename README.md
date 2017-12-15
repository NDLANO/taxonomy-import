# taxonomy-import
Import utilities for the taxonomy api. 

## Build and test the project

The import program needs the taxonomy-api running locally in order for tests to run. Build the project with `maven clean install`. 
You can run parser tests without the taxonomy-api, but all importer tests communicate with the REST service. 

## Importing subjects to the taxonomy API

The importer is set up *nix command line style. It imports a Google sheet (one subject per sheet) in `.tsv` format. Example of use: 
``` 
cat subjectToBeImported.tsv | target/taxonomy-import.jar -n "Subject name" -i urn:subject:1 
``` 

The above line will import the contents of the sheet to the local instance of the taxonomy REST service. 
The subject name will be `Subject name` and its URI `urn:subject:1`. 
If you don't assign a URI to the subject one will be generated. You can connect your local REST service to the AWS databases, 
but the preferred way to import is to connect directly to the EB instance using the `-e` flag followed by the URL to the taxonomy API.