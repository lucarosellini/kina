package kina.testutils;

import java.io.Serializable;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.bson.BSONObject;
import org.testng.annotations.Test;

import scala.Tuple2;

import com.mongodb.hadoop.BSONFileInputFormat;

@Test
public class MiscUtils implements Serializable{
	
	@Test
	public void testMisc(){
		SparkConf conf = new SparkConf().setMaster("local").setAppName("SparkRecommender");
		
		String uri = "file:///tmp/backup/oplog.bson";
		JavaSparkContext sc = new JavaSparkContext(conf);
		Configuration bsonDataConfig = new Configuration();
		bsonDataConfig.set("mongo.job.input.format", "com.mongodb.hadoop.BSONFileInputFormat");
		JavaRDD<BSONObject> rdd = sc.newAPIHadoopFile(
	            uri, BSONFileInputFormat.class, Object.class,
	                BSONObject.class, bsonDataConfig).map(
	                        new Function<Tuple2<Object, BSONObject>, Object>() {
	                            @Override
	                            public Object call(Tuple2<Object, BSONObject> doc) throws Exception {
	                                return doc._2;
	                            }
	                        }
	                    );
		
		List<BSONObject> objs = rdd.collect();
		
		for (BSONObject o : objs) {
			o.removeField("o");
			System.out.println(o);
		}
		
		/*
		
		Map<Object, BSONObject> collected = rdd.collectAsMap();
		
		Logger log = sc.sc().log();
		
		for (Map.Entry<Object, BSONObject> e : collected.entrySet()) {
			log.info(e.getKey() + ": " + e.getValue().toString());
		}
		*/
	}
}
