package com.cesi.decode.services;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Stateless
public class DecodeService implements DecodeServiceLocal {
    @Resource(lookup = "jdbc/words")
    DataSource dataSource;
    private final static String REGEX = "(?<=L'information secrète est : ).[^.]*";

    /**
     * Check if the file is a correct file in french language.
     *
     * @param content - The content of the file.
     * @return true if it's a french document else false;
     * @throws SQLException SQLException thrown if something goes wrong.
     */
    @Override
    public boolean isFrench(String content) throws SQLException {
        ArrayList<String> dictionary = getDictionary();
        List<String> words = Arrays.asList(content.split(" |'"));
        return getConfidence(words, dictionary, 0.2);
    }

    /**
     * Search secret.
     *
     * @param content - The content of the file.
     * @return The secret if exist.
     */
    @Override
    public String searchSecret(String content) {
        return findSecret(content);
    }

    /**
     * Convert a byte array in a String to a String.
     *
     * @param content - The array byte corresponding to the content of the file.
     * @return The content od the file.
     */
    @Override
    public String byteToString(String content) {
        List<String> bits = Arrays.asList(content.split(","));
        byte[] byteArray = new byte[bits.size()];
        for (int i = 0; i < bits.size(); i++) {
            int bitValue = Integer.parseInt(bits.get(i));
            if (bitValue > 127) {
                bitValue = (256 - bitValue) * (-1);
            }
            byteArray[i] = (byte) bitValue;
        }
        String newContent = new String(byteArray);
        newContent = newContent.replace("\u0000", "");
        newContent = newContent.replace("\\u0000", "");
        return newContent;
    }

    /**
     * Get the words from the database.
     *
     * @return The words from the database.
     * @throws SQLException SQLException thrown if something goes wrong.
     */
    private ArrayList<String> getDictionary() throws SQLException {
        ArrayList<String> stringArrayList = new ArrayList<>();
        java.sql.Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery("select * from words");
        while (resultSet.next()) {
            stringArrayList.add(resultSet.getString(1));
        }
        conn.close();
        return stringArrayList;
    }

    /**
     * Check if the file is in french language.
     *
     * @param words - The words from the file.
     * @param dictionary - The words from the database.
     * @param rate - The rate of error.
     * @return true if superior to the given rate.
     */
    private boolean getConfidence(List<String> words, ArrayList<String> dictionary, double rate) {
        if (words.size() < 5) {
            return false;
        }
        int wordsCount = 0;
        int wordsLength = words.size();
        double wordsToCount = wordsLength * rate;

        for (String word : words) {
            if (dictionary.contains(word.toLowerCase())) {
                wordsCount++;
            }
            if (wordsCount >= wordsToCount)
                break;
        }
        return wordsCount >= wordsToCount;
    }

    /**
     * Find the secret in the file.
     *
     * @param content - The content of the file.
     * @return The secret if exist.
     */
    private String findSecret(String content) {
        // Regex to find an email address
        Pattern p = Pattern.compile(REGEX);
        // Search if there is a match with the regex
        Matcher m = p.matcher(content);

        String result = "false";
        if (m.find()) {
            result = m.group();
        }
        System.out.println("The result is : " + result);
        return result;
    }
}
