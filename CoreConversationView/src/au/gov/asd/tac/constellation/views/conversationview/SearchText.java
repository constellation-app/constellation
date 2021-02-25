/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * The SearchText class provides static helper methods to search a body of text for some given text, and related information.
 * 
 * @author sol695510
 */
public class SearchText {
    
    private static final Logger LOGGER = Logger.getLogger(SearchText.class.getName());
    private static int logCounter = 1;
    
    /**
     * Returns the number of times the text typed in the search bar is found in the messages currently being displayed in the view.
     * 
     * @param messages A list of ConversationMessage messages.
     * @return The number of matches of the searched text found in the messages.
     */
    protected static int getSearchHits (final List<ConversationMessage> messages) {
        final TextField searchText = ConversationBox.searchBubbleTextField;
        int hits = 0;
        
        if (!searchText.getText().isEmpty()) {
            for (final ConversationMessage message : messages) {
                for (final ConversationContribution contribution : message.getVisibleContributions()) {
                    hits += (StringUtilities.searchRange(contribution.getText(), searchText.getText())).size();
                    
                    LOGGER.log(Level.INFO, searchText.getText()
                            + "|" + String.valueOf(logCounter)
                            + "|" + contribution.getText()
                            + "|" + String.valueOf(hits));
                    logCounter++;
                }
            }
        }
        
        return hits;
    }
    
    /**
     * Find searched text in a given String and return a TextFlow formatted with the searched text highlighted.
     * 
     * @param textFlow TextFlow to add formatted Text elements to.
     * @param text String to be searched for specified text.
     * @return TextFlow region formatted with searched text highlighted.
     */
    protected static TextFlow createRegionWithSearchHits(final TextFlow textFlow, final String text) {
        final TextField searchText = ConversationBox.searchBubbleTextField;
        
        // If the search bar is not empty and has text in it.
        if (!searchText.getText().isEmpty()) {
            List<Tuple<Integer, Integer>> textResults = StringUtilities.searchRange(text, searchText.getText());

            // If the text in the search bar appears in the given conversation text.
            if (!textResults.isEmpty()) {
                int textStart = 0;
                int counter = 0;
                final List<Text> textList = new ArrayList<>();
                
                for (final Tuple<Integer, Integer> textResult : textResults) {
                    final Text beforeSearched = new Text(text.substring(textStart, textResult.getFirst()));
                    final Text textSearched = new Text(text.substring(textResult.getFirst(), textResult.getSecond()));
                    
                    // Yellow fill and shadow effect added to search result to provide better contrast.
                    textSearched.setStyle("-fx-fill: yellow; -fx-effect: dropshadow(gaussian, black, 5.0, 0.0, 0.0, 0.0);");
                    
                    /**
                     * If 'textStart' is not equal to the inclusive start index of the new search result,
                     * add any text that is present before the search result to 'textList'.
                     * 
                     * When 'textStart' is equal to the inclusive start index of the new search result,
                     * the search result could be "o" which would appear one after another in the word "look",
                     * thus any text before the search would be not be added to 'textList',
                     * since there is not any other text other than the next search result.
                    */
                    if (textStart != textResult.getFirst()) {
                        textList.add(beforeSearched);
                    }
                    
                    // Add 'textSearched', the styled search result, to textList.
                    textList.add(textSearched);
                    
                    // Set the next 'textStart' to the exclusive end index of 'textResult',
                    // so the next iteration will begin at the index after the previous search result.
                    textStart = textResult.getSecond();
                    counter++;
                    
                    // At the last search result instance, add any remaining text after the search result to 'textList'.
                    if (counter == textResults.size()) {
                        final Text afterSearched = new Text(text.substring(textStart,text.length()));
                        textList.add(afterSearched);
                    }
                }
                
                textFlow.getChildren().addAll(textList);
                return textFlow;
            }
        }
        
        textFlow.getChildren().add(new Text(text));
        return textFlow;
    }
}
