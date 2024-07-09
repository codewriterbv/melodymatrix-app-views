package be.codewriter.melodymatrix.view.stage.ledstrip

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent

class LedStripStage : VisualizerStage() {
    override fun onMidiData(midiData: MidiData) {
        TODO("Not yet implemented")
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        TODO("Not yet implemented")
    }
    /*
        private static final int WIDTH = 500;
        private static final int HEIGHT = 500;

        @Override
        public void start(Stage primaryStage) {
            WritableImage image = new WritableImage(WIDTH, HEIGHT);
            PixelWriter pixelWriter = image.getPixelWriter();

            createGradient(pixelWriter, 0, 0, WIDTH / 2, HEIGHT, Color.RED, Color.GREEN);
            createGradient(pixelWriter, WIDTH / 2, 0, WIDTH, HEIGHT, Color.BLUE, Color.YELLOW);

            ImageView imageView = new ImageView(image);
            primaryStage.setScene(new Scene(new StackPane(imageView), WIDTH, HEIGHT));
            primaryStage.show();
        }

        private void createGradient(PixelWriter pixelWriter, int xStart, int yStart, int xEnd, int yEnd, Color color1, Color color2) {

            for (int x = xStart; x < xEnd; x++) {
                for (int y = yStart; y < yEnd; y++) {
                double ratio = (double) (x - xStart) / (xEnd - xStart);
                int red = (int) ((1 - ratio) * color1.getRed() * 255 + ratio * color2.getRed() * 255);
                int green = (int) ((1 - ratio) * color1.getGreen() * 255 + ratio * color2.getGreen() * 255);
                int blue = (int) ((1 - ratio) * color1.getBlue() * 255 + ratio * color2.getBlue() * 255);

                pixelWriter.setArgb(x, y, (255 << 24) | (red << 16) | (green << 8) | blue);
            }
            }
        }*/
}