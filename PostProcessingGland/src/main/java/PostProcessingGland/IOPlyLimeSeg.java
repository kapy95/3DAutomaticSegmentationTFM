package PostProcessingGland;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.smurn.jply.ElementReader;
import org.smurn.jply.ElementType;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;
import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;

/**
 * Class that will be called by Fiji/ImageJ and load the output of the 3D
 * Segmentation Adaptation of LimeSeg (Lipid Membrane Segmentation): a
 * coarse-grained lipid membrane simulation for 3D image segmentation - by Sarah
 * Machado et al. 
 * 
 * A computationally efficient and spatially continuous 3D
 * segmentation method. LimeSeg is easy-to-use and can process many and/or
 * highly convoluted objects. Based on the concept of SURFace ELements
 * (“Surfels”), LimeSeg resembles a highly coarse-grained simulation of a lipid
 * membrane in which a set of particles, analogous to lipid molecules, are
 * attracted to local image maxima. The particles are self-generating and
 * self-destructing thus providing the ability for the membrane to evolve
 * towards the contour of the objects of interest.
 * https://doi.org/10.1186/s12859-018-2471-0
 * 
 * @author Antonio Tagua
 */

public class IOPlyLimeSeg {

	public IOPlyLimeSeg() {
		// TODO Auto-generated constructor stub
	}

static public void loadCellTFromPly(String path) {
  try {		   
	   PlyReader plyreader = new PlyReaderFile(path);
	   ElementReader reader = plyreader.nextElementReader();
      while (reader != null) {
           ElementType type = reader.getElementType();
           // In PLY files vertices always have a type named "vertex".
           if (type.getName().equals("vertex")) {
               ArrayList<DotN> dots = new ArrayList<DotN>(reader.getCount());
               // Read the elements. They all share the same type.
           	org.smurn.jply.Element element = reader.readElement();
               while (element != null) {
               	 DotN dn = new DotN();
               	 dn.pos.x=(float) element.getDouble("x");
               	 dn.pos.y=(float) element.getDouble("y");
               	 dn.pos.z=(float) element.getDouble("z");

               	 dn.Norm.x=(float) element.getDouble("nx");
               	 dn.Norm.y=(float) element.getDouble("ny");
               	 dn.Norm.z=(float) element.getDouble("nz");
               	 //dn.ct=ct;
                    element = reader.readElement();
                    dots.add(dn);
               }
               //ct.dots=dots;
           }
          /* if (type.getName().equals("face")) {
           	ArrayList<TriangleN> tris = new ArrayList<TriangleN>(reader.getCount());
	            org.smurn.jply.Element triangle = reader.readElement();
               while (triangle != null) {
	            	int[] indices = triangle.getIntList("vertex_index");
	            	TriangleN tri = new TriangleN();
	            	tri.id1=indices[0];
	            	tri.id2=indices[1];
	            	tri.id3=indices[2];
	            	triangle = reader.readElement();
                   tris.add(tri);
                   
               }
           	ct.triangles=tris;
           	ct.tesselated=true;
           }	*/            		            
           // Close the reader for the current type before getting the next one.
           reader.close();
           reader = plyreader.nextElementReader();
      }
      plyreader.close();
  } catch (IOException e) {
		e.printStackTrace();
  }
}

static public void hydrateCellT(Cell c, String path) {
  // path should contain the folder with the ply files for each timepoint
  c.cellTs.clear();
  Pattern pattern = Pattern.compile("[0-9]+"); 
  File dir = new File(path);
  if (!dir.isDirectory()) {
 		System.out.println("Error, folder for cell "+c.id_Cell+" not found.");
 		return;
  }		   
  File[] files = dir.listFiles(new FilenameFilter() {
       @Override
       public boolean accept(File dir, String name) {
           return name.matches("T_[0-9]+.ply");
       }
  });
  for (File f:files) {
	   String fileName = f.getAbsolutePath();
	   System.out.println("Found cellT file : "+fileName);
	   Matcher matcher = pattern.matcher(f.getName());
	   matcher.find(); 			   
	   String match = matcher.group(); // Get the matching string
	   int tp = Integer.valueOf(match);
	   System.out.println("TP="+tp);			   		   
	   CellT ct = new CellT(c,tp);
	   loadCellTFromPly(f.getAbsolutePath());
	   ct.frame=tp;
	   c.cellTs.add(ct);
  }
}

static public boolean askYesNo(String question) {
     return askYesNo(question, "[Y]", "[N]");
}

static public boolean askYesNo(String question, String positive, String negative) {
     Scanner input = new Scanner(System.in);
     // Convert everything to upper case for simplicity...
     positive = positive.toUpperCase();
     negative = negative.toUpperCase();
     String answer;
     do {
         System.out.print(question);
         answer = input.next().trim().toUpperCase();
     } while (!answer.matches(positive) && !answer.matches(negative));
     // Assess if we match a positive response
     input.close();
     return answer.matches(positive);
}
 
static void purgeDirectory(File dir, int height) {
  // no need to clean below level 
  if (height>=0) {
   for (File file: dir.listFiles()) {
       if (file.isDirectory()) purgeDirectory(file, height-1);
       file.delete();
   }
  }
}

}