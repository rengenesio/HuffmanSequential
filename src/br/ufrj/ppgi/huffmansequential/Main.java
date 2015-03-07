package br.ufrj.ppgi.huffmansequential;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class Main {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		
		String fileName = args[0];
		String pathSuffix = args[1];
		
		long totalTime, startTime, endTime;
		
		try {
			fs.delete(new Path(fileName + "." + pathSuffix + "/sequentialdecompressed"), true);
		} catch(Exception ex) { }
		
		startTime = System.nanoTime();
		DecoderSequential decoderSequential = new DecoderSequential(fileName, pathSuffix);
		decoderSequential.Decode();
		endTime = System.nanoTime();
		System.out.println("Descompressão completa!");
			
		totalTime = endTime - startTime;
		System.out.println(totalTime/1000000000.0 + " s (decoder)");
	}

}
