#1. Reporting

Since this one has some more complex build requirements I'm providing a [docker](https://www.docker.com/) image.

To get started run my image from DockerHub.

```
docker run -it svds/spark-pivot-reporting
```

Alternatively, if you really want build the Dockerfile and run it

```
docker build -t spark-pivot-reporting 1-Reporting
docker run -it spark-pivot-reporting
```

This will drop you into a spark-shell with the TPC-DS tables ready to query. If you prefer pyspark add that to the end of your run command.

Now you can try a query, the one below gives sales by category (rows) and quarter (column). Note, to make copy/paste easy in the scala REPL it's wrapped in (). 

```scala
(sql("""select *, concat('Q', d_qoy) as qoy
  from store_sales
  join date_dim on ss_sold_date_sk = d_date_sk
  join item on ss_item_sk = i_item_sk""")
  .groupBy("i_category")
  .pivot("qoy")
  .agg(round(sum("ss_sales_price")/1000000,2))
  .show)
```

To see a list of available tables check `sqlContext.tableNames`
