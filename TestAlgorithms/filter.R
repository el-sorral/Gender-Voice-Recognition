# load the library
library(caret)
library(doParallel)
set.seed(1)

t <- proc.time()

cl <- makeCluster(detectCores()-1)
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
    # csv[12],
    csv[13], # selected
    # csv[14],
    # csv[15],
    # csv[16],
    # csv[17],
    csv[18], # selected
    # csv[19],
    # csv[20],
    csv[21] # label
  ));
}

# Prepare data
train_csv_full <- filter_csv(read.csv("./voice.csv", head = FALSE))
features <- ncol(train_csv_full)
trainIndex <- createDataPartition(train_csv_full$V21, p = .8, 
                                  list = FALSE, 
                                  times = 1)

train_csv <- train_csv_full[ trainIndex,]
test_csv  <- train_csv_full[-trainIndex,]

#test_csv  <- filter_csv(read.csv("./test.csv", head = FALSE))


# Prepare train data
train_data <- train_csv[,-features]
train_labels <- as.factor(train_csv[, features])

# Prepare test data
features <- ncol(test_csv)
test_data <- test_csv[, -features]
test_labels <- as.factor(test_csv[, features])
#factor(c(1), levels = c(2, 1), labels = c("female", "male"))
#as.factor(test_csv[, features])

fitControl <- trainControl(method = "cv",
                           number = 10)

# Train the model
model <- train(train_data, train_labels, 
               trControl = fitControl,
               method = "svmRadial")

print.train(model)
plot.train(model)


#partGrid <- expand.grid(cp = (0:10)*0.01)
#knnGrid <- expand.grid(k = 1:5)
#grid <- expand.grid(k= 1:5, cp = (0:5)*0.1)


# Predict values
predictions <- predict(model, test_data)

# print results
output <- data.frame(PREDICT=predictions, LABEL=test_labels,
                     HIT=predictions==test_labels)
head(output)

# summarize results
res <- confusionMatrix(predictions, test_labels)
res
res$overall["Accuracy"]

proc.time()-t

# Other result
model$results
print(paste("Acuracy[real/expected]: ", round(res$overall["Accuracy"], 3), "/", round(max(model$results$Accuracy), 3)))


