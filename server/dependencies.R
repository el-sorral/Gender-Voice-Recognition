r = getOption("repos") # hard code the UK repo for CRAN
r["CRAN"] = "http://cran.uk.r-project.org"
options(repos = r)

install.packages("tuneR");
install.packages("seewave");
install.packages("gbm");
install.packages("xgboost");
install.packages("randomForest");
install.packages("e1071");

install.packages("caret");
install.packages("doParallel");
install.packages("fftw");