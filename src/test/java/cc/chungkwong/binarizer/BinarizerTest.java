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
		byte[] r=getData(result);
		byte[] g=getData(groundtruth);
		long se=0;
		for(int i=0;i<r.length;i++){
			int d=Byte.toUnsignedInt(g[i])-Byte.toUnsignedInt(r[i]);
			se+=d*d;
		}
		return 10*Math.log10(255*255*1.0*r.length/se);
	}
	private static byte[] getData(BufferedImage image){
		return ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
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
	public static void searchForParameter(File directory,File groundtruths,int[] sizes,double[] weights){
		System.out.println("Searching: "+directory);
		EfficientBinarizer efficientBinarizer=new EfficientBinarizer();
		int count=0;
		double[][][] measure=new double[sizes.length][weights.length][2];
		for(File file:directory.listFiles()){
			System.out.println(file);
			try{
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				File gtFile=new File(groundtruths,file.getName().substring(0,file.getName().lastIndexOf('.'))+".tiff");
				BufferedImage groundtruth=GrayscaleBinarizer.toGrayscale(ImageIO.read(gtFile));
				double bestF=0, bestFWeight=0;
				int bestFWindow=-1;
				double bestPsnr=0, bestPsnrWeight=0;
				int bestPsnrWindow=-1;
				for(int j=0;j<weights.length;j++){
					for(int i=0;i<sizes.length;i++){
						BufferedImage result=new NiblackBasedBinarizer(NiblackBasedBinarizer.getSauvolaFast(weights[j],getR(input)),efficientBinarizer,sizes[i]).binarize(input);
						double f=getF(result,groundtruth);
						double psnr=getPsnr(result,groundtruth);
						measure[i][j][0]+=f;
						measure[i][j][1]+=psnr;
//						System.out.println(sizes[i]+","+weights[j]+"\t"+f+"\t"+psnr);
						if(f>bestF){
							bestF=f;
							bestFWindow=sizes[i];
							bestFWeight=weights[j];
						}
						if(psnr>bestPsnr){
							bestPsnr=psnr;
							bestPsnrWindow=sizes[i];
							bestPsnrWeight=weights[j];
						}
					}
				}
				++count;
				System.out.println("F best: "+bestF+" at "+bestFWindow+","+bestFWeight);
				System.out.println("PSNR best: "+bestPsnr+" at "+bestPsnrWindow+","+bestPsnrWeight);
			}catch(IOException ex){
				Logger.getLogger(BinarizerTest.class.getName()).log(Level.SEVERE,null,ex);
			}
		}
		double bestF=0, bestFWeight=0;
		int bestFWindow=-1;
		double bestPsnr=0, bestPsnrWeight=0;
		int bestPsnrWindow=-1;
		System.out.println("Average:");
		for(int j=0;j<weights.length;j++){
			for(int i=0;i<sizes.length;i++){
				double f=measure[i][j][0]/count;
				double psnr=measure[i][j][1]/count;
				if(f>bestF){
					bestF=f;
					bestFWindow=sizes[i];
					bestFWeight=weights[j];
				}
				if(psnr>bestPsnr){
					bestPsnr=psnr;
					bestPsnrWindow=sizes[i];
					bestPsnrWeight=weights[j];
				}
				System.out.println(sizes[i]+","+weights[j]+"\t"+f+"\t"+psnr);
			}
		}
		System.out.println("F best: "+bestF+" at "+bestFWindow+","+bestFWeight);
		System.out.println("PSNR best: "+bestPsnr+" at "+bestPsnrWindow+","+bestPsnrWeight);
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
//		testQuality(new File("../datasets/binarization/2009/test"),new File("../datasets/binarization/2009/gt"),getStandardBinarizers());
//		testQuality(new File("../datasets/binarization/2010/test"),new File("../datasets/binarization/2010/gt"),getStandardBinarizers());
//		testQuality(new File("../datasets/binarization/2011/test"),new File("../datasets/binarization/2011/gt"),getStandardBinarizers());
//		searchForParameter(new File("../datasets/binarization/2018/test"),new File("../datasets/binarization/2018/gt"),getSequence(3,2,30),getSequence(0.0,0.02,40));
		searchForParameter(new File("../datasets/binarization/all/test"),new File("../datasets/binarization/all/gt"),getSequence(3,2,30),getSequence(0.0,0.02,40));
//		searchForParameter(new File("../datasets/binarization/2011/test"),new File("../datasets/binarization/2011/gt"),getSequence(3,2,30),getSequence(0.0,0.02,40));
//		searchForParameter(new File("../datasets/binarization/2010/test"),new File("../datasets/binarization/2010/gt"),new int[]{5,11,17,23,29,35},new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6});
//		searchForParameter(new File("../datasets/binarization/2011/test"),new File("../datasets/binarization/2011/gt"),new int[]{5,11,17,23,29,35},new double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6});
//		testFormulaEquality(new File("/home/kwong/projects/datasets/binarization/H_DIBCO2010_test_images"),32,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaFast(0.2));
	}
	private static int[] getSequence(int start,int step,int steps){
		return IntStream.range(0,steps).map((k)->k*step+start).toArray();
	}
	private static double[] getSequence(double start,double step,int steps){
		return IntStream.range(0,steps).mapToDouble((k)->k*step+start).toArray();
	}
	private static double getR(BufferedImage input){
		byte[] data=getData(input);
		int min=255, max=0;
		for(byte b:data){
			int bb=Byte.toUnsignedInt(b);
			if(bb>max){
				max=bb;
			}
			if(bb<min){
				min=bb;
			}
		}
		return (max-min)*0.5;
	}
}
