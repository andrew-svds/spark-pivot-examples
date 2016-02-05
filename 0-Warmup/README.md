#0. Warmup

Do a pivot on this sample table

|A|B|C|D|
|---|---|---|---|
|foo|one|small|1|
|foo|one|large|2|
|foo|one|large|2|
|foo|two|small|3|
|foo|two|small|3|
|bar|one|large|4|
|bar|one|small|5|
|bar|two|small|6|
|bar|two|large|7|

Start spark-shell with the databricks csv package

```
spark-shell --packages com.databricks:spark-csv_2.10:1.3.0
```

Load the [table](0-Warmup/table.csv) and do a pivot (adjusting the path to table.csv as necessary)

```scala
val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("0-Warmup/table.csv")
df.groupBy("A", "B").pivot("C").sum("D").show
```

You should get something like

|  A|  B|large|small|
|---|---|-----|-----|
|foo|two| null|    6|
|bar|two|    7|    6|
|foo|one|    4|    1|
|bar|one|    4|    5|

Now try something more complex like

```scala
df.groupBy("C").pivot("A").agg(avg("D"), max("B")).show
```
