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
	@Override
	public BufferedImage binarize(BufferedImage input){
		int width=input.getWidth();
		int height=input.getHeight();
		byte[] from=((DataBufferByte)toGrayscale(input).getRaster().getDataBuffer()).getData();
		BufferedImage output=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		byte[] to=((DataBufferByte)output.getRaster().getDataBuffer()).getData();
		binarize(from,to,width,height);
		return output;
	}
	public static BufferedImage toGrayscale(BufferedImage input){
		BufferedImage gray;
		if(input.getType()==BufferedImage.TYPE_BYTE_GRAY){
			gray=input;
		}else{
			gray=new BufferedImage(input.getWidth(),input.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
			TO_GRAYSCALE.filter(input,gray);
		}
		return gray;
	}
	protected abstract void binarize(byte[] from,byte[] to,int width,int height);
}
