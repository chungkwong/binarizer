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
public abstract class GlobalBinarizer extends GrayscaleBinarizer{
	public GlobalBinarizer(){
	}
	public GlobalBinarizer(boolean linear){
		super(linear);
	}
	@Override
	protected void binarize(byte[] from,byte[] to,int width,int height){
		int len=width*height;
		int threshold=getThreshold(from,width,height);
		for(int i=0;i<len;i++){
			to[i]=(from[i]&0xFF)<=threshold?0x00:(byte)0xFF;
		}
	}
	protected abstract int getThreshold(byte[] pixels,int width,int height);
}
