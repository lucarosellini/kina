package kina.config;

import org.bson.BSONObject;

/**
 * * Configuration object for RAW mongo RDDs.
 *
 * Created by luca on 1/12/14.
 */
public class RawMongoKinaConfig extends GenericMongoKinaConfig<BSONObject>  {
    public RawMongoKinaConfig() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<BSONObject> getEntityClass() {
        return BSONObject.class;
    }
}
