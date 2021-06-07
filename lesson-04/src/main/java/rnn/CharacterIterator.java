package rnn;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class CharacterIterator implements DataSetIterator {
    private static final long serialVersionUID = -7287833919126626356L;
    private static final int MAX_SCAN_LENGTH = 200;
    private final int numCharacters;
    private final boolean alwaysStartAtNewLine;
    private char[] validCharacters;
    private Map<Character, Integer> charToIdxMap;
    private char[] fileCharacters;
    private int exampleLength;
    private int miniBatchSize;
    private int numExamplesToFetch;
    private int examplesSoFar = 0;
    private Random rng;

    public CharacterIterator(String path, int miniBatchSize, int exampleSize, int numExamplesToFetch) throws IOException {
        this(path, Charset.defaultCharset(), miniBatchSize, exampleSize, numExamplesToFetch, getDefaultCharacterSet(), new Random(), true);
    }

    public CharacterIterator(String textFilePath, Charset textFileEncoding, int miniBatchSize, int exampleLength,
                             int numExamplesToFetch, char[] validCharacters, Random rng, boolean alwaysStartAtNewLine) throws IOException {
        if (!new File(textFilePath).exists())
            throw new IOException("Could not access file (does not exist): " + textFilePath);
        if (numExamplesToFetch % miniBatchSize != 0)
            throw new IllegalArgumentException("numExamplesToFetch must be a multiple of miniBatchSize");
        if (miniBatchSize <= 0) throw new IllegalArgumentException("Invalid miniBatchSize (must be >0)");
        this.validCharacters = validCharacters;
        this.exampleLength = exampleLength;
        this.miniBatchSize = miniBatchSize;
        this.numExamplesToFetch = numExamplesToFetch;
        this.rng = rng;
        this.alwaysStartAtNewLine = alwaysStartAtNewLine;

        charToIdxMap = new HashMap<>();
        for (int i = 0; i < validCharacters.length; i++) charToIdxMap.put(validCharacters[i], i);
        numCharacters = validCharacters.length;

        boolean newLineValid = charToIdxMap.containsKey('\n');
        List<String> lines = Files.readAllLines(new File(textFilePath).toPath(), textFileEncoding);
        int maxSize = lines.size();
        for (String s : lines) maxSize += s.length();
        char[] characters = new char[maxSize];
        int currIdx = 0;
        for (String s : lines) {
            char[] thisLine = s.toCharArray();
            for (int i = 0; i < thisLine.length; i++) {
                if (!charToIdxMap.containsKey(thisLine[i])) continue;
                characters[currIdx++] = thisLine[i];
            }
            if (newLineValid) characters[currIdx++] = '\n';
        }

        if (currIdx == characters.length) {
            fileCharacters = characters;
        } else {
            fileCharacters = Arrays.copyOfRange(characters, 0, currIdx);
        }
        if (exampleLength >= fileCharacters.length) throw new IllegalArgumentException("exampleLength=" + exampleLength
                + " cannot exceed number of valid characters in file (" + fileCharacters.length + ")");

        int nRemoved = maxSize - fileCharacters.length;
        System.out.println("Loaded and converted file: " + fileCharacters.length + " valid characters of "
                + maxSize + " total characters (" + nRemoved + " removed)");
    }

    public static char[] getMinimalCharacterSet() {
        List<Character> validChars = new LinkedList<>();
        //for (char c = 'a'; c <= 'z'; c++) validChars.add(c);
        //for (char c = 'A'; c <= 'Z'; c++) validChars.add(c);
        for (char c = 'А'; c <= 'Я'; c++) validChars.add(c);
        for (char c = 'а'; c <= 'я'; c++) validChars.add(c);
        for (char c = '0'; c <= '9'; c++) validChars.add(c);
        char[] temp = {'!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\n', '\t'};
        for (char c : temp) validChars.add(c);
        char[] out = new char[validChars.size()];
        int i = 0;
        for (Character c : validChars) out[i++] = c;
        return out;
    }

    public static char[] getDefaultCharacterSet() {
        List<Character> validChars = new LinkedList<>();
        for (char c : getMinimalCharacterSet()) validChars.add(c);
        char[] additionalChars = {'@', '#', '$', '%', '^', '*', '{', '}', '[', ']', '/', '+', '_',
                '\\', '|', '<', '>'};
        for (char c : additionalChars) validChars.add(c);
        char[] out = new char[validChars.size()];
        int i = 0;
        for (Character c : validChars) out[i++] = c;
        return out;
    }

    public char convertIndexToCharacter(int idx) {
        return validCharacters[idx];
    }

    public int convertCharacterToIndex(char c) {
        return charToIdxMap.get(c);
    }

    public char getRandomCharacter() {
        return validCharacters[(int) (rng.nextDouble() * validCharacters.length)];
    }

    public boolean hasNext() {
        return examplesSoFar + miniBatchSize <= numExamplesToFetch;
    }

    public DataSet next() {
        return next(miniBatchSize);
    }

    public DataSet next(int num) {
        if (examplesSoFar + num > numExamplesToFetch) throw new NoSuchElementException();
        INDArray input = Nd4j.zeros(new int[]{num, numCharacters, exampleLength});
        INDArray labels = Nd4j.zeros(new int[]{num, numCharacters, exampleLength});

        int maxStartIdx = fileCharacters.length - exampleLength;

        for (int i = 0; i < num; i++) {
            int startIdx = (int) (rng.nextDouble() * maxStartIdx);
            int endIdx = startIdx + exampleLength;
            int scanLength = 0;
            if (alwaysStartAtNewLine) {
                while (startIdx >= 1 && fileCharacters[startIdx - 1] != '\n' && scanLength++ < MAX_SCAN_LENGTH) {
                    startIdx--;
                    endIdx--;
                }
            }

            int currCharIdx = charToIdxMap.get(fileCharacters[startIdx]);    //Current input
            int c = 0;
            for (int j = startIdx + 1; j <= endIdx; j++, c++) {
                int nextCharIdx = charToIdxMap.get(fileCharacters[j]);        //Next character to predict
                input.putScalar(new int[]{i, currCharIdx, c}, 1.0);
                labels.putScalar(new int[]{i, nextCharIdx, c}, 1.0);
                currCharIdx = nextCharIdx;
            }
        }

        examplesSoFar += num;
        return new DataSet(input, labels);
    }

    public int totalExamples() {
        return numExamplesToFetch;
    }

    public int inputColumns() {
        return numCharacters;
    }

    public int totalOutcomes() {
        return numCharacters;
    }

    public void reset() {
        examplesSoFar = 0;
    }

    public int batch() {
        return miniBatchSize;
    }

    public int cursor() {
        return examplesSoFar;
    }

    public int numExamples() {
        return numExamplesToFetch;
    }

    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}