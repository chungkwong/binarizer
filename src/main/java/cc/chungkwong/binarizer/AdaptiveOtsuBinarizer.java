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
public class AdaptiveOtsuBinarizer extends AbstractBinarizer<int[]>{
	public AdaptiveOtsuBinarizer(int windowWidth,int windowHeight){
		super(windowWidth,windowHeight);
	}
	@Override
	protected int[] create(){
		return new int[256];
	}
	@Override
	protected void add(int[] tmp,int pixel){
		++tmp[pixel];
	}
	@Override
	protected void remove(int[] tmp,int pixel){
		--tmp[pixel];
	}
	@Override
	protected void add(int[] tmpWindow,int[] tmpColumn){
		for(int i=0;i<256;i++){
			tmpWindow[i]+=tmpColumn[i];
		}
	}
	@Override
	protected void remove(int[] tmpWindow,int[] tmpColumn){
		for(int i=0;i<256;i++){
			tmpWindow[i]-=tmpColumn[i];
		}
	}
	@Override
	protected boolean isForeground(int pixel,int[] histogram){
		double except=0, uT=0, bestVar=0;
		int acc=0, bestValue=0, total=0;
		for(int i=0;i<256;i++){
			total+=histogram[i];
			uT+=i*histogram[i];
		}
		for(int t=0;t<255;t++){
			acc+=histogram[t];
			except+=histogram[t]*t;
			if(acc==0){
				continue;
			}
			if(total==acc){
				break;
			}
			double u0=except/acc;
			double u1=(uT-except)/(total-acc);
			double var=(u0-u1)*(u0-u1)*acc*(total-acc);
			if(var>bestVar){
				bestVar=var;
				bestValue=t;
			}
		}
		return pixel<=bestValue;
	}
}
