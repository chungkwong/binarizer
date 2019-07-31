/*
 * Copyright (C) 2019 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.chungkwong.binarizer;
import com.github.chungkwong.binarizer.*;
import java.awt.image.*;
import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class BinarizerTest{
	public static void testEquality(File directory,Binarizer... binarizers){
		for(File file:directory.listFiles()){
			try{
				System.out.print(file.getName());
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				byte[] last=null;
				for(Binarizer binarizer:binarizers){
					byte[] curr=((DataBufferByte)binarizer.binarize(input).getRaster().getDataBuffer()).getData();
					if(last!=null){
						System.out.print("\t"+Arrays.equals(curr,last));
					}else{
						last=curr;
					}
				}
			}catch(IOException ex){
				Logger.getLogger(BinarizerTest.class.getName()).log(Level.SEVERE,null,ex);
			}
			System.out.println();
		}
	}
	public static void testSpeed(File directory,Binarizer... binarizers){
		ThreadMXBean timer=ManagementFactory.getThreadMXBean();
		long[] time=new long[binarizers.length];
		int count=0;
		for(File file:directory.listFiles()){
			try{
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				for(int i=0;i<binarizers.length;i++){
					long startTime=timer.getCurrentThreadCpuTime();
					binarizers[i].binarize(input);
					time[i]+=timer.getCurrentThreadCpuTime()-startTime;
				}
				System.out.println(++count);
			}catch(IOException ex){
				Logger.getLogger(BinarizerTest.class.getName()).log(Level.SEVERE,null,ex);
			}
		}
		System.out.println(Arrays.toString(time));
		for(long t:time){
			System.out.println(t*1e-9/count+"s");
		}
	}
	public static void testNiblackImplementationsEquality(File directory,NiblackBasedBinarizer.ThresholdFormula formula,int window){
		NiblackBasedBinarizer naive=new NiblackBasedBinarizer(formula,new NaiveBinarizer(),window);
		NiblackBasedBinarizer integral=new NiblackBasedBinarizer(formula,new IntegralImageBinarizer(),window);
		NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formula,new EfficientBinarizer(),window);
		testEquality(directory,naive,integral,effient);
	}
	public static void testFormulaEquality(File directory,int window,NiblackBasedBinarizer.ThresholdFormula... formulas){
		testEquality(directory,Arrays.stream(formulas).map((f)->new NiblackBasedBinarizer(f,new EfficientBinarizer(),window)).toArray(Binarizer[]::new));
	}
	public static void testSpeed(File directory,NiblackBasedBinarizer.ThresholdFormula formula,int window){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		NiblackBasedBinarizer naive=new NiblackBasedBinarizer(formula,new NaiveBinarizer(),window);
		NiblackBasedBinarizer integral=new NiblackBasedBinarizer(formula,new IntegralImageBinarizer(),window);
		NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formula,new EfficientBinarizer(),window);
		testSpeed(directory,fixed,otsu,naive,integral,effient);
	}
	public static void main(String[] args){
//		testNiblackImplementationsEquality(new File("../datasets/binarization/DIBC02009_Test_images-handwritten"),NiblackBasedBinarizer.getSauvola(0.2),32);
//		testNiblackImplementationsSpeed(new File("../datasets/binarization/DIBC02009_Test_images-handwritten"),NiblackBasedBinarizer.getSauvola(0.2),32);
//		testSpeed(new File("/home/kwong/projects/datasets/binarization/H_DIBCO2010_test_images"),NiblackBasedBinarizer.getSauvolaFast(0.2),32);
//		testFormulaEquality(new File("/home/kwong/projects/datasets/binarization/H_DIBCO2010_test_images"),32,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaFast(0.2));
	}
}
