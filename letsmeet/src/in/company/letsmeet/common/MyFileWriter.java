/**
 * All data copyrights of SocialEyez.co
 *
 */
package in.company.letsmeet.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.os.Environment;

/**
 * @author pradeep
 *
 */
public class MyFileWriter implements Writer {
	private FileWriter fWriter;
	private File locationFile;
	public MyFileWriter(String fileName) {
		try {
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File (sdCard.getAbsolutePath() + "/socialeyez/");
			dir.mkdirs();
			locationFile = new File(dir,fileName);
			if(!locationFile.exists()) {
				locationFile.createNewFile();
			}
			this.fWriter = new FileWriter(locationFile, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see in.company.letsmeet.common.Writer#writeData(java.lang.String)
	 */
	@Override
	public void writeData(String data) {
		// TODO Auto-generated method stub
		try {
			if (data != null) {
				data = data.concat(":::").concat(new Date(System.currentTimeMillis()).toLocaleString()).concat("\n");
				fWriter.write(data);
				//fWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
