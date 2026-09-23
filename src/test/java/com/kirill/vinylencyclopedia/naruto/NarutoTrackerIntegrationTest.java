package com.kirill.vinylencyclopedia.naruto;

import com.kirill.vinylencyclopedia.repository.NarutoWatchRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class NarutoTrackerIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired NarutoWatchRepository repository;
    @Autowired NarutoTrackerService service;
    private MockMvc mvc;
    private MockHttpSession unlocked;
    private String today;

    @BeforeEach
    void prepare() {
        // Test resources select an isolated in-memory database, never the real collection.
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        unlocked = new MockHttpSession(); unlocked.setAttribute("narutoAccessGranted", true);
        today = LocalDate.now(NarutoStats.ZONE).toString();
    }

    @Test
    void allDataAndMutationsRequireBothLoginAndNarutoPassword() throws Exception {
        mvc.perform(get("/pet-projects/naruto/api/state")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/pet-projects/naruto/api/state").with(user("friend"))).andExpect(status().isForbidden());
        mvc.perform(get("/pet-projects/naruto/api/catalog").with(user("friend"))).andExpect(status().isForbidden());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mark())).andExpect(status().isForbidden());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).session(unlocked)
                .contentType(MediaType.APPLICATION_JSON).content(mark())).andExpect(status().isForbidden());
        assertEquals(0, repository.count());
    }

    @Test
    void sharedMarksPersistAreIdempotentAndCanBeUndoneByTheOtherFriend() throws Exception {
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("first")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mark()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stats.watched").value(1));
        var second = new MockHttpSession(); second.setAttribute("narutoAccessGranted", true);
        mvc.perform(get("/pet-projects/naruto/api/state").with(user("second").roles("ADMIN")).session(second))
                .andExpect(status().isOk()).andExpect(jsonPath("$.marks.s1-ep-1.markedBy").value("first"));
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("second")).session(second).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mark()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stats.watched").value(1));
        assertEquals("first", repository.findById("s1-ep-1").orElseThrow().getMarkedBy());
        var mark = repository.findById("s1-ep-1").orElseThrow();
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("second")).session(second).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"watched\":false,\"expectedMarkedAt\":\"" + mark.getMarkedAt() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stats.watched").value(0));
        assertEquals(0, repository.count());
    }

    @Test
    void rejectsOmittedEpisodesFutureDatesMissingValuesAndStaleUndo() throws Exception {
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-26").with(user("friend")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mark())).andExpect(status().isNotFound());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"watched\":true,\"watchedOn\":\"2099-01-01\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(mark())).andExpect(status().isOk());
        mvc.perform(put("/pet-projects/naruto/api/items/s1-ep-1").with(user("friend")).session(unlocked).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"watched\":false,\"expectedMarkedAt\":\"2000-01-01T00:00:00Z\"}"))
                .andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    @Test
    void pageRendersCsrfAndCatalogHasIndividualItems() throws Exception {
        mvc.perform(get("/pet-projects/naruto").with(user("friend")).session(unlocked))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"pace-chart\"")));
        mvc.perform(get("/pet-projects/naruto/api/catalog").with(user("friend")).session(unlocked))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].items.length()").value(5))
                .andExpect(jsonPath("$[0].items[0].id").value("s1-ep-1"));
        mvc.perform(get("/pet-projects/naruto/api/state").with(user("friend")).session(unlocked))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stats.total").value(592));
    }

    @Test
    void simultaneousMarksFromTwoFriendsNeverDoubleCount() throws Exception {
        var barrier = new java.util.concurrent.CyclicBarrier(2);
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var tasks = java.util.List.of("first", "second").stream().map(name -> executor.submit(() -> {
                barrier.await();
                try {
                    service.setWatched("s1-ep-3", true, LocalDate.parse(today), null, name);
                } catch (org.springframework.dao.DataIntegrityViolationException expectedRace) {
                    // The API converts a competing insert to 409 and refreshes the shared state.
                }
                return null;
            })).toList();
            for (var task : tasks) task.get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(1, repository.count());
            assertEquals(1, service.state("first").stats().watched());
        } finally {
            executor.shutdownNow();
        }
    }

    private String mark() { return "{\"watched\":true,\"watchedOn\":\"" + today + "\"}"; }
}
