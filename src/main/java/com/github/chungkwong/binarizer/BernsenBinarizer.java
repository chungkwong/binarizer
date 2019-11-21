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
public class BernsenBinarizer extends AbstractBinarizer<Pair>{
	private final int minContrast, capacity;
	private final double weight, weight1;
	public BernsenBinarizer(int windowWidth,int windowHeight,double weight,int minContrast){
		super(windowWidth,windowHeight);
		this.capacity=Math.max(windowWidth,windowHeight)+1;
		this.weight=weight;
		this.weight1=1.0-weight;
		this.minContrast=minContrast;
	}
	@Override
	protected final Pair create(){
		return new Pair(capacity);
	}
	@Override
	protected final void add(Pair tmp,int pixel){
		tmp.addMax(pixel);
		tmp.addMin(pixel);
	}
	@Override
	protected final void remove(Pair tmp,int pixel){
		tmp.removeMax(pixel);
		tmp.removeMin(pixel);
	}
	@Override
	protected final void add(Pair tmpWindow,Pair tmpColumn){
		tmpWindow.addMax(tmpColumn.getMax());
		tmpWindow.addMin(tmpColumn.getMin());
	}
	@Override
	protected final void remove(Pair tmpWindow,Pair tmpColumn){
		tmpWindow.removeMax(tmpColumn.getMax());
		tmpWindow.removeMin(tmpColumn.getMin());
	}
	@Override
	protected final boolean isForeground(int pixel,Pair tmp){
		int max=tmp.getMax();
		int min=tmp.getMin();
		return max-min>=minContrast&&pixel<=weight*max+weight1*min;
	}
	public double getWeight(){
		return weight;
	}
	public int getMinimumContrast(){
		return minContrast;
	}
	@Override
	public String toString(){
		return "Bernsen("+getWindowWidth()+"x"+getWindowHeight()+","+weight+","+minContrast+")";
	}
}
class Pair{
	private final CyclicQueue maxQueue;
	private final CyclicQueue minQueue;
	public Pair(int capacity){
		this.minQueue=new CyclicQueue(capacity);
		this.maxQueue=new CyclicQueue(capacity);
	}
	public final void addMax(int pixel){
		while(!maxQueue.isEmpty()&&maxQueue.getLast()<pixel){
			maxQueue.removeLast();
		}
		maxQueue.addLast(pixel);
	}
	public final void removeMax(int pixel){
		if(maxQueue.getFirst()==pixel){
			maxQueue.removeFirst();
		}
	}
	public final void addMin(int pixel){
		while(!minQueue.isEmpty()&&minQueue.getLast()>pixel){
			minQueue.removeLast();
		}
		minQueue.addLast(pixel);
	}
	public final void removeMin(int pixel){
		if(minQueue.getFirst()==pixel){
			minQueue.removeFirst();
		}
	}
	public final int getMax(){
		return maxQueue.getFirst();
	}
	public final int getMin(){
		return minQueue.getFirst();
	}
	private static final class CyclicQueue{
		private final int[] queue;
		private final int capacity;
		private int indexStart=0, indexEnd;
		public CyclicQueue(int capacity){
			this.indexEnd=0;
			this.queue=new int[capacity+1];
			this.capacity=capacity;
			this.indexEnd=capacity;
		}
		public boolean isEmpty(){
			return indexEnd==indexStart-1||(indexStart==0&&indexEnd==capacity);
		}
		public int getFirst(){
			return queue[indexStart];
		}
		public void removeFirst(){
			if(indexStart==capacity){
				indexStart=0;
			}else{
				++indexStart;
			}
		}
		public int getLast(){
			return queue[indexEnd];
		}
		public void addLast(int pixel){
			if(indexEnd==capacity){
				indexEnd=0;
			}else{
				++indexEnd;
			}
			queue[indexEnd]=pixel;
		}
		public void removeLast(){
			if(indexEnd==0){
				indexEnd=capacity;
			}else{
				--indexEnd;
			}
		}
	}
}
