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
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class QualityTest{
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
	public static <T> void gridRecord(File directory,File groundtruths,File tsv,Function<T,Binarizer> factory,T[] parameters){
		try(BufferedWriter out=Files.newBufferedWriter(tsv.toPath())){
			out.append("File\tParameter\tF\tPSNR");
			for(File file:directory.listFiles()){
				System.out.println(file);
				BufferedImage input=GrayscaleBinarizer.toGrayscale(ImageIO.read(file));
				File gtFile=new File(groundtruths,file.getName().substring(0,file.getName().lastIndexOf('.'))+".tiff");
				BufferedImage groundtruth=GrayscaleBinarizer.toGrayscale(ImageIO.read(gtFile));
				for(T p:parameters){
					BufferedImage result=factory.apply(p).binarize(input);
					double f=getF(result,groundtruth);
					double psnr=getPsnr(result,groundtruth);
					out.newLine();
					if(p instanceof double[]){
						out.append(file.getName()+"\t"+Arrays.toString((double[])p)+"\t"+f+"\t"+psnr);
					}else{
						out.append(file.getName()+"\t"+p+"\t"+f+"\t"+psnr);
					}
				}
			}
		}catch(Exception ex){
			Logger.getLogger(BinarizerTest.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void gridTune(File tsv,String trainSet,String testSet,BiFunction<Double,Double,Double> measure,int limit){
		gridTest(tsv,testSet,gridSearch(tsv,trainSet,measure,limit));
	}
	public static List<String> gridSearch(File tsv,String regex,BiFunction<Double,Double,Double> measure,int limit){
		System.out.println("Searching:");
		List<String> result=new ArrayList<>();
		try{
			Iterator<String[]> iterator=Files.lines(tsv.toPath()).skip(1).map((line)->line.split("\\t")).filter((fields)->fields[0].matches(regex)).iterator();
			Map<String,double[]> sum=new HashMap<>();
			Set<String> images=new HashSet<>();
			while(iterator.hasNext()){
				String[] next=iterator.next();
				images.add(next[0]);
				double[] s=sum.get(next[1]);
				if(s==null){
					s=new double[]{0,0};
					sum.put(next[1],s);
				}
				s[0]+=Double.parseDouble(next[2]);
				s[1]+=Double.parseDouble(next[3]);
			}
			int count=images.size();
			for(Map.Entry<String,double[]> entry:sum.entrySet()){
				entry.getValue()[0]/=count;
				entry.getValue()[1]/=count;
			}
			Iterator<Map.Entry<String,double[]>> candidate=sum.entrySet().stream().sorted(Comparator.comparingDouble((Map.Entry<String,double[]> e)->-measure.apply(e.getValue()[0],e.getValue()[1]))).limit(limit).iterator();
			while(candidate.hasNext()){
				Map.Entry<String,double[]> e=candidate.next();
				System.out.println(e.getKey()+"\t"+e.getValue()[0]+"\t"+e.getValue()[1]);
				result.add(e.getKey());
			}
			System.out.println(images.size()+" processed");
		}catch(IOException ex){
			Logger.getLogger(QualityTest.class.getName()).log(Level.SEVERE,null,ex);
		}
		return result;
	}
	public static void gridTest(File tsv,String regex,List<String> candidate){
		System.out.println("Testing:");
		try{
			Iterator<String[]> iterator=Files.lines(tsv.toPath()).skip(1).
					map((line)->line.split("\\t")).
					filter((fields)->fields[0].matches(regex)&&candidate.contains(fields[1])).iterator();
			Map<String,double[]> sum=new HashMap<>();
			Set<String> images=new HashSet<>();
			while(iterator.hasNext()){
				String[] next=iterator.next();
				images.add(next[0]);
				double[] s=sum.get(next[1]);
				if(s==null){
					s=new double[]{0,0};
					sum.put(next[1],s);
				}
				s[0]+=Double.parseDouble(next[2]);
				s[1]+=Double.parseDouble(next[3]);
			}
			int count=images.size();
			for(Map.Entry<String,double[]> entry:sum.entrySet()){
				entry.getValue()[0]/=count;
				entry.getValue()[1]/=count;
			}
			for(String p:candidate){
				double[] s=sum.get(p);
				System.out.println(p+"\t"+s[0]+"\t"+s[1]);
			}
		}catch(IOException ex){
			Logger.getLogger(QualityTest.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args){
//		EfficientAlgorithm algorithm=new EfficientAlgorithm();
//		gridRecord(new File("../datasets/binarization/all/test"),
//				new File("../datasets/binarization/all/gt"),
//				new File("../datasets/binarization/sauvola"),
//				(p)->new NiblackBasedBinarizer(NiblackBasedBinarizer.getSauvola(p[0]),algorithm,(int)(p[1]+0.5)),
//				zip(BinarizerTest.getSequence(0.0,0.02,40),BinarizerTest.getSequence(3.0,2.0,30))
//		);
//		gridSearch(new File("../datasets/binarization/sauvola"),"2018_.*",(f,psnr)->f*100+psnr*4,50);
//		gridTune(new File("../datasets/binarization/sauvola"),"2016_.*","2018_.*",(f,psnr)->f*100+psnr*4,50);
//		gridTune(new File("../datasets/binarization/sauvola"),"(2009_H|2010_H|2011_HW|2012_H|2013_HW|2014_H|2016_|2017_[0-9]\\.|2017_10).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
////		gridTune(new File("../datasets/binarization/sauvola"),"(2009_H|2010_H|2011_HW|2012_H|2013_HW|2014_H|2016_|2017_[0-9]\\.|2017_10|2018_).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
//		gridTune(new File("../datasets/binarization/sauvola"),"(2009_P|2011_PR|2013_PR|2017_1[1-9]|2017_20).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
//		gridTune(new File("../datasets/binarization/sauvola"),"...[^8].*","2018_.*",(f,psnr)->f*100+psnr*4,100);
		gridRecord(new File("../datasets/binarization/all/test"),
				new File("../datasets/binarization/all/gt"),
				new File("../datasets/binarization/bernsen"),
				(p)->new BernsenBinarizer((int)(p[0]+0.5),(int)(p[0]+0.5),p[1],(int)(p[2]+0.5)),
				zip(BinarizerTest.getSequence(3.0,2.0,15),BinarizerTest.getSequence(0.4,0.05,5),BinarizerTest.getSequence(8.0,8,31))
		);
		gridSearch(new File("../datasets/binarization/bernsen"),"2018_.*",(f,psnr)->f*100+psnr*4,50);
		gridTune(new File("../datasets/binarization/bernsen"),"2016_.*","2018_.*",(f,psnr)->f*100+psnr*4,50);
		gridTune(new File("../datasets/binarization/bernsen"),"(2009_H|2010_H|2011_HW|2012_H|2013_HW|2014_H|2016_|2017_[0-9]\\.|2017_10).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
//		gridTune(new File("../datasets/binarization/bernsen"),"(2009_H|2010_H|2011_HW|2012_H|2013_HW|2014_H|2016_|2017_[0-9]\\.|2017_10|2018_).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
		gridTune(new File("../datasets/binarization/bernsen"),"(2009_P|2011_PR|2013_PR|2017_1[1-9]|2017_20).*","2018_.*",(f,psnr)->f*100+psnr*4,100);
		gridTune(new File("../datasets/binarization/bernsen"),"...[^8].*","2018_.*",(f,psnr)->f*100+psnr*4,100);
	}
	private static double[][] zip(double[]... sequences){
		int length=1;
		for(double[] sequence:sequences){
			length*=sequence.length;
		}
		int dim=sequences.length;
		int[] status=new int[dim];
		status[dim-1]=-1;
		double[][] product=new double[length][dim];
		for(int i=0;i<length;i++){
			int k=dim-1;
			++status[k];
			while(status[k]==sequences[k].length){
				status[k]=0;
				--k;
				++status[k];
			}
			for(int j=0;j<dim;j++){
				product[i][j]=sequences[j][status[j]];
			}
		}
		return product;
	}
}
