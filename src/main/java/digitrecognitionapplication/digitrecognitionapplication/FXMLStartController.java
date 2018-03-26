package digitrecognitionapplication.digitrecognitionapplication;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class FXMLStartController implements Initializable {

    @FXML
    private Button bBegin;


        private final int CANVAS_WIDTH = 250;
	private final int CANVAS_HEIGHT = 250;
	private NativeImageLoader loader;
	private MultiLayerNetwork model;
	private Label lblResult;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void loadProject(ActionEvent event) {
        try{
            
            /*FXMLLoader loader=new FXMLLoader(getClass().getResource("Scene.fxml"));
            Parent root=loader.load();
            Screen screen=Screen.getPrimary();
            double x=screen.getBounds().getWidth();
            double y=screen.getBounds().getHeight();
            Scene scene=new Scene(root);
            Stage stage=new Stage();
            stage.setScene(scene);*/
            
                Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		ImageView imgView = new ImageView();
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		
		model = ModelSerializer.restoreMultiLayerNetwork(new File("mnist-model.zip"));
		loader = new NativeImageLoader(28,28,1,true);
		imgView.setFitHeight(150);
		imgView.setFitWidth(150);
		ctx.setLineWidth(10);
		ctx.setLineCap(StrokeLineCap.SQUARE);
		lblResult = new Label();
		
		HBox hbBottom = new HBox(10,canvas, imgView);
		VBox root = new VBox(5, hbBottom, lblResult);
		hbBottom.setAlignment(Pos.CENTER);
		root.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(root, 700, 500);
                Stage stage=new Stage();
            
		stage.setScene(scene);
                MainApp.window.close();
                MainApp.window=stage;
            
                stage.show();
		stage.setTitle("Handwritten Digit Recognition - Prateek Sawhney");
		
                canvas.setOnMousePressed(e -> {
			ctx.setStroke(Color.WHITE);
            ctx.beginPath();
            ctx.moveTo(e.getX(), e.getY());
            ctx.stroke();
		});
		canvas.setOnMouseDragged(e -> {
			ctx.setStroke(Color.WHITE);
			ctx.lineTo(e.getX(), e.getY());
			ctx.stroke();
		});
		canvas.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				clear(ctx);
			}
		});
		canvas.setOnKeyReleased(e -> {
			if(e.getCode() == KeyCode.ENTER) {
                BufferedImage scaledImg = getScaledImage(canvas);
                imgView.setImage(SwingFXUtils.toFXImage(scaledImg, null));
                try {
                	predictImage(scaledImg);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
                    }
		});
		clear(ctx);
		canvas.requestFocus();
	
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    private BufferedImage getScaledImage(Canvas canvas) {
		// for a better recognition we should improve this part of how we retrieve the image from the canvas
		WritableImage writableImage = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
		canvas.snapshot(null, writableImage);
		Image tmp =  SwingFXUtils.fromFXImage(writableImage, null).getScaledInstance(28, 28, Image.SCALE_SMOOTH);
		BufferedImage scaledImg = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
		Graphics graphics = scaledImg.getGraphics();
		graphics.drawImage(tmp, 0, 0, null);
		graphics.dispose();
		return scaledImg;
	}

	private void clear(GraphicsContext ctx) {
		ctx.setFill(Color.BLACK);
		ctx.fillRect(0, 0, 300, 300);
	}
	
	private void predictImage(BufferedImage img ) throws IOException {
		ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
		INDArray image = loader.asRowVector(img);
		imagePreProcessingScaler.transform(image);
		INDArray output = model.output(image);
		String putStr = output.toString();
		lblResult.setText("Prediction: " + model.predict(image)[0] + "\n " + putStr);
	}


}
