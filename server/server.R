#packages <- c('tuneR', 'seewave', 'gbm')
#if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
#  install.packages(setdiff(packages, rownames(installed.packages())))  
#}

# .libPaths( c(.libPaths(),"/home/gerard/R/x86_64-pc-linux-gnu-library/3.3"));


library(tuneR);
library(seewave);
library(gbm);
library(xgboost);
library(randomForest);
library(e1071);

library(caret);
library(doParallel);
set.seed(1);


cl <- makeCluster(detectCores() - 1)
registerDoParallel(cl)


extract_features <-function (file) {
  cat("Reading file", file, "\n")
  gender <- "male";
  
  bp <- c(0,22);
  wl <- 2048;
  threshold <- 5;
  parallel <- 1;
  
  r <- tuneR::readWave(paste("uploads/", file, sep = ""), from=0, units = "seconds");

  b<- bp #in case bp its higher than can be due to sampling rate
  if(b[2] > ceiling(r@samp.rate/2000) - 1) b[2] <- ceiling(r@samp.rate/2000) - 1;
  
  
  #frequency spectrum analysis
  songspec <- seewave::spec(r, f = r@samp.rate, plot = FALSE);
  analysis <- seewave::specprop(songspec, f = r@samp.rate, flim = c(0, 280/1000), plot = FALSE);
  
  #save parameters
  meanfreq <- analysis$mean/1000;
  sd <- analysis$sd/1000;
  median <- analysis$median/1000;
  Q25 <- analysis$Q25/1000;
  Q75 <- analysis$Q75/1000;
  IQR <- analysis$IQR/1000;
  skew <- analysis$skewness;
  kurt <- analysis$kurtosis;
  sp.ent <- analysis$sh;
  sfm <- analysis$sfm;
  mode <- analysis$mode/1000;
  centroid <- analysis$cent/1000;
  
  #Frequency with amplitude peaks
  peakf <- 0#seewave::fpeaks(songspec, f = r@samp.rate, wl = wl, nmax = 3, plot = FALSE)[1, 1]
  
  #Fundamental frequency parameters
  ff <- seewave::fund(r, f = r@samp.rate, ovlp = 50, threshold = threshold, 
                      fmax = 280, ylim=c(0, 280/1000), plot = FALSE, wl = wl)[, 2];
  meanfun<-mean(ff, na.rm = T);
  minfun<-min(ff, na.rm = T);
  maxfun<-max(ff, na.rm = T);
  
  #Dominant frecuency parameters
  y <- seewave::dfreq(r, f = r@samp.rate, wl = wl, ylim=c(0, 280/1000), ovlp = 0, plot = F, threshold = threshold, bandpass = b * 1000, fftw = TRUE)[, 2];
  meandom <- mean(y, na.rm = TRUE);
  mindom <- min(y, na.rm = TRUE);
  maxdom <- max(y, na.rm = TRUE);
  dfrange <- (maxdom - mindom);
  duration <- seewave::duration(wave = r, f = r@samp.rate);
  
  #modulation index calculation
  changes <- vector();
  for(j in which(!is.na(y))){
    change <- abs(y[j] - y[j + 1]);
    changes <- append(changes, change);
  }
  if(mindom==maxdom) modindx<-0 else modindx <- mean(changes, na.rm = T)/dfrange;
  
  wave <<- r;

  data <- data.frame(meanfreq, sd, median, Q25, Q75, IQR, skew, kurt, sp.ent, sfm, mode, 
                    centroid, meanfun, minfun, maxfun, meandom, mindom, maxdom, dfrange, modindx,
                    gender) 
  colnames(data) <- c("V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "V10",
                      "V11", "V12", "V13", "V14", "V15", "V16", "V17", "V18", "V19", "V20", "V21")
  return(data)
}


filter_csv <- function(csv) {
  return(data.frame(
    # csv[1],
    csv[2], # selected
    csv[3], # selected
    # csv[4],
    # csv[5],
    # csv[6],
    # csv[7],
    # csv[8],
    csv[9], # selected
    # csv[10],
    csv[11], # selected
    #csv[12],
    csv[13], # selected
    # csv[14],
    # csv[15],
    #csv[16],
    # csv[17],
    csv[18], # selected
    # csv[19],
    #csv[20],
    csv[21] # label
  ));
}


predict_gender <- function(con, data) {
  
  file <- data[1];
  features <- extract_features(file);
  fFeatures = filter_csv(features)
  nFeatures <- ncol(fFeatures)
  test_data <- fFeatures[, -nFeatures]
  
  model = readRDS("model.rds");
  predictions <- predict(model, test_data);
  gender = as.character(predictions[1]);
  writeLines(gender, con);
}


store_gender <- function (con, data) {
  file <- data[1];
  gender <- data[2];

  if(gender %in% c("male", "female")) {
    features <- extract_features(file);
    features[21] = gender;
    write.table(features, paste("uploads/", file, ".csv", sep = ""), 
              row.names=FALSE, col.names=FALSE, sep=",");
  }
}


handle_socket <- function(con) {

  data <- readLines(con, 1);
  writeLines(paste("Message receiver:", data));
  pData <- strsplit(data, " ")[[1]];
  method <- pData[1];

  if(method == "predict") {
    predict_gender(con, pData[-1]);
  } else if(method == "store") {
    store_gender(con, pData[-1]);
  }

  close(con);
}


server <- function() {
  while(TRUE) {
    writeLines("Listening...");
    tryCatch({
      con <- socketConnection(host="0.0.0.0", port = 5001, blocking = TRUE,
                              server=TRUE);
      handle_socket(con)
    }, error= function(err) {
      writeLines(paste("Connection timeout. Open another socket.", err));
    })
  }
}
server()
