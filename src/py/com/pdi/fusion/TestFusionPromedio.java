package py.com.pdi.fusion;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.DiskStrel;

public class TestFusionPromedio {

	public static void main(String[] args) {
		
		ImagePlus vis = IJ.openImage("C:\\Users\\fegas\\workspace\\ImageJ-PDI\\imagenes\\Camp_Vis.png");
		ImagePlus ir = IJ.openImage("C:\\Users\\fegas\\workspace\\ImageJ-PDI\\imagenes\\Camp_IR.png");

		vis.show();
		ir.show();
		
		ImageProcessor ip_suma = Suma(vis.getProcessor(), ir.getProcessor());
		ImageProcessor ip_fusionado = MultPorEscalar(ip_suma, 0.5f);
		
		ImagePlus im_fusionado = new ImagePlus("Imagen fusionada", ip_fusionado);
		
		im_fusionado.show();
		
		System.out.print("SD = ");
		System.out.println(sd(ip_fusionado));
		System.out.print("IE = ");
		System.out.println(ie(ip_fusionado));
		
		// FUSION DE IMAGENES UTILIZANDO TOP-HAT
		
		int r = 21;
		Strel B = DiskStrel.fromRadius(r);
		
		ImageProcessor th_vis = Morphology.whiteTopHat(vis.getProcessor(),  B);
		ImageProcessor th_ir = Morphology.whiteTopHat(ir.getProcessor(),  B);

		ImageProcessor bh_vis = Morphology.blackTopHat(vis.getProcessor(),  B);
		ImageProcessor bh_ir = Morphology.blackTopHat(ir.getProcessor(),  B);
								
		ImageProcessor ip_op1 = Suma(ip_fusionado, Max(th_ir, th_vis));
		ImageProcessor ip_fusionado_th = Diferencia(ip_op1, Max(bh_ir, bh_vis));
		
		
		ImagePlus im_fusionado_th = new ImagePlus("Imagen fusionada TH", ip_fusionado_th);
		im_fusionado_th.show();
		
		System.out.print("SD = ");
		System.out.println(sd(ip_fusionado_th));
		System.out.print("IE = ");
		System.out.println(ie(ip_fusionado_th));
		
		
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

	private static ImageProcessor Max(ImageProcessor ip1, ImageProcessor ip2) {

		ImageProcessor ip_max = ip1.createProcessor(ip1.getWidth(), ip1.getHeight());

		for (int i = 0; i < ip1.getWidth(); i++) {
			for (int j = 0; j < ip1.getHeight(); j++) {
				int v = Math.max(ip1.getPixel(i, j), ip2.getPixel(i, j));

				ip_max.putPixel(i, j, v);
			}
		}

		return ip_max;
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
	
    // desviacion estandar
    private static float sd(ImageProcessor ip) {
    	int W = ip.getWidth();
    	int H = ip.getHeight();
    	
    	float avg = promedio(ip);
    	float _sd  = 0;

    	for (int k = 0; k < 256; ++k)
    		_sd += (k - avg) * (k - avg) * (frecuenciaPixel(ip, k) / (float)(W * H));

    	return (float)(Math.sqrt(_sd));
    }
    
    // entropia
    private static float ie(ImageProcessor ip) {
    	int W = ip.getWidth();
    	int H = ip.getHeight();
    	
    	float entropia  = 0;

    	for (int i = 0; i < 256; ++i) {
    		float p_i = frecuenciaPixel(ip, i) / (float)(W * H);
    		if (p_i > 0.0)
    			entropia += p_i * log(p_i, 2);
    	}

    	return (-entropia);
    }

    private static int frecuenciaPixel(ImageProcessor ip, int k) {
    	int M = ip.getWidth();
    	int N = ip.getHeight();
    	int c = 0;
    	    	
    	for (int u = 0; u < M; u++)
    		for (int v = 0; v < N; v++)
    			if (ip.getPixel(u, v) == k)
    				c++;
    	
    	return c;
    }
    
    private static float promedio(ImageProcessor ip) {
    	int M = ip.getWidth();
    	int N = ip.getHeight();
    	
    	long s = 0;
    	
    	for (int u = 0; u < M; u++)
    		for (int v = 0; v < N; v++)
    			s += ip.getPixel(u, v);
    	
    	return (float)(s) / (float)(M * N);
    }

    private static float log(double num, int base) {
        return (float)(Math.log10(num) / Math.log10(base));
    }
}
