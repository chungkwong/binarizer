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
import java.util.stream.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class BinarizerTest{
	public static void testEquality(File directory,Binarizer... binarizers){
		System.out.println("Testing correctness: "+directory);
		for(Binarizer binarizer:binarizers){
			System.out.print(binarizer+"\t");
		}
		System.out.println();
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
		System.out.println("Testing speed: "+directory);
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
		for(int i=0;i<binarizers.length;i++){
			System.out.println(binarizers[i].toString()+"\t"+time[i]*1e-9+"s"+"\t"+time[i]*1e-9/count+"s");
		}
	}
	public static void testQuality(File directory,File groundtruths,Binarizer... binarizers){
		System.out.println("Testing quality: "+directory);
		int count=0;
		double[][] measure=new double[binarizers.length][2];
		for(File file:directory.listFiles()){
			try{
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				File gtFile=new File(groundtruths,file.getName().substring(0,file.getName().lastIndexOf('.'))+".tiff");
				BufferedImage groundtruth=GrayscaleBinarizer.toGrayscale(ImageIO.read(gtFile));
				for(int i=0;i<binarizers.length;i++){
					BufferedImage result=binarizers[i].binarize(input);
					measure[i][0]+=getF(result,groundtruth);
					measure[i][1]+=getPsnr(result,groundtruth);
				}
				++count;
			}catch(IOException ex){
				Logger.getLogger(BinarizerTest.class.getName()).log(Level.SEVERE,null,ex);
			}
		}
		int c=count;
		for(int i=0;i<binarizers.length;i++){
			System.out.println(binarizers[i].toString()+"\t"+Arrays.stream(measure[i]).mapToObj((x)->Double.toString(x/c)).collect(Collectors.joining("\t")));
		}
	}
	public static double getF(BufferedImage result,BufferedImage groundtruth){
		byte[] r=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
		byte[] g=((DataBufferByte)groundtruth.getRaster().getDataBuffer()).getData();
		int tp=0, fp=0, tn=0, fn=0;
		for(int i=0;i<g.length;i++){
			if(g[i]==0){
				if(r[i]==0){
					++tp;
				}else{
					++fn;
				}
			}else{
//				if(g[i]!=(byte)255){
//					System.err.println("bad");
//				}
				if(r[i]==0){
					++fp;
				}else{
					++tn;
				}
			}
		}
		double recall=tp*1.0/(tp+fn);
		double precision=tp*1.0/(tp+fp);
		return 2*recall*precision/(recall+precision);
	}
	public static double getPsnr(BufferedImage result,BufferedImage groundtruth){
		byte[] r=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
		byte[] g=((DataBufferByte)groundtruth.getRaster().getDataBuffer()).getData();
		long se=0;
		for(int i=0;i<r.length;i++){
			int d=Byte.toUnsignedInt(g[i])-Byte.toUnsignedInt(r[i]);
			se+=d*d;
		}
		return 10*Math.log10(255*255*1.0*r.length/se);
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
	public static void testSpeed(File directory){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		int window=32;
		NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvolaFast(0.2);
		NiblackBasedBinarizer naive=new NiblackBasedBinarizer(formula,new NaiveBinarizer(),window);
		NiblackBasedBinarizer integral=new NiblackBasedBinarizer(formula,new IntegralImageBinarizer(),window);
		NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formula,new EfficientBinarizer(),window);
		testSpeed(directory,fixed,otsu,naive,integral,effient);
	}
	public static Binarizer[] getStandardBinarizers(){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		int window=32;
		NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvolaFast(0.5);
		NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formula,new EfficientBinarizer(),window);
		return new Binarizer[]{fixed,otsu,effient};
	}
	public static void main(String[] args){
//		testNiblackImplementationsEquality(new File("../datasets/binarization/DIBC02009_Test_images-handwritten"),NiblackBasedBinarizer.getSauvola(0.2),32);
//		testNiblackImplementationsSpeed(new File("../datasets/binarization/DIBC02009_Test_images-handwritten"),NiblackBasedBinarizer.getSauvola(0.2),32);
//		testFormulaEquality(new File("../datasets/binarization/2009/DIBC02009_Test_images-handwritten"),32,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaFast(0.2));
//		testFormulaEquality(new File("../datasets/binarization/2009/DIBCO2009_Test_images-printed"),32,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaFast(0.2));
//		testNiblackImplementationsEquality(new File("../datasets/binarization/2009/DIBC02009_Test_images-handwritten"),NiblackBasedBinarizer.getSauvolaFast(0.2),32);
//		testNiblackImplementationsEquality(new File("../datasets/binarization/2009/DIBCO2009_Test_images-printed"),NiblackBasedBinarizer.getSauvolaFast(0.2),32);
//		testSpeed(new File("../datasets/binarization/2009/DIBC02009_Test_images-handwritten"));
//		testSpeed(new File("../datasets/binarization/2009/DIBCO2009_Test_images-printed"));
		testQuality(new File("../datasets/binarization/2009/test"),new File("../datasets/binarization/2009/gt"),getStandardBinarizers());
		testQuality(new File("../datasets/binarization/2010/test"),new File("../datasets/binarization/2010/gt"),getStandardBinarizers());
		testQuality(new File("../datasets/binarization/2011/test"),new File("../datasets/binarization/2011/gt"),getStandardBinarizers());
//		testFormulaEquality(new File("/home/kwong/projects/datasets/binarization/H_DIBCO2010_test_images"),32,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaFast(0.2));
	}
}
