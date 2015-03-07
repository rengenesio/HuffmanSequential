package br.ufrj.ppgi.huffmansequential;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class DecoderSequential {
	Codification[] codificationArray;
	byte max_code = 0;
	Path in, out, cb;
	byte[] codificationArrayElementSymbol;
	boolean[] codificationArrayElementUsed;
	FileSystem fileSystem;
	Configuration configuration;
	
	String fileName;
	String pathSuffix;

	public DecoderSequential(String fileName, String pathSuffix) throws IOException {
		this.fileName = fileName;
		this.pathSuffix = pathSuffix;
		
		// YARN configuration
		this.configuration = new Configuration();
		
		// Initializes HDFS access object
		this.fileSystem = FileSystem.get(this.configuration);
	}
	
	public void Decode() throws IOException {
		fileToCodification();
		codeToTreeArray();
		huffmanDecompressor();
	}

	public void fileToCodification() throws IOException {
		FSDataInputStream inputStream = fileSystem.open(new Path(fileName + "." + pathSuffix + "/codification"));

		byte[] byteArray = new byte[inputStream.available()];
		inputStream.readFully(byteArray);

		this.codificationArray = SerializationUtility.deserializeCodificationArray(byteArray);

		///*
		System.out.println("CODIFICATION: symbol (size) code"); 
		for(short i = 0 ; i < codificationArray.length ; i++)
			System.out.println(codificationArray[i].toString());
		//*/
	}

	public void codeToTreeArray() {
		for(short i = 0 ; i < this.codificationArray.length ; i++) {
			this.max_code = (this.codificationArray[i].size > this.max_code) ? this.codificationArray[i].size : this.max_code;  
		}
		
		codificationArrayElementSymbol = new byte[(int) Math.pow(2, (max_code + 1))];
		codificationArrayElementUsed = new boolean[(int) Math.pow(2, (max_code + 1))];

		for (short i = 0; i < this.codificationArray.length; i++) {
			int index = 0;
			for (byte b : codificationArray[i].code) {
				index <<= 1;
				if (b == 0)
					index += 1;
				else
					index += 2;
			}
			codificationArrayElementSymbol[index] = codificationArray[i].symbol;
			codificationArrayElementUsed[index] = true;
		}

		/*
		System.out.println("codeToTreeArray():");
		System.out.println("TREE_ARRAY:"); 
		for(int i = 0 ; i < Math.pow(2,(max_code + 1)) ; i++) 
			if(codificationArrayElementUsed[i])
				System.out.println("i: " + i + " -> " + codificationArrayElementSymbol[i]);
		System.out.println("------------------------------");
		*/
	}
	
	
	public void huffmanDecompressor() throws IOException {
		Path pathIn = new Path(this.fileName + "." + this.pathSuffix + "/compressed/");
		System.out.println(this.fileName + "." + this.pathSuffix + "/compressed/");
		Path pathOut = new Path(this.fileName + "." + this.pathSuffix + "/sequentialdecompressed");
		FSDataOutputStream outputStream = fileSystem.create(pathOut);
		
		// Buffer to store data to be written in disk
		byte[] bufferOutput = new byte[Defines.writeBufferSize];
		int bufferOutputIndex = 0;
		
		// Buffer to store read from disk
		byte[] bufferInput = new byte[Defines.readBufferSize];

		int codificationArrayIndex = 0;
		for(FileStatus fileStatus : this.fileSystem.listStatus(pathIn)) {
			int readBytes = 0;
			int totalReadBytes = 0;
			
			FSDataInputStream inputStream = fileSystem.open(fileStatus.getPath());
			System.out.println(fileStatus.toString());
			
			do {
				readBytes = inputStream.read(totalReadBytes, bufferInput, 0, (totalReadBytes + Defines.readBufferSize > inputStream.available() ? inputStream.available() : Defines.readBufferSize));
				totalReadBytes += readBytes;
				System.out.println("TotalReadBytes: " + totalReadBytes);
				
				for (int i = 0; i < readBytes * Defines.bitsCodification ; i++) {
//					try {
						codificationArrayIndex <<= 1;
						if (BitUtility.checkBit(bufferInput, i) == false)
							codificationArrayIndex += 1;
						else
							codificationArrayIndex += 2;
		
						if (codificationArrayElementUsed[codificationArrayIndex]) {
							if (codificationArrayElementSymbol[codificationArrayIndex] != 0) {
								bufferOutput[bufferOutputIndex++] = codificationArrayElementSymbol[codificationArrayIndex];
								
								if(bufferOutputIndex >= Defines.writeBufferSize) {
									outputStream.write(bufferOutput, 0, bufferOutputIndex);
									bufferOutputIndex = 0;
								}
								codificationArrayIndex = 0;
							} else {
								if(bufferOutputIndex > 0) {
									outputStream.write(bufferOutput, 0, bufferOutputIndex);
								}
								inputStream.close();
								break;
							}
						}
//					} catch(Exception exception) {
//						System.out.println(String.format("Número de bytes que já li: %d", totalReadBytes));
//						System.out.println(String.format("Estou neste byte: %d", totalReadBytes + i/8));
//						System.out.println(String.format("i: %d", i));
//						return;
//					}
				}
			} while (readBytes > 0);
		}
		
		outputStream.close();
	}
}
