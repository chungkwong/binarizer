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
/**
 *
 * @author Chan Chung Kwong
 */
public class NaiveBernsenBinarizer extends GrayscaleBinarizer{
	private final int windowWidth, windowHeight;
	private final double weight, weight1;
	private final int minContrast;
	public NaiveBernsenBinarizer(int windowWidth,int windowHeight,double weight,int minContrast){
		this.windowWidth=windowWidth;
		this.windowHeight=windowHeight;
		this.weight=weight;
		this.weight1=1.0-weight;
		this.minContrast=minContrast;
	}
	@Override
	protected void binarize(byte[] from,byte[] to,int width,int height){
		int r=windowWidth/2+1;
		int l=-(windowWidth+1)/2+1;
		int u=windowHeight/2+1;
		int o=-(windowHeight+1)/2+1;
		double weight1=1.0-weight;
		for(int i=0, index=0;i<height;i++){
			int top=Math.max(i+o,0);
			int bottom=Math.min(i+u,height);
			for(int j=0;j<width;j++,index++){
				int min=256, max=0;
				int left=Math.max(j+l,0);
				int right=Math.min(j+r,width);
				for(int m=top, rowStart=top*width;m<bottom;m++,rowStart+=width){
					for(int n=left, pos=rowStart+left;n<right;n++,pos++){
						int pixel=from[pos]&0xFF;
						if(pixel>max){
							max=pixel;
						}
						if(pixel<min){
							min=pixel;
						}
					}
				}
				to[index]=max-min>=minContrast&&(from[index]&0xFF)<=weight*max+weight1*min?0x00:(byte)0xFF;
			}
		}
	}
	public double getWeight(){
		return weight;
	}
	public int getMinimumContrast(){
		return minContrast;
	}
	public int getWindowWidth(){
		return windowWidth;
	}
	public int getWindowHeight(){
		return windowHeight;
	}
	@Override
	public String toString(){
		return "Naive Bernsen("+getWindowWidth()+"x"+getWindowHeight()+","+weight+","+minContrast+")";
	}
}
