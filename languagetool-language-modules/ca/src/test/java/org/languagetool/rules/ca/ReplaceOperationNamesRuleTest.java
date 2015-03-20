/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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
package org.languagetool.rules.ca;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

/**
 * @author Jaume Ortolà
 */
public class ReplaceOperationNamesRuleTest extends TestCase {

  private ReplaceOperationNamesRule rule;
  private JLanguageTool         langTool;

  @Override
  public void setUp() throws IOException {
    rule = new ReplaceOperationNamesRule(null);
    langTool = new JLanguageTool(new Catalan());
  }

  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("Va aconseguir el liderat de primera divisió.");
    assertCorrect("el llibre empaquetat");
    assertCorrect("un resultat equilibrat");
    assertCorrect("el nostre equip era bastant equilibrat");
    assertCorrect("un llibre ben empaquetat");
    assertCorrect("l'informe filtrat pel ministre");
    assertCorrect("L'informe filtrat és terrible");
    assertCorrect("ha liderat la batalla");
    assertCorrect("L'equip, liderat pel capità, aconseguí la victòria.");
    assertCorrect("L'equip, ben liderat, podria arribar lluny.");
    assertCorrect("Els tinc empaquetats");
    assertCorrect("amb tractament unitari i equilibrat");
    assertCorrect("Processat després de la mort de Carles II");
    assertCorrect("Processat diverses vegades");
    assertCorrect("moltes vegades empaquetat amb pressa");
    assertCorrect("és llavors embotellat i llançat al mercat");
    assertCorrect("la comercialització de vi embotellat amb les firmes comercials");
    assertCorrect("eixia al mercat el vi blanc embotellat amb la marca");
    assertCorrect("que arribi a un equilibrat matrimoni");
    assertCorrect("el liderat per Kerensky");
    assertCorrect("sector enfrontat al liderat pel qui havia estat secretari general");
    assertCorrect("en dos grups, un liderat per");
    assertCorrect("liderats per Ria Beckers");
    assertCorrect("És un cafè amb molt de cos i molt equilibrat.");
    assertCorrect("i per tant etiquetat com a observat");
    assertCorrect("Molt equilibrat en les seves característiques");
    assertCorrect("filtrat per Wikileaks");
    assertCorrect("una vegada filtrat");
    assertCorrect("no equilibrat");
    
    // errors:
    assertIncorrect("Cal vigilar el filtrat del vi");
    assertIncorrect("El procés d'empaquetat");
    assertIncorrect("Els equilibrats de les rodes");
    assertIncorrect("Duplicat de claus");
    assertIncorrect("El procés d'etiquetat de les ampolles");
    assertIncorrect("El rentat de cotes");

    

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("El repicat i el rejuntat."));
    assertEquals(2, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("El procés de relligat dels llibres."));
    assertEquals(1, matches.length);
    assertEquals ("relligadura", matches[0].getSuggestedReplacements().get(0));
    assertEquals ("relligament", matches[0].getSuggestedReplacements().get(1));
    assertEquals ("relligada", matches[0].getSuggestedReplacements().get(2));
    
    
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

  public void testPositions() throws IOException {
    final AccentuationCheckRule rule = new AccentuationCheckRule(null);
    final RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(new Catalan());

    matches = rule.match(langTool
        .getAnalyzedSentence("Són circumstancies extraordinàries."));
    assertEquals(4, matches[0].getFromPos());
    assertEquals(18, matches[0].getToPos());
  }

}
