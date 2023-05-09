package py.com.pdi;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class TestEcualizacionHistograma {

	public static void main(String[] args) {

		String sCarpAct = System.getProperty("user.dir").concat("\\imagenes");
		
		File carpeta = new File(sCarpAct);
		String[] listado = carpeta.list();
		if (listado == null || listado.length == 0) {
			System.out.println("no hay");
		} else {
			for (int i=0; i<listado.length; i++) {
				System.out.println(listado[i]);
				String ruta = sCarpAct.concat("\\").concat(listado[i]);
				System.out.println(ruta);
				
				ImagePlus im = IJ.openImage(ruta);
				
				im.show();
				
				System.out.println("Entropia -> " + ie(im.getProcessor()));
				
			}
		}
		
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
    
    private static float log(double num, int base) {
        return (float)(Math.log10(num) / Math.log10(base));
    }

}

