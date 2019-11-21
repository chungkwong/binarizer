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
import static cc.chungkwong.binarizer.BinarizerTest.getSequence;
import com.github.chungkwong.binarizer.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class CorrectnessTest{
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
	public static void testBernsenImplementationsEquality(File directory){
//		int[] sizes=getSequence(1,2,25);
		int[] sizes=getSequence(1,2,10);
		double[] weights=getSequence(0.4,0.1,3);
		testEquality(directory,IntStream.of(sizes).mapToObj((window)
				->DoubleStream.of(weights).mapToObj((weight)->new Binarizer[]{
			new NaiveBernsenBinarizer(window,window,weight,80),
			new BernsenBinarizer(window,window,weight,80)
		})).flatMap((s)->s).toArray(Binarizer[][]::new));
	}
	public static void testFormulaEquality(File directory,int window,NiblackBasedBinarizer.ThresholdFormula... formulas){
		testEquality(directory,Arrays.stream(formulas).map((f)->new NiblackBasedBinarizer(f,new EfficientAlgorithm(),window)).toArray(Binarizer[]::new));
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
	public static void main(String[] args){
//		testNiblackImplementationsEquality(new File("../datasets/binarization/all/test"));
//		testFormulaEquality(new File("../datasets/binarization/all/test"),25,NiblackBasedBinarizer.getNiblack(0.2),NiblackBasedBinarizer.getNiblackLegacy(0.2));
//		testFormulaEquality(new File("../datasets/binarization/all/test"),25,NiblackBasedBinarizer.getSauvola(0.2),NiblackBasedBinarizer.getSauvolaLegacy(0.2));
//		testBernsenImplementationsEquality(new File("../datasets/binarization/all/test"));
	}
}
