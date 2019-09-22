package it.antonio.zeppelin;

import java.io.Serializable;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;

public interface ZeppelinExecutor extends Serializable {

	void execute(SparkContext sc, SQLContext sqlContext);

}
