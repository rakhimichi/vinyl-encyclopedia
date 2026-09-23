package com.kirill.vinylencyclopedia.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;

class PetProjectsControllerTest {
    private final PetProjectsController controller = new PetProjectsController();

    @Test
    void freshSessionRequiresPassword() {
        assertEquals("naruto-login", controller.showNarutoTracker(new MockHttpSession()));
    }

    @Test
    void correctPasswordUnlocksOnlyCurrentSessionAndRotatesId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String previousId = request.getSession().getId();
        var attributes = new RedirectAttributesModelMap();
        // Assemble the shared phrase here so it is never part of a served resource.
        String password = String.join("", "I", "LOVE", "SASUKE", "UCHIHA");
        assertEquals("redirect:/pet-projects/naruto", controller.unlockNaruto(password, request, attributes));
        assertNotEquals(previousId, request.getSession().getId());
        assertEquals("naruto-tracker", controller.showNarutoTracker(request.getSession()));
        assertEquals("naruto-login", controller.showNarutoTracker(new MockHttpSession()));
        assertTrue(attributes.getFlashAttributes().isEmpty());
    }

    @Test
    void incorrectPasswordDoesNotGrantAccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        var attributes = new RedirectAttributesModelMap();
        controller.unlockNaruto("wrong", request, attributes);
        assertEquals("naruto-login", controller.showNarutoTracker(request.getSession()));
        assertEquals("Incorrect password. Try again.", attributes.getFlashAttributes().get("passwordError"));
    }

    @Test
    void failedAttemptClearsExistingAccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("narutoAccessGranted", true);
        controller.unlockNaruto("", request, new RedirectAttributesModelMap());
        assertEquals("naruto-login", controller.showNarutoTracker(request.getSession()));
    }
}
