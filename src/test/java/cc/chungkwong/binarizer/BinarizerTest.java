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
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class BinarizerTest{
	public static Binarizer[] getStandardBinarizers(){
		FixedBinarizer fixed=new FixedBinarizer(128);
		OtsuBinarizer otsu=new OtsuBinarizer();
		int window=25;
		double k=0.4;
		NiblackBasedBinarizer.ThresholdFormula formula=NiblackBasedBinarizer.getSauvola(k);
		NiblackBasedBinarizer sauvola=new NiblackBasedBinarizer(formula,new EfficientAlgorithm(),window);
		BernsenBinarizer bernsen=new BernsenBinarizer(window,window,0.5,25);
		return new Binarizer[]{fixed,otsu,sauvola,bernsen};
	}
	static int[] getSequence(int start,int step,int steps){
		return IntStream.range(0,steps).map((k)->k*step+start).toArray();
	}
	static double[] getSequence(double start,double step,int steps){
		return IntStream.range(0,steps).mapToDouble((k)->k*step+start).toArray();
	}
}
