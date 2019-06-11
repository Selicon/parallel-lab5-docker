package com.parallel5;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.sum;
import static org.apache.spark.sql.functions.window;

import java.util.Date;
import java.util.Objects;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

public class Main {

  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("parallel5");
    // .setMaster("spark://master:7077");
    JavaSparkContext sc = new JavaSparkContext(conf);
    SparkSession session = SparkSession.builder().getOrCreate();

    JavaRDD<String> input = sc.textFile("hdfs:///input");

    JavaRDD<Record> records = input.map(Record::fromString).filter(Objects::nonNull);

    records
        .filter(record -> record.getStatus() >= 500)
        .mapToPair(record -> new Tuple2<>(record.getRequest(), 1))
        .reduceByKey(Integer::sum)
        .saveAsTextFile("hdfs:///output1");

    records
        .mapToPair(
            record ->
                new Tuple2<>(
                    record.getMethod() + record.getStatus() + record.getDate(),
                    new Tuple2<>(record.getDate(), 1)))
        .reduceByKey((a, b) -> new Tuple2<>(a._1(), a._2() + b._2()))
        .filter(pair -> pair._2()._2() >= 10)
        .mapToPair(pair -> new Tuple2<>(pair._2()._1(), pair._2()._2()))
        .reduceByKey(Integer::sum)
        .sortByKey()
        .saveAsTextFile("hdfs:///output2");

    JavaRDD<Row> rows = records
        .filter(record -> record.getStatus() >= 400)
        .map(record -> RowFactory.create(new java.sql.Date(record.getDate().getTime()), 1));

    StructField[] fields = new StructField[]{
        new StructField("date", DataTypes.DateType, false, Metadata.empty()),
        new StructField("count", DataTypes.IntegerType, false, Metadata.empty())
    };

    session.createDataFrame(rows, new StructType(fields))
        .groupBy(window(col("date"), "1 week", "1 day"))
        .agg(sum("count"))
        .orderBy("window")
        .toJavaRDD()
        .saveAsTextFile("hdfs:///output3");
  }
}
