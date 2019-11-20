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
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class AbstractBinarizer<T> extends GrayscaleBinarizer{
	private final int windowWidth, windowHeight;
	public AbstractBinarizer(int windowWidth,int windowHeight){
		this.windowWidth=windowWidth;
		this.windowHeight=windowHeight;
	}
	@Override
	protected void binarize(byte[] from,byte[] to,int width,int height){
		List<T> integral=IntStream.range(0,width).mapToObj((i)->create()).collect(Collectors.toList());
		int l=(windowWidth+1)/2, r=windowWidth/2;
		int o=(windowHeight+1)/2, u=windowHeight/2;
		for(int i=0, index=0, imax=Math.min(height,u);i<imax;i++){
			for(int j=0;j<width;j++,index++){
				int pixel=(from[index])&0xFF;
				add(integral.get(j),pixel);
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
					remove(integral.get(j),pixel);
				}
			}else{
				winTop=-1;
			}
			if(winBottom<height){
				for(int j=0;j<width;j++,indexBottom++){
					int pixel=(from[indexBottom])&0xFF;
					add(integral.get(j),pixel);
				}
			}else{
				winBottom=height-1;
			}
			int winHeight=winBottom-winTop;
			T sum=create();
			for(int j=0;j<dr1;j++){
				add(sum,integral.get(j));
			}
			int winRight=r;
			for(;winRight<dr0;winRight++,index++){
				add(sum,integral.get(winRight));
				to[index]=isForeground(from[index]&0xFF,sum)?(byte)0x00:(byte)0xff;
			}
			int winLeft=winRight-windowWidth;
			if(winRight>=width){
				for(;winLeft<dr3;winLeft++,index++){
					to[index]=isForeground(from[index]&0xFF,sum)?(byte)0x00:(byte)0xff;
				}
			}else{
				for(;winRight<width;winLeft++,winRight++,index++){
					add(sum,integral.get(winRight));
					remove(sum,integral.get(winLeft));
					to[index]=isForeground(from[index]&0xFF,sum)?(byte)0x00:(byte)0xff;
				}
			}
			for(;winLeft<dr2;winLeft++,index++){
				remove(sum,integral.get(winLeft));
				to[index]=isForeground(from[index]&0xFF,sum)?(byte)0x00:(byte)0xff;
			}
		}
	}
	protected abstract T create();
	protected abstract void add(T tmp,int pixel);
	protected abstract void remove(T tmp,int pixel);
	protected abstract void add(T tmpWindow,T tmpColumn);
	protected abstract void remove(T tmpWindow,T tmpColumn);
	protected abstract boolean isForeground(int pixel,T tmp);
}
