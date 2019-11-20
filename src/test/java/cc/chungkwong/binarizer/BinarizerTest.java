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
		testEquality(directory,new Binarizer[][]{binarizers});
	}
	public static void testEquality(File directory,Binarizer[][] binarizerss){
		System.out.println("Testing correctness: "+directory);
		for(File file:directory.listFiles()){
			try{
				System.out.println(file.getName());
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				for(Binarizer[] binarizers:binarizerss){
					byte[] last=null;
					for(Binarizer binarizer:binarizers){
						byte[] curr=((DataBufferByte)binarizer.binarize(input).getRaster().getDataBuffer()).getData();
						if(last!=null){
							System.out.print(Arrays.equals(curr,last)+"\t");
							for(int i=0;i<curr.length;i++){
								if(curr[i]!=last[i]){
									System.out.println(i+":"+i/input.getWidth()+","+i%input.getWidth());
								}
							}
						}else{
							last=curr;
						}
						System.out.print(binarizer+"\t");
					}
					System.out.println();
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
				++count;
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
	public static void testNiblackImplementationsEquality(File directory){
//		int[] sizes=getSequence(1,2,25);
		int[] sizes=getSequence(1,4,10);
		double[] weights=getSequence(0.2,0.1,4);
		testEquality(directory,IntStream.of(sizes).mapToObj((window)
				->DoubleStream.of(weights).mapToObj((weight)->NiblackBasedBinarizer.getSauvola(weight)).map((formula)->new Binarizer[]{
			new NiblackBasedBinarizer(formula,new NaiveAlgorithm(),window),
			new NiblackBasedBinarizer(formula,new IntegralImageAlgorithm(),window),
			new NiblackBasedBinarizer(formula,new EfficientAlgorithm(),window)
		})).flatMap((s)->s).toArray(Binarizer[][]::new));
	}
	public static void testFormulaEquality(File directory,int window,NiblackBasedBinarizer.ThresholdFormula... formulas){
		testEquality(directory,Arrays.stream(formulas).map((f)->new NiblackBasedBinarizer(f,new EfficientAlgorithm(),window)).toArray(Binarizer[]::new));
	}
	public static void testSpeed(File directory){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		for(int window:getSequence(1,2,16)){
			NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvola(0.4);
			NiblackBasedBinarizer.ThresholdFormula formulaLagacy=NiblackBasedBinarizer.getSauvolaLegacy(0.4);
			NiblackBasedBinarizer naive=new NiblackBasedBinarizer(formulaLagacy,new NaiveAlgorithm(),window);
			NiblackBasedBinarizer integral=new NiblackBasedBinarizer(formulaLagacy,new IntegralImageAlgorithm(),window);
			NiblackBasedBinarizer integral2=new NiblackBasedBinarizer(formula,new IntegralImageAlgorithm(),window);
			NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formulaLagacy,new EfficientAlgorithm(),window);
			NiblackBasedBinarizer effient2=new NiblackBasedBinarizer(formula,new EfficientAlgorithm(),window);
			NaiveBernsenBinarizer bernsen=new NaiveBernsenBinarizer(window,window,0.5);
			BernsenBinarizer bernsen2=new BernsenBinarizer(13,13,0.5,80);
			testSpeed(directory,fixed,otsu,naive,integral,integral2,effient,effient2,bernsen,bernsen2);
		}
	}
	public static Binarizer[] getStandardBinarizers(){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		int window=25;
		double k=0.4;
		NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvola(k);
		NiblackBasedBinarizer effient=new NiblackBasedBinarizer(formula,new EfficientAlgorithm(),window);
		BernsenBinarizer bernsen=new BernsenBinarizer(window,window,0.5,25);
		return new Binarizer[]{fixed,otsu,effient,bernsen};
	}
	public static void searchForParameter(File directory,File groundtruths,int[] sizes,double[] weights){
		System.out.println("Searching: "+directory);
		EfficientAlgorithm efficientBinarizer=new EfficientAlgorithm();
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
						BufferedImage result=new NiblackBasedBinarizer(NiblackBasedBinarizer.getSauvola(weights[j]),efficientBinarizer,sizes[i]).binarize(input);
//						BufferedImage result=new BernsenBinarizer(sizes[i],sizes[i],weights[j],80).binarize(input);
//						BufferedImage result=new BernsenBinarizer(15,15,weights[j],sizes[i]).binarize(input);
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
//		testEquality(new File("../datasets/binarization/2018/test"),new NaiveBernsenBinarizer(15,15,0.5),new BernsenBinarizer2(15,15,0.5));
//		testFormulaEquality(new File("../datasets/binarization/all/test"),25,NiblackBasedBinarizer.getNiblack(0.2),NiblackBasedBinarizer.getNiblackLegacy(0.2));
//		testFormulaEquality(new File("../datasets/binarization/all/test"),32,NiblackBasedBinarizer.getSauvola(0.5),NiblackBasedBinarizer.getSauvolaLegacy(0.5));
//		testNiblackImplementationsEquality(new File("../datasets/binarization/all/test"));
//		testSpeed(new File("../datasets/binarization/all/test"));
//		testSpeed(new File("../datasets/binarization/2016/test"));
//		testQuality(new File("../datasets/binarization/2018/test"),new File("../datasets/binarization/2018/gt"),getStandardBinarizers());
//		testQuality(new File("../datasets/binarization/2010/test"),new File("../datasets/binarization/2010/gt"),getStandardBinarizers());
//		testQuality(new File("../datasets/binarization/2011/test"),new File("../datasets/binarization/2011/gt"),getStandardBinarizers());
//		searchForParameter(new File("../datasets/binarization/2018/test"),new File("../datasets/binarization/2018/gt"),getSequence(3,2,30),getSequence(0.0,0.02,40));
//		searchForParameter(new File("../datasets/binarization/2018/test"),new File("../datasets/binarization/2018/gt"),getSequence(3,2,15),getSequence(0.0,0.1,11));
//		searchForParameter(new File("../datasets/binarization/2018/test"),new File("../datasets/binarization/2018/gt"),getSequence(0,8,31),getSequence(0.5,0.1,1));
//		searchForParameter(new File("../datasets/binarization/all/test"),new File("../datasets/binarization/all/gt"),getSequence(3,2,20),getSequence(0.15,0.01,40));
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
}
