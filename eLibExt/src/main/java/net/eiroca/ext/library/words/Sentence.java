/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.ext.library.words;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Italian;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import net.eiroca.ext.library.words.WordInfo.WordType;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.csv.CSVData;
import net.eiroca.library.csv.CSVMap;

public class Sentence {

  private static final char COMMENT = '#';
  private static final String ENCODING = "UTF-8";

  private static CSVMap validWords = new CSVMap();
  private static CSVMap replaces = new CSVMap();
  private static CSVMap alias = new CSVMap();
  private static List<String[]> preferred;

  private static final Map<String, WordInfo> words = new HashMap<>();

  public static int wordMinSize = 5;

  private final StringBuilder sb = new StringBuilder();
  private final StringBuilder word = new StringBuilder();
  private String text;

  private char lastChar;
  private boolean lastCharIsWordable;
  private boolean hasDigit;
  private boolean isNumber;

  private static final ThreadLocal<JLanguageTool> langToolIT = new ThreadLocal<JLanguageTool>() {

    @Override
    protected JLanguageTool initialValue() {
      final JLanguageTool tool = new JLanguageTool(new Italian());
      for (final Rule rule : tool.getAllActiveRules()) {
        if (!(rule instanceof SpellingCheckRule)) {
          tool.disableRule(rule.getId());
        }
      }
      return tool;
    }
  };

  private static final ThreadLocal<JLanguageTool> langToolEN = new ThreadLocal<JLanguageTool>() {

    @Override
    protected JLanguageTool initialValue() {
      final JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
      for (final Rule rule : tool.getAllActiveRules()) {
        if (!(rule instanceof SpellingCheckRule)) {
          tool.disableRule(rule.getId());
        }
      }
      return tool;
    }
  };

  public static void loadConf(final String validWordFile, final String wordReplaceFile, final String aliasFile, final String preferredFile, final char sep) {
    Sentence.validWords = new CSVMap(validWordFile, sep, (char)0, Sentence.COMMENT, Sentence.ENCODING);
    Sentence.replaces = new CSVMap(wordReplaceFile, sep, (char)0, Sentence.COMMENT, Sentence.ENCODING);
    Sentence.alias = new CSVMap(aliasFile, sep, (char)0, Sentence.COMMENT, Sentence.ENCODING);
    Sentence.preferred = new CSVData(preferredFile, sep, (char)0, Sentence.COMMENT, Sentence.ENCODING).getData();
  }

