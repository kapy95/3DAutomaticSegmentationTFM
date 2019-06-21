package PostProcessingGland.GUI.CustomElements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;

import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;

import PostProcessingGland.IOPlyLimeSeg;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.PointRoi;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;

/**
CONTENT
 * Helper class for LimeSeg segmentation
 * The segmentation objects are called Cells
 * They are contained within the Array allCells
 * Each Cell contains one or Many CellT, which is a Cell at a timepoint
 * Each CellT contains many dots. It a represent a 3D object
 * This class contains many functions to:
 * - store / retrieves these objects (I/O section)
 * - display them in 2D on ImagePlus image
 * - display them in 3D in a custom 3D viewer
 *
 */
public class ImageOverlay {
	
//List of cells currently stored by LimeSeg
	static public ArrayList<Cell> allCells;  				
	
//Set of dots being overlayed on workingImP
	static public ArrayList<DotN> dots_to_overlay;			

//Current selected Cell of limeseg
	static public Cell currentCell;    						
  
//Current selected Dot of limeseg
	static public DotN currentDot;							
 
//Current working image in IJ1 format 
// Used for IJ1 interaction (ROI, and JOGLCellRenderer synchronization)        
	
	static public int currentFrame=1;
	
//----------------- 2D View
  /**
   * Put current cell to overlays (requires updateOverlay to be effective)
   */
  static public void putCurrentCellToOverlay() { 
  	if (currentCell!=null) {
          addToOverlay(currentCell);
  	}    			
  }
  
  /**
   * Put dots of current user selected slice to overlays (requires updateOverlay to be effective)
   * @return 
   */

  static public void putCurrentSliceToOverlay(ImagePlus workingImP) { 
  	if (workingImP!=null) {
		int zSlice; //= workingImP.getZ();

        if ((workingImP.getNFrames()!=1)||(workingImP.getNChannels()!=1)) {
        	zSlice = workingImP.getZ();
        } else {
        	zSlice = workingImP.getSlice();
        }
    	for (Cell c:allCells) {
    		CellT ct = c.getCellTAt(ImageOverlay.currentFrame);
    		if (ct!=null) {
    			for (DotN dn:ct.dots) {
    				if ((int)(dn.pos.z)==zSlice-1) {
    					ImageOverlay.dots_to_overlay.add(dn);
    				}
    			}
    		}
    	}
  	}
  }
  
  static public void putCurrentZPositionToOverlay(ImagePlus workingImP) { 
  	if (workingImP!=null) {
    	for (Cell c:allCells) {
    		CellT ct = c.getCellTAt(ImageOverlay.currentFrame);
    		if (ct!=null) {
    			for (DotN dn:ct.dots) {
    					ImageOverlay.dots_to_overlay.add(dn);
    			}
    		}
    	}
  	}
  }
  
  /**
   *  Clears image overlay (requires updateOverlay to be effective)
   */
  static public void clearOverlay() {
      if (dots_to_overlay!=null)
          dots_to_overlay.clear();
  }
  
  /**
   * Adds all cells into image overlay (requires updateOverlay to be effective
   */

  static public void addAllCellsToOverlay() {
      for (int i=0;i<allCells.size();i++) {
          Cell c= allCells.get(i);
          addToOverlay(c);
      }
  } 
  
  /**
   * updates Overlay of the working image with registeres dots to be overlayed
   */
  
  static public void updateOverlay(ImagePlus workingImP) {
      Overlay ov = new Overlay();
      if (workingImP!=null) {
        workingImP.setOverlay(ov);
        Iterator<DotN> i=dots_to_overlay.iterator();
        if ((workingImP.getNFrames()!=1)||(workingImP.getNChannels()!=1)) {
            while (i.hasNext()) {
                DotN nd = i.next();
                PointRoi roi;
                roi = new PointRoi(nd.pos.x,nd.pos.y);//,c);
                Color color = new Color((int)(nd.ct.c.color[0]*255),(int)(nd.ct.c.color[1]*255),(int)(nd.ct.c.color[2]*255));
                roi.setStrokeColor(color);
                int zpos=1+(int)(nd.pos.z);
                if ((zpos>0)&&(zpos<=workingImP.getNSlices())) {
                    roi.setPosition(nd.ct.c.cellChannel, zpos, nd.ct.frame);
                    ov.addElement(roi); 
                }   
            }   
        } else {
            while (i.hasNext()) {
            		DotN loadedDots = i.next();
                PointRoi roi;
                roi = new PointRoi(loadedDots.pos.x,loadedDots.pos.y);//,c);   
                Color color = new Color((int)(loadedDots.ct.c.color[0]*255),(int)(loadedDots.ct.c.color[1]*255),(int)(loadedDots.ct.c.color[2]*255));
                roi.setStrokeColor(color);
                int zpos=1+(int)((float) (loadedDots.pos.z));
                if ((zpos>0)&&(zpos<=workingImP.getNSlices())) {
                    roi.setPosition(zpos);
                    ov.addElement(roi);  
                }
            }
        }
        workingImP.updateAndDraw();
      }
  } 
	
  /** Methods **/
  
  /**
   * adds a cell timepoint to list of points which will be overlayed
   * @param ct cell timepoint to add
   */
  static public void addToOverlay(CellT zPosition) {
      for (int i=0;i<zPosition.dots.size();i++) {
          DotN zDots = zPosition.dots.get(i);
          dots_to_overlay.add(zDots);           
      }
  }
  
  /**
   * adds a cell to list of points which will be overlayed (i.e. all associated cell timepoints)
   * @param c cell timepoint to add
   */
  static public void addToOverlay(Cell c) {
      for (int i=0;i<c.cellTs.size();i++) {
          CellT ct= c.cellTs.get(i);
          addToOverlay(ct);
      }
  }  
  
  /**
   * adds all points of all cellt found to be at the specified frame
   * @param frame
   */
  public void addToOverlay(int frame) {
      for (int i=0;i<allCells.size();i++) {
          Cell c= allCells.get(i);
          CellT ct = c.getCellTAt(frame);
          if (ct!=null) {
              addToOverlay(ct);
          }
      }
  } 
  
  static public void setWorkingImage(ImagePlus workingImP) {        
    currentFrame = workingImP.getCurrentSlice();
}
  
  static void updateWorkingImage(ImagePlus workingImP) {
  	if (workingImP!=null) {
          setWorkingImage(workingImP);
  	} else {
  		//IJ.log("Cannot change image : the Optimizer is running");
  	}
  }
  
  static public void setCurrentFrame(ImagePlus workingImP, int cFrame) {
  	currentFrame=cFrame;
  	updateWorkingImage(workingImP);
  }
}