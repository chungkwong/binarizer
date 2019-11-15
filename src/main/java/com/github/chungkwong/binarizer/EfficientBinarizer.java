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
public class EfficientBinarizer implements NiblackBasedBinarizer.ThresholdAlgorithm{
	@Override
	public void binarize(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		int[] integral=new int[width+1];
		int[] integralSquare=new int[width+1];
		int l=(windowWidth+1)/2, r=windowWidth/2;
		int o=(windowHeight+1)/2, u=windowHeight/2;
		for(int i=0, ind=0, imax=Math.min(height,u);i<imax;i++){
			for(int j=1;j<=width;j++,ind++){
				int pixel=(from[ind])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr1=Math.min(r,width);
		int dr2=Math.max(width-r+1,1);
		for(int i=0, ind=0;i<height;i++){
			int winTop=Math.max(i-o,-1), winBottom=Math.min(height-1,i+u);
			if(i>=l){
				for(int j=1, index=winTop*width;j<=width;j++,index++){
					int pixel=from[index]&0xFF;
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}
			if(i+r<height){
				for(int j=1, index=winBottom*width;j<=width;j++,index++){
					int pixel=(from[index])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}
			int sum=0;
			int squareSum=0;
			for(int j=1;j<=dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			for(int j=1;j<=width-r;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=j+r;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum+=integral[winRight]-integral[winLeft];
				squareSum+=integralSquare[winRight]-integralSquare[winLeft];
				to[ind]=formula.isForeground(from[ind]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
			for(int j=dr2;j<=width;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=width;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum-=integral[winLeft];
				squareSum-=integralSquare[winLeft];
				to[ind]=formula.isForeground(from[ind]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
		}
	}
	@Override
	public String toString(){
		return "Chan";
	}
}
