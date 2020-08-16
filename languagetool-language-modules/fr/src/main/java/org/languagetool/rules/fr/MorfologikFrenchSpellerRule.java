/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.languagetool.org)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.rules.fr;

import org.languagetool.*;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.fr.FrenchTagger;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MorfologikFrenchSpellerRule extends MorfologikSpellerRule {

  private String dictFilename;
  private static final String SPELLING_FILE = "/fr/hunspell/spelling.txt";

  private static final Pattern PARTICULA_INICIAL = Pattern.compile(
      "^(non|en|a|le|la|les|pour|de|du|des|un|une|mon|ma|mes|ton|ta|tes|son|sa|ses|leur|leurs|ce|cet) (..+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIX_AMB_ESPAI = Pattern.compile(
      "^(auto|ex|extra|macro|mega|meta|micro|multi|mono|mini|post|retro|semi|super|trans) (..+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern APOSTROF_INICI_VERBS = Pattern.compile("^([clnts])(h?[aeiouàéèíòóú].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_VERBS_M = Pattern.compile("^(m)(h?[aeiouàéèíòóú].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_SING = Pattern.compile("^([ld])(h?[aeiouàéèíòóú]...+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = Pattern.compile("^(d)(h?[aeiouàéèíòóú].+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  
  private static final Pattern GUIONET_FINAL = Pattern.compile(
      "^([\\p{L}]+)[’']?(ce|elle|elles|en|il|ils|je|la|le|les|leur|lui|moi|nous|on|toi|tu|vous|vs|y)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  //private static final Pattern MOVE_TO_SECOND_POS = Pattern.compile("^(.+'[nt])$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V .*(ind|sub).*");
  private static final Pattern VERB_INDSUBJ_M = Pattern.compile("V .* [123] s.*|V .*[23] p.*");
  private static final Pattern NOM_SING = Pattern.compile("[NJ] .* s|V .inf|V .*ppa.* s");
  private static final Pattern NOM_PLURAL = Pattern.compile("[NJ] .* p|V .*ppa.* p");
  //private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");
  //private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private FrenchTagger tagger;

  public MorfologikFrenchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
    tagger = new FrenchTagger();
    dictFilename = "/fr/french.dict";
  }

  @Override
  public String getFileName() {
    return dictFilename;
  }

  @Override
  public String getSpellingFileName() {
    return SPELLING_FILE;
  }

  @Override
  public String getId() {
    return "FR_SPELLING_RULE"; // same name just for diffs
  }

  @Override
  // Use this rule in LO/OO extension despite being a spelling rule
  public boolean useInOffice() {
    return true;
  }

  @Override
  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    // move some run-on-words suggestions to the top
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    // for (SuggestedReplacement suggestion : suggestions) {
    for (int i = 0; i < suggestions.size(); i++) {

      // remove wrong split prefixes
      if (PREFIX_AMB_ESPAI.matcher(suggestions.get(i).getReplacement()).matches()) {
        continue;
      }

      // move some split words to first place
      Matcher matcher = PARTICULA_INICIAL.matcher(suggestions.get(i).getReplacement());
      if (matcher.matches()) {
        newSuggestions.add(0, suggestions.get(i));
        continue;
      }

      // move words with apostrophe or hyphen to second position
      String cleanSuggestion = suggestions.get(i).getReplacement().replaceAll("'", "").replaceAll("-", "");
      if (i > 1 && suggestions.size() > 2 && cleanSuggestion.equalsIgnoreCase(word)) {
        newSuggestions.add(1, suggestions.get(i));
        continue;
      }

      // move "queda'n" to second position
      /*if (i == 1) {
        Matcher m = MOVE_TO_SECOND_POS.matcher(suggestions.get(0).getReplacement());
        if (m.matches()) {
          newSuggestions.add(0, suggestions.get(i));
          continue;
        }
      }*/

      newSuggestions.add(suggestions.get(i));

    }
    return newSuggestions;
  }

  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word)
      throws IOException {
    List<String> suggestionsList = suggestions.stream().map(SuggestedReplacement::getReplacement)
        .collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    /*
     * if (word.length() < 5) { return Collections.emptyList(); }
     */
    String suggestion = "";
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_VERBS, VERB_INDSUBJ, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_VERBS_M, VERB_INDSUBJ_M, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_SING, NOM_SING, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_PLURAL, NOM_PLURAL, 2, "'", true);
    //suggestion = findSuggestion(suggestion, word, APOSTROF_FINAL, VERB_INFGERIMP, 1, "'", true);
    //suggestion = findSuggestion(suggestion, word, APOSTROF_FINAL_S, VERB_INF, 1, "'", true);
    suggestion = findSuggestion(suggestion, word, GUIONET_FINAL, VERB_INDSUBJ, 1, "-", true);
    if (!suggestion.isEmpty()) {
      return Collections.singletonList(suggestion);
    }
    return Collections.emptyList();
  }

  private String findSuggestion(String suggestion, String word, Pattern wordPattern, Pattern postagPattern,
      int suggestionPosition, String separator, boolean recursive) throws IOException {
    if (!suggestion.isEmpty()) {
      return suggestion;
    }
    Matcher matcher = wordPattern.matcher(word);
    if (matcher.matches()) {
      String newSuggestion = matcher.group(suggestionPosition);
      AnalyzedTokenReadings newatr = tagger.tag(Arrays.asList(newSuggestion)).get(0);
      if (!newatr.hasPosTag("VMIP1S0B") && matchPostagRegexp(newatr, postagPattern)) {
        return matcher.group(1) + separator + matcher.group(2);
      }
      if (recursive) {
        List<String> moresugg = this.speller1.getSuggestions(newSuggestion);
        if (moresugg.size() > 0) {
          String newWord;
          if (suggestionPosition == 1) {
            newWord = moresugg.get(0).toLowerCase() + matcher.group(2);
          } else {
            newWord = matcher.group(1) + moresugg.get(0).toLowerCase();
          }
          return findSuggestion(suggestion, newWord, wordPattern, postagPattern, suggestionPosition, separator, false);
        }
      }
    }
    return "";
  }

  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

}
