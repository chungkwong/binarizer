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
public class SpeedTest{
	public static void testSpeed(File directory){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		for(int window:BinarizerTest.getSequence(1,2,16)){
			NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvola(0.4);
			NiblackBasedBinarizer.ThresholdFormula formulaLagacy=NiblackBasedBinarizer.getSauvolaLegacy(0.4);
			List<Binarizer> binarizers=new ArrayList<>();
//			binarizers.add(new NiblackBasedBinarizer(formulaLagacy,new NaiveAlgorithm(),window));
//			binarizers.add(new NiblackBasedBinarizer(formulaLagacy,new IntegralImageAlgorithm(),window));
//			binarizers.add(new NiblackBasedBinarizer(formula,new IntegralImageAlgorithm(),window));
//			binarizers.add(new NiblackBasedBinarizer(formulaLagacy,new EfficientAlgorithm(),window));
//			binarizers.add(new NiblackBasedBinarizer(formula,new EfficientAlgorithm(),window));
			binarizers.add(new NaiveBernsenBinarizer(window,window,0.5,80));
			binarizers.add(new BernsenBinarizer(window,window,0.5,80));
			testSpeed(directory,binarizers.toArray(Binarizer[]::new));
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
	public static void main(String[] args){
		testSpeed(new File("../datasets/binarization/all/test"));
	}
}
