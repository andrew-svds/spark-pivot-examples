val scaleFactor = 1
println(s"*** Creating TPC-DS dataset at scale factor $scaleFactor")
import com.databricks.spark.sql.perf.tpcds.Tables
val tables = new Tables(sqlContext, "/tpcds-kit/tools", 1)
tables.genData("/tables", "parquet", true, false, false, false, false)
tables.createExternalTables("/tables", "parquet", "default", false)
println("*** Done generating tables")
exit
