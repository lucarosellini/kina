package kina.rdd.mongodb;

import com.mongodb.hadoop.input.BSONFileRecordReader;
import com.mongodb.hadoop.splitter.BSONSplitter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an Hadoop input format capable of reading mongo bson dump files.
 *
 * Created by luca on 27/11/14.
 */
public class KinaBSONFileInputFormat extends FileInputFormat<Object,BSONObject> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordReader<Object,BSONObject> createRecordReader(final InputSplit split, final TaskAttemptContext context)
            throws IOException, InterruptedException {

        RecordReader reader = new BSONFileRecordReader();
        reader.initialize(split, context);
        return reader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<InputSplit> getSplits(final JobContext context) throws IOException {
        return new ArrayList<>(super.getSplits(context));
    }
}
