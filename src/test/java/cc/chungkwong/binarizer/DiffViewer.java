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
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class DiffViewer extends JFrame{
	private final JTabbedPane tabs=new JTabbedPane();
	private final File inputDir=new File(System.getProperty("user.home"),"datasets/binarization/all/test");
	private final File groundTruthDir=new File(System.getProperty("user.home"),"datasets/binarization/all/gt");
	public DiffViewer() throws HeadlessException{
		setTitle("Binarize");
		setJMenuBar(createMenuBar());
		getContentPane().add(tabs);
		addBinarizeTab();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	private JMenuBar createMenuBar(){
		JMenuBar bar=new JMenuBar();
		JMenu file=new JMenu("File");
		JMenuItem binarize=new JMenuItem("Binarize");
		binarize.addActionListener((e)->addBinarizeTab());
		file.add(binarize);
		bar.add(file);
		return bar;
	}
	private void addBinarizeTab(){
		JPanel output=new JPanel(new BorderLayout());
		JLabel viewer=new JLabel();
		output.add(new JScrollPane(viewer),BorderLayout.CENTER);
		Box toolbar=Box.createHorizontalBox();
		JButton save=new JButton("Save");
		save.addActionListener((e)->{
			JFileChooser saveFile=new JFileChooser();
			if(saveFile.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
				BufferedImage image=(BufferedImage)((ImageIcon)viewer.getIcon()).getImage();
				File file=saveFile.getSelectedFile();
				String format=file.getName().contains(".")
						?file.getName().substring(file.getName().lastIndexOf('.')+1).toUpperCase():"PNG";
				try{
					ImageIO.write(image,format,file);
				}catch(IOException ex){
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		toolbar.add(save);
		output.add(toolbar,BorderLayout.SOUTH);
		JPanel input=new JPanel(new BorderLayout());
		Main.MethodChooser methodChooser=new Main.MethodChooser();
		input.add(new JScrollPane(methodChooser),BorderLayout.SOUTH);
		try{
			JComboBox<File> fileChooser=new JComboBox(Files.list(inputDir.toPath()).map((p)->p.toFile()).sorted().toArray());
			fileChooser.addActionListener((e)->{
				refresh((File)fileChooser.getSelectedItem(),methodChooser.getBinarizer(),viewer);
			});
			JButton apply=new JButton("Refresh");
			apply.addActionListener((e)->{
				refresh((File)fileChooser.getSelectedItem(),methodChooser.getBinarizer(),viewer);
			});
			Box bar=Box.createHorizontalBox();
			bar.add(fileChooser);
			bar.add(apply);
			input.add(bar,BorderLayout.CENTER);
		}catch(IOException ex){
			Logger.getLogger(DiffViewer.class.getName()).log(Level.SEVERE,null,ex);
		}
		JSplitPane pane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,input,output);
		pane.setOneTouchExpandable(true);
		addTab("Binarize",pane);
		pane.setResizeWeight(0.3);
	}
	private void addTab(String title,JComponent component){
		int index=tabs.getTabCount();
		Box header=Box.createHorizontalBox();
		header.add(new JLabel(title));
		JButton close=new JButton("x");
		close.setMargin(new Insets(0,0,0,0));
		close.addActionListener((e)->tabs.remove(component));
		header.add(close);
		tabs.add(component,index);
		tabs.setTabComponentAt(index,header);
		tabs.setSelectedIndex(index);
	}
	private void refresh(File inputFile,Binarizer binarizer,JLabel viewer){
		try{
			BufferedImage image=binarizer.binarize(ImageIO.read(inputFile));
			File gtFile=new File(groundTruthDir,inputFile.getName().substring(0,inputFile.getName().lastIndexOf('.'))+".tiff");
			BufferedImage groundtruth=GrayscaleBinarizer.toGrayscale(ImageIO.read(gtFile));
			int len=groundtruth.getWidth()*groundtruth.getHeight();
			byte[] in=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
			byte[] gt=((DataBufferByte)groundtruth.getRaster().getDataBuffer()).getData();
			int tp=0, fp=0, tn=0, fn=0;
			for(int i=0;i<len;i++){
				if(in[i]==0){
					if(gt[i]!=0){
						in[i]=85;
						++fp;
					}else{
						++tp;
					}
				}else{
					if(gt[i]==0){
						in[i]=(byte)170;
						++fn;
					}else{
						++tn;
					}
				}
			}
			viewer.setIcon(new ImageIcon(image));
			double recall=tp*1.0/(tp+fn);
			double precision=tp*1.0/(tp+fp);
			double f=2*recall*precision/(recall+precision);
			double psnr=10*Math.log10(len*1.0/(fp+fn));
			viewer.setHorizontalTextPosition(JLabel.CENTER);
			viewer.setVerticalTextPosition(JLabel.BOTTOM);
			viewer.setText("TP="+tp+"\nFP="+fp+"\nFN="+fn+"\nTN="+tn+"\nP="+precision+"\nR="+recall+"\nFM="+f+"\nPSNR="+psnr);
		}catch(IOException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args){
		new DiffViewer();
	}
}
