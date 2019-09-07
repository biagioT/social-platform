package it.antonio;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;

public interface ZeppelinExecutor {

	void execute(SparkContext sc, SQLContext sqlContext);

}
