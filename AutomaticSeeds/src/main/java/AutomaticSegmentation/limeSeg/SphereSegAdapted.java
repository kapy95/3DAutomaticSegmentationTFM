package AutomaticSegmentation.limeSeg;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.commands.CommandHelper;
import eu.kiaru.limeseg.commands.SphereSegAdvanced;
import eu.kiaru.limeseg.struct.CellT;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.plugin.frame.RoiManager;
import ij.plugin.FolderOpener;

/**
 * 
 */

/**
 * Based on Nicolas Chiaruttini's "SphereSegAdvanced" function
 * @author Pablo Vicente-Munuera
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>LimeSeg>Sphere Seg (Advanced)")
public class SphereSegAdapted extends Thread implements Command {
	
	protected Path path;

	@Parameter(persist=true, stepSize="0.1", min="0")
	protected float d_0=2.0f;
	
	@Parameter(persist=true, stepSize="0.005", min="-0.04", max="0.04")
	protected float f_pressure = 0.015f;
	
	@Parameter(persist=true)
	protected double z_scale = 1f;

    @Parameter(persist=true)
    //private ImagePlus imp;
    protected ImagePlus imp;
    
    @Parameter(persist=true)
    protected float range_in_d0_units = 2;
    
    protected static int index=0;
    
    @Parameter(persist=true)
    protected ColorRGB color;
    
    @Parameter(persist=true)
    protected boolean sameCell;
    
    @Parameter(persist=true)
    protected boolean showOverlayOuput = true;
    
    @Parameter(persist=true)
    protected boolean show3D = true;
    
    @Parameter(persist=true)
    protected boolean constructMesh=true;
    
    @Parameter(persist=true)
    protected int numberOfIntegrationStep=-1;
    
    @Parameter(persist=true)
    protected boolean randomColors;
    
    @Parameter(persist=true)
    protected boolean appendMeasures;
    
    @Parameter(persist=true)
    protected float realXYPixelSize=1f;
	
	@Parameter(persist=true)
	protected boolean clearOptimizer;
	
	@Parameter(persist=true)
	protected boolean stallDotsPreviouslyInOptimizer=false;
	
	private LimeSeg lms;
	
	private int numberOfCells;
	
	
	@Override
	public void run() {
		
		//this.setShow3D(false);
		//this.setShow3D(true);
		this.setImp2();
		this.setLimeSeg();
		//RoiManager roiManager = RoiManager.getRoiManager();
		
		//roiManager
		//this.setImp(IJ.openImage(this.path+"\\ImageSequence"));
		/*
        if (roiManager==null) {
        	System.err.println("No roi manager found - command aborted.");
        	return;
        }
        */
        lms.initialize();
		LimeSeg.saveOptState();
        if (clearOptimizer) {LimeSeg.clearOptimizer();}
        if ((!clearOptimizer)) {
        	if ((stallDotsPreviouslyInOptimizer)) {        		
	        	LimeSeg.opt.dots.forEach(dn -> {
	        		dn.stallDot();
	        	});
        	} else {
        		LimeSeg.opt.dots.forEach(dn -> {
        			dn.freeDot();
            	});
        	}
        }
        
    	LimeSeg.opt.setOptParam("ZScale", (float)z_scale);
        LimeSeg.opt.setOptParam("d_0",d_0);
        LimeSeg.opt.setOptParam("radiusSearch",d_0*range_in_d0_units);
        LimeSeg.opt.setOptParam("normalForce",f_pressure);

        float avgX=0;
        float avgY=0;
        float avgZ=0;
        int NCells=0;
        ArrayList<CellT> currentlyOptimizedCellTs = new ArrayList<>();
		/*
        if (imp == null)
        	imp = IJ.openImage();
        */	
		
		//System.out.println(imp.getChannel());
        LimeSeg.currentChannel = imp.getChannel();
        
        if (sameCell) {
        	LimeSeg.newCell();
        }
     
        
		File dir = new File(path.toString()+"\\data\\RoiSet");
		File[] listOfFiles = dir.listFiles();
        
		int nRois=listOfFiles.length;
		this.setNumberOfCells(nRois);
		
		for(File file:listOfFiles) {
			
			Roi roi=RoiDecoder.open(dir.toString()+"\\"+file.getName());
		
			if (roi.getClass().equals(OvalRoi.class)) {
				OvalRoi circle = (OvalRoi) roi;
				float r0 = (float) ((circle.getFloatWidth()/2 + circle.getFloatHeight()/2)/2);
				LimeSeg.currentFrame = circle.getTPosition();
				if (LimeSeg.currentFrame==0) {LimeSeg.currentFrame=1;}
				float z0 = circle.getZPosition();
				if (z0 == 0){
					z0 = circle.getPosition();
				}
				float x0 = (float)(circle.getXBase()+circle.getFloatWidth()/2);
				float y0 = (float)(circle.getYBase()+circle.getFloatHeight()/2);
				
				avgX+=x0;
				avgY+=y0;
				avgZ+=z0;
				NCells++;
				//LimeSeg.update3DDisplay();//cambio aqui
		    	if (!sameCell) {
		    		LimeSeg.newCell();
		    	}
		        if ((this.sameCell)||(nRois==1)) {
		        	LimeSeg.setCellColor((float) (color.getRed()/255.0),
		        					 	 (float) (color.getGreen()/255.0),
		        					 	 (float) (color.getBlue()/255.0),
		        					 	 1.0f);
		        } else {
		        	LimeSeg.setCellColor((float) (java.lang.Math.random()),
    					 				 (float) (java.lang.Math.random()),
    					 				 (float) (java.lang.Math.random()),
    					 				 1.0f);
		        }		       
		        LimeSeg.makeSphere(x0,y0,z0,r0);      
		        LimeSeg.pasteDotsToCellT();
		        if (!sameCell) {
		        	LimeSeg.putCurrentCellTToOptimizer();
		        }
		    	currentlyOptimizedCellTs.add(LimeSeg.currentCell.getCellTAt(LimeSeg.currentFrame));
			}			
		
		LimeSeg.setWorkingImage(imp, LimeSeg.currentChannel, LimeSeg.currentFrame);
		}
		
		if (sameCell) {
			LimeSeg.putCurrentCellTToOptimizer();
		}
		

    	if (show3D) {

    		LimeSeg.make3DViewVisible();
    		LimeSeg.putAllCellsTo3DDisplay();
    		LimeSeg.set3DViewCenter(avgX/NCells,avgY/NCells,avgZ/NCells);
    		
    	}
    	
 	    float k_grad=(float) LimeSeg.opt.getOptParam("k_grad");
        LimeSeg.opt.setOptParam("k_grad",0.0f);
        LimeSeg.opt.setOptParam("normalForce",0);        
       	//LimeSeg.opt.setCUDAContext();
       	try{
       		LimeSeg.runOptimisation(500);// el comando runOptimisation determina los pasos que se dan hasta que se pare la segmentacion, antes lo pusiste a 100 y salian cosas raras
       	}catch(NullPointerException n) {
       		LimeSeg.requestStopOptimisation=true;
       		LimeSeg.stopOptimisation();
       	}
       	//LimeSeg.stopOptimisation();
        LimeSeg.opt.requestResetDotsConvergence=true;
        LimeSeg.opt.setOptParam("k_grad",k_grad);
        LimeSeg.opt.setOptParam("normalForce",f_pressure);
        boolean RadiusDeltaChanged=false;
        if ((LimeSeg.opt.cellTInOptimizer.size()>1)&&(LimeSeg.opt.getOptParam("radiusDelta")==0)) {
        	RadiusDeltaChanged=true;
        	LimeSeg.opt.setOptParam("radiusDelta", d_0/2);
        }
        
        try {
        LimeSeg.runOptimisation(numberOfIntegrationStep);
        }catch(NullPointerException n) {
        	LimeSeg.requestStopOptimisation=true;
       		LimeSeg.stopOptimisation();
        	
        }
        
        if (RadiusDeltaChanged) {
        	LimeSeg.opt.setOptParam("radiusDelta", 0);
        }
      
       
       	if (constructMesh) {
       		try {
       			
       	   	for (CellT ct : currentlyOptimizedCellTs) {
       	   		ct.constructMesh();
       	   	}
       	   	
       	   	for (CellT ct : currentlyOptimizedCellTs) {
       	   		LimeSeg.setCell3DDisplayMode(1);
            	LimeSeg.currentCell=ct.c;
            	
   	    	}
       	   	
       		}catch(NullPointerException n) {
            	LimeSeg.requestStopOptimisation=true;
           		LimeSeg.stopOptimisation();
       		}
       	}
       	LimeSeg.notifyCellExplorerCellsModif=true;
       	if (showOverlayOuput) {
       	   	for (CellT ct : currentlyOptimizedCellTs) {
       	   		LimeSeg.addToOverlay(ct);
       	   	}
       	   	LimeSeg.updateOverlay(); //error aqui de concurrentmodificationException
       	}
       	
