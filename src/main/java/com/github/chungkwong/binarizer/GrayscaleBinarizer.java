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
import java.awt.*;
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class GrayscaleBinarizer implements Binarizer{
	private static final ColorConvertOp TO_GRAYSCALE;
	static{
		Map<RenderingHints.Key,Object> config=new HashMap<>();
		config.put(RenderingHints.KEY_DITHERING,RenderingHints.VALUE_DITHER_DISABLE);
		config.put(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED);
		TO_GRAYSCALE=new ColorConvertOp(new RenderingHints(config));
	}
	private final boolean linear;
	public GrayscaleBinarizer(){
		this(true);
	}
	public GrayscaleBinarizer(boolean linear){
		this.linear=linear;
	}
	@Override
	public BufferedImage binarize(BufferedImage input){
		int width=input.getWidth();
		int height=input.getHeight();
		byte[] from=((DataBufferByte)toGrayscale(input,linear).getRaster().getDataBuffer()).getData();
		BufferedImage output=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		byte[] to=((DataBufferByte)output.getRaster().getDataBuffer()).getData();
		binarize(from,to,width,height);
		return output;
	}
	public static BufferedImage toGrayscale(BufferedImage input){
		return toGrayscale(input,true);
	}
	public static BufferedImage toGrayscale(BufferedImage input,boolean linear){
		BufferedImage gray;
		if(input.getType()==BufferedImage.TYPE_BYTE_GRAY){
			gray=input;
		}else{
			gray=new BufferedImage(input.getWidth(),input.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
			if(linear){
				TO_GRAYSCALE.filter(input,gray);
			}else{
				byte[] to=((DataBufferByte)gray.getRaster().getDataBuffer()).getData();
				int wR=316, wG=624, wB=84, divisor=wR+wG+wB;
				for(int i=0, ind=0;i<input.getHeight();i++){
					for(int j=0;j<input.getWidth();j++,ind++){
						int rgb=input.getRGB(j,i);
						int alpha=(rgb>>>24)&0xff, red=(rgb>>>16)&0xff, green=(rgb>>>8)&0xff, blue=rgb&0xff;
						to[ind]=(byte)(255-(255-(red*wR+green*wG+blue*wB)/divisor)*alpha/255);
					}
				}
			}
		}
		return gray;
	}
	protected abstract void binarize(byte[] from,byte[] to,int width,int height);
}
