package it.antonio;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;

import scala.Tuple2;

public class TwitterWords implements ZeppelinExecutor {
	 private static final Pattern SPACE = Pattern.compile(" ");
	 
	 public static void main(String[] args) throws Exception {
	     
		 SparkConf sparkConf = new SparkConf();
			sparkConf.setAppName("Twitter to Mongo");
			
			sparkConf.setMaster("local[2]");
			sparkConf.set("spark.mongodb.input.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");
			sparkConf.set("spark.mongodb.output.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");

			
			SparkContext sc = new SparkContext(sparkConf);
			
			SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
			SQLContext sqlcontext= new SQLContext(sparkSession);
			
			TwitterWords tw = new TwitterWords();   
		    tw.execute(sc, sqlcontext);
		 	

		 	
		 	Dataset<Row> sqlDF = sqlcontext.sql("SELECT * FROM words order by count desc");
		 	sqlDF.show();
		 	
		   
		 	
	    }
	 
	 @Override
	 public void execute(SparkContext sc, SQLContext sqlContext) {
		 JavaSparkContext jsc = new JavaSparkContext(sc);
				JavaMongoRDD<Document> rdd = MongoSpark.load(jsc);
		 	
		 	// filtered on mongo before load to spark
		 	LocalDateTime dateTime = LocalDateTime.now().minusHours(8);
		 	
		 	String isoDate = DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
			Document mongoQuery = Document.parse("{ $match: { createdAt : { $gte : \"" +isoDate + "\" } } }");
		 	
		 	JavaMongoRDD<Document> filteredDocuments = rdd.withPipeline(Collections.singletonList(mongoQuery));
		 	
		 	JavaRDD<String> lines = filteredDocuments.map(d -> d.getString("text"));
		 	
		 	JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
	        
		 	JavaPairRDD<String, Integer> wordAsTuple = words.mapToPair(word -> new Tuple2<>(word, 1));
	        
		 	JavaPairRDD<String, Integer> wordWithCount = wordAsTuple.reduceByKey((Integer i1, Integer i2)->i1 + i2);
	        
		 	JavaRDD<WordCount> output = wordWithCount.map(t -> {
		 		WordCount wc = new WordCount();
		 		wc.setWord(t._1);
		 		wc.setCount(t._2);
		 		return wc;
		 	});
		 	
		 	Dataset<Row> df = sqlContext.createDataFrame(output, WordCount.class);
		 	
		 	df.createOrReplaceTempView("words");
	 }
	 
	 
	 public static class WordCount implements Serializable {

	        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
			private String word;
	        private Integer count;
			public String getWord() {
				return word;
			}
			public void setWord(String word) {
				this.word = word;
			}
			public Integer getCount() {
				return count;
			}
			public void setCount(Integer count) {
				this.count = count;
			}
	        
	        
	    }
}
