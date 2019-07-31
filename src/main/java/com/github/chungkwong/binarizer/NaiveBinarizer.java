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
public class NaiveBinarizer implements NiblackBasedBinarizer.ThresholdAlgorithm{
	@Override
	public void binarize(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		int r=windowWidth/2+1;
		int l=-(windowWidth+1)/2+1;
		int u=windowHeight/2+1;
		int o=-(windowHeight+1)/2+1;
		for(int i=0, index=0;i<height;i++){
			int top=Math.max(i+o,0);
			int bottom=Math.min(i+u,height);
			for(int j=0;j<width;j++,index++){
				long sum=0;
				long squareSum=0;
				int left=Math.max(j+l,0);
				int right=Math.min(j+r,width);
				int count=(right-left)*(bottom-top);
				for(int m=top, rowStart=top*width;m<bottom;m++,rowStart+=width){
					for(int n=left, pos=rowStart+left;n<right;n++,pos++){
						int pixel=from[pos]&0xFF;
						sum+=pixel;
						squareSum+=pixel*pixel;
					}
				}
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?0x00:(byte)0xFF;
			}
		}
	}
}
