package com.concordia.qualiair;

import org.junit.Test;
import static org.junit.Assert.*;

public class FAQUnitTest {

    @Test
    public void faqItem_storesQuestionCorrectly() {
        FAQItem item = new FAQItem("What is NH3?", "It is ammonia.");
        assertEquals("What is NH3?", item.getQuestion());
    }

    @Test
    public void faqItem_storesAnswerCorrectly() {
        FAQItem item = new FAQItem("What is NH3?", "It is ammonia.");
        assertEquals("It is ammonia.", item.getAnswer());
    }

    @Test
    public void faqItem_emptyQuestion_doesNotThrow() {
        FAQItem item = new FAQItem("", "Some answer");
        assertEquals("", item.getQuestion());
    }

    @Test
    public void faqItem_emptyAnswer_doesNotThrow() {
        FAQItem item = new FAQItem("Some question", "");
        assertEquals("", item.getAnswer());
    }
}