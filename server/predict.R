# load the library
library(caret)
library(doParallel)
set.seed(1)

t <- proc.time()

cl <- makeCluster(detectCores() - 1)
registerDoParallel(cl)

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
test_csv  <- filter_csv(read.csv("./test.csv", head = FALSE))
features <- ncol(test_csv)
test_data <- test_csv[, -features]


predictions <- predict(readRDS("model.rds"), test_data)
as.character(predictions[1])