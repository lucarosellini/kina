package kina.rdd.mongodb;

import kina.config.MongoKinaConfig;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.KinaMongoRDD;
import org.bson.BSONObject;
import scala.Tuple2;
import scala.reflect.ClassTag$;

/**
 * RDD exposing the raw BSON Object
 *
 * Created by luca on 1/12/14.
 */
public class MongoRawRDD extends KinaMongoRDD<BSONObject> {
    /**
     * Public constructor that builds a new MongoRawRDD RDD given the context and the configuration file.
     *
     * @param sc     the spark context to which the RDD will be bound to.
     * @param config the kina configuration object.
     */
    @SuppressWarnings("unchecked")
    public MongoRawRDD(SparkContext sc, MongoKinaConfig<BSONObject> config) {
        super(sc, config, ClassTag$.MODULE$.<BSONObject>apply(config.getEntityClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BSONObject transformElement(Tuple2<Object, BSONObject> tuple) {
        return tuple._2();
    }
}
