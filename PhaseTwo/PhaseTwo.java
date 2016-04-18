import java.io.IOException;
import java.util.*;
        
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
        
public class PhaseTwo 
{
    public static class Map extends Mapper<LongWritable, Text, Text, Text>
    {
	//private final static IntWritable one = new IntWritable(1);
	private Text outKey = new Text();
        
	public void map(LongWritable key,Text value, Context context) throws IOException, InterruptedException 
	{
	    String inputLine = value.toString();
	    String temp[] = inputLine.split("\t"); //spliting input string to get pair of word,document name and frequency
	    int wordCntr = Integer.parseInt(temp[1]);//getting word frequency
	    String docPart[]=temp[0].split(",");//seperating document name and word
	    String docName = docPart[1]; //getting the document number or the document name
	    outKey.set(docName);
	    String outval = docPart[0]+","+wordCntr;
	    context.write(outKey,new Text(outval));
	   
	    //String word = docPart[0];//getting the input word
	    //String tempStr=""; //temp string to construct the key part
	    
	    //loop is not required in this mapper as we know that the input string will only have 3 parts
	}
    } 
        
    public static class Reduce extends Reducer<Text, Text, Text, Text>
    {

	public void reduce(Text key, Iterable<Text> values, Context context) 
	    throws IOException, InterruptedException
	    {
		int sum = 0;
		ArrayList<String> valuelist=new ArrayList<String>();
    int i=0;
for (Text text : values) {
    valuelist.add(text.toString());
    String content[] = text.toString().split(",");
    sum=sum+Integer.parseInt(content[1]);
}
    List<Float> valli = new ArrayList<Float>();
for (String text : valuelist) {
    String content1[] = text.split(",");
    float freq = Float.parseFloat(content1[1])/sum;
    valli.add(freq);    
    }
    float maximu = Collections.max(valli);
for (String text : valuelist){
    float norm_freq = valli.get(i)/maximu;
    i++;
    String content2[] = text.split(",");
    String keys = content2[0];
    String outvalue = key.toString() +"," + norm_freq;
    context.write(new Text (keys), new Text(outvalue));    
}
	//context.write(key, new Text(values.toString()+","+sum));
    }
    }
        
    public static void main(String[] args) throws Exception
    {
	Configuration conf = new Configuration();
        
        Job job = new Job(conf, "PhaseTwo");
    
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(Text.class);
	
	job.setMapOutputKeyClass(Text.class);
  job.setMapOutputValueClass(Text.class);
  job.setJarByClass(PhaseTwo.class);

	job.setMapperClass(Map.class);
	job.setReducerClass(Reduce.class);
        
	job.setInputFormatClass(TextInputFormat.class);
	job.setOutputFormatClass(TextOutputFormat.class);
        
	FileInputFormat.addInputPath(job, new Path(args[0]));
	FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
	job.waitForCompletion(true);
    }
        
}
