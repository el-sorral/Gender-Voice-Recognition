# Gender Voice Recognition

This repository holds the source code for Big Data lecture done on MASTEAM at EETAC

The directories found on the root folder contains several pieces of code:

## Feature Extraction

Holds the code that was firstly developed in order to extract the audio properties from the wav files. Basically for a given audio file some properties are extracted on the analysis variable, and this holds the information that will be used on the training and predicting algorithms.
Follow this link to see the code: https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/FeatureExtraction/main.R

## Test Algorithms

Inside this directory, you will find the *draft* source code for model building and testing. It was used also to test the performance of the different algorithms.

The file [filter.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/filter.R) is a helper that we used to select different columns of the CSV, so we could quickly test how correlation had effects over the results.

Apart from this one, you will be able to find scripts for the different algorithms and classifiers learned during the subject:

[knn.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/knn.R)

[nb.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/nb.R)

[nnet.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/nnet.R)

[rf.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/rf.R)

[rpart.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/rpart.R)

[svmRadial.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/TestAlgorithms/svmRadial.R)



## android/Genderizer

This folder contains the source code for the Sample Android Application, which is written in Java.

## extractor

In this case, the extractor directory holds a couple of bash and R-scripts that helped on the automatization of transcoding audios from mp3 to wav. And then, also to do batch operations over large quantities of files, so it was possible to convert wav files to *csv* property files in a simple way when using Linux.

## server

Finally, the server directory contains the required scripts, code and files to build a *Genderizer* server.

[server.py](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/server/server.py): Uses the Flask stack, and is the part of the software in charge of binding to Internet and listen for incomming audio/prediction/assertment requests. This piece of code will perform a call to an internal deamon written in R which performs the feature extraction and predicting tasks.

[server.R](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/server/server.R): This file contains the predicting part of the program. It's written in R and the idea was to make it work as a deamon, binding to a network port/filepath and listen for requests. It handles the feature extraction, and then the prediction of the given data.
As R is able to bind to network ports, we used this setup to increase performance. Spawning processes from Python was not performing properly

Example of binding:
```R
server <- function() {
  while(TRUE) {
    writeLines("Listening...");
    tryCatch({
      con <- socketConnection(host="0.0.0.0", port = 5001, blocking = TRUE,
                              server=TRUE);
      handle_socket(con) # Handle socket will read the file from a directory, and then process and predict it
    }, error= function(err) {
      writeLines(paste("Connection timeout. Open another socket.", err));
    })
  }
}
server()
```

[docker-compose.yml](https://github.com/el-sorral/Gender-Voice-Recognition/blob/master/server/docker-compose.yml): This is the orchestration file. It uses docker engine to make the application agnostic of the underlying Operating System. Basically starts two containers one running the python part, the second one running the R part. And then mounts a storage volume to share the audio files that have been uploaded.
