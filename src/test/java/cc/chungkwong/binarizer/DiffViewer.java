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
import com.github.chungkwong.binarizer.*;
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
	private final File inputDir=new File("../datasets/binarization/all/test");
	private final File groundTruthDir=new File("../datasets/binarization/all/gt");
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
		class MethodChooser extends JPanel{
			JRadioButton fixed=new JRadioButton("Fixed",false);
			JRadioButton otsu=new JRadioButton("Otsu",false);
			JRadioButton sauvola=new JRadioButton("Sauvola",true);
			JRadioButton niblack=new JRadioButton("Niblack",false);
			JRadioButton bernsen=new JRadioButton("Bernsen",false);
			JTextField sauvolaWeight=new JTextField("0.32");
			JTextField sauvolaWindow=new JTextField("21");
			JTextField niblackWeight=new JTextField("0.5");
			JTextField niblackWindow=new JTextField("32");
			JTextField bernsenWeight=new JTextField("0.5");
			JTextField bernsenContrast=new JTextField("80");
			JTextField bernsenWindow=new JTextField("11");
			JTextField fixedThreshold=new JTextField("128");
			public MethodChooser(){
				setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
				ButtonGroup group=new ButtonGroup();
				Box fixedBar=Box.createHorizontalBox();
				fixedBar.add(fixed);
				fixedBar.add(new JLabel("Threshold:"));
				fixedBar.add(fixedThreshold);
				group.add(fixed);
				fixedBar.setAlignmentX(0);
				add(fixedBar);
				group.add(otsu);
				otsu.setAlignmentX(0);
				add(otsu);
				Box sauvolaBar=Box.createHorizontalBox();
				sauvolaBar.add(sauvola);
				sauvolaBar.add(new JLabel("Window size:"));
				sauvolaBar.add(sauvolaWindow);
				sauvolaBar.add(new JLabel("Weight:"));
				sauvolaBar.add(sauvolaWeight);
				group.add(sauvola);
				sauvolaBar.setAlignmentX(0);
				add(sauvolaBar);
				Box niblackBar=Box.createHorizontalBox();
				niblackBar.add(niblack);
				niblackBar.add(new JLabel("Window size:"));
				niblackBar.add(niblackWindow);
				niblackBar.add(new JLabel("Weight:"));
				niblackBar.add(niblackWeight);
				group.add(niblack);
				niblackBar.setAlignmentX(0);
				add(niblackBar);
				Box bernsenBar=Box.createHorizontalBox();
				bernsenBar.add(bernsen);
				bernsenBar.add(new JLabel("Window size:"));
				bernsenBar.add(bernsenWindow);
				bernsenBar.add(new JLabel("Min contrast:"));
				bernsenBar.add(bernsenContrast);
				bernsenBar.add(new JLabel("Weight:"));
				bernsenBar.add(bernsenWeight);
				group.add(bernsen);
				bernsenBar.setAlignmentX(0);
				add(bernsenBar);
			}
			public Binarizer getBinarizer(){
				if(otsu.isSelected()){
					return new OtsuBinarizer();
				}else if(fixed.isSelected()){
					return new FixedBinarizer(Integer.parseInt(fixedThreshold.getText()));
				}else if(sauvola.isSelected()){
					return new NiblackBasedBinarizer(
							NiblackBasedBinarizer.getSauvola(Double.parseDouble(sauvolaWeight.getText())),
							new EfficientAlgorithm(),
							Integer.parseInt(sauvolaWindow.getText()));
				}else if(niblack.isSelected()){
					return new NiblackBasedBinarizer(
							NiblackBasedBinarizer.getNiblack(Double.parseDouble(niblackWeight.getText())),
							new EfficientAlgorithm(),
							Integer.parseInt(niblackWindow.getText()));
				}else if(bernsen.isSelected()){
					return new BernsenBinarizer(
							Integer.parseInt(bernsenWindow.getText()),
							Integer.parseInt(bernsenWindow.getText()),
							Double.parseDouble(bernsenWeight.getText()),
							Integer.parseInt(bernsenContrast.getText()));
				}else{
					return new OtsuBinarizer();
				}
			}
		}
		MethodChooser methodChooser=new MethodChooser();
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
			for(int i=0;i<len;i++){
				if(in[i]==0){
					if(gt[i]!=0){
						in[i]=85;
					}
				}else{
					if(gt[i]==0){
						in[i]=(byte)170;
					}
				}
			}
			viewer.setIcon(new ImageIcon(image));
		}catch(IOException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args){
		new DiffViewer();
	}
}
