package rnn;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MovieTitleInventor {

    private static final Random randGenerator = new Random(12345);
    private static final Charset defaultCharset = StandardCharsets.UTF_8;

    public static void main(String[] args) throws Exception {
        int lstmLayerSize = 200; //Количество нейронов в каждое слое GravesLSTM
        int miniBatchSize = 32; //Количество пачки образцов для обучения
        int examplesPerEpoch = 100 * miniBatchSize; //то есть, сколько примеров выучить между генерацией образцов
        int exampleLength = 40; //Длина каждого обучающего образца
        int numEpochs = 1000; //Общее количество итераций обучения + генерации
        int nSamplesToGenerate = 100; //Количество генерируемых после эпохи обучения образцов
        int nCharactersToSample = 40; //Длина генерируемого образца
        String generationInitialization = null; //Строка для инициализации сети, если не задана, используется
        // рандомная последовательность символов. Символы должны входить в множество, определенное
        // CharacterIterator.getMinimalCharacterSet()

        //Получает DataSetIterator который векторизирует текст во что-то пригодное для обучения нейронки
        CharacterIterator iter = getIMDBIterator(miniBatchSize, exampleLength, examplesPerEpoch);
        int nOut = iter.totalOutcomes();

        //Установка конфигурации нейронки
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.1)
                .rmsDecay(0.95)
                .seed(12345)
                .regularization(true)
                .l2(0.001)
                .list(3)
                .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                        .updater(Updater.RMSPROP)
                        .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
                        .dist(new UniformDistribution(-0.08, 0.08)).build())
                .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .updater(Updater.RMSPROP)
                        .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
                        .dist(new UniformDistribution(-0.08, 0.08)).build())
                .layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation("softmax")        //MCXENT + softmax for classification
                        .updater(Updater.RMSPROP)
                        .nIn(lstmLayerSize).nOut(nOut).weightInit(WeightInit.DISTRIBUTION)
                        .dist(new UniformDistribution(-0.08, 0.08)).build())
                .pretrain(false).backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        //Выводит количество параметров в сети для каждого слоя
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println("Количество параметров в слое " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Всего параметров в нейронной сети: " + totalNumParams);

        //Обучаем, а потом генерируем образцы
        for (int i = 0; i < numEpochs; i++) {
            net.fit(iter);

            System.out.println("--------------------");
            System.out.println("Завершена итерация обучения " + i);
            System.out.println("Сэмплируем символы из заданной строки инициализации: \"" +
                    (generationInitialization == null ? "" : generationInitialization) + "\"");
            String[] samples = sampleCharactersFromNetwork(generationInitialization, net, iter,
                    nCharactersToSample, nSamplesToGenerate);
            for (int j = 0; j < samples.length; j++) {
                System.out.println("----- Образец " + j + " -----");
                System.out.println(samples[j]);
                System.out.println();
            }
            iter.reset(); //Сбрасываем итератор для следующей итерации обучения
        }

        System.out.println("\n\nПример завершен");
    }

    private static CharacterIterator getIMDBIterator(int miniBatchSize, int exampleLength, int examplesPerEpoch) throws Exception {
        String fileLocation = "D:\\Work\\Наборы данных\\2021-06-02\\russian titles.txt";
        File f = new File(fileLocation);

        if (!f.exists()) throw new IOException("File does not exist: " + fileLocation);    //Download problem?

        char[] validCharacters = CharacterIterator.getMinimalCharacterSet();    //Which characters are allowed? Others will be removed
        return new CharacterIterator(fileLocation, defaultCharset,
                miniBatchSize, exampleLength, examplesPerEpoch, validCharacters,
                randGenerator, true);
    }

    private static String[] sampleCharactersFromNetwork(
            String initialization, MultiLayerNetwork net,
            CharacterIterator iter, int charactersToSample, int numSamples) {
        if (initialization == null) {
            initialization = String.valueOf(iter.getRandomCharacter());
        }

        //Create input for initialization
        INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), initialization.length());
        char[] init = initialization.toCharArray();
        for (int i = 0; i < init.length; i++) {
            int idx = iter.convertCharacterToIndex(init[i]);
            for (int j = 0; j < numSamples; j++) {
                initializationInput.putScalar(new int[]{j, idx, i}, 1.0f);
            }
        }

        StringBuilder[] sb = new StringBuilder[numSamples];
        for (int i = 0; i < numSamples; i++) sb[i] = new StringBuilder(initialization);

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        //Sampling is done in parallel here
        net.rnnClearPreviousState();
        INDArray output = net.rnnTimeStep(initializationInput);
        output = output.tensorAlongDimension(output.size(2) - 1, 1, 0);    //Gets the last time step output

        for (int i = 0; i < charactersToSample; i++) {
            //Set up next input (single time step) by sampling from previous output
            INDArray nextInput = Nd4j.zeros(numSamples, iter.inputColumns());
            //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
            for (int s = 0; s < numSamples; s++) {
                double[] outputProbDistribution = new double[iter.totalOutcomes()];
                for (int j = 0; j < outputProbDistribution.length; j++)
                    outputProbDistribution[j] = output.getDouble(s, j);
                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution);

                nextInput.putScalar(new int[]{s, sampledCharacterIdx}, 1.0f);        //Prepare next time step input
                sb[s].append(iter.convertIndexToCharacter(sampledCharacterIdx));    //Add sampled character to StringBuilder (human readable output)
            }

            output = net.rnnTimeStep(nextInput);    //Do one time step of forward pass
        }

        String[] out = new String[numSamples];
        for (int i = 0; i < numSamples; i++) out[i] = sb[i].toString();
        return out;
    }

    private static int sampleFromDistribution(double[] distribution) {
        double d = randGenerator.nextDouble();
        double sum = 0.0;
        for (int i = 0; i < distribution.length; i++) {
            sum += distribution[i];
            if (d <= sum) return i;
        }
        throw new IllegalArgumentException("Distribution is invalid? d=" + d + ", sum=" + sum);
    }

}