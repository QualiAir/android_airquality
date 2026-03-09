package com.concordia.qualiair;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Tests for FAQ classes (FAQItem, FAQAdapter logic)
 * Location: src/test/java/com/concordia/qualiair/FAQUnitTest.java
 *
 * Run with: ./gradlew test
 */
@RunWith(JUnit4.class)
public class FAQUnitTest {

    private FAQItem item;

    @Before
    public void setUp() {
        item = new FAQItem("Support contact?", "Email us at elec390team1@gmail.com.");
    }

    // -------------------------------------------------------------------------
    // FAQItem — constructor & getters
    // -------------------------------------------------------------------------

    @Test
    public void testFAQItem_getQuestion_returnsCorrectQuestion() {
        assertEquals("Support contact?", item.getQuestion());
    }

    @Test
    public void testFAQItem_getAnswer_returnsCorrectAnswer() {
        assertEquals("Email us at elec390team1@gmail.com.", item.getAnswer());
    }

    @Test
    public void testFAQItem_defaultExpanded_isFalse() {
        assertFalse("New FAQItem should not be expanded by default", item.isExpanded());
    }

    // -------------------------------------------------------------------------
    // FAQItem — expand / collapse toggle
    // -------------------------------------------------------------------------

    @Test
    public void testFAQItem_setExpanded_true_isExpanded() {
        item.setExpanded(true);
        assertTrue("FAQItem should be expanded after setExpanded(true)", item.isExpanded());
    }

    @Test
    public void testFAQItem_setExpanded_false_isCollapsed() {
        item.setExpanded(true);  // first open it
        item.setExpanded(false); // then close it
        assertFalse("FAQItem should be collapsed after setExpanded(false)", item.isExpanded());
    }

    @Test
    public void testFAQItem_toggle_expandsAndCollapses() {
        // Simulate the toggle pattern used in FAQAdapter's click listener
        assertFalse(item.isExpanded());

        item.setExpanded(!item.isExpanded()); // first tap → open
        assertTrue(item.isExpanded());

        item.setExpanded(!item.isExpanded()); // second tap → close
        assertFalse(item.isExpanded());
    }

    // -------------------------------------------------------------------------
    // FAQItem — multiple items, independent state
    // -------------------------------------------------------------------------

    @Test
    public void testFAQItems_expandOneDoesNotAffectOther() {
        FAQItem item1 = new FAQItem("Q1", "A1");
        FAQItem item2 = new FAQItem("Q2", "A2");

        item1.setExpanded(true);

        assertTrue("item1 should be expanded", item1.isExpanded());
        assertFalse("item2 should remain collapsed", item2.isExpanded());
    }

    // -------------------------------------------------------------------------
    // FAQAdapter — item count
    // -------------------------------------------------------------------------

    @Test
    public void testFAQAdapter_getItemCount_emptyList_returnsZero() {
        List<FAQItem> emptyList = new ArrayList<>();
        FakeFAQAdapter adapter = new FakeFAQAdapter(emptyList);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testFAQAdapter_getItemCount_oneItem_returnsOne() {
        List<FAQItem> list = new ArrayList<>();
        list.add(new FAQItem("Q1", "A1"));
        FakeFAQAdapter adapter = new FakeFAQAdapter(list);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testFAQAdapter_getItemCount_multipleItems_returnsCorrectCount() {
        List<FAQItem> list = new ArrayList<>();
        list.add(new FAQItem("Q1", "A1"));
        list.add(new FAQItem("Q2", "A2"));
        list.add(new FAQItem("Q3", "A3"));
        FakeFAQAdapter adapter = new FakeFAQAdapter(list);
        assertEquals(3, adapter.getItemCount());
    }

    // -------------------------------------------------------------------------
    // FAQAdapter — toggle logic (no Android context needed)
    // -------------------------------------------------------------------------

    @Test
    public void testFAQAdapter_clickItem_togglesExpandedState() {
        List<FAQItem> list = new ArrayList<>();
        FAQItem faq = new FAQItem("Q1", "A1");
        list.add(faq);

        // Simulate the click handler: item.setExpanded(!item.isExpanded())
        assertFalse(faq.isExpanded());
        faq.setExpanded(!faq.isExpanded());
        assertTrue("Item should be expanded after first click", faq.isExpanded());
    }

    @Test
    public void testFAQAdapter_clickItemTwice_collapsesItem() {
        FAQItem faq = new FAQItem("Q1", "A1");

        faq.setExpanded(!faq.isExpanded()); // open
        faq.setExpanded(!faq.isExpanded()); // close

        assertFalse("Item should be collapsed after two clicks", faq.isExpanded());
    }

    // -------------------------------------------------------------------------
    // Fake adapter (no Android dependencies — just tests getItemCount logic)
    // -------------------------------------------------------------------------

    static class FakeFAQAdapter {
        private final List<FAQItem> faqList;

        FakeFAQAdapter(List<FAQItem> faqList) {
            this.faqList = faqList;
        }

        public int getItemCount() {
            return faqList.size();
        }
    }
}