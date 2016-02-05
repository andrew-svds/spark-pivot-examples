#2. Feature Generation

Download [MovieLense 1M dataset](http://files.grouplens.org/datasets/movielens/ml-1m.zip) and unzip. In a spark-shell try the following (adjusting the path to ml-1m as necessary).

```scala
val ratings_raw = sc.textFile("Downloads/ml-1m/ratings.dat")
case class Rating(user: Int, movie: Int, rating: Int)
val ratings = ratings_raw.map(_.split("::").map(_.toInt)).map(r => Rating(r(0),r(1),r(2))).toDF.repartition(20, col("user"))

val users_raw = sc.textFile("Downloads/ml-1m/users.dat")
case class User(user: Int, gender: String, age: Int)
val users = users_raw.map(_.split("::")).map(u => User(u(0).toInt, u(1), u(2).toInt)).toDF

// To get a balanced sample we keep only 2/5 of the men.
val sample_users = users.where(expr("gender = 'F' or ( rand() * 5 < 2 )"))
sample_users.groupBy("gender").count().show

/*
+------+-----+
|gender|count|
+------+-----+
|     F| 1709|
|     M| 1744|
+------+-----+
*/

// This gets the 100 most popular movies by rating count.
val popular = ratings.groupBy("movie").count().orderBy($"count".desc).limit(100).map(_.get(0)).collect

// Pivoting to get the features (ratings for popular movies) of a user.
val ratings_pivot = ratings.groupBy("user").pivot("movie", popular.toSeq).agg(expr("coalesce(first(rating),3)").cast("double"))

// Adding a label 1.0 = male, 0.0 = female.
val data = ratings_pivot.join(sample_users, "user").withColumn("label", expr("if(gender = 'M', 1, 0)").cast("double"))

// Combine our features into one single column.
import org.apache.spark.ml.feature.VectorAssembler
val assembler = new VectorAssembler().setInputCols(popular.map(_.toString)).setOutputCol("features")

import org.apache.spark.ml.classification.LogisticRegression
val lr = new LogisticRegression()

import org.apache.spark.ml.Pipeline
val pipeline = new Pipeline().setStages(Array(assembler, lr))

val Array(training, test) = data.randomSplit(Array(0.9, 0.1), seed = 12345)

val model = pipeline.fit(training)

val res = model.transform(test).select("label", "prediction")

// Use pivot to get confusion matrix, 1.0 = male and 0.0 = female.
// Rows are actuals and columns are predicted.
res.groupBy("label").pivot("prediction", Seq(1.0, 0.0)).count().show

/*
+-----+---+---+
|label|1.0|0.0|
+-----+---+---+
|  1.0|114| 74|
|  0.0| 46|146|
+-----+---+---+
*/
```
