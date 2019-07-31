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
package com.github.chungkwong.binarizer;
/**
 *
 * @author Chan Chung Kwong
 */
public class NiblackBasedBinarizer extends GrayscaleBinarizer{
	private final ThresholdFormula formula;
	private final ThresholdAlgorithm algorithm;
	private final int windowWidth, windowHeight;
	public NiblackBasedBinarizer(ThresholdFormula formula,ThresholdAlgorithm algorithm,int windowSide){
		this(formula,algorithm,windowSide,windowSide);
	}
	public NiblackBasedBinarizer(ThresholdFormula formula,ThresholdAlgorithm algorithm,int windowWidth,int windowHeight){
		this.formula=formula;
		this.windowHeight=windowHeight;
		this.windowWidth=windowWidth;
		this.algorithm=algorithm;
	}
	public static ThresholdFormula getSauvola(double k){
		return (pixel,sum,squareSum,count)->{
			double mean=((double)sum)/count;
			double variance=((double)squareSum)/count-mean*mean;
			return pixel<=mean*(1+k*(Math.sqrt(variance)/128-1));
		};
	}
	public static ThresholdFormula getSauvolaFast(double k){
		double k2=k*k/128/128;
		if(k>=0){
			return (pixel,sum,squareSum,count)->{
				double mean=((double)sum)/count;
				double variance=((double)squareSum)/count-mean*mean;
				double tmp=(pixel+mean*(k-1));
				return tmp<=0||tmp*tmp<=mean*mean*k2*variance;
			};
		}else{
			return (pixel,sum,squareSum,count)->{
				double mean=((double)sum)/count;
				double variance=((double)squareSum)/count-mean*mean;
				double tmp=(pixel+mean*(k-1));
				return tmp<=0&&tmp*tmp>=mean*mean*k2*variance;
			};
		}
	}
	public static ThresholdFormula getNiblack(double k){
		return (pixel,sum,squareSum,count)->{
			double mean=((double)sum)/count;
			double variance=((double)squareSum)/count-mean*mean;
			return pixel<=mean+k*Math.sqrt(variance);
		};
	}
	@Override
	protected void binarize(byte[] from,byte[] to,int width,int height){
		algorithm.binarize(from,to,width,height,windowWidth,windowHeight,formula);
	}
	public static interface ThresholdFormula{
		boolean isForeground(int pixel,long sum,long squareSum,int count);
	}
	public static interface ThresholdAlgorithm{
		void binarize(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,ThresholdFormula formula);
	}
}
