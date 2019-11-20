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
public class EfficientAlgorithm implements NiblackBasedBinarizer.ThresholdAlgorithm{
	@Override
	public void binarize(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		//if(height>=width){
		binarizeTall(from,to,width,height,windowWidth,windowHeight,formula);
		//}else{
		//	binarizeFat(from,to,width,height,windowWidth,windowHeight,formula);
		//}
	}
	public void binarizeTall(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		int[] integral=new int[width];
		int[] integralSquare=new int[width];
		int l=(windowWidth+1)/2, r=windowWidth/2;
		int o=(windowHeight+1)/2, u=windowHeight/2;
		for(int i=0, index=0, imax=Math.min(height,u);i<imax;i++){
			for(int j=0;j<width;j++,index++){
				int pixel=(from[index])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr0=Math.min(windowWidth,width);
		int dr1=Math.min(r,width);
		int dr2=width-l;
		int dr3=Math.min(dr2,0);
		int indexTop=0, indexBottom=u*width;
		for(int i=0, index=0;i<height;i++){
			int winTop=i-o, winBottom=i+u;
			if(winTop>=0){
				for(int j=0;j<width;j++,indexTop++){
					int pixel=from[indexTop]&0xFF;
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}else{
				winTop=-1;
			}
			if(winBottom<height){
				for(int j=0;j<width;j++,indexBottom++){
					int pixel=(from[indexBottom])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}else{
				winBottom=height-1;
			}
			int winHeight=winBottom-winTop;
			int sum=0;
			long squareSum=0;
			for(int j=0;j<dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			int count=dr1*winHeight;
			int winRight=r;
			for(;winRight<dr0;winRight++,index++){
				count+=winHeight;
				sum+=integral[winRight];
				squareSum+=integralSquare[winRight];
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
			int winLeft=winRight-windowWidth;
			if(winRight>=width){
				for(;winLeft<dr3;winLeft++,index++){
					to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
				}
			}else{
				for(;winRight<width;winLeft++,winRight++,index++){
					sum+=integral[winRight]-integral[winLeft];
					squareSum+=integralSquare[winRight]-integralSquare[winLeft];
					to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
				}
			}
			for(;winLeft<dr2;winLeft++,index++){
				count-=winHeight;
				sum-=integral[winLeft];
				squareSum-=integralSquare[winLeft];
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
		}
	}
	public void binarizeFat(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		int[] integral=new int[height];
		int[] integralSquare=new int[height];
		int o=(windowHeight+1)/2, u=windowHeight/2;
		int l=(windowWidth+1)/2, r=windowWidth/2;
		for(int i=0, imax=Math.min(width,r);i<imax;i++){
			for(int j=0, index=i;j<height;j++,index+=width){
				int pixel=(from[index])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr0=Math.min(windowHeight,height);
		int dr1=Math.min(u,height);
		int dr2=height-o;
		int dr3=Math.min(dr2,0);
		for(int i=0;i<width;i++){
			int winLeft=i-l, winRight=i+r;
			if(winLeft>=0){
				for(int j=0, indexLeft=winLeft;j<height;j++,indexLeft+=width){
					int pixel=from[indexLeft]&0xFF;
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}else{
				winLeft=-1;
			}
			if(winRight<width){
				for(int j=0, indexRight=winRight;j<height;j++,indexRight+=width){
					int pixel=(from[indexRight])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}else{
				winRight=width-1;
			}
			int winWidth=winRight-winLeft;
			int sum=0;
			long squareSum=0;
			for(int j=0;j<dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			int count=dr1*winWidth;
			int winBottom=u;
			int index=i;
			for(;winBottom<dr0;winBottom++,index+=width){
				count+=winWidth;
				sum+=integral[winBottom];
				squareSum+=integralSquare[winBottom];
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
			int winTop=winBottom-windowHeight;
			if(winBottom>=height){
				for(;winTop<dr3;winTop++,index+=width){
					to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
				}
			}else{
				for(;winBottom<height;winTop++,winBottom++,index+=width){
					sum+=integral[winBottom]-integral[winTop];
					squareSum+=integralSquare[winBottom]-integralSquare[winTop];
					to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
				}
			}
			for(;winTop<dr2;winTop++,index+=width){
				count-=winWidth;
				sum-=integral[winTop];
				squareSum-=integralSquare[winTop];
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
		}
	}
	@Override
	public String toString(){
		return "Chan";
	}
}
