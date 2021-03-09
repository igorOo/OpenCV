import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton loadButton;
    private javax.swing.JLabel JLabel;
    private JButton parseButton;
    private Mat matrix;
    private String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    public MainDialog() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        setContentPane(contentPane);
        setModal(true);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 300));
        getRootPane().setDefaultButton(buttonOK);

        UIManager.setLookAndFeel(windows);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MainDialog.this.onLoad();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        parseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onParse();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (JOptionPane.showConfirmDialog(
                this,
                "Действительно выйти?",
                "Закрытие окна",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) {
            dispose();
        }
    }

    private void onLoad() throws IOException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Открыть", FileDialog.LOAD);
        fileDialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean t = name.endsWith(".jpg");
                return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith("gif") || name.endsWith(".bmp");
            }
        });
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        String filePath = fileDialog.getDirectory();
        if (fileName != null && filePath != null) {
            LoadImage(filePath + fileName);
        }
    }

    private void LoadImage(String filename) throws IOException {
        Imgcodecs imageCodecs = new Imgcodecs();
        matrix = imageCodecs.imread(filename);
        Image image = ImageIO.read(new File(filename));
        Image imageScaled = image.getScaledInstance(400, 300, Image.SCALE_DEFAULT);
        int height = image.getHeight(this);
        int width = image.getWidth(this);
        ImageIcon icon = new ImageIcon(imageScaled);
        JLabel.setSize(imageScaled.getWidth(this), imageScaled.getHeight(this));
        JLabel.setIcon(icon);
        JLabel.setText(null);
    }

    private void onParse() throws IOException {

        new Thread(()-> {
            matrix = parseImage(matrix);

            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", matrix, matOfByte);

            byte[] byteArray = matOfByte.toArray();

            //Preparing the Buffered Image
            InputStream in = new ByteArrayInputStream(byteArray);
            BufferedImage bufImage = null;
            try {
                bufImage = ImageIO.read(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image imageScaled = bufImage.getScaledInstance(400, 300, Image.SCALE_DEFAULT);
            SwingUtilities.invokeLater(()->{
                JLabel.setIcon(new ImageIcon(imageScaled));
            });
        }).start();


    }

    private Mat parseImage(Mat fileMatrix){
        String xmlFile = "C:/opencv/build/etc/lbpcascades/lbpcascade_frontalface.xml";
        CascadeClassifier classifier = new CascadeClassifier(xmlFile);

        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(fileMatrix, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    fileMatrix,                                               // where to draw the box
                    new Point(rect.x, rect.y),                            // bottom left
                    new Point(rect.x + rect.width, rect.y + rect.height), // top right
                    new Scalar(0, 0, 255),
                    3                                                     // RGB colour
            );
        }

        return fileMatrix;
    }
}
