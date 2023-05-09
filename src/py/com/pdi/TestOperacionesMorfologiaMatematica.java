package py.com.pdi;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Reconstruction;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.DiskStrel;

public class TestOperacionesMorfologiaMatematica {

	public static void main(String[] args) {
		
		String ruta = "C:\\Users\\fegas\\workspace\\ImageJ-PDI\\imagenes\\1.jpg";
		ImagePlus im = IJ.openImage(ruta);
		im.show();
		ImagePlus im2 = im.duplicate();
		ImageConverter ic = new ImageConverter(im2);
		ic.convertToGray8();
		ImageProcessor ip = im2.getProcessor();
		
		/*
		 * // Erosion int r = 1; Strel B = DiskStrel.fromRadius(r);
		 * ImageProcessor erosion = Morphology.erosion(ip, B); ImagePlus
		 * eroImage = new ImagePlus("Erosion", erosion); eroImage.show();
		 * 
		 * // Dilatacion ImageProcessor dilatacion = Morphology.dilation(ip, B);
		 * ImagePlus dilImage = new ImagePlus("Dilatacion", dilatacion);
		 * dilImage.show();
		 */

		// *** Apertura = Dilatacion de la Erosion

		int r = 7;
		Strel B = DiskStrel.fromRadius(r);
		
		ImageProcessor erosion = Morphology.erosion(ip, B);
		ImageProcessor apertura = Morphology.dilation(erosion, B);
		ImagePlus aperturaImage = new ImagePlus("Apertura", apertura);
		//aperturaImage.show();

		// *** Cierre = Erosion de la Dilatacion
		ImageProcessor dilatacion = Morphology.dilation(ip, B);
		ImageProcessor cierre = Morphology.erosion(dilatacion, B);
		ImagePlus cierreImage = new ImagePlus("Cierre", cierre);
		//cierreImage.show();

		// *** Top-Hat = Imagen - Apertura
		ImageProcessor th = Diferencia(ip, apertura);
		ImagePlus thImage = new ImagePlus("Top-Hat", th);
		//thImage.show();
		
		// *** Botton-Hat = Cierre - Imagen
		ImageProcessor bh = Diferencia(cierre, ip);
		ImagePlus bhImage = new ImagePlus("Botton-Hat", bh);
		//bhImage.show();
		
		// ** Mejora Top-Hat = Imagen + TH - BH
		ImageProcessor sum = Suma(ip, th);
		ImageProcessor mejoraTH = Diferencia(sum, bh);
		ImagePlus imgMejoraTH = new ImagePlus("Mejora Top-Hat", mejoraTH);
		//imgMejoraTH.show();
		
		// ** MMALCE
		
		int n = 7;
		ArrayList<ImageProcessor> vectorTH = new ArrayList<>();
		ArrayList<ImageProcessor> vectorBH = new ArrayList<>();		
				
		for (int i = 1; i <= n; i++) {
			int ra = i;
			Strel BE = DiskStrel.fromRadius(ra);
			ImageProcessor _th = Morphology.whiteTopHat(ip,  BE);
			ImageProcessor _bh = Morphology.blackTopHat(ip,  BE);
			vectorTH.add(_th);
			vectorBH.add(_bh);
		}
		
		ImageProcessor SW = vectorTH.get(0);
		ImageProcessor SB = vectorBH.get(0);
		for (int i = 1; i < n; i++) {
			SW = Suma(SW, vectorTH.get(i));
			SB = Suma(SB, vectorBH.get(i));
		}
		
		ImageProcessor sum2 = MultPorEscalar(SW, 0.5f);
		ImageProcessor sum3 = MultPorEscalar(SB, 0.5f);
		ImageProcessor sum1 = Suma(ip, sum2);
		ImageProcessor ipMMALCE = Diferencia(sum1, sum3);
		ImagePlus imgMMALCE = new ImagePlus("MMALCE", ipMMALCE);
		imgMMALCE.show();
		
		// clase del 04/05, paper chino
		
		ArrayList<ImageProcessor> vectorTH_2 = new ArrayList<>();
		ArrayList<ImageProcessor> vectorBH_2 = new ArrayList<>();	
		
		for (int i = 1; i < n; i++) {
			
			int ra = i;
			Strel BE = DiskStrel.fromRadius(ra);
			
			ImageProcessor erosion_2 = Morphology.erosion(ip, BE);
			ImageProcessor aReconst = Reconstruction.reconstructByDilation(erosion_2,  ip, 8);
			ImageProcessor rth = Diferencia(ip, aReconst);
			ImageProcessor dilatacion_2 = Morphology.dilation(ip, BE);
			ImageProcessor cReconst = Reconstruction.reconstructByErosion(dilatacion_2,  ip, 8);
			ImageProcessor rtb = Diferencia(cReconst, ip);
			
			vectorTH_2.add(rth);
			vectorBH_2.add(rtb);
		}
		
		ImageProcessor SW_2 = vectorTH_2.get(0);
		ImageProcessor SB_2 = vectorBH_2.get(0);
		for (int i = 1; i < n; i++) {
			SW_2 = Suma(SW_2, vectorTH_2.get(i));
			SB_2 = Suma(SB_2, vectorBH_2.get(i));
		}
		
		ImageProcessor sum2_2 = MultPorEscalar(SW_2, 0.5f);
		ImageProcessor sum3_2 = MultPorEscalar(SB_2, 0.5f);
		ImageProcessor sum1_2 = Suma(ip, sum2_2);
		ImageProcessor ipMMALCE_2 = Diferencia(sum1_2, sum3_2);
		ImagePlus imgMMALCE_2 = new ImagePlus("Paper Chino", ipMMALCE_2);
		imgMMALCE_2.show();
		
	}

	private static ImageProcessor Diferencia(ImageProcessor ip1, ImageProcessor ip2) {

		ImageProcessor ip_dif = ip1.createProcessor(ip1.getWidth(), ip1.getHeight());

		for (int i = 0; i < ip1.getWidth(); i++) {
			for (int j = 0; j < ip1.getHeight(); j++) {
				int dif = ip1.getPixel(i, j) - ip2.getPixel(i, j);

				if (dif < 0)
					dif = 0;

				ip_dif.putPixel(i, j, dif);
			}
		}

		return ip_dif;
	}

	private static ImageProcessor Suma(ImageProcessor ip1, ImageProcessor ip2) {

		ImageProcessor ip_sum = ip1.createProcessor(ip1.getWidth(), ip1.getHeight());

		for (int i = 0; i < ip1.getWidth(); i++) {
			for (int j = 0; j < ip1.getHeight(); j++) {
				int sum = ip1.getPixel(i, j) + ip2.getPixel(i, j);

				if (sum > 255)
					sum = 255;

				ip_sum.putPixel(i, j, sum);
			}
		}

		return ip_sum;
	}
	
	private static ImageProcessor MultPorEscalar(ImageProcessor ip, float m) {

		ImageProcessor ip_mult = ip.createProcessor(ip.getWidth(), ip.getHeight());

		for (int i = 0; i < ip.getWidth(); i++) {
			for (int j = 0; j < ip.getHeight(); j++) {
				int mul = (int)((float)(ip.getPixel(i, j)) * m);

				if (mul > 255)
					mul = 255;

				ip_mult.putPixel(i, j, mul);
			}
		}

		return ip_mult;
	}
}
