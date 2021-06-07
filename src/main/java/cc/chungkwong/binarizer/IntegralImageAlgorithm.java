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
public class IntegralImageAlgorithm implements NiblackBasedBinarizer.ThresholdAlgorithm{
	@Override
	public void binarize(byte[] from,byte[] to,int width,int height,int windowWidth,int windowHeight,NiblackBasedBinarizer.ThresholdFormula formula){
		long[][] intImg=getIntegralImage(from,width,height);
		long[][] sqIntImg=getSquaredIntegralImage(from,width,height);
		int r=windowWidth/2+1;
		int l=-(windowWidth+1)/2+1;
		int u=windowHeight/2+1;
		int o=-(windowHeight+1)/2+1;
		for(int i=0, index=0;i<height;i++){
			int top=Math.max(i+o,0);
			int bottom=Math.min(i+u,height);
			for(int j=0;j<width;j++,index++){
				int left=Math.max(j+l,0);
				int right=Math.min(j+r,width);
				long sum=windowValue(intImg,width,height,left,right,top,bottom);
				long squareSum=windowValue(sqIntImg,width,height,left,right,top,bottom);
				int count=(right-left)*(bottom-top);
				to[index]=formula.isForeground(from[index]&0xFF,sum,squareSum,count)?(byte)0x00:(byte)0xff;
			}
		}
	}
	/**
	 * Create a integral image
	 *
	 * @param	pixels the input grayscale picture
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @return integral image
	 */
	public static final long[][] getIntegralImage(byte[] pixels,int width,int height){
		long[][] intImg=new long[height+1][width+1];
		for(int i=1, ind=0;i<=height;i++){
			long found=0;
			for(int j=1;j<=width;j++,ind++){
				found+=(pixels[ind]&0xFF);
				intImg[i][j]=intImg[i-1][j]+found;
			}
		}
		return intImg;
	}
	/**
	 * Create a squared integral image
	 *
	 * @param	pixels the input grayscale picture
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @return squared integral image
	 */
	public static final long[][] getSquaredIntegralImage(byte[] pixels,int width,int height){
		long[][] intImg=new long[height+1][width+1];
		for(int i=1, ind=0;i<=height;i++){
			long found=0;
			for(int j=1;j<=width;j++,ind++){
				found+=square((pixels[ind]&0xFF));
				intImg[i][j]=intImg[i-1][j]+found;
			}
		}
		return intImg;
	}
	/**
	 * Compute square of a integer
	 *
	 * @param	k the number to be squared
	 * @return square of k
	 */
	private static int square(int k){
		return k*k;
	}
	/**
	 * Compute the sum of pixels in a window
	 *
	 * @param intImg the integral image
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return the sum
	 */
	public static final long windowValue(long[][] intImg,int width,int height,int left,int right,int top,int bottom){
		return intImg[bottom][right]-intImg[bottom][left]-intImg[top][right]+intImg[top][left];
	}
	@Override
	public String toString(){
		return "Shafait";
	}
}