  public static void saveStats(final String path) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(path));
      bw.write("word\tnewWord\tsuggested\ttype\tcount\n");
      for (final Entry<String, WordInfo> entry : Sentence.words.entrySet()) {
        bw.write(entry.getValue().toString() + "\n");
      }
    }
    catch (final IOException e) {
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(bw);
    }
  }

  public Sentence(final String aText) {
    normalize(aText);
  }

  public void open() {
    text = null;
    sb.setLength(0);
    word.setLength(0);
    lastChar = ' ';
    lastCharIsWordable = false;
    hasDigit = false;
    isNumber = false;
    sb.append(lastChar);
  }

  public void close() {
    addNormalizeChar(' ');
    text = sb.toString();
    sb.setLength(0);
    word.setLength(0);
  }

  public static boolean isWordable(final char ch) {
    return (Character.isAlphabetic(ch) || Character.isDigit(ch) || (ch == '\''));
  }

  public void addNormalizeChar(char ch) {
    if (((ch >= (char)0) && (ch <= (char)31)) || (ch == (char)160)) {
      ch = ' ';
    }
    else if ((ch == '`') || (ch == '’')) {
      ch = ' ';
    }
    else if (ch == (char)96) {
      ch = '\'';
    }
    else {
      ch = Character.toLowerCase(ch);
    }
    if ((ch == ' ') && (lastChar == ' ')) { return; }
    final boolean isWordable = Sentence.isWordable(ch);
    final boolean isDigit = Character.isDigit(ch);
    hasDigit = hasDigit || isDigit;
    isNumber = isNumber && (Character.isDigit(ch) || (ch == '.'));
    int state;
    if (isWordable && lastCharIsWordable) {
      state = 0;
    }
    else if (isWordable && !lastCharIsWordable) {
      state = 1;
    }
    else if (!isWordable && lastCharIsWordable) {
      state = 2;
      final String wordStr = word.toString();
      if ((ch != ' ') && (wordStr.startsWith("http") || wordStr.startsWith("https"))) {
        state = 0;
      }
      else if (isNumber) {
        state = 0;
      }
    }
    else if (!isWordable && !lastCharIsWordable) {
      state = 3;
    }
    else {
      state = -1;
    }
    switch (state) {
      case 0: // in word
        word.append(ch);
        lastChar = ch;
        break;
      case 1: // word start
        word.append(ch);
        if (lastChar != ' ') {
          sb.append(' ');
        }
        lastChar = ch;
        lastCharIsWordable = true;
        hasDigit = isDigit;
        isNumber = isDigit;
        break;
      case 2:// word end
        String theWord = word.toString();
        boolean addDot = false;
        if (theWord.endsWith(".")) {
          theWord = theWord.substring(0, theWord.length() - 1);
          addDot = true;
        }
        processWord(sb, theWord, !hasDigit);
        word.setLength(0);
        if (addDot) {
          sb.append(" .");
        }
        if (ch != ' ') {
          sb.append(' ');
        }
        sb.append(ch);
        lastChar = ch;
        lastCharIsWordable = false;
        hasDigit = false;
        break;
      case 3:// not in word
        sb.append(ch);
        lastChar = ch;
        break;
      default:
        System.err.println("Sentence invalid state");
        break;
    }
  }

  private void processWord(final StringBuilder buffer, final String word, final boolean addUnknown) {
    if (LibStr.isEmptyOrNull(word)) { return; }
    WordInfo info;
    info = Sentence.words.get(word);
    if (info == null) {
      info = new WordInfo(word);
      Sentence.words.put(word, info);
      calcWordOut(info, word);
    }
    info.count++;
    buffer.append(info.newWord);
  }

  private void calcWordOut(final WordInfo info, final String word) {
    info.type = WordType.UNKNOWN;
    String newWord = Sentence.validWords.getData(word, null);
    if (newWord != null) {
      info.type = WordType.WHITELIST;
    }
    else {
      newWord = Sentence.replaces.getData(word, null);
      if (newWord != null) {
        info.type = WordType.BLACKLIST;
      }
      else {
        newWord = word;
        switch (detect(info)) {
          case CORRECTED:
          case CHANGED:
            if ((Sentence.wordMinSize > 0) && (word.length() >= Sentence.wordMinSize)) {
              newWord = info.suggested;
            }
            else {
              newWord = word;
            }
            break;
          default:
            break;
        }
      }
    }
    final StringBuilder sb = new StringBuilder();
    if (newWord.indexOf(' ') > 0) {
      final String[] words = newWord.split("\\s+");
      boolean sep = false;
      for (final String w : words) {
        if (sep) {
          sb.append(' ');
          sep = false;
        }
        sep = calcAlias(sb, w);
      }
    }
    else {
      calcAlias(sb, newWord);
    }
    info.newWord = sb.toString();
  }

  private WordType detect(final WordInfo info) {
    List<RuleMatch> matches;
    final String word = info.word;
    matches = check(Sentence.langToolIT.get(), word);
    if (isValid(word, matches)) {
      info.type = WordType.ITALIAN;
      return info.type;
    }
    final List<RuleMatch> enMatcher = check(Sentence.langToolEN.get(), word);
    if (isValid(word, enMatcher)) {
      info.type = WordType.ENGLISH;
      return info.type;
    }
    boolean found = false;
    if (matches.size() > 0) {
      final List<String> suggested = matches.get(0).getSuggestedReplacements();
      if (suggested.size() > 0) {
        String suggWord = suggested.get(0).toLowerCase();
        info.type = WordType.CORRECTED;
        info.suggested = suggWord;
        for (int idx = 0; idx < suggested.size(); idx++) {
          suggWord = suggested.get(idx).toLowerCase();
          for (int j = 0; j < Sentence.preferred.size(); j++) {
            if (suggWord.equals(Sentence.preferred.get(j)[0])) {
              found = true;
              info.suggested = suggWord;
              info.type = WordType.CHANGED;
              break;
            }
          }
          if (found) {
            break;
          }
        }
      }
    }
    return info.type;
  }

  private boolean isValid(final String word, final List<RuleMatch> matches) {
    if (matches != null) {
      if (matches.size() == 0) {
        return true;
      }
      else {
        final List<String> suggested = matches.get(0).getSuggestedReplacements();
        if (suggested.size() > 0) {
          final String suggWord = suggested.get(0).toLowerCase();
          if (word.equals(suggWord)) { return true; }
        }
      }
    }
    return false;
  }

  private List<RuleMatch> check(final JLanguageTool langTool, final String word) {
    try {
      return langTool.check(word);
    }
    catch (final IOException e) {
      return null;
    }
  }

  private boolean calcAlias(final StringBuilder buffer, final String word) {
    boolean sep = false;
    String wordToAdd = Sentence.alias.getData(word);
    if (wordToAdd == null) {
      wordToAdd = word;
    }
    if (LibStr.isNotEmptyOrNull(wordToAdd)) {
      buffer.append(wordToAdd);
      sep = true;
    }
    return sep;
  }

  private void normalize(final String note) {
    open();
    if (note != null) {
      for (int i = 0; i < note.length(); i++) {
        addNormalizeChar(note.charAt(i));
      }
    }
    close();
  }

  @Override
  public String toString() {
    return (text != null ? text : (sb.length() > 0) ? sb.toString() : null);
  }

}
