package br.ufrj.ppgi.huffmansequential;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class Main {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		System.out.println("aaaaaaaaaaaaaaaaaaaaa");
		String compressedPath = args[0];
		String decompressedFileName = args[1];
		
		long totalTime, startTime, endTime;
		
		try {
			fs.delete(new Path(decompressedFileName), true);
		} catch(Exception ex) { }
		
		startTime = System.nanoTime();
		new DecoderSequential(compressedPath, decompressedFileName);
		endTime = System.nanoTime();
		System.out.println("Descompressão completa!");
			
		totalTime = endTime - startTime;
		System.out.println(totalTime/1000000000.0 + " s (decoder)");
	}

}
