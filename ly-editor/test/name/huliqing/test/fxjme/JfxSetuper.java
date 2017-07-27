package name.huliqing.test.fxjme;

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import javafx.embed.swing.JFXPanel;

public class JfxSetuper {

	private boolean jfxIsSetup;
	
    public void setupJavaFX() throws RuntimeException {
        if (!jfxIsSetup) {
            jfxIsSetup = true;
        } else {
        	return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