//       	if (appendMeasures) {
//       		CommandHelper.displaySegmentationOutput(currentlyOptimizedCellTs, 
//       												this.realXYPixelSize, 
//       												this.constructMesh);
//       	}
       	
       	/*
       	File dir = new File("nameoffolder");
       	dir.mkdir();
       	*/

       	System.out.println("SphereSegAdapted ha terminado");
       	//LimeSeg.saveStateToXmlPly((path.toString()+"\\resultados"));
	}

	/**
	 * @return the d_0
	 */
	public float getD_0() {
		return d_0;
	}

	/**
	 * @return the f_pressure
	 */
	public float getF_pressure() {
		return f_pressure;
	}

	/**
	 * @return the z_scale
	 */
	public double getZ_scale() {
		return z_scale;
	}

	/**
	 * @return the imp
	 */
	


	public ImagePlus getImp() {
	
		return imp;
	}

	/**
	 * @return the range_in_d0_units
	 */
	public float getRange_in_d0_units() {
		return range_in_d0_units;
	}

	/**
	 * @return the color
	 */
	public ColorRGB getColor() {
		return color;
	}
	
	public Path getPath() {
		
		return this.path;
	}

	/**
	 * @return the sameCell
	 */
	public boolean isSameCell() {
		return sameCell;
	}

	/**
	 * @return the showOverlayOuput
	 */
	public boolean isShowOverlayOuput() {
		return showOverlayOuput;
	}

	/**
	 * @return the show3D
	 */
	public boolean isShow3D() {
		return show3D;
	}

	/**
	 * @return the constructMesh
	 */
	public boolean isConstructMesh() {
		return constructMesh;
	}

	/**
	 * @return the numberOfIntegrationStep
	 */
	public int getNumberOfIntegrationStep() {
		return numberOfIntegrationStep;
	}

	/**
	 * @return the randomColors
	 */
	public boolean isRandomColors() {
		return randomColors;
	}

	/**
	 * @return the appendMeasures
	 */
	public boolean isAppendMeasures() {
		return appendMeasures;
	}

	/**
	 * @return the realXYPixelSize
	 */
	public float getRealXYPixelSize() {
		return realXYPixelSize;
	}
	


	/**
	 * @return the clearOptimizer
	 */
	public boolean isClearOptimizer() {
		return clearOptimizer;
	}

	/**
	 * @return the stallDotsPreviouslyInOptimizer
	 */
	public boolean isStallDotsPreviouslyInOptimizer() {
		return stallDotsPreviouslyInOptimizer;
	}
	
	public void set_path(String pathfile){

		this.path = Paths.get(pathfile);
	}

	/**
	 * @param d_0 the d_0 to set
	 */
	/*
	public void set_Imp(){
		//openImage(java.lang.String path)
		//Opens, but does not display, the specified image file and returns an ImagePlus object object if successful,
		//or returns null if the file is not in a supported format or is not found.
		this.imp = IJ.openImage(this.path+"\\ImageSequence");
	}
	*/
	
	
	public void setD_0(float d_0) {
		this.d_0 = d_0;
	}

	/**
	 * @param f_pressure the f_pressure to set
	 */
	public void setF_pressure(float f_pressure) {
		this.f_pressure = f_pressure;
	}

	/**
	 * @param z_scale the z_scale to set
	 */
	public void setZ_scale(double z_scale) {
		this.z_scale = z_scale;
	}

	/**
	 * @param imp the imp to set
	
	 */
	public void setImp(ImagePlus imp) {
		
		this.imp = imp;
				//IJ.openImage(path.toString());
	}
	
	
	public void setImp2() {
		
		this.imp=FolderOpener.open(path.toString()+"\\data\\ImageSequence");
	}

	/**
	 * @param range_in_d0_units the range_in_d0_units to set
	 */
	public void setRange_in_d0_units(float range_in_d0_units) {
		this.range_in_d0_units = range_in_d0_units;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(ColorRGB color) {
		this.color = color;
	}

	/**
	 * @param sameCell the sameCell to set
	 */
	public void setSameCell(boolean sameCell) {
		this.sameCell = sameCell;
	}

	/**
	 * @param showOverlayOuput the showOverlayOuput to set
	 */
	public void setShowOverlayOuput(boolean showOverlayOuput) {
		this.showOverlayOuput = showOverlayOuput;
	}

	/**
	 * @param show3d the show3D to set
	 */
	public void setShow3D(boolean show3d) {
		show3D = show3d;
	}

	/**
	 * @param constructMesh the constructMesh to set
	 */
	public void setConstructMesh(boolean constructMesh) {
		this.constructMesh = constructMesh;
	}

	/**
	 * @param numberOfIntegrationStep the numberOfIntegrationStep to set
	 */
	public void setNumberOfIntegrationStep(int numberOfIntegrationStep) {
		this.numberOfIntegrationStep = numberOfIntegrationStep;
	}

	/**
	 * @param randomColors the randomColors to set
	 */
	public void setRandomColors(boolean randomColors) {
		this.randomColors = randomColors;
	}

	/**
	 * @param appendMeasures the appendMeasures to set
	 */
	public void setAppendMeasures(boolean appendMeasures) {
		this.appendMeasures = appendMeasures;
	}

	/**
	 * @param realXYPixelSize the realXYPixelSize to set
	 */
	public void setRealXYPixelSize(float realXYPixelSize) {
		this.realXYPixelSize = realXYPixelSize;
	}

	/**
	 * @param clearOptimizer the clearOptimizer to set
	 */
	public void setClearOptimizer(boolean clearOptimizer) {
		this.clearOptimizer = clearOptimizer;
	}

	/**
	 * @param stallDotsPreviouslyInOptimizer the stallDotsPreviouslyInOptimizer to set
	 */
	public void setStallDotsPreviouslyInOptimizer(boolean stallDotsPreviouslyInOptimizer) {
		this.stallDotsPreviouslyInOptimizer = stallDotsPreviouslyInOptimizer;
	}
	
	public void setLimeSeg() {
		this.lms = new LimeSeg();
	}
	
	public void stopLimeSeg() {
		LimeSeg.stopOptimisation();
	}
	public void deleteLimeSeg() {
		this.lms=null;
	}

	public int getNumberOfCells() {
		return numberOfCells;
	}

	public void setNumberOfCells(int numberOfCells) {
		this.numberOfCells = numberOfCells;
	}

	
}
