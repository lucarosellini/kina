package kina.config;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kina-spacific hadoop path filter. Those patterns specified using {@link kina.config.MongoKinaConfig#bsonFilesExcludePatterns(String[])}
 * will NOT be accepted.
 *
 * Created by luca on 27/11/14.
 */
public class KinaMongoPathFilter implements PathFilter, Configurable {

    private transient static final Logger LOG = LoggerFactory.getLogger(KinaMongoPathFilter.class);

    public final static String PATH_FILTER_CONF = "kina.mongo.path.filter.conf";
    private Configuration conf;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(Path path) {
        String[] excludePatterns = conf.getStrings(PATH_FILTER_CONF, new String[]{});

        if (excludePatterns == null || excludePatterns.length == 0) {
            return true;
        }

        boolean condition = true;

        for (String pattern : excludePatterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(path.toString());
            condition &= !m.matches();
        }

        LOG.debug(path.toString() + " accepted: " + condition);

        return condition;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }
}
