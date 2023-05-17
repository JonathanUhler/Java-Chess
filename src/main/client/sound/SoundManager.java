package client.sound;


import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class SoundManager {

	public static final String PATH = "assets/sounds";
	public static final String EXTENSION = ".wav";
	

	private SoundManager() { }
	

	public static void playSound(String fileName) {
		Thread soundThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String path = SoundManager.PATH + "/" + fileName + SoundManager.EXTENSION;
						URL url = Thread.currentThread()
							.getContextClassLoader()
							.getResource(path);
						
						AudioInputStream ais = AudioSystem.getAudioInputStream(url);  
						Clip c = AudioSystem.getClip();
						c.open(ais);
						c.start();
					}
					catch (Exception e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			});

		soundThread.start();
	}

}
